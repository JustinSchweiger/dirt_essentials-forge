package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;

public class BroadcastCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("broadcast")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.BROADCAST))
				.then(Commands.argument("message", StringArgumentType.greedyString())
						.executes(BroadcastCommand::execute));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		String message = StringArgumentType.getString(commandSourceStackCommandContext, "message");

		DirtEssentials.SERVER.getPlayerList().broadcastMessage(new TextComponent(""), ChatType.CHAT, Util.NIL_UUID);
		DirtEssentials.SERVER.getPlayerList().broadcastMessage(new TextComponent(""), ChatType.CHAT, Util.NIL_UUID);
		DirtEssentials.SERVER.getPlayerList().broadcastMessage(new TextComponent(EssentialsConfig.BROADCAST_PREFIX.get() + Utils.formatColorString(message)), ChatType.CHAT, Util.NIL_UUID);
		DirtEssentials.SERVER.getPlayerList().broadcastMessage(new TextComponent(""), ChatType.CHAT, Util.NIL_UUID);
		DirtEssentials.SERVER.getPlayerList().broadcastMessage(new TextComponent(""), ChatType.CHAT, Util.NIL_UUID);

		return Command.SINGLE_SUCCESS;
	}
}
