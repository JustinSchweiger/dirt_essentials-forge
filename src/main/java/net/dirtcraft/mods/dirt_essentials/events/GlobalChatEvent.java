package net.dirtcraft.mods.dirt_essentials.events;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;

public class GlobalChatEvent extends Event {
	Component message;

	public GlobalChatEvent(Component message) {
		this.message = message;
	}

	public Component getMessage() {
		return message;
	}
}
