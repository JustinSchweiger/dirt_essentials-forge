package net.dirtcraft.mods.dirt_essentials.listeners;

import net.dirtcraft.mods.dirt_essentials.manager.ChatManager;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class OnServerChat {
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void event(ServerChatEvent event) {
		ServerPlayer player = event.getPlayer();
		UUID uuid = player.getUUID();
		String message = event.getMessage();

		if (event.isCanceled() || message.isEmpty() || event.getResult() == ServerChatEvent.Result.DENY)
			return;

		boolean isInStaffChat = ChatManager.isInStaffChat(player.getUUID());
		if (!isInStaffChat && PermissionHandler.hasPermission(uuid, ChatPermissions.WRITE_GLOBAL)) {
			event.setComponent(ChatManager.global(player, message));
			return;
		}

		if (isInStaffChat && PermissionHandler.hasPermission(uuid, ChatPermissions.WRITE_STAFF)) {
			event.setComponent(ChatManager.staff(player, message));
		}
	}
}
