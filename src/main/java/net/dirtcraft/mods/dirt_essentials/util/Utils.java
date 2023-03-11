package net.dirtcraft.mods.dirt_essentials.util;

import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

	public static String formatTimePlayed(DirtPlayer player) {
		int hours = (int) Math.floor(player.getTimePlayed() / 3600F);
		int minutes = (int) Math.floor((player.getTimePlayed() - (hours * 3600)) / 60F);
		int seconds = (int) Math.floor(player.getTimePlayed() % 60F);

		StringBuilder builder = new StringBuilder();
		if (hours == 1)
			builder.append(hours).append(" hour ");
		if (hours > 1)
			builder.append(hours).append(" hours ");
		if (minutes == 1)
			builder.append(minutes).append(" minute ");
		if (minutes > 1)
			builder.append(minutes).append(" minutes ");
		if (seconds == 1)
			builder.append(seconds).append(" second");
		if (seconds > 1)
			builder.append(seconds).append(" seconds");

		return builder.toString();
	}

	public static Component getPaginator(int page, int maxPages, String command) {
		MutableComponent paginator = new TextComponent("");

		TextComponent pagePrev;
		if (page == 1) {
			pagePrev = new TextComponent("  §7§l◀  ");
			HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§cYou are already on the first page!"));
			pagePrev.setStyle(pagePrev.getStyle().withHoverEvent(hover));
		} else {
			pagePrev = new TextComponent("  §a§l◀  ");
			HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§aClick to go to the previous page!"));
			ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (page - 1));
			pagePrev.setStyle(pagePrev.getStyle().withHoverEvent(hover).withClickEvent(click));
		}
		paginator.append(pagePrev);

		paginator.append(new TextComponent("§7Page §8" + page + " §7of §8" + maxPages));

		TextComponent pageNext;
		if (page == maxPages) {
			pageNext = new TextComponent("  §7§l▶  ");
			HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§cYou are already on the last page!"));
			pageNext.setStyle(pageNext.getStyle().withHoverEvent(hover));
		} else {
			pageNext = new TextComponent("  §a§l▶  ");
			HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§aClick to go to the next page!"));
			ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (page + 1));
			pageNext.setStyle(pageNext.getStyle().withHoverEvent(hover).withClickEvent(click));
		}

		paginator.append(pageNext);

		return paginator;
	}

	public static String formatCoordinates(double x, double y, double z) {
		return String.format(Locale.US, "§b%.2f§7, §b%.2f§7, §b%.2f", x, y, z);
	}

	public static List<String> getSummonableEntities() {
		List<String> summonableEntities = new ArrayList<>();
		for (EntityType<?> entityType : ForgeRegistries.ENTITIES.getValues()) {
			if (entityType.canSummon()) {
				ResourceLocation registryName = entityType.getRegistryName();
				if (registryName == null) continue;

				summonableEntities.add(registryName.toString());
			}

		}

		return summonableEntities;
	}
}
