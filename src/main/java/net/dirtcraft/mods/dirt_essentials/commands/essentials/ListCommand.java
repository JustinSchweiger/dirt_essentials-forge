package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ListCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("list")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.LIST))
				.executes(ListCommand::execute));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();

		List<ServerPlayer> players = DirtEssentials.SERVER.getPlayerList().getPlayers();
		List<ServerPlayer> staffPlayers = players.stream().filter(player -> PermissionHandler.hasPermission(player.getUUID(), ChatPermissions.STAFF)).toList();
		List<ServerPlayer> nonStaffPlayers = players.stream().filter(player -> !PermissionHandler.hasPermission(player.getUUID(), ChatPermissions.STAFF)).toList();

		source.sendSuccess(new TextComponent(""), false);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§3Players online§7:"), false);
		source.sendSuccess(new TextComponent("§7There is currently §d" + players.size() + " §7player" + (players.size() == 1 ? "" : "s") + " online!"), false);
		source.sendSuccess(new TextComponent(""), false);

		if (!staffPlayers.isEmpty()) {
			TextComponent staffPlayersMessage = new TextComponent("§cStaff online§7: ");
			staffPlayersMessage.append(new TextComponent(String.join("§7, ", staffPlayers.stream().map(player -> player.getDisplayName().getString()).toList())));
			source.sendSuccess(staffPlayersMessage, false);
		}

		if (!nonStaffPlayers.isEmpty()) {
			TextComponent nonStaffPlayersMessage = new TextComponent("§ePlayers online§7: ");
			nonStaffPlayersMessage.append(new TextComponent(String.join("§7, ", nonStaffPlayers.stream().map(player -> player.getDisplayName().getString()).toList())));
			source.sendSuccess(nonStaffPlayersMessage, false);
		}

		source.sendSuccess(new TextComponent(""), false);

		return Command.SINGLE_SUCCESS;
	}
}
