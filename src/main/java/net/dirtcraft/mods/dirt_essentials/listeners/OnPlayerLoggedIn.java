package net.dirtcraft.mods.dirt_essentials.listeners;

import com.mojang.logging.LogUtils;
import net.dirtcraft.mods.dirt_essentials.data.hibernate.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.data.hibernate.HibernateUtil;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;

import java.util.UUID;

public class OnPlayerLoggedIn {
	private static final Logger LOGGER = LogUtils.getLogger();
	@SubscribeEvent
	public static void event(PlayerEvent.PlayerLoggedInEvent event) {
		UUID uuid = event.getPlayer().getUUID();
		DirtPlayer player = null;

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			player = session.get(DirtPlayer.class, uuid);

			if (player == null) {
				player = new DirtPlayer(uuid, event.getPlayer().getName().getString());
				session.persist(player);
			}
			session.getTransaction().commit();
		} catch (Exception ignored) {}

		if (player == null) return;

		LOGGER.info("Player {} logged in.", player.getUsername());
	}
}
