package net.dirtcraft.mods.dirt_essentials.listeners;

import net.dirtcraft.mods.dirt_essentials.manager.ChatManager;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class OnServerChat {
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void event(ServerChatEvent event) {
		ServerPlayer player = event.getPlayer();
		UUID uuid = player.getUUID();
		String message = event.getMessage();

		event.setCanceled(true);

		boolean isInStaffChat = ChatManager.isInStaffChat(player.getUUID());
		if (!isInStaffChat && PermissionHandler.hasPermission(uuid, ChatPermissions.WRITE_GLOBAL)) {
			ChatManager.broadcastGlobal(player, message);
		}

		if (isInStaffChat && PermissionHandler.hasPermission(uuid, ChatPermissions.WRITE_STAFF)) {
			ChatManager.broadcastStaff(player, message);
		}
	}
}
