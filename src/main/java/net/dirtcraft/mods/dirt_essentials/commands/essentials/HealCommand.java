package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class HealCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("heal")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HEAL))
				.executes(HealCommand::self)
				.then(Commands.argument("target", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HEAL_OTHERS))
						.executes(HealCommand::other)));
	}

	private static int other(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "target");

		player.setHealth(player.getMaxHealth());
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You have been §ahealed§7!"), Util.NIL_UUID);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You have healed §6" + player.getName().getString() + "§7!"), true);

		return Command.SINGLE_SUCCESS;
	}

	private static int self(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		player.setHealth(player.getMaxHealth());
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You have been §ahealed§7!"), Util.NIL_UUID);

		return Command.SINGLE_SUCCESS;
	}
}
