package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.manager.FlyManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class FlyCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("fly")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.FLY))
				.executes(FlyCommand::selfFly)
				.then(Commands.argument("player", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.FLY_OTHERS))
						.executes(FlyCommand::otherFly));

		dispatcher.register(commandBuilder);
	}

	private static int otherFly(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");
		if (FlyManager.isFlying(player)) {
			FlyManager.disableFly(player);
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Creative flight is now §cdisabled §7for §6" + player.getGameProfile().getName()), false);
		} else {
			FlyManager.enableFly(player);
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Creative flight is now §aenabled §7for §6" + player.getGameProfile().getName()), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int selfFly(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		if (FlyManager.isFlying(player)) {
			FlyManager.disableFly(player);
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Creative flight is now §cdisabled§7!"), false);
		} else {
			FlyManager.enableFly(player);
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Creative flight is now §aenabled§7!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
