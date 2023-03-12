package net.dirtcraft.mods.dirt_essentials.listeners;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.JLManager;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
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

			String message;
			if (dirtPlayer.getCustomLeaveMessage().isBlank()) {
				message = dirtPlayer.getUsername();
			} else {
				message = dirtPlayer.getCustomLeaveMessage();
			}
			Component component = JLManager.getLeaveMessage(message, player.getGameProfile().getName(), PermissionHandler.hasPermission(player.getUUID(), ChatPermissions.STAFF));
			DirtEssentials.SERVER.getPlayerList().broadcastMessage(component, ChatType.SYSTEM, Util.NIL_UUID);

			dirtPlayer.setFlyingWhenLoggedOut(player.getAbilities().flying);
			session.beginTransaction();
			session.persist(dirtPlayer);
			session.getTransaction().commit();
		}
	}
}
