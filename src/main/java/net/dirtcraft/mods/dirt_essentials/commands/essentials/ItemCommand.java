package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("item")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ITEM))
				.then(Commands.literal("name")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ITEM_NAME))
						.then(Commands.argument("name", StringArgumentType.greedyString())
								.executes(ItemCommand::name)))
				.then(Commands.literal("lore")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ITEM_LORE))
						.then(Commands.literal("add")
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ITEM_LORE_ADD))
								.then(Commands.argument("lore", StringArgumentType.greedyString())
										.executes(ItemCommand::addLore)))
						.then(Commands.literal("remove")
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ITEM_LORE_REMOVE))
								.then(Commands.argument("line", IntegerArgumentType.integer(1))
										.executes(ItemCommand::removeLore)))
						.then(Commands.literal("clear")
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ITEM_LORE_CLEAR))
								.executes(ItemCommand::clearLore))));
	}

	private static int addLore(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		ItemStack stack = player.getMainHandItem();
		String lore = StringArgumentType.getString(commandSourceStackCommandContext, "lore");
		Utils.addLore(stack, Utils.formatColorString(lore));
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Added lore §d\"" + Utils.formatColorString(lore) + "§d\" §7to the item!"), Util.NIL_UUID);
		return Command.SINGLE_SUCCESS;
	}

	private static int removeLore(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		int line = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "line");
		ItemStack stack = player.getMainHandItem();

		if (Utils.getLore(stack).size() < line) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Line §d" + line + "§7 does not exist!"));
			return Command.SINGLE_SUCCESS;
		}

		Utils.removeLore(stack, line - 1);
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aRemoved line §d" + line + "§a from the item!"), Util.NIL_UUID);

		return Command.SINGLE_SUCCESS;
	}

	private static int clearLore(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		ItemStack stack = player.getMainHandItem();
		Utils.clearLore(stack);
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Lore §acleared§7!"), Util.NIL_UUID);
		return Command.SINGLE_SUCCESS;
	}


	private static int name(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		ItemStack stack = player.getMainHandItem();
		String name = StringArgumentType.getString(commandSourceStackCommandContext, "name");
		stack.setHoverName(new TextComponent(Utils.formatColorString(name)));

		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Name set to §d\"" + Utils.formatColorString(name) + "§d\""), Util.NIL_UUID);

		return Command.SINGLE_SUCCESS;
	}
}
