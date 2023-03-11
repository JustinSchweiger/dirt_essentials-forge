package net.dirtcraft.mods.dirt_essentials.manager;

import net.minecraft.server.level.ServerPlayer;

public class FlyManager {

	public static boolean isFlying(ServerPlayer player) {
		return player.getAbilities().flying;
	}

	public static void enableFly(ServerPlayer player) {
		player.getAbilities().flying = true;
		player.getAbilities().mayfly = true;
		player.onUpdateAbilities();
	}

	public static void disableFly(ServerPlayer player) {
		player.getAbilities().flying = false;
		player.getAbilities().mayfly = false;
		player.onUpdateAbilities();
	}
}
