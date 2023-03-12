package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("help")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HELP))
				.executes(HelpCommand::mainPage)
				.then(Commands.argument("page", StringArgumentType.word())
						.suggests(((context, builder) -> SharedSuggestionProvider.suggest(getPages(), builder)))
						.executes(HelpCommand::page)));
	}

	private static List<String> getPages() {
		List<String> pages = new ArrayList<>();

		for (String page : EssentialsConfig.HELP.get()) {
			EssentialsConfig.Help help = EssentialsConfig.Help.deserialize(page);
			pages.add(help.getName());
		}

		return pages;
	}

	private static boolean isPage(String page) {
		for (String pageString : EssentialsConfig.HELP.get()) {
			EssentialsConfig.Help help = EssentialsConfig.Help.deserialize(pageString);

			if (help.getName().equalsIgnoreCase(page)) {
				return true;
			}
		}

		return false;
	}

	private static int page(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		String page = StringArgumentType.getString(commandSourceStackCommandContext, "page");

		if (EssentialsConfig.HELP.get().isEmpty()) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cNo help pages found!"), false);
			return Command.SINGLE_SUCCESS;
		}

		if (!isPage(page)) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7No help page found with the name §c" + page + "§7!"), false);
			return Command.SINGLE_SUCCESS;
		}

		EssentialsConfig.Help help = EssentialsConfig.Help.get(page);

		source.sendSuccess(new TextComponent(""), false);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Help page for §d" + help.getTitle() + "§7:"), false);
		source.sendSuccess(new TextComponent(""), false);

		for (String line : help.getLines()) {
			source.sendSuccess(new TextComponent(line), false);
		}

		source.sendSuccess(new TextComponent(""), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int mainPage(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();

		source.sendSuccess(new TextComponent(""), false);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aAvailable help pages§7:"), false);
		source.sendSuccess(new TextComponent("    §7▼ §8§oClick for more info! §7▼"), false);
		source.sendSuccess(new TextComponent(""), false);

		if (EssentialsConfig.HELP.get().isEmpty()) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cNo help pages found!"), false);
			source.sendSuccess(new TextComponent(""), false);
			return Command.SINGLE_SUCCESS;
		}

		for (String pageString : EssentialsConfig.HELP.get()) {
			EssentialsConfig.Help help = EssentialsConfig.Help.deserialize(pageString);

			TextComponent pageEntry = new TextComponent(" §7➤ " + help.getTitle());
			pageEntry.setStyle(pageEntry.getStyle()
					.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/help " + help.getName()))
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§3ℹ §7Click to view this page!"))));

			source.sendSuccess(pageEntry, false);
		}

		source.sendSuccess(new TextComponent(""), false);
		return Command.SINGLE_SUCCESS;
	}
}
