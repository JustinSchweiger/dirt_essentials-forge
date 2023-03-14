package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.data.entites.Spawn;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.hibernate.Session;

import java.util.UUID;

public class SpawnManager {
	public static SpawnResult teleportToOverworldSpawn(UUID target) {
		ServerPlayer player = DirtEssentials.SERVER.getPlayerList().getPlayer(target);
		if (player == null)
			return SpawnResult.PLAYER_NOT_FOUND;

		Spawn spawn;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			spawn = session.get(Spawn.class, Level.OVERWORLD.location().toString());
		}

		if (spawn == null)
			return SpawnResult.NO_OVERWORLD_SPAWN;

		ServerLevel level = DirtEssentials.SERVER.getLevel(spawn.getLevel());
		if (level == null)
			return SpawnResult.LEVEL_NOT_FOUND;

		PlayerTeleportEvent event = new PlayerTeleportEvent(player, spawn.getX(), spawn.getY(), spawn.getZ());
		MinecraftForge.EVENT_BUS.post(event);

		player.teleportTo(level, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch());
		return SpawnResult.SUCCESS;
	}

	public static SpawnResult teleportToSpawnInWorld(UUID target, ResourceKey<Level> level) {
		ServerPlayer player = DirtEssentials.SERVER.getPlayerList().getPlayer(target);
		if (player == null)
			return SpawnResult.PLAYER_NOT_FOUND;

		Spawn spawn;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			spawn = session.get(Spawn.class, level.location().toString());
		}

		if (spawn == null)
			return SpawnResult.NO_SPAWN_IN_WORLD;

		ServerLevel serverLevel = DirtEssentials.SERVER.getLevel(level);
		if (serverLevel == null)
			return SpawnResult.LEVEL_NOT_FOUND;

		PlayerTeleportEvent event = new PlayerTeleportEvent(player, spawn.getX(), spawn.getY(), spawn.getZ());
		MinecraftForge.EVENT_BUS.post(event);

		player.teleportTo(serverLevel, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch());
		return SpawnResult.SUCCESS;
	}

	public static void sendToOverworldSpawnOnNextLogin(UUID target) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, target);
			player.setTeleportToSpawnOnNextLogin(true);
			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
		}
	}

	public enum SpawnResult {
		NO_OVERWORLD_SPAWN,
		NO_SPAWN_IN_WORLD,
		PLAYER_NOT_FOUND,
		LEVEL_NOT_FOUND,
		SUCCESS
	}
}
