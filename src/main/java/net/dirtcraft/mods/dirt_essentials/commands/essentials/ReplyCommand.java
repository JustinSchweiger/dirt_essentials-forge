package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.dirtcraft.mods.dirt_essentials.manager.MsgManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class ReplyCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> commandBuilder = dispatcher.register(Commands
				.literal("reply")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.REPLY))
				.then(Commands.argument("message", StringArgumentType.greedyString())
						.executes(ReplyCommand::execute)));

		dispatcher.register(Commands.literal("r").requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.REPLY)).redirect(commandBuilder));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		String message = StringArgumentType.getString(commandSourceStackCommandContext, "message");
		ServerPlayer player = source.getPlayerOrException();
		ServerPlayer target = MsgManager.getLastMessagedPlayer(player.getUUID());
		if (target == null || target.hasDisconnected()) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou have noone to reply to!"));
			return Command.SINGLE_SUCCESS;
		}

		MsgManager.message(player, target, message);
		MsgManager.setLastMessagedPlayer(target.getUUID(), player);
		return Command.SINGLE_SUCCESS;
	}
}
