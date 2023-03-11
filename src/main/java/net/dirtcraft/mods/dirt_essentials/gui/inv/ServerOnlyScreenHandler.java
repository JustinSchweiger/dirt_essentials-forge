package net.dirtcraft.mods.dirt_essentials.gui.inv;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class ServerOnlyScreenHandler extends AbstractContainerMenu {
	public final int rows;
	private final SeparateInventoryImpl inventory;

	protected ServerOnlyScreenHandler(int syncId, Inventory playerInventory, int rows) {
		super(getMenuType(rows), syncId);
		this.rows = rows;

		int i = (rows - 4) * 18;
		this.inventory = new SeparateInventoryImpl(rows * 9);
		this.fillInventoryWith(playerInventory.player, this.inventory);

		for (int n = 0; n < rows; n++) {
			for (int m = 0; m < 9; m++) {
				this.addSlot(new SlotDelegate(this.inventory, m + n * 9, 8 + m * 18, 18 + n * 18));
			}
		}

		for (int n = 0; n < 3; n++) {
			for (int m = 0; m < 9; m++) {
				this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i) {
					@Override
					public boolean mayPlace(@NotNull ItemStack stack) {
						return false;
					}

					@Override
					public boolean mayPickup(@NotNull Player playerEntity) {
						return false;
					}
				});
			}
		}

		for (int n = 0; n < 9; n++) {
			this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 161 + i) {
				@Override
				public boolean mayPlace(@NotNull ItemStack stack) {
					return false;
				}

				@Override
				public boolean mayPickup(@NotNull Player playerEntity) {
					return false;
				}
			});
		}
	}

	private static MenuType<ChestMenu> getMenuType(int rows) {
		return switch (rows) {
			case 2 -> MenuType.GENERIC_9x2;
			case 3 -> MenuType.GENERIC_9x3;
			case 4 -> MenuType.GENERIC_9x4;
			case 5 -> MenuType.GENERIC_9x5;
			case 6 -> MenuType.GENERIC_9x6;
			default -> MenuType.GENERIC_9x1;
		};
	}

	protected abstract void fillInventoryWith(Player player, SeparateInventory inv);

	@Override
	public boolean stillValid(@NotNull Player player) {
		return true;
	}

	@Override
	public void clicked(int i, int j, @NotNull ClickType actionType, @NotNull Player playerEntity) {
		if (i < 0)
			return;
		Slot slot = this.slots.get(i);
		if (this.isRightSlot(i)) {
			this.handleSlotClicked((ServerPlayer) playerEntity, i, slot, j);
		}
	}

	@Override
	public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
		if (index < 0)
			return ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (this.isRightSlot(index))
			this.handleSlotClicked((ServerPlayer) player, index, slot, 0);
		return slot.getItem().copy();
	}

	protected abstract boolean isRightSlot(int slot);

	/**
	 * @param clickType 0 for left click, 1 for right click
	 */
	protected abstract boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType);
}
