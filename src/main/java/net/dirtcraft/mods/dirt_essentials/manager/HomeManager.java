package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.data.entites.Home;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeManager {
	private static final Map<UUID, LocalDateTime> cooldowns = new HashMap<>();

	public static boolean hasAvailableHomes(UUID uuid) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, uuid);
			return player.getHomes().size() < player.getHomeAmount();
		}
	}

	public static boolean hasHome(UUID uuid, String name) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, uuid);
			return player.getHomes().stream().anyMatch(home -> home.getName().equals(name));
		}
	}

	public static boolean hasHomes(UUID uuid) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, uuid);
			return !player.getHomes().isEmpty();
		}
	}

	public static boolean isOnCooldown(UUID uuid) {
		int cooldown = EssentialsConfig.HOME_COOLDOWN.get();

		if (!cooldowns.containsKey(uuid)) {
			cooldowns.put(uuid, LocalDateTime.now().plusSeconds(cooldown));
			return false;
		}

		if (cooldowns.get(uuid).isBefore(LocalDateTime.now())) {
			cooldowns.put(uuid, LocalDateTime.now().plusSeconds(cooldown));
			return false;
		}

		return true;
	}

	public static long getCooldown(UUID uuid) {
		return LocalDateTime.now().until(cooldowns.get(uuid), ChronoUnit.SECONDS);
	}

	public static Iterable<String> getHomes(String name) {
		UUID uuid = PlayerManager.getUuid(name);
		if (uuid == null)
			return new ArrayList<>();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, uuid);
			return player.getHomes().stream().map(Home::getName).toList();
		}
	}
}
