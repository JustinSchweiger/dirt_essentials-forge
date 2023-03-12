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
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class MsgCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> commandBuilder = dispatcher.register(Commands
				.literal("msg")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.MSG))
				.then(Commands.argument("player", EntityArgument.player())
						.then(Commands.argument("message", StringArgumentType.greedyString())
								.executes(MsgCommand::execute))));

		dispatcher.register(Commands.literal("tell").requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.MSG)).redirect(commandBuilder));
		dispatcher.register(Commands.literal("w").requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.MSG)).redirect(commandBuilder));
		dispatcher.register(Commands.literal("whisper").requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.MSG)).redirect(commandBuilder));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer sender = source.getPlayerOrException();
		ServerPlayer receiver = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");
		String message = StringArgumentType.getString(commandSourceStackCommandContext, "message");

		if (receiver.getUUID().equals(sender.getUUID())) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou can't message yourself!"));
			return Command.SINGLE_SUCCESS;
		}

		MsgManager.setLastMessagedPlayer(sender.getUUID(), receiver);
		MsgManager.message(sender, receiver, message);

		return Command.SINGLE_SUCCESS;
	}
}
