package net.dirtcraft.mods.dirt_essentials.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

public class Utils {
	public static String formatColorString(String string) {
		return string.replaceAll("&", "§");
	}

	public static void addLore(ItemStack stack, String lore) {
		CompoundTag display = stack.getOrCreateTagElement("display");
		ListTag tag = display.getList("Lore", 8);
		String[] loreArray = lore.split("\\n");
		for (String s : loreArray) {
			tag.add(StringTag.valueOf("{\"text\":\"" + s + "\"}"));
		}
		display.put("Lore", tag);
	}
}
