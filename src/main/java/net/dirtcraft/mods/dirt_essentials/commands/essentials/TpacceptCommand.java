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

public class TpacceptCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("tpaccept")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TPACCEPT))
				.executes(context -> tpaccept(context.getSource())));
	}

	private static int tpaccept(CommandSourceStack source) throws CommandSyntaxException {
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();

		if (!TeleportManager.hasIncomingTeleportRequests(player.getUUID())) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou do not have any teleport requests!"));
			return Command.SINGLE_SUCCESS;
		}

		TeleportManager.acceptAllTeleportRequests(player.getUUID());

		return Command.SINGLE_SUCCESS;
	}
}
