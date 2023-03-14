package net.dirtcraft.mods.dirt_essentials.manager;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeleportManager {
	public static final Map<UUID, List<ServerPlayer>> outgoingRequests = new HashMap<>();
	public static final Map<UUID, List<ServerPlayer>> incomingRequests = new HashMap<>();
	public static final Set<UUID> tpDisabled = new HashSet<>();

	public static boolean hasTeleportsDisabled(UUID uuid) {
		return tpDisabled.contains(uuid);
	}

	public static void enableTeleports(UUID uuid) {
		tpDisabled.remove(uuid);
	}

	public static void disableTeleports(UUID uuid) {
		tpDisabled.add(uuid);
	}
}
