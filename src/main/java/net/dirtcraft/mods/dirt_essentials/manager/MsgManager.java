package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.events.WhisperEvent;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MsgManager {
	private static final Map<UUID, ServerPlayer> lastMessagedPlayers = new HashMap<>();
	private static final Set<UUID> socialSpyEnabled = new HashSet<>();

	public static void setLastMessagedPlayer(UUID sender, ServerPlayer receiver) {
		lastMessagedPlayers.put(sender, receiver);
	}

	@Nullable
	public static ServerPlayer getLastMessagedPlayer(UUID sender) {
		return lastMessagedPlayers.get(sender);
	}

	public static void enableSocialSpy(UUID player) {
		socialSpyEnabled.add(player);
	}

	public static void disableSocialSpy(UUID player) {
		socialSpyEnabled.remove(player);
	}

	public static boolean isSocialSpyEnabled(UUID player) {
		return socialSpyEnabled.contains(player);
	}

	@SubscribeEvent
	public static void onWhisper(WhisperEvent event) {
		ServerPlayer sender = event.getSender();
		ServerPlayer receiver = event.getReceiver();
		String message = event.getMessage();

		for (UUID uuid : socialSpyEnabled) {
			ServerPlayer player = DirtEssentials.SERVER.getPlayerList().getPlayer(uuid);
			if (player == null) continue;
			if (player.getUUID().equals(sender.getUUID()) || player.getUUID().equals(receiver.getUUID())) continue;

			TextComponent text = new TextComponent(
					"§7[§8SocialSpy§7] [" + sender.getGameProfile().getName() + " §c▶ §7" + receiver.getGameProfile().getName() + "] §8» §7§o" + message
			);

			player.sendMessage(text, Util.NIL_UUID);
		}
	}

	public static void message(ServerPlayer sender, ServerPlayer receiver, String message) {
		TextComponent senderMessage = new TextComponent(
				"§6You §7▶ §c" + receiver.getDisplayName().getString() + " §8» §7§o" + message
		);

		TextComponent receiverMessage = new TextComponent(
				"§c" + sender.getDisplayName().getString() + " §7▶ §6You §8» §7§o" + message
		);

		WhisperEvent event = new WhisperEvent(sender, receiver, message);
		boolean canceled = MinecraftForge.EVENT_BUS.post(event);
		if (canceled) return;

		sender.sendMessage(senderMessage, Util.NIL_UUID);
		receiver.sendMessage(receiverMessage, Util.NIL_UUID);
	}
}
