package net.dirtcraft.mods.dirt_essentials.gui.inv;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SeparateInventoryImpl extends SimpleContainer implements SeparateInventory {

	public SeparateInventoryImpl(int size) {
		super(size);
	}

	@Override
	public @NotNull ItemStack getItem(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public @NotNull List<ItemStack> removeAllItems() {
		return new ArrayList<>();
	}

	@Override
	public @NotNull ItemStack removeItem(int slot, int amount) {
		return ItemStack.EMPTY;
	}

	@Override
	public @NotNull ItemStack removeItemType(@NotNull Item item, int count) {
		return ItemStack.EMPTY;
	}

	@Override
	public @NotNull ItemStack addItem(@NotNull ItemStack stack) {
		return stack;
	}

	@Override
	public boolean canAddItem(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public @NotNull ItemStack removeItemNoUpdate(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, @NotNull ItemStack stack) {

	}

	@Override
	public boolean stillValid(@NotNull Player player) {
		return false;
	}

	@Override
	public void updateStack(int slot, ItemStack stack) {
		super.setItem(slot, stack);
	}

	@Override
	public ItemStack getActualStack(int slot) {
		return super.getItem(slot);
	}
}
