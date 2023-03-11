package net.dirtcraft.mods.dirt_essentials.manager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
	private static final Map<UUID, String> playerData = new HashMap<>();

	@Nullable
	public static String getUsername(UUID uuid) {
		return playerData.get(uuid);
	}

	@Nullable
	public static UUID getUuid(String username) {
		return playerData.entrySet().stream()
				.filter(entry -> entry.getValue().equalsIgnoreCase(username))
				.map(Map.Entry::getKey)
				.findFirst()
				.orElse(null);
	}

	public static void addPlayerData(UUID uuid, String username) {
		playerData.put(uuid, username);
	}

	public static List<String> getAllUsernames() {
		return new ArrayList<>(playerData.values());
	}
}
