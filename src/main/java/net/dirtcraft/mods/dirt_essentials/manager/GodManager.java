package net.dirtcraft.mods.dirt_essentials.manager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodManager {
	private static final Set<UUID> godModeEnabled = new HashSet<>();

	public static boolean isGodModeEnabled(UUID uuid) {
		return godModeEnabled.contains(uuid);
	}

	public static void setGodModeEnabled(UUID uuid, boolean enabled) {
		if (enabled)
			godModeEnabled.add(uuid);
		else
			godModeEnabled.remove(uuid);
	}

	@SubscribeEvent
	public static void onLivingAttack(LivingAttackEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		if (isGodModeEnabled(event.getEntity().getUUID()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		if (event.isCanceled())
			return;

		if (isGodModeEnabled(event.getEntity().getUUID()))
			event.setCanceled(true);
	}
}
