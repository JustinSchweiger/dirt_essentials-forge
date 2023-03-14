package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

public class TpposCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("tppos")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TPPOS))
				.then(Commands.argument("pos", BlockPosArgument.blockPos())
						.executes(context -> teleport(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), context.getSource().getLevel(), context.getSource().getPlayerOrException()))
						.then(Commands.argument("dimension", DimensionArgument.dimension())
								.executes(context -> teleport(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), DimensionArgument.getDimension(context, "dimension"), context.getSource().getPlayerOrException()))
								.then(Commands.argument("player", EntityArgument.player())
										.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TPPOS_OTHERS))
										.executes(context -> teleport(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), DimensionArgument.getDimension(context, "dimension"), EntityArgument.getPlayer(context, "player")))))));
	}

	private static int teleport(CommandSourceStack source, BlockPos pos, ServerLevel dimension, ServerPlayer player) {
		PlayerTeleportEvent event = new PlayerTeleportEvent(player, pos.getX(), pos.getY(), pos.getZ());
		MinecraftForge.EVENT_BUS.post(event);

		player.teleportTo(dimension, pos.getX(), pos.getY(), pos.getZ(), player.getYRot(), player.getXRot());
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Teleported §6" + player.getName().getString() + " §7to §e" + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " §7in §e" + dimension.dimension().location()), false);
		return Command.SINGLE_SUCCESS;
	}
}
