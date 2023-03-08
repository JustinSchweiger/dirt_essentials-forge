package net.dirtcraft.mods.dirt_essentials.events;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;

public class StaffChatEvent extends Event {
	Component message;
	Component staffMessage;

	public StaffChatEvent(Component message, Component staffMessage) {
		this.message = message;
		this.staffMessage = staffMessage;
	}

	public Component getMessage() {
		return message;
	}

	public Component getStaffMessage() {
		return staffMessage;
	}
}
