package net.dirtcraft.mods.dirt_essentials.commands.rtp;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.manager.RtpManager;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.permissions.RtpPermissions;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class RtpCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> rtpCommand = Commands
				.literal("rtp")
				.requires(source -> PermissionHandler.hasPermission(source, RtpPermissions.RTP))
				.executes(RtpCommand::rtp)
				.then(Commands.argument("player", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, RtpPermissions.RTP_OTHERS))
						.executes(RtpCommand::rtpOther));

		dispatcher.register(rtpCommand);
	}

	private static int rtp(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.RTP_PREFIX + "You must be a player to use this command!"), true);
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		ServerLevel world = player.getLevel();

		new Thread(() -> RtpManager.rtpPlayer(player, world, false)).start();

		return Command.SINGLE_SUCCESS;
	}

	private static int rtpOther(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");
		ServerLevel world = player.getLevel();

		new Thread(() -> RtpManager.rtpPlayer(player, world, true)).start();

		return Command.SINGLE_SUCCESS;
	}
}
