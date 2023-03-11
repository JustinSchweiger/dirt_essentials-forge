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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;

public class EnderchestCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("enderchest")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ENDERCHEST))
				.executes(EnderchestCommand::ownEnderchest)
				.then(Commands.argument("player", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ENDERCHEST_OTHERS))
						.executes(EnderchestCommand::otherEnderchest));

		dispatcher.register(commandBuilder);
	}

	private static int otherEnderchest(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		ServerPlayer target = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");

		player.openMenu(getEnderchestMenu(target));

		return Command.SINGLE_SUCCESS;
	}

	private static int ownEnderchest(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		player.openMenu(getEnderchestMenu(player));

		return Command.SINGLE_SUCCESS;
	}

	private static MenuProvider getEnderchestMenu(ServerPlayer player) {
		PlayerEnderChestContainer container = player.getEnderChestInventory();
		return new SimpleMenuProvider((id, inventory, playerEntity) -> ChestMenu.threeRows(id, inventory, container), new TextComponent("Enderchest"));
	}
}
