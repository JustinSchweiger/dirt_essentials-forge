package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class WorldCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("world")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.WORLD))
				.executes(WorldCommand::execute);

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"), true);
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + player.getCommandSenderWorld().dimension()), Util.NIL_UUID);
		return Command.SINGLE_SUCCESS;
	}
}
