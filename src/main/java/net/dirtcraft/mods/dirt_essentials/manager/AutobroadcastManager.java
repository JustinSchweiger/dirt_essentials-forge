package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AutobroadcastManager {
	private static final Set<UUID> autobroadcastsDisabled = new HashSet<>();
	private static int tickCounter = 0;

	public static void disableAutobroadcasts(UUID player) {
		autobroadcastsDisabled.add(player);
	}

	public static void enableAutobroadcasts(UUID player) {
		autobroadcastsDisabled.remove(player);
	}

	public static boolean areAutobroadcastsDisabled(UUID player) {
		return autobroadcastsDisabled.contains(player);
	}

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;

		if (DirtEssentials.SERVER == null)
			return;

		if (tickCounter != 20 * EssentialsConfig.AUTOBROADCAST_DELAY.get()) {
			tickCounter++;
			return;
		}
		tickCounter = 0;

		List<EssentialsConfig.Autobroadcast> autobroadcasts = EssentialsConfig.AUTOBROADCASTS.get().stream().map(EssentialsConfig.Autobroadcast::deserialize).toList();
		if (autobroadcasts.isEmpty())
			return;

		int random = (int) (Math.random() * autobroadcasts.size());
		EssentialsConfig.Autobroadcast broadcast = autobroadcasts.get(random);

		if (broadcast.getLines().isEmpty())
			return;

		TextComponent message = new TextComponent(
				String.join("\n", broadcast.getLines())
		);

		if (broadcast.getAction() != null && !broadcast.getClickValue().equalsIgnoreCase("")) {
			message.setStyle(message.getStyle()
					.withClickEvent(new ClickEvent(broadcast.getAction(), broadcast.getClickValue())));
		}

		if (!broadcast.getHoverEventText().equalsIgnoreCase("")) {
			message.setStyle(message.getStyle()
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(broadcast.getHoverEventText()))));
		}

		for (ServerPlayer player : DirtEssentials.SERVER.getPlayerList().getPlayers()) {
			if (areAutobroadcastsDisabled(player.getUUID()))
				continue;

			player.sendMessage(message, Util.NIL_UUID);
		}
	}
}
