package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class JLManager {
	public static Component getJoinMessage(String message, String playerName, boolean isFirst, boolean isStaff) {
		String s = message.replace("{PLAYER}", playerName);
		s = Utils.formatColorString(s);
		s = (isFirst ? "§5§l→ §dPlease welcome §b" : isStaff ? "§a§l→ §c" : "§a§l→ §7") + s;

		return new TextComponent(s);
	}

	public static Component getLeaveMessage(String message, String playerName, boolean isStaff) {
		String s = message.replace("{PLAYER}", playerName);
		s = Utils.formatColorString(s);
		s = (isStaff ? "§c§l← §c" : "§c§l← §7") + s;

		return new TextComponent(s);
	}
}
