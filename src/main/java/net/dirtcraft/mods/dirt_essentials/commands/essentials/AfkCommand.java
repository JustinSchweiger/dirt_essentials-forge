package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.manager.AfkManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class AfkCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("afk")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.AFK))
				.executes(AfkCommand::execute);

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = (ServerPlayer) source.getEntity();

		if (AfkManager.isPlayerAfk(player)) {
			AfkManager.removeAfk(player);
		} else {
			AfkManager.setAfk(player);
		}

		return Command.SINGLE_SUCCESS;
	}
}
