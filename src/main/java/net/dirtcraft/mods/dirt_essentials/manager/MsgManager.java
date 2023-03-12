package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.events.WhisperEvent;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MsgManager {
	private static final Map<UUID, ServerPlayer> lastMessagedPlayers = new HashMap<>();

	public static void setLastMessagedPlayer(UUID sender, ServerPlayer receiver) {
		lastMessagedPlayers.put(sender, receiver);
	}

	@Nullable
	public static ServerPlayer getLastMessagedPlayer(UUID sender) {
		return lastMessagedPlayers.get(sender);
	}

	public static void message(ServerPlayer sender, ServerPlayer receiver, String message) {
		TextComponent senderMessage = new TextComponent(
				"§6You §7-> §c" + receiver.getDisplayName().getString() + " §8» §7§o" + message
		);

		TextComponent receiverMessage = new TextComponent(
				"§c" + sender.getDisplayName().getString() + " §7-> §6You §8» §7§o" + message
		);

		WhisperEvent event = new WhisperEvent(sender, receiver, message);
		boolean canceled = MinecraftForge.EVENT_BUS.post(event);
		if (canceled) return;

		sender.sendMessage(senderMessage, Util.NIL_UUID);
		receiver.sendMessage(receiverMessage, Util.NIL_UUID);
	}
}
