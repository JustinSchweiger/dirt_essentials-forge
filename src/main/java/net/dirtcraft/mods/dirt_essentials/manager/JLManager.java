package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class JLManager {
	public static Component getJoinMessage(String message, String playerName, boolean isFirst, boolean isStaff) {
		String s = message.replace("{PLAYER}", playerName);
		s = Utils.formatColorString(s);
		s = (isFirst ? "§5§l▶ " : isStaff ? "§4§l▶ " : "§a§l▶ ") + s;

		return new TextComponent(s);
	}

	public static Component getLeaveMessage(String message, String playerName, boolean isStaff) {
		String s = message.replace("{PLAYER}", playerName);
		s = Utils.formatColorString(s);
		s = (isStaff ? "§4§l◀ " : "§c§l◀ ") + s;

		return new TextComponent(s);
	}
}