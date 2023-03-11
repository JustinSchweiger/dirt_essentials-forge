package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class FeedCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("feed")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.FEED))
				.executes(FeedCommand::selfFeed)
				.then(Commands.argument("player", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.FEED_OTHERS))
						.executes(FeedCommand::otherFeed));

		dispatcher.register(commandBuilder);
	}

	private static int otherFeed(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		ServerPlayer target = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");

		target.getFoodData().eat(20, 20);
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You have §efed §6" + target.getName().getString() + "§7!"), player.getUUID());
		target.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You have been §efed §7by §6" + player.getName().getString() + "§7!"), target.getUUID());
		return Command.SINGLE_SUCCESS;
	}

	private static int selfFeed(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		player.getFoodData().eat(20, 20);

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You have been §efed§7!"), false);
		return Command.SINGLE_SUCCESS;
	}
}
