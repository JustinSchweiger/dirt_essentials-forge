package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AfkManager {
	private static final Map<UUID, Long> afkPlayers = new HashMap<>();
	private static final Map<UUID, Long> activePlayers = new HashMap<>();
	private static int tickCounter = 0;

	public static void setAfk(ServerPlayer player) {
		if (afkPlayers.containsKey(player.getUUID())) return;

		afkPlayers.put(player.getUUID(), System.currentTimeMillis());
		activePlayers.remove(player.getUUID());

		player.sendMessage(new TextComponent("§7You are now AFK!"), Util.NIL_UUID);

		DirtEssentials.SERVER.getPlayerList().getPlayers().stream()
				.filter(p -> !p.getUUID().equals(player.getUUID()))
				.forEach(p -> p.sendMessage(new TextComponent("§7" + player.getName().getString() + " is now AFK!"), Util.NIL_UUID));

		DirtEssentials.LOGGER.info("§7" + player.getName().getString() + " is now AFK!");
	}

	public static void removeAfk(ServerPlayer player) {
		if (!afkPlayers.containsKey(player.getUUID())) return;

		afkPlayers.remove(player.getUUID());
		activePlayers.put(player.getUUID(), System.currentTimeMillis());

		player.sendMessage(new TextComponent("§7You are no longer AFK!"), Util.NIL_UUID);

		DirtEssentials.SERVER.getPlayerList().getPlayers().stream()
				.filter(p -> !p.getUUID().equals(player.getUUID()))
				.forEach(p -> p.sendMessage(new TextComponent("§7" + player.getName().getString() + " is no longer AFK!"), Util.NIL_UUID));

		DirtEssentials.LOGGER.info("§7" + player.getName().getString() + " is no longer AFK!");
	}

	public static boolean isPlayerAfk(ServerPlayer player) {
		return afkPlayers.containsKey(player.getUUID());
	}

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (DirtEssentials.SERVER == null)
			return;

		if (tickCounter != 20) {
			tickCounter++;
			return;
		}
		tickCounter = 0;

		List<ServerPlayer> onlinePlayers = DirtEssentials.SERVER.getPlayerList().getPlayers();

		activePlayers.forEach((key, value) -> {
			ServerPlayer player = onlinePlayers.stream().filter(p -> p.getUUID().equals(key)).findFirst().orElse(null);
			if (player == null) return;

			if (System.currentTimeMillis() - value >= EssentialsConfig.AFK_TIME.get() * 1000)
				setAfk(player);
		});

		if (!EssentialsConfig.AFK_KICK.get()) return;

		afkPlayers.forEach((key, value) -> {
			if (System.currentTimeMillis() - value >= EssentialsConfig.AFK_KICK_TIME.get() * 1000) {
				ServerPlayer player = onlinePlayers.stream().filter(p -> p.getUUID().equals(key)).findFirst().orElse(null);
				if (player == null) return;

				if (PermissionHandler.hasPermission(player.getUUID(), EssentialsPermissions.AFK_BYPASS)) return;

				player.connection.disconnect(new TextComponent(EssentialsConfig.AFK_KICK_MESSAGE.get()));
			}
		});
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		activePlayers.put(event.getPlayer().getUUID(), System.currentTimeMillis());
		afkPlayers.remove(event.getPlayer().getUUID());
	}

	@SubscribeEvent
	public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
		activePlayers.remove(event.getPlayer().getUUID());
		afkPlayers.remove(event.getPlayer().getUUID());
	}

	@SubscribeEvent
	public static void onPlayerMove(LivingEvent.LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;

		if (isPlayerAfk(player)) {
			removeAfk(player);
		} else {
			activePlayers.put(player.getUUID(), System.currentTimeMillis());
		}
	}

	@SubscribeEvent
	public static void onChat(ServerChatEvent event) {
		ServerPlayer player = event.getPlayer();

		if (isPlayerAfk(player)) {
			removeAfk(player);
		} else {
			activePlayers.put(player.getUUID(), System.currentTimeMillis());
		}
	}
}
