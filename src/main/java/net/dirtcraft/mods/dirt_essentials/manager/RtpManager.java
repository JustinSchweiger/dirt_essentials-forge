package net.dirtcraft.mods.dirt_essentials.manager;

import com.mojang.brigadier.Command;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.config.RtpConfig;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.permissions.RtpPermissions;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class RtpManager {
	private static final Map<UUID, LocalDateTime> rtpcCooldown = new HashMap<>();
	private static final Map<UUID, LocalDateTime> rtpCooldown = new HashMap<>();

	public static boolean canRtp(UUID uuid) {
		if (rtpCooldown.containsKey(uuid)) {
			LocalDateTime time = rtpCooldown.get(uuid);
			return time.isBefore(LocalDateTime.now());
		}
		return true;
	}

	public static boolean canRtpc(UUID uuid) {
		if (rtpcCooldown.containsKey(uuid)) {
			LocalDateTime time = rtpcCooldown.get(uuid);
			return time.isBefore(LocalDateTime.now());
		}
		return true;
	}

	public static void setRtpcCooldown(UUID uuid) {
		rtpcCooldown.put(uuid, LocalDateTime.now().plusSeconds(RtpConfig.RTPC_COOLDOWN.get()));
	}

	public static void setRtpCooldown(UUID uuid) {
		rtpCooldown.put(uuid, LocalDateTime.now().plusSeconds(RtpConfig.RTP_COOLDOWN.get()));
	}

	public static int getRptCooldownLeft(UUID uuid) {
		if (rtpCooldown.containsKey(uuid)) {
			LocalDateTime time = rtpCooldown.get(uuid);
			return (int) (time.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
		}
		return 0;
	}

	public static int getRptcCooldownLeft(UUID uuid) {
		if (rtpcCooldown.containsKey(uuid)) {
			LocalDateTime time = rtpcCooldown.get(uuid);
			return (int) (time.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
		}
		return 0;
	}

	public static void rtpcPlayer(ServerPlayer player, ServerLevel world, boolean isOtherRtpc) {
		if (!world.dimension().equals(Level.END)) {
			if (!isOtherRtpc) {
				player.sendMessage(new TextComponent(Strings.RTP_PREFIX + "§cYou must be in the end to use this command!"), Util.NIL_UUID);
				return;
			}
		}

		if (isOtherRtpc) {
			rtpc(player, world);
			return;
		}

		if (!canRtpc(player.getUUID()) && !PermissionHandler.hasPermission(player.getUUID(), RtpPermissions.RTPC_BYPASS)) {
			player.sendMessage(new TextComponent(Strings.RTP_PREFIX + "§cYou must wait §6" + getRptcCooldownLeft(player.getUUID()) + "§c seconds before using this command again!"), Util.NIL_UUID);
			return;
		}

		rtpc(player, world);
		setRtpcCooldown(player.getUUID());
	}

	private static void rtpc(ServerPlayer player, ServerLevel world) {
		Random random = new Random();
		int randomX = random.nextInt(101) - random.nextInt(101);
		int randomZ = random.nextInt(101) - random.nextInt(101);

		int islandX = randomX * 10000;
		int islandZ = randomZ * 10000;
		int y = world.getHeight(Heightmap.Types.WORLD_SURFACE, islandX, islandZ) + 3;

		String coords = String.format("§b%d§7, §b%d§7, §b%d", islandX, y, islandZ);

		player.teleportTo(world, islandX, y, islandZ, player.getYRot(), player.getXRot());
		player.connection.send(new ClientboundCustomSoundPacket(new ResourceLocation("minecraft:entity.enderman.teleport"), SoundSource.PLAYERS, new Vec3(player.getX(), player.getY(), player.getZ()), 1.0F, 1.0F));
		player.connection.send(new ClientboundSetTitleTextPacket(new TextComponent("§cDirtCraft §bChaos Rtp")));
		player.connection.send(new ClientboundSetSubtitleTextPacket(new TextComponent(coords)));
		player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 10));
	}

	public static void rtpPlayer(ServerPlayer player, ServerLevel world, boolean isOtherRtp) {
		boolean worldsWhitelist = RtpConfig.WORLD_LIST_TYPE.get();
		List<String> worldList = new ArrayList<>(RtpConfig.WORLD_LIST.get());
		List<ResourceKey<Level>> worlds = new ArrayList<>();

		for (String worldName : worldList) {
			worlds.add(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(worldName)));
		}

		if (worldsWhitelist) {
			if (worlds.stream().noneMatch(world.dimension()::equals)) {
				player.sendMessage(new TextComponent(Strings.RTP_PREFIX + "§cYou cannot use this command in this world!"), Util.NIL_UUID);
				return;
			}
		} else {
			if (worlds.stream().anyMatch(world.dimension()::equals)) {
				player.sendMessage(new TextComponent(Strings.RTP_PREFIX + "§cYou cannot use this command in this world!"), Util.NIL_UUID);
				return;
			}
		}

		if (isOtherRtp) {
			rtp(player, world);
			return;
		}

		if (!canRtp(player.getUUID()) && !PermissionHandler.hasPermission(player.getUUID(), RtpPermissions.RTP_BYPASS)) {
			player.sendMessage(new TextComponent(Strings.RTP_PREFIX + "§cYou must wait §6" + getRptCooldownLeft(player.getUUID()) + "§c seconds before using this command again!"), Util.NIL_UUID);
			return;
		}

		if (rtp(player, world)) {
			setRtpCooldown(player.getUUID());
		}
	}

	private static boolean rtp(ServerPlayer player, ServerLevel level) {
		int attempts = 1;
		int maxAttempts = RtpConfig.MAX_ATTEMPTS.get();
		List<String> blacklistedBiomes = new ArrayList<>(RtpConfig.BLACKLISTED_BIOMES.get());

		while (attempts <= maxAttempts) {
			if (attempts == maxAttempts) {
				player.sendMessage(new TextComponent(Strings.RTP_PREFIX + "§cCould not find a safe location after §6" + maxAttempts + "§c attempts!"), Util.NIL_UUID);
				return false;
			}

			WorldBorder worldBorder = level.getWorldBorder();
			Random random = new Random();

			int centerX = (int) worldBorder.getCenterX();
			int centerZ = (int) worldBorder.getCenterZ();
			int radius = (int) worldBorder.getSize() / 2;

			int minusX = centerX - radius;
			int minusZ = centerZ - radius;

			int plusX = centerX + radius;
			int plusZ = centerZ + radius;

			int x = random.nextInt(plusX - minusX) + minusX;
			int z = random.nextInt(plusZ - minusZ) + minusZ;

			int y = 10;

			boolean safe = false;
			while (!safe) {
				Block block = level.getBlockState(new BlockPos(x, y, z)).getBlock();
				Block blockAbove = level.getBlockState(new BlockPos(x, y + 1, z)).getBlock();
				Block blockBelow = level.getBlockState(new BlockPos(x, y - 1, z)).getBlock();

				if (block == Blocks.AIR && blockAbove == Blocks.AIR && blockBelow != Blocks.AIR && blockBelow != Blocks.WATER && blockBelow != Blocks.LAVA) {
					safe = true;
				}

				if (level.dimension() == Level.OVERWORLD && y > 160)
					break;

				if (level.dimension() == Level.NETHER && y > RtpConfig.NETHER_ROOF.get())
					break;

				y++;
			}

			if (!safe) {
				attempts++;
				continue;
			}

			int finalY = y;
			boolean foundBlacklistedBiome = blacklistedBiomes
					.stream()
					.anyMatch(biome -> level.getBiome(new BlockPos(x, finalY, z)).is(new ResourceLocation(biome)));

			if (foundBlacklistedBiome) {
				attempts++;
				continue;
			}

			if (level.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.WATER || level.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.LAVA ) {
				attempts++;
				continue;
			}

			String coords = String.format("§b%d§7, §b%d§7, §b%d", x, y, z);

			player.teleportTo(level, x + 0.5, y, z + 0.5, player.getYRot(), player.getXRot());
			player.connection.send(new ClientboundCustomSoundPacket(new ResourceLocation("minecraft:entity.enderman.teleport"), SoundSource.PLAYERS, new Vec3(player.getX(), player.getY(), player.getZ()), 1.0F, 1.0F));
			player.connection.send(new ClientboundSetTitleTextPacket(new TextComponent("§cDirtCraft §bRtp")));
			player.connection.send(new ClientboundSetSubtitleTextPacket(new TextComponent(coords)));
			player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 10));
			return true;
		}

		return false;
	}
}
