package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.events.GlobalChatEvent;
import net.dirtcraft.mods.dirt_essentials.events.StaffChatEvent;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager {
	private static final HashMap<UUID, Channel> channelTracker = new HashMap<>();

	public static boolean isInStaffChat(UUID uuid) {
		return channelTracker.getOrDefault(uuid, Channel.GLOBAL) == Channel.STAFF;
	}

	public static void chatGlobal(ServerPlayer player) {
		player.sendMessage(new TextComponent(Strings.CHAT_PREFIX + "§7You are now in the channel §9GLOBAL§7."), Util.NIL_UUID);
		channelTracker.put(player.getUUID(), Channel.GLOBAL);
	}

	public static void chatStaff(ServerPlayer player) {
		player.sendMessage(new TextComponent(Strings.CHAT_PREFIX + "§7You are now in the channel §9STAFF§7."), Util.NIL_UUID);
		channelTracker.put(player.getUUID(), Channel.STAFF);
	}

	public static Component global(ServerPlayer player, String message) {
		Component messageComponent = getNormalChat(player, message);
		GlobalChatEvent event = new GlobalChatEvent(messageComponent);
		MinecraftForge.EVENT_BUS.post(event);

		return messageComponent;
	}

	public static Component staff(ServerPlayer player, String message) {
		Component messageComponent = getNormalChat(player, message);

		MutableComponent staffMessageComponent = new TextComponent("")
				.append(new TextComponent("§7◆ §6Staff Chat §7◆ ")
						.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§bThis message is only visible for online staff members!"))))
				).append(messageComponent);

		StaffChatEvent event = new StaffChatEvent(messageComponent, staffMessageComponent);
		MinecraftForge.EVENT_BUS.post(event);

		return staffMessageComponent;
	}

	private static Component getNormalChat(ServerPlayer player, String message) {
		DirtPlayer dirtPlayer = DirtPlayer.get(player);
		String prefix = PermissionHandler.getPrefix(player.getUUID());
		if (prefix == null)
			prefix = "";

		String groupPrefix = PermissionHandler.getGroupPrefix(PermissionHandler.getPrimaryGroup(player.getUUID()));
		if (groupPrefix.isBlank())
			groupPrefix = "";

		TextComponent prefixComponent = new TextComponent(prefix);
		TextComponent playerComponent = (TextComponent) player.getDisplayName();
		HoverEvent playerHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(
				"§6Name§7: " + dirtPlayer.getUsername() + "\n" +
						"§6Nickname§7: " + player.getDisplayName().getString() + "\n" +
						"§6Rank§7: " + groupPrefix.replaceAll("[^a-zA-Z§0-9]", "") + "\n" +
						"§6Staff§7: " + (PermissionHandler.hasPermission(player.getUUID(), ChatPermissions.STAFF) ? "§atrue" : "§5false") + "\n" +
						"§6Balance§7: §a" + dirtPlayer.getFormattedBalance()
		));
		ClickEvent playerClick = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getGameProfile().getName() + " ");

		playerComponent.setStyle(playerComponent.getStyle().withHoverEvent(playerHover).withClickEvent(playerClick));

		MutableComponent messageComponent = new TextComponent("")
				.append(prefixComponent)
				.append(playerComponent)
				.append(new TextComponent("§8 » §r"));

		boolean itemPerm = PermissionHandler.hasPermission(player.getUUID(), ChatPermissions.ITEM_LINK);
		boolean hyperlinkPerm = PermissionHandler.hasPermission(player.getUUID(), ChatPermissions.HYPERLINK);
		boolean colorPerm = PermissionHandler.hasPermission(player.getUUID(), ChatPermissions.COLOR_CHAT);
		Pattern patterns = Pattern.compile("@item|(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
		Matcher patternMatcher = patterns.matcher(message);

		while (patternMatcher.find()) {
			if (patternMatcher.group().startsWith("@item") && itemPerm) {
				String part = message.substring(0, patternMatcher.start());
				messageComponent.append(colorPerm ? new TextComponent(Utils.formatColorString(part)) : new TextComponent(ChatFormatting.stripFormatting(part)));

				ItemStack stack = player.getMainHandItem();
				HoverEvent itemHover = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack));
				Component itemComponent = stack.getDisplayName().copy().withStyle(Style.EMPTY.withHoverEvent(itemHover));
				messageComponent.append(itemComponent);
				message = message.substring(patternMatcher.end());
				patternMatcher = patterns.matcher(message);
			} else if (hyperlinkPerm) {
				String part = message.substring(0, patternMatcher.start());
				messageComponent.append(colorPerm ? new TextComponent(Utils.formatColorString(part)) : new TextComponent(ChatFormatting.stripFormatting(part)));
				String url = message.substring(patternMatcher.start(), patternMatcher.end());

				ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
				HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§3ℹ §7Click to open the link."));

				messageComponent.append(new TextComponent("§9§o" + url).setStyle(Style.EMPTY.withClickEvent(clickEvent).withHoverEvent(hoverEvent)));
				message = message.substring(patternMatcher.end());
				patternMatcher = patterns.matcher(message);
			}
		}

		messageComponent.append(colorPerm ? new TextComponent(Utils.formatColorString(message)) : new TextComponent(ChatFormatting.stripFormatting(message)));

		return messageComponent;
	}

	enum Channel {
		STAFF,
		GLOBAL
	}
}
