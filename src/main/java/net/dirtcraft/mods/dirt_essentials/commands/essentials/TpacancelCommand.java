package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.manager.TeleportManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class TpacancelCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("tpacancel")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TPACANCEL))
				.executes(context -> tpacancel(context.getSource())));
	}

	private static int tpacancel(CommandSourceStack source) throws CommandSyntaxException {
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();

		if (!TeleportManager.hasOutgoingTeleportRequests(player.getUUID())) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou cannot cancel any teleport requests!"));
			return Command.SINGLE_SUCCESS;
		}

		TeleportManager.cancelAllTeleportRequests(player.getUUID());
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You have canceled all teleport requests."), false);

		return Command.SINGLE_SUCCESS;
	}
}
