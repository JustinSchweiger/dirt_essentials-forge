package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.data.Location;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BackManager {
	private static final Map<UUID, Location> backLocations = new HashMap<>();

	public static void setBackLocation(UUID uuid, Location location) {
		backLocations.put(uuid, location);
	}

	public static Location getBackLocation(UUID uuid) {
		return backLocations.get(uuid);
	}

	public static boolean hasBackLocation(UUID uuid) {
		return backLocations.containsKey(uuid);
	}

	@SubscribeEvent
	public static void teleportCommandEvent(EntityTeleportEvent.TeleportCommand event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		setBackLocation(player.getUUID(), new Location(player.getCommandSenderWorld().dimension(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));
	}

	@SubscribeEvent
	public static void spreadCommandEvent(EntityTeleportEvent.SpreadPlayersCommand event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		setBackLocation(player.getUUID(), new Location(player.getCommandSenderWorld().dimension(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));
	}

	@SubscribeEvent
	public static void teleportEvent(PlayerTeleportEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		setBackLocation(player.getUUID(), new Location(player.getCommandSenderWorld().dimension(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));
	}

	@SubscribeEvent
	public static void entityDeathEvent(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		List<String> backWorlds = new ArrayList<>(EssentialsConfig.BACK_WORLDS.get());
		List<ResourceKey<Level>> worlds = new ArrayList<>();

		for (String world : backWorlds) {
			String registry = world.split(";;;")[0];
			String location = world.split(";;;")[1];
			worlds.add(ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation(registry)), new ResourceLocation(location)));
		}

		if (EssentialsConfig.BACK_WORLDS_WHITELIST.get()) {
			if (worlds.stream().noneMatch(player.getLevel().dimension()::equals)) {
				return;
			}
		} else {
			if (worlds.stream().anyMatch(player.getLevel().dimension()::equals)) {
				return;
			}
		}

		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Use ??c/back ??7to teleport back to where you died!"), Util.NIL_UUID);
		BackManager.setBackLocation(player.getUUID(), new Location(player.getCommandSenderWorld().dimension(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot()));
	}
}
