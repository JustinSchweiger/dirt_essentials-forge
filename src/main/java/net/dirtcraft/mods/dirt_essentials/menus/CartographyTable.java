package net.dirtcraft.mods.dirt_essentials.menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;

public class CartographyTable extends CartographyTableMenu {
	public CartographyTable(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pContainerLevelAccess) {
		super(pContainerId, pPlayerInventory, pContainerLevelAccess);
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return true;
	}
}
