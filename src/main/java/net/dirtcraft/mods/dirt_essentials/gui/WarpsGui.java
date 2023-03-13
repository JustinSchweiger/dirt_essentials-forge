package net.dirtcraft.mods.dirt_essentials.gui;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.entites.Warp;
import net.dirtcraft.mods.dirt_essentials.gui.inv.SeparateInventory;
import net.dirtcraft.mods.dirt_essentials.gui.inv.ServerOnlyScreenHandler;
import net.dirtcraft.mods.dirt_essentials.manager.WarpManager;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WarpsGui extends ServerOnlyScreenHandler {
	private static final List<Warp> warps = new ArrayList<>();
	private static int rows;

	protected WarpsGui(int syncId, Inventory playerInventory, int rows) {
		super(syncId, playerInventory, rows);
	}

	public static void openWarpsGui(Player player) {
		warps.clear();
		warps.addAll(WarpManager.getWarps());
		MenuProvider fac = new MenuProvider() {
			@Override
			public AbstractContainerMenu createMenu(int syncId, @NotNull Inventory inv, @NotNull Player player) {
				int warpsCount = warps.size();

				rows = (int) Math.ceil(warpsCount / 9.0);

				return new WarpsGui(syncId, inv, rows);
			}

			@Override
			public @NotNull Component getDisplayName() {
				return new TextComponent("§cDirtCraft §6Warps");
			}
		};
		player.openMenu(fac);
	}

	@Override
	protected void fillInventoryWith(Player player, SeparateInventory inv) {
		ItemStack fillerItem = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
		fillerItem.setHoverName(new TextComponent("§f"));

		for (int i = 0; i < rows * 9; i++) {
			inv.updateStack(i, fillerItem);
		}

		for (int i = 0; i < warps.size(); i++) {
			inv.updateStack(i, warps.get(i).getItem());
		}
	}

	@Override
	protected boolean isRightSlot(int slot) {
		for (int i = 0; i < warps.size(); i++) {
			if (slot == i)
				return true;
		}

		return false;
	}

	@Override
	protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
		if (slot.getItem().isEmpty())
			return false;

		ItemStack stack = slot.getItem();
		Warp warp = warps.stream().filter(w -> w.getItem().serializeNBT().toString().equals(stack.serializeNBT().toString())).findFirst().orElse(null);
		if (warp == null)
			return false;

		ResourceKey<Level> dim = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation(warp.getRegistry())), new ResourceLocation(warp.getLocation()));
		ServerLevel level = DirtEssentials.SERVER.getLevel(dim);
		if (level == null)
			return false;

		player.teleportTo(level, warp.getX(), warp.getY(), warp.getZ(), player.getYRot(), player.getXRot());
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Teleported to warp §e" + warp.getName() + "§7!"), Util.NIL_UUID);
		player.closeContainer();

		return true;
	}
}
