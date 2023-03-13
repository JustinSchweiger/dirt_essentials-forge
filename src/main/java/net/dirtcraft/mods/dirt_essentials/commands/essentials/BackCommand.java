package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.Location;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.dirtcraft.mods.dirt_essentials.manager.BackManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

public class BackCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("back")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.BACK))
				.executes(BackCommand::execute);

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = (ServerPlayer) source.getEntity();

		if (!BackManager.hasBackLocation(player.getUUID())) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You do not have a location to teleport back to!"), false);
			return Command.SINGLE_SUCCESS;
		}

		Location location = BackManager.getBackLocation(player.getUUID());
		ServerLevel level = DirtEssentials.SERVER.getLevel(location.getLevel());
		if (level == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThe world you are trying to teleport to does no longer exist!"), false);
			return Command.SINGLE_SUCCESS;
		}

		PlayerTeleportEvent event = new PlayerTeleportEvent(player, player.getX(), player.getY(), player.getZ());
		MinecraftForge.EVENT_BUS.post(event);

		player.teleportTo(level, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Teleported back to your previous location!"), Util.NIL_UUID);

		return Command.SINGLE_SUCCESS;
	}
}
