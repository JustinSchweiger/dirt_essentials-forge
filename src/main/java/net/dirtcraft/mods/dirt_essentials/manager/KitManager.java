package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.database.Kit;
import net.dirtcraft.mods.dirt_essentials.database.KitTracker;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class KitManager {
	public static List<Kit> getAllKits() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.createQuery("from Kit", Kit.class).list();
		}
	}

	public static List<Kit> getAllKitsWithPermission(CommandSourceStack source) {
		if (source.getEntity() == null)
			return getAllKits();
		UUID uuid = source.getEntity().getUUID();

		List<Kit> kits = getAllKits();

		kits.removeIf(kit -> !PermissionHandler.hasPermission(uuid, EssentialsPermissions.KIT + "." + kit.getName()) && !PermissionHandler.hasPermission(uuid, EssentialsPermissions.KIT + ".*"));

		return kits;
	}

	public static boolean isKitClaimable(UUID uuid, Kit kit) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			KitTracker tracker = session.createQuery("from KitTracker where player.uuid = :uuid and kit.name = :kit", KitTracker.class)
					.setParameter("uuid", uuid)
					.setParameter("kit", kit.getName())
					.uniqueResult();

			if (tracker == null)
				return true;

			LocalDateTime lastClaimed = tracker.getLastClaimed();
			LocalDateTime now = LocalDateTime.now();

			if (kit.getCooldown() < 0)
				return false;

			return switch (kit.getTimeUnit()) {
				case SECONDS -> lastClaimed.plusSeconds(kit.getCooldown()).isBefore(now);
				case MINUTES -> lastClaimed.plusMinutes(kit.getCooldown()).isBefore(now);
				case HOURS -> lastClaimed.plusHours(kit.getCooldown()).isBefore(now);
			};
		}
	}

	public static void claimKit(ServerPlayer player, Kit kit) {
		boolean isClaimable = isKitClaimable(player.getUUID(), kit);

		if (kit.getCooldown() < 0 && !isClaimable) {
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "You can only claim this kit once!"), Util.NIL_UUID);
			return;
		}

		if (!isClaimable) {
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Please wait " + getTimeLeft(player.getUUID(), kit) + " §7before claiming this kit again!"), Util.NIL_UUID);
			return;
		}

		kit.getItems().forEach(itemStack -> {
			int freeSlot = player.getInventory().getFreeSlot();
			if (freeSlot == -1) {
				player.drop(itemStack, false);
			} else {
				player.getInventory().setItem(freeSlot, itemStack);
			}
		});

		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "You have claimed the kit §b" + kit.getName() + "§7!"), Util.NIL_UUID);

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			KitTracker tracker = session.createQuery("from KitTracker where player.uuid = :uuid and kit.name = :kit", KitTracker.class)
					.setParameter("uuid", player.getUUID())
					.setParameter("kit", kit.getName())
					.uniqueResult();

			if (tracker == null) {
				DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
				tracker = new KitTracker(dirtPlayer, kit, LocalDateTime.now());
			} else {
				tracker.setLastClaimed(LocalDateTime.now());
			}

			session.persist(tracker);
			session.getTransaction().commit();
		}
	}

	public static String getTimeLeft(UUID uuid, Kit kit) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			KitTracker tracker = session.createQuery("from KitTracker where player.uuid = :uuid and kit.name = :kit", KitTracker.class)
					.setParameter("uuid", uuid)
					.setParameter("kit", kit.getName())
					.uniqueResult();

			if (tracker == null)
				return "§cThere was an error!";

			LocalDateTime lastClaimed = tracker.getLastClaimed();
			LocalDateTime now = LocalDateTime.now();

			if (kit.getCooldown() < 0)
				return "§cThis kit can only be claimed once!";

			long secondsLeft = 0;
			switch (kit.getTimeUnit()) {
				case SECONDS -> secondsLeft = now.until(lastClaimed.plusSeconds(kit.getCooldown()), ChronoUnit.SECONDS);
				case MINUTES -> secondsLeft = now.until(lastClaimed.plusMinutes(kit.getCooldown()), ChronoUnit.SECONDS);
				case HOURS -> secondsLeft = now.until(lastClaimed.plusHours(kit.getCooldown()), ChronoUnit.SECONDS);
			}

			int hours = (int) Math.floor(secondsLeft / 3600F);
			int minutes = (int) Math.floor((secondsLeft - (hours * 3600)) / 60F);
			int seconds = (int) Math.floor(secondsLeft % 60F);

			StringBuilder builder = new StringBuilder();
			if (hours == 1)
				builder.append("§b").append(hours).append(" §chour ");
			if (hours > 1)
				builder.append("§b").append(hours).append(" §chours ");
			if (minutes == 1)
				builder.append("§b").append(minutes).append(" §cminute ");
			if (minutes > 1)
				builder.append("§b").append(minutes).append(" §cminutes ");
			if (seconds == 1)
				builder.append("§b").append(seconds).append(" §csecond");
			if (seconds > 1)
				builder.append("§b").append(seconds).append(" §cseconds");

			return builder.toString();
		}
	}
}
