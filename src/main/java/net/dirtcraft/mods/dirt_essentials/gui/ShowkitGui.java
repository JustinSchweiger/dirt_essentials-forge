package net.dirtcraft.mods.dirt_essentials.gui;

import net.dirtcraft.mods.dirt_essentials.data.entites.Kit;
import net.dirtcraft.mods.dirt_essentials.gui.inv.SeparateInventory;
import net.dirtcraft.mods.dirt_essentials.gui.inv.ServerOnlyScreenHandler;
import net.dirtcraft.mods.dirt_essentials.manager.KitManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ShowkitGui extends ServerOnlyScreenHandler {
	private static Kit kit;

	protected ShowkitGui(int syncId, Inventory playerInventory, int rows) {
		super(syncId, playerInventory, rows);
	}

	public static void openShowkitGui(Player player, Kit kit) {
		ShowkitGui.kit = kit;
		MenuProvider fac = new MenuProvider() {
			@Override
			public AbstractContainerMenu createMenu(int syncId, @NotNull Inventory inv, @NotNull Player player) {

				return new ShowkitGui(syncId, inv, 5);
			}

			@Override
			public @NotNull Component getDisplayName() {
				return new TextComponent("§cKit: §b" + kit.getName());
			}
		};
		player.openMenu(fac);
	}

	@Override
	protected void fillInventoryWith(Player player, SeparateInventory inv) {
		if (!(player instanceof ServerPlayer))
			return;

		List<ItemStack> items = kit.getItems();
		for (int i = 0; i < items.size(); i++) {
			if (i > 26) break;
			inv.updateStack(i, items.get(i));
		}

		ItemStack claimKit = new ItemStack(Items.HOPPER);
		claimKit.setHoverName(new TextComponent("§aClaim this Kit"));

		List<Integer> border = new ArrayList<>(List.of(27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 41, 42, 43, 44));
		border.forEach(index -> {
			ItemStack borderItem = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
			borderItem.setHoverName(new TextComponent("§f"));
			inv.updateStack(index, borderItem);
		});

		inv.updateStack(40, claimKit);
	}

	@Override
	protected boolean isRightSlot(int slot) {
		return slot == 40;
	}

	@Override
	protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
		if (index == 40) {
			KitManager.claimKit(player, kit);
			player.closeContainer();
			return true;
		}

		return false;
	}
}
