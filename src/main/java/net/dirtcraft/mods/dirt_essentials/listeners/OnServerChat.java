package net.dirtcraft.mods.dirt_essentials.listeners;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OnServerChat {
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void event(ServerChatEvent event) {
		ServerPlayer player = event.getPlayer();
		String message = event.getMessage();


	}
}
