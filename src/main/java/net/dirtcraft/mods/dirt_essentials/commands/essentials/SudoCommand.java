package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class SudoCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("sudo")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SUDO))
				.then(Commands.argument("player", EntityArgument.player())
						.then(Commands.argument("command", StringArgumentType.greedyString())
								.executes(SudoCommand::execute))));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");
		String command = StringArgumentType.getString(commandSourceStackCommandContext, "command");

		if (!command.startsWith("/"))
			command = "/" + command;

		ServerGamePacketListenerImpl connection = player.connection;
		connection.handleChat(new ServerboundChatPacket(command));

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You made §6" + player.getGameProfile().getName() + " §7execute §d" + command), false);

		return Command.SINGLE_SUCCESS;
	}
}
