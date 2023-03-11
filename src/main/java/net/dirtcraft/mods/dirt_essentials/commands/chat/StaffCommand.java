package net.dirtcraft.mods.dirt_essentials.commands.chat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.manager.ChatManager;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class StaffCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> argumentBuilder = Commands
				.literal("staff")
				.requires(source -> PermissionHandler.hasPermission(source, ChatPermissions.WRITE_STAFF))
				.executes(StaffCommand::changeChannel)
				.then(Commands.argument("message", StringArgumentType.greedyString())
						.executes(StaffCommand::sendMessage));

		dispatcher.register(argumentBuilder);
	}

	private static int sendMessage(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole) {
			source.sendSuccess(new TextComponent("You cannot send messages to the staff channel from console!"), true);
			return 0;
		}

		String message = StringArgumentType.getString(commandSourceStackCommandContext, "message");
		Component component = ChatManager.staff(source.getPlayerOrException(), message);

		DirtEssentials.SERVER.sendMessage(component, source.getPlayerOrException().getUUID());
		DirtEssentials.SERVER.getPlayerList().getPlayers().forEach(player -> {
			if (PermissionHandler.hasPermission(player.getUUID(), ChatPermissions.READ_STAFF)) {
				player.sendMessage(component, player.getUUID());
			}
		});

		return Command.SINGLE_SUCCESS;
	}

	private static int changeChannel(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole) {
			source.sendSuccess(new TextComponent("You cannot do that from console!"), true);
			return 0;
		}

		ChatManager.chatStaff(source.getPlayerOrException());

		return Command.SINGLE_SUCCESS;
	}
}
