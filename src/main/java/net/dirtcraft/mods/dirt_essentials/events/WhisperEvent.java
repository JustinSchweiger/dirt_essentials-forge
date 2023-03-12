package net.dirtcraft.mods.dirt_essentials.events;

import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class WhisperEvent extends Event {
	@Getter
	private final ServerPlayer sender;
	@Getter
	private final ServerPlayer receiver;
	@Getter
	private final String message;

	public WhisperEvent(ServerPlayer sender, ServerPlayer receiver, String message) {
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
	}
}
