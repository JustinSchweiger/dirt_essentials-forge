package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.TeleportRequest;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeleportManager {
	private static final Map<UUID, List<TeleportRequest>> teleportRequests = new HashMap<>();
	private static final Set<UUID> tpDisabled = new HashSet<>();
	private static int tickCounter = 0;


	public static boolean hasTeleportsDisabled(UUID uuid) {
		return tpDisabled.contains(uuid);
	}

	public static void enableTeleports(UUID uuid) {
		tpDisabled.remove(uuid);
	}

	public static void disableTeleports(UUID uuid) {
		tpDisabled.add(uuid);
	}

	public static void addTeleportRequest(ServerPlayer player, TeleportRequest request) {
		if (!teleportRequests.containsKey(player.getUUID()))
			teleportRequests.put(player.getUUID(), new ArrayList<>());

		teleportRequests.get(player.getUUID()).add(request);
	}

	public static boolean hasOutgoingRequestToTarget(UUID player, UUID target, TeleportRequest.TeleportType type) {
		if (!teleportRequests.containsKey(player))
			teleportRequests.put(player, new ArrayList<>());

		return teleportRequests.get(player).stream().anyMatch(request ->
				request.getTo().equals(target) &&
						request.getTeleportType().equals(type) &&
						request.getType() == TeleportRequest.Type.OUTGOING
		);
	}

	public static boolean hasIncomingTeleportRequests(UUID player) {
		if (!teleportRequests.containsKey(player))
			teleportRequests.put(player, new ArrayList<>());

		return teleportRequests.get(player).stream().anyMatch(request -> request.getType() == TeleportRequest.Type.INCOMING);
	}

	public static void denyAllTeleportRequests(UUID uuid) {
		if (!teleportRequests.containsKey(uuid))
			teleportRequests.put(uuid, new ArrayList<>());

		teleportRequests.get(uuid).forEach(request -> {
			if (request.getType() == TeleportRequest.Type.INCOMING) {
				notifyPlayerOfDeny(request.getFrom(), request.getTo());
			}
		});

		teleportRequests.values().forEach(list -> list.removeIf(request -> request.getTo().equals(uuid)));
	}

	private static void notifyPlayerOfDeny(UUID from, UUID to) {
		if (DirtEssentials.SERVER.getPlayerList().getPlayer(from) == null) {
			return;
		}

		ServerPlayer serverPlayer = DirtEssentials.SERVER.getPlayerList().getPlayer(from);
		ServerPlayer target = DirtEssentials.SERVER.getPlayerList().getPlayer(to);

		assert serverPlayer != null;
		assert target != null;
		serverPlayer.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§d" + target.getGameProfile().getName() + " §7has §cdenied §7your teleport request!"), Util.NIL_UUID);
	}

	public static void acceptAllTeleportRequests(UUID uuid) {
		if (!teleportRequests.containsKey(uuid))
			teleportRequests.put(uuid, new ArrayList<>());

		teleportRequests.get(uuid).forEach(request -> {
			if (request.getType() == TeleportRequest.Type.INCOMING) {
				notifyPlayerOfAccept(request.getFrom(), request.getTo(), request.getTeleportType());
			}
		});

		teleportRequests.values().forEach(list -> list.removeIf(request -> request.getTo().equals(uuid)));
	}

	private static void notifyPlayerOfAccept(UUID requestFrom, UUID requestTo, TeleportRequest.TeleportType teleportType) {
		ServerPlayer target = DirtEssentials.SERVER.getPlayerList().getPlayer(requestTo);
		if (target == null)
			return;

		ServerPlayer player = DirtEssentials.SERVER.getPlayerList().getPlayer(requestFrom);
		if (player == null) {
			target.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "The player that sent you this teleport request is no longer online!"), Util.NIL_UUID);
			return;
		}

		if (teleportType == TeleportRequest.TeleportType.TPA) {
			PlayerTeleportEvent event = new PlayerTeleportEvent(player, target.getX(), target.getY(), target.getZ());
			MinecraftForge.EVENT_BUS.post(event);

			player.teleportTo(target.getLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§d" + target.getGameProfile().getName() + " §7has accepted your teleport request!"), Util.NIL_UUID);
			target.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§d" + player.getGameProfile().getName() + " §7has been teleported to you!"), Util.NIL_UUID);
		} else {
			PlayerTeleportEvent event = new PlayerTeleportEvent(target, player.getX(), player.getY(), player.getZ());
			MinecraftForge.EVENT_BUS.post(event);

			target.teleportTo(player.getLevel(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§d" + target.getGameProfile().getName() + " §7has accepted your teleport request!"), Util.NIL_UUID);
			target.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "You have been teleported to §d" + player.getGameProfile().getName()), Util.NIL_UUID);
		}
	}

	public static boolean hasOutgoingTeleportRequests(UUID player) {
		if (!teleportRequests.containsKey(player))
			teleportRequests.put(player, new ArrayList<>());

		return teleportRequests.get(player).stream().anyMatch(request -> request.getType() == TeleportRequest.Type.OUTGOING);
	}

	public static void cancelAllTeleportRequests(UUID uuid) {
		if (!teleportRequests.containsKey(uuid))
			teleportRequests.put(uuid, new ArrayList<>());

		teleportRequests.get(uuid).forEach(request -> {
			if (request.getType() == TeleportRequest.Type.OUTGOING) {
				notifyPlayerOfCancel(request.getFrom(), request.getTo());
			}
		});

		teleportRequests.values().forEach(list -> list.removeIf(request -> request.getFrom().equals(uuid)));
	}

	private static void notifyPlayerOfCancel(UUID from, UUID to) {
		if (DirtEssentials.SERVER.getPlayerList().getPlayer(to) == null) {
			return;
		}

		ServerPlayer player = DirtEssentials.SERVER.getPlayerList().getPlayer(from);
		ServerPlayer target = DirtEssentials.SERVER.getPlayerList().getPlayer(to);

		assert player != null;
		assert target != null;
		target.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§d" + player.getGameProfile().getName() + " §7has §ccanceled §7their teleport request!"), Util.NIL_UUID);
	}

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;

		if (DirtEssentials.SERVER == null)
			return;

		if (tickCounter != 20) {
			tickCounter++;
			return;
		}
		tickCounter = 0;

		List<UUID> playersNotified = new ArrayList<>();
		teleportRequests.values().forEach(list -> list.removeIf(request -> {
			if (request.getTimeSent().plusSeconds(30).isBefore(LocalDateTime.now())) {
				ServerPlayer fromPlayer = DirtEssentials.SERVER.getPlayerList().getPlayer(request.getFrom());

				if (fromPlayer != null) {
					if (playersNotified.contains(request.getFrom())) {
						return true;
					}

					fromPlayer.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Your teleport request §ctimed out§7!"), Util.NIL_UUID);
					playersNotified.add(request.getFrom());
				}
				return true;
			}

			return false;
		}));
	}
}
