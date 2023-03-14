package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.dirtcraft.mods.dirt_essentials.manager.TeleportManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

public class TpCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("tp")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TP))
				.then(Commands.argument("target", EntityArgument.player())
						.executes(context -> teleport(context.getSource(), EntityArgument.getPlayer(context, "target"), null))
						.then(Commands.argument("target2", EntityArgument.player())
								.executes(context -> teleport(context.getSource(), EntityArgument.getPlayer(context, "target"), EntityArgument.getPlayer(context, "target2"))))));
	}

	private static int teleport(CommandSourceStack source, ServerPlayer target1, ServerPlayer target2) throws CommandSyntaxException {
		if (target2 == null) {
			ServerPlayer player = source.getPlayerOrException();

			if (TeleportManager.hasTeleportsDisabled(target1.getUUID()) && !PermissionHandler.hasPermission(source, EssentialsPermissions.TP_BYPASS)) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou cannot teleport to §6" + target1.getName().getString() + " §cbecause this player has teleports disabled!"));
				return Command.SINGLE_SUCCESS;
			}

			player.teleportTo(target1.getLevel(), target1.getX(), target1.getY(), target1.getZ(), target1.getYRot(), target1.getXRot());
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You teleported to §6" + target1.getName().getString()), false);
			return Command.SINGLE_SUCCESS;
		}

		PlayerTeleportEvent event = new PlayerTeleportEvent(target1, target2.getX(), target2.getY(), target2.getZ());
		MinecraftForge.EVENT_BUS.post(event);

		target1.teleportTo(target2.getLevel(), target2.getX(), target2.getY(), target2.getZ(), target2.getYRot(), target2.getXRot());
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You teleported §6" + target1.getName().getString() + " §7to §6" + target2.getName().getString()), false);
		return Command.SINGLE_SUCCESS;
	}
}
