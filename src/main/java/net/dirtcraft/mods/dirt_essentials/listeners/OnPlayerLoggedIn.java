package net.dirtcraft.mods.dirt_essentials.listeners;

import com.mojang.logging.LogUtils;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.manager.PlaytimeManager;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
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
		UUID uuid = event.getPlayer().getUUID();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DirtPlayer player = session.get(DirtPlayer.class, uuid);

			if (player == null) {
				player = new DirtPlayer(uuid);

				player.setDisplayName(event.getPlayer().getGameProfile().getName());
				LOGGER.info("» Creating new DirtPlayer for " + event.getPlayer().getGameProfile().getName());
				PlayerManager.addPlayerData(uuid, event.getPlayer().getGameProfile().getName());
			}

			if (PermissionHandler.hasPermission(uuid, ChatPermissions.STAFF))
				player.setStaff(true);

			player.setUsername(event.getPlayer().getGameProfile().getName());
			player.setTimesJoined(player.getTimesJoined() + 1);
			player.setLastJoined(LocalDateTime.now());

			session.persist(player);
			session.getTransaction().commit();
		} catch (Exception ignored) {}
	}
}
