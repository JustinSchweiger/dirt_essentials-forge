package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class SkullCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> commandNode = dispatcher.register(Commands
				.literal("skull")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SKULL))
				.executes(context -> skull(context, context.getSource().getPlayerOrException().getGameProfile().getName()))
				.then(Commands.argument("player", StringArgumentType.word())
						.executes(context -> skull(context, StringArgumentType.getString(context, "player")))));

		dispatcher.register(Commands.literal("head").requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SKULL)).redirect(commandNode));
	}

	private static int skull(CommandContext<CommandSourceStack> context, String name) {
		CommandSourceStack source = context.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new net.minecraft.network.chat.TextComponent("You must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		DirtEssentials.SERVER.getCommands().performCommand(DirtEssentials.SERVER.createCommandSourceStack(),
				"give " + source.getEntity().getName().getString() + " minecraft:player_head{display:{Name:'{\"text\":\"" + name + "\\'s Head\",\"color\":\"yellow\"}'},SkullOwner:\"" + name + "\"} 1"
		);

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You have been given the head of §6" + name), false);

		return Command.SINGLE_SUCCESS;
	}
}
