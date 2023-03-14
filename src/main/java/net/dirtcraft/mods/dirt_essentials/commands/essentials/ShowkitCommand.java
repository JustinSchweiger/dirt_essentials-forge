package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.database.Kit;
import net.dirtcraft.mods.dirt_essentials.gui.ShowkitGui;
import net.dirtcraft.mods.dirt_essentials.manager.KitManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;

public class ShowkitCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("showkit")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SHOWKIT))
				.then(Commands.argument("name", StringArgumentType.string())
						.suggests(((context, builder) -> SharedSuggestionProvider.suggest(KitManager.getAllKitsWithPermission(context.getSource()).stream().map(Kit::getName), builder)))
						.executes(ShowkitCommand::execute));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		String name = StringArgumentType.getString(commandSourceStackCommandContext, "name");
		boolean isConsole = source.getEntity() == null;

		if (isConsole) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		Kit kit = Kit.get(name);
		if (kit == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "A kit with that name doesn't exists!"), false);
			return Command.SINGLE_SUCCESS;
		}

		if (PermissionHandler.hasPermission(source, EssentialsPermissions.KIT + "." + name) || PermissionHandler.hasPermission(source, EssentialsPermissions.KIT + ".*")) {
			ShowkitGui.openShowkitGui(source.getPlayerOrException(), kit);
		} else {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou don't have permission to view this kit!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
