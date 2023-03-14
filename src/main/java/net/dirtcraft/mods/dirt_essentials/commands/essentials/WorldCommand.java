package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class WorldCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("world")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.WORLD))
				.executes(WorldCommand::execute));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You are currently in world:\n\n  §a§o" + player.getLevel().dimension().registry() + " §7| §e§o" + player.getLevel().dimension().location() + "\n"), false);

		return Command.SINGLE_SUCCESS;
	}
}
