package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class BalCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("bal")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.BAL))
				.executes(context -> execute(context, false))
				.then(Commands.argument("target", StringArgumentType.string())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.BAL_OTHERS))
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder))
						.executes(context -> execute(context, true)));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext, boolean hasTarget) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole && !hasTarget) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		UUID target = hasTarget ? PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "target")) : source.getEntity().getUUID();
		if (target == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"), false);
			return Command.SINGLE_SUCCESS;
		}

		DirtPlayer player = DirtPlayer.get(target);

		if (hasTarget) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§a" + player.getUsername() + "§6's Balance§7: " + player.getFormattedBalance()), false);
		} else {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§6Balance§7: " + player.getFormattedBalance()), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
