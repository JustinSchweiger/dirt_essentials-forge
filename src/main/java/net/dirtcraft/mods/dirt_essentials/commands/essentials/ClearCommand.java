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
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ClearCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("clear")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.CLEAR))
				.executes(ClearCommand::clearOwn)
				.then(Commands.argument("item", ItemArgument.item())
						.executes(ClearCommand::clearOwnItem))
				.then(Commands.argument("target", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.CLEAR_OTHERS))
						.executes(ClearCommand::clearTarget)
						.then(Commands.argument("item", ItemArgument.item())
								.executes(ClearCommand::clearTargetItem)));

		dispatcher.register(Commands.literal("clear").requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.CLEAR)).redirect(dispatcher.register(commandBuilder)));
	}

	private static int clearTargetItem(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer target = EntityArgument.getPlayer(commandSourceStackCommandContext, "target");
		Item item = ItemArgument.getItem(commandSourceStackCommandContext, "item").getItem();

		int count = 0;
		for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
			ItemStack stack = target.getInventory().getItem(i);
			if (stack.getItem() == item) {
				count += stack.getCount();
				target.getInventory().removeItem(i, stack.getCount());
			}
		}

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Cleared §b" + count + "x §e" + item.getRegistryName() + " §7from §6" + target.getGameProfile().getName() + "§7's inventory."), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int clearTarget(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer target = EntityArgument.getPlayer(commandSourceStackCommandContext, "target");
		target.getInventory().clearContent();
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Cleared §e" + target.getGameProfile().getName() + "§7's inventory."), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int clearOwnItem(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = source.getEntity() == null;

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		Item item = ItemArgument.getItem(commandSourceStackCommandContext, "item").getItem();

		int count = 0;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.getItem() == item) {
				count += stack.getCount();
				player.getInventory().removeItem(i, stack.getCount());
			}
		}

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Cleared §b" + count + "x §e" + item.getRegistryName() + " §7from your inventory."), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int clearOwn(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = source.getEntity() == null;

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		player.getInventory().clearContent();
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Cleared your inventory."), false);
		return Command.SINGLE_SUCCESS;
	}
}
