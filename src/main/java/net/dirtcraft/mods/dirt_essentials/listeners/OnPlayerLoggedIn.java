package net.dirtcraft.mods.dirt_essentials.listeners;

import com.mojang.logging.LogUtils;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.Location;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.database.Note;
import net.dirtcraft.mods.dirt_essentials.manager.AutobroadcastManager;
import net.dirtcraft.mods.dirt_essentials.manager.GodManager;
import net.dirtcraft.mods.dirt_essentials.manager.JLManager;
import net.dirtcraft.mods.dirt_essentials.manager.MsgManager;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.manager.SpawnManager;
import net.dirtcraft.mods.dirt_essentials.manager.TeleportManager;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.hibernate.Session;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class OnPlayerLoggedIn {
	private static final Logger LOGGER = LogUtils.getLogger();

	@SubscribeEvent
	public static void event(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer serverPlayer = (ServerPlayer) event.getPlayer();
		UUID uuid = event.getPlayer().getUUID();
		boolean isFirstJoin = false;

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DirtPlayer player = session.get(DirtPlayer.class, uuid);
			if (player == null) {
				player = new DirtPlayer(uuid);
				isFirstJoin = true;

				LOGGER.info("» Creating new DirtPlayer for " + event.getPlayer().getGameProfile().getName());
				PlayerManager.addPlayerData(uuid, event.getPlayer().getGameProfile().getName());
				SpawnManager.SpawnResult result = SpawnManager.teleportToOverworldSpawn(uuid);
				if (result == SpawnManager.SpawnResult.NO_OVERWORLD_SPAWN) {
					LOGGER.info("» No overworld spawn found, teleporting to default world spawn");
					ServerLevel overworld = DirtEssentials.SERVER.overworld();
					serverPlayer.teleportTo(overworld, overworld.getSharedSpawnPos().getX(), overworld.getSharedSpawnPos().getY(), overworld.getSharedSpawnPos().getZ(), 0, 0);

				}
			}

			player.setLastKnownIp(((ServerPlayer) event.getPlayer()).getIpAddress());
			player.setLastKnownLocation(new Location(event.getPlayer().level.dimension(), event.getPlayer().getX(), event.getPlayer().getY(), event.getPlayer().getZ(), event.getPlayer().getYRot(), event.getPlayer().getXRot()));
			player.setUsername(event.getPlayer().getGameProfile().getName());
			player.setTimesJoined(player.getTimesJoined() + 1);
			player.setLastJoined(LocalDateTime.now());

			String message;
			if (player.getCustomJoinMessage().isBlank()) {
				message = player.getUsername();
			} else {
				message = player.getCustomJoinMessage();
			}
			Component component = JLManager.getJoinMessage(message, event.getPlayer().getGameProfile().getName(), isFirstJoin, PermissionHandler.hasPermission(uuid, ChatPermissions.STAFF));
			DirtEssentials.SERVER.getPlayerList().broadcastMessage(component, ChatType.SYSTEM, Util.NIL_UUID);

			if (PermissionHandler.hasPermission(uuid, ChatPermissions.STAFF))
				player.setStaff(true);

			if (player.getNotes().size() > 0) {
				List<ServerPlayer> onlineStaff = Utils.getOnlineStaff();
				if (onlineStaff.size() > 0) {
					List<Note> notes = player.getNotes();

					MutableComponent finalComponent = new TextComponent("");
					finalComponent.append(new TextComponent(""));

					TextComponent notesComponent = new TextComponent(Strings.ESSENTIALS_PREFIX + "§cFound notes for §d" + player.getUsername() + "§7:\n");
					finalComponent.append(notesComponent);
					finalComponent.append(new TextComponent("\n"));

					for (Note note : notes) {
						TextComponent noteComponent = new TextComponent("§7[§d" + note.getId() + "§7]§a: §6" + note.getNote());
						noteComponent.setStyle(noteComponent.getStyle()
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§6Added by§7: " + note.getAddedBy() + "\n§6Added on§7: §3" + note.getAdded().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " §7at §e" + note.getAdded().format(DateTimeFormatter.ofPattern("HH:mm"))))));

						TextComponent removeComponent = new TextComponent("  §7[§c✕§7] ");
						removeComponent.setStyle(removeComponent.getStyle()
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§cClick to remove this note!")))
								.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/note remove " + player.getUsername() + " " + note.getId())));

						finalComponent.append(removeComponent);
						finalComponent.append(noteComponent);
					}

					finalComponent.append(new TextComponent(""));

					for (ServerPlayer staff : onlineStaff) {
						staff.sendMessage(new TextComponent(""), Util.NIL_UUID);
						staff.sendMessage(finalComponent, Util.NIL_UUID);
						staff.sendMessage(new TextComponent(""), Util.NIL_UUID);
					}
				}
			}

			if (player.isFlyingWhenLoggedOut() && PermissionHandler.hasPermission(uuid, EssentialsPermissions.FLY)) {
				serverPlayer.getAbilities().flying = true;
				serverPlayer.getAbilities().mayfly = true;
				serverPlayer.onUpdateAbilities();
				serverPlayer.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Creative flight is now §aenabled§7!"), Util.NIL_UUID);
			}

			if (player.isSocialSpyEnabled() && PermissionHandler.hasPermission(uuid, EssentialsPermissions.SOCIALSPY)) {
				MsgManager.enableSocialSpy(uuid);
				serverPlayer.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "SocialSpy is now §aenabled§7!"), Util.NIL_UUID);
			}

			if (player.isAutobroadcastsDisabled()) {
				AutobroadcastManager.disableAutobroadcasts(uuid);
			}

			if (player.isTpDisabled()) {
				TeleportManager.disableTeleports(uuid);
			}

			if (player.isTeleportToSpawnOnNextLogin()) {
				SpawnManager.teleportToOverworldSpawn(uuid);
				player.setTeleportToSpawnOnNextLogin(false);
				serverPlayer.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "You have been teleported to spawn!"), Util.NIL_UUID);
			}

			if (player.isGodModeEnabled() && PermissionHandler.hasPermission(uuid, EssentialsPermissions.GOD)) {
				serverPlayer.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "God mode is now §aenabled§7!"), Util.NIL_UUID);
				GodManager.setGodModeEnabled(uuid, true);
			}

			session.persist(player);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
