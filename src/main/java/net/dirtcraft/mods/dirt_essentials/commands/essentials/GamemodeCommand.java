package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class GamemodeCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("gamemode")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE))
				.then(Commands.literal("survival")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_SURVIVAL))
						.executes(context -> GamemodeCommand.setMode(context.getSource(), context.getSource().getPlayerOrException(), GameType.SURVIVAL, false))
						.then(Commands.argument("player", EntityArgument.player())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_SURVIVAL_OTHERS))
								.executes(context -> GamemodeCommand.setMode(context.getSource(), EntityArgument.getPlayer(context, "player"), GameType.SURVIVAL, true))))
				.then(Commands.literal("creative")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_CREATIVE))
						.executes(context -> GamemodeCommand.setMode(context.getSource(), context.getSource().getPlayerOrException(), GameType.CREATIVE, false))
						.then(Commands.argument("player", EntityArgument.player())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_CREATIVE_OTHERS))
								.executes(context -> GamemodeCommand.setMode(context.getSource(), EntityArgument.getPlayer(context, "player"), GameType.CREATIVE, true))))
				.then(Commands.literal("adventure")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_ADVENTURE))
						.executes(context -> GamemodeCommand.setMode(context.getSource(), context.getSource().getPlayerOrException(), GameType.ADVENTURE, false))
						.then(Commands.argument("player", EntityArgument.player())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_ADVENTURE_OTHERS))
								.executes(context -> GamemodeCommand.setMode(context.getSource(), EntityArgument.getPlayer(context, "player"), GameType.ADVENTURE, true))))
				.then(Commands.literal("spectator")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_SPECTATOR))
						.executes(context -> GamemodeCommand.setMode(context.getSource(), context.getSource().getPlayerOrException(), GameType.SPECTATOR, false))
						.then(Commands.argument("player", EntityArgument.player())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_SPECTATOR_OTHERS))
								.executes(context -> GamemodeCommand.setMode(context.getSource(), EntityArgument.getPlayer(context, "player"), GameType.SPECTATOR, true)))));

		dispatcher.register(Commands.literal("gms")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_SURVIVAL))
				.executes(context -> GamemodeCommand.setMode(context.getSource(), context.getSource().getPlayerOrException(), GameType.SURVIVAL, false))
				.then(Commands.argument("player", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_SURVIVAL_OTHERS))
						.executes(context -> GamemodeCommand.setMode(context.getSource(), EntityArgument.getPlayer(context, "player"), GameType.SURVIVAL, true))));

		dispatcher.register(Commands.literal("gmc")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_CREATIVE))
				.executes(context -> GamemodeCommand.setMode(context.getSource(), context.getSource().getPlayerOrException(), GameType.CREATIVE, false))
				.then(Commands.argument("player", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_CREATIVE_OTHERS))
						.executes(context -> GamemodeCommand.setMode(context.getSource(), EntityArgument.getPlayer(context, "player"), GameType.CREATIVE, true))));

		dispatcher.register(Commands.literal("gma")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_ADVENTURE))
				.executes(context -> GamemodeCommand.setMode(context.getSource(), context.getSource().getPlayerOrException(), GameType.ADVENTURE, false))
				.then(Commands.argument("player", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_ADVENTURE_OTHERS))
						.executes(context -> GamemodeCommand.setMode(context.getSource(), EntityArgument.getPlayer(context, "player"), GameType.ADVENTURE, true))));

		dispatcher.register(Commands.literal("gmsp")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_SPECTATOR))
				.executes(context -> GamemodeCommand.setMode(context.getSource(), context.getSource().getPlayerOrException(), GameType.SPECTATOR, false))
				.then(Commands.argument("player", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GAMEMODE_SPECTATOR_OTHERS))
						.executes(context -> GamemodeCommand.setMode(context.getSource(), EntityArgument.getPlayer(context, "player"), GameType.SPECTATOR, true))));
	}

	private static int setMode(CommandSourceStack source, ServerPlayer player, GameType gameType, boolean isOther) {
		if (source.getEntity() == null && !isOther) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		String mode = "";
		switch (gameType) {
			case CREATIVE -> mode = "§ccreative";
			case SURVIVAL -> mode = "§asurvival";
			case ADVENTURE -> mode = "§eadventure";
			case SPECTATOR -> mode = "§bspectator";
		}

		if (isOther) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You have set §6" + player.getName().getString() + " §7to " + mode + " §7mode!"), true);
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Your are now in " + mode + " §7mode!"), player.getUUID());
		} else {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You are now in " + mode + " §7mode!"), true);
		}

		player.setGameMode(gameType);
		return Command.SINGLE_SUCCESS;
	}
}
