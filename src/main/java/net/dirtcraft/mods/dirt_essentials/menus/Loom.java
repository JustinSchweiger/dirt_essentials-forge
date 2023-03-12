package net.dirtcraft.mods.dirt_essentials.menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.LoomMenu;

public class Loom extends LoomMenu {
	public Loom(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
		super(pContainerId, pPlayerInventory, pAccess);
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return true;
	}
}
