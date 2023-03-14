package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.dirtcraft.mods.dirt_essentials.manager.GodManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class UnaliveCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> commandBuilder = dispatcher.register(Commands
				.literal("unalive")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.UNALIVE))
				.executes(UnaliveCommand::execute));

		dispatcher.register(Commands.literal("r").requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.UNALIVE)).redirect(commandBuilder));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();

		if (GodManager.isGodModeEnabled(player.getUUID())) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou cannot unalive yourself while in god mode!"));
			return Command.SINGLE_SUCCESS;
		}

		player.hurt(DamageSource.GENERIC, Float.MAX_VALUE);
		return Command.SINGLE_SUCCESS;
	}
}
