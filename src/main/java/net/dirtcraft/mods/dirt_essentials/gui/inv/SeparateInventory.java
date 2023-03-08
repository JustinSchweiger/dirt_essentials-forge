package net.dirtcraft.mods.dirt_essentials.gui.inv;

import net.minecraft.world.item.ItemStack;

/**
 * This ensures that other mods will never be able to modify and get items from the inventory.
 * For inventory management mods that also do things on the server (e.g. Quark)
 * Credit to <a href="https://www.curseforge.com/minecraft/mc-mods/flan-forge">Flan Mod</a> for the idea
 */
public interface SeparateInventory {

	void updateStack(int slot, ItemStack stack);

	ItemStack getActualStack(int slot);
}
