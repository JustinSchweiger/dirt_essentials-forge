package net.dirtcraft.mods.dirt_essentials.gui.inv;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotDelegate extends Slot {

	private final int index;

	public SlotDelegate(SeparateInventoryImpl inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.index = index;
	}

	@Override
	public void set(@NotNull ItemStack stack) {
		((SeparateInventoryImpl) this.container).updateStack(this.index, stack);
	}

	@Override
	public @NotNull ItemStack getItem() {
		return ((SeparateInventoryImpl) this.container).getActualStack(this.index);
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public boolean mayPickup(@NotNull Player playerEntity) {
		return false;
	}
}
