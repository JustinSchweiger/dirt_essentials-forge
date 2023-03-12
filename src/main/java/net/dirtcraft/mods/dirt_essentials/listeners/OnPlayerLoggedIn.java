package net.dirtcraft.mods.dirt_essentials.listeners;

import com.mojang.logging.LogUtils;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.GodManager;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.hibernate.Session;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.UUID;

public class OnPlayerLoggedIn {
	private static final Logger LOGGER = LogUtils.getLogger();

	@SubscribeEvent
	public static void event(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer serverPlayer = (ServerPlayer) event.getPlayer();
		UUID uuid = event.getPlayer().getUUID();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DirtPlayer player = session.get(DirtPlayer.class, uuid);

			if (player == null) {
				player = new DirtPlayer(uuid);

				LOGGER.info("» Creating new DirtPlayer for " + event.getPlayer().getGameProfile().getName());
				PlayerManager.addPlayerData(uuid, event.getPlayer().getGameProfile().getName());
			}

			if (PermissionHandler.hasPermission(uuid, ChatPermissions.STAFF))
				player.setStaff(true);

			if (player.isFlyingWhenLoggedOut() && PermissionHandler.hasPermission(uuid, EssentialsPermissions.FLY)) {
				serverPlayer.getAbilities().flying = true;
				serverPlayer.getAbilities().mayfly = true;
				serverPlayer.onUpdateAbilities();
				serverPlayer.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Creative flight is now §aenabled§7!"), Util.NIL_UUID);
			}

			if (player.isGodModeEnabled() && PermissionHandler.hasPermission(uuid, EssentialsPermissions.GOD)) {
				serverPlayer.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "God mode is now §aenabled§7!"), Util.NIL_UUID);
				GodManager.setGodModeEnabled(uuid, true);
			}

			player.setUsername(event.getPlayer().getGameProfile().getName());
			player.setTimesJoined(player.getTimesJoined() + 1);
			player.setLastJoined(LocalDateTime.now());

			session.persist(player);
			session.getTransaction().commit();
		} catch (Exception ignored) {}
	}
}
