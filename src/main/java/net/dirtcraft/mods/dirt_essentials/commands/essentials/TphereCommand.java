package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

public class TphereCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("tphere")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TPHERE))
				.then(Commands.argument("target", EntityArgument.player())
						.executes(context -> teleport(context.getSource(), EntityArgument.getPlayer(context, "target")))));
	}

	private static int teleport(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();

		if (player.getUUID().equals(target.getUUID())) {
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou cannot teleport to yourself!"), Util.NIL_UUID);
			return Command.SINGLE_SUCCESS;
		}

		PlayerTeleportEvent event = new PlayerTeleportEvent(target, player.getX(), player.getY(), player.getZ());
		MinecraftForge.EVENT_BUS.post(event);

		target.teleportTo(player.getLevel(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aTeleported §6" + target.getName().getString() + " §ato you!"), Util.NIL_UUID);
		target.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aYou have been teleported to §6" + player.getGameProfile().getName()), Util.NIL_UUID);

		return Command.SINGLE_SUCCESS;
	}
}
