package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.Spawn;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.manager.SpawnManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;

public class SpawnCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("spawn")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SPAWN))
				.executes(context -> execute(context, context.getSource().getPlayerOrException().getUUID(), false))
				.then(Commands.literal("set")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SPAWN_SET))
						.executes(SpawnCommand::setSpawn))
				.then(Commands.literal("remove")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SPAWN_REMOVE))
						.executes(SpawnCommand::removeSpawn))
				.then(Commands.argument("player", StringArgumentType.word())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SPAWN_OTHERS))
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder))
						.executes(context -> execute(context, PlayerManager.getUuid(StringArgumentType.getString(context, "player")), true))));
	}

	private static int removeSpawn(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ResourceKey<Level> dimension = source.getLevel().dimension();
		ServerPlayer player = source.getPlayerOrException();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Spawn spawn = session.createQuery("FROM Spawn WHERE registry = :registry AND location = :location", Spawn.class)
					.setParameter("registry", player.getLevel().dimension().registry().toString())
					.setParameter("location", player.getLevel().dimension().location().toString())
					.getSingleResultOrNull();

			if (spawn == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cSpawn not set for world §d" + dimension.location() + "§c!"));
			} else {
				session.beginTransaction();
				session.remove(spawn);
				session.getTransaction().commit();
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSpawn removed for world §d" + dimension.location() + "§a! Falling back to default one!"), false);
			}

			return Command.SINGLE_SUCCESS;
		}
	}

	private static int setSpawn(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ResourceKey<Level> dimension = source.getLevel().dimension();
		ServerPlayer player = source.getPlayerOrException();
		double x = player.getX();
		double y = player.getY();
		double z = player.getZ();
		float yaw = player.getYRot();
		float pitch = player.getXRot();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			List<Spawn> spawns = session.createQuery("FROM Spawn", Spawn.class).getResultList();
			boolean override = false;
			for (Spawn spawn : spawns) {
				if (spawn.getLevel().equals(dimension)) {
					session.beginTransaction();
					session.remove(spawn);
					session.getTransaction().commit();
					override = true;
				}
			}

			Spawn spawn = new Spawn(dimension, x, y, z, yaw, pitch);
			session.beginTransaction();
			session.persist(spawn);
			session.getTransaction().commit();

			if (override) {
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSpawn override for world §d" + dimension.location() + "§a!"), false);
			} else {
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSpawn set for world §d" + dimension.location() + "§a!"), false);
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext, UUID target, boolean isOther) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null && !isOther) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		if (target == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
			return Command.SINGLE_SUCCESS;
		}

		boolean targetIsOnline = DirtEssentials.SERVER.getPlayerList().getPlayer(target) != null;

		if (targetIsOnline) {
			ServerPlayer player = DirtEssentials.SERVER.getPlayerList().getPlayer(target);
			if (player == null)
				return Command.SINGLE_SUCCESS;

			if (isOther) {
				SpawnManager.SpawnResult result = SpawnManager.teleportToOverworldSpawn(target);
				switch (result) {
					case PLAYER_NOT_FOUND -> source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
					case LEVEL_NOT_FOUND -> source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cLevel not found!"));
					case SUCCESS ->
							source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Teleported §6" + player.getName().getString() + " §7to main spawn!"), false);
					case NO_OVERWORLD_SPAWN -> {
						ServerLevel overworld = DirtEssentials.SERVER.overworld();
						player.teleportTo(overworld, overworld.getSharedSpawnPos().getX(), overworld.getSharedSpawnPos().getY(), overworld.getSharedSpawnPos().getZ(), 0, 0);
						source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Teleported §6" + player.getName().getString() + " §7to default world spawn!"), false);
					}
				}
			} else {
				SpawnManager.SpawnResult result = SpawnManager.teleportToSpawnInWorld(target, player.getLevel().dimension());
				switch (result) {
					case PLAYER_NOT_FOUND -> source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
					case LEVEL_NOT_FOUND -> source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cLevel not found!"));
					case SUCCESS -> source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Teleported to spawn!"), false);
					case NO_SPAWN_IN_WORLD -> {
						SpawnManager.SpawnResult result2 = SpawnManager.teleportToOverworldSpawn(target);
						switch (result2) {
							case PLAYER_NOT_FOUND -> source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
							case LEVEL_NOT_FOUND -> source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cLevel not found!"));
							case SUCCESS -> source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Teleported to main spawn!"), false);
							case NO_OVERWORLD_SPAWN -> {
								ServerLevel overworld = DirtEssentials.SERVER.overworld();
								player.teleportTo(overworld, overworld.getSharedSpawnPos().getX(), overworld.getSharedSpawnPos().getY(), overworld.getSharedSpawnPos().getZ(), 0, 0);
								source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Teleported to default world spawn!"), false);
							}
						}
					}
				}
			}

			return Command.SINGLE_SUCCESS;
		}

		SpawnManager.sendToOverworldSpawnOnNextLogin(target);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Teleporting §6" + PlayerManager.getUsername(target) + " §7to spawn on next login!"), false);

		return Command.SINGLE_SUCCESS;
	}
}
