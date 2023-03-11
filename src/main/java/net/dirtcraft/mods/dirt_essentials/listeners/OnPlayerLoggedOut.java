package net.dirtcraft.mods.dirt_essentials.listeners;

import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.hibernate.Session;

public class OnPlayerLoggedOut {
	@SubscribeEvent
	public static void event(PlayerEvent.PlayerLoggedOutEvent event) {
		ServerPlayer player = (ServerPlayer) event.getPlayer();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());

			dirtPlayer.setFlyingWhenLoggedOut(player.getAbilities().flying);
			session.beginTransaction();
			session.persist(dirtPlayer);
			session.getTransaction().commit();
		}
	}
}
