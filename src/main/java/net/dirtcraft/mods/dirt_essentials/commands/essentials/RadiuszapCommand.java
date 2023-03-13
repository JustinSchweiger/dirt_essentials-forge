package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class RadiuszapCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("radiuszap")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.RADIUSZAP))
				.then(Commands.argument("world", DimensionArgument.dimension())
						.then(Commands.argument("pos", BlockPosArgument.blockPos())
								.then(Commands.argument("radius", IntegerArgumentType.integer(1))
										.executes(context -> execute(context, false))
										.then(Commands.argument("block", BlockStateArgument.block())
												.executes(context -> execute(context, true)))))));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext, boolean hasBlock) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();

		ServerLevel world = DimensionArgument.getDimension(commandSourceStackCommandContext, "world");
		BlockPos pos = BlockPosArgument.getLoadedBlockPos(commandSourceStackCommandContext, "pos");
		int radius = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "radius");
		BlockPos start = new BlockPos(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius);
		BlockPos end = new BlockPos(pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);

		Block block = null;
		if (hasBlock) {
			block = BlockStateArgument.getBlock(commandSourceStackCommandContext, "block").getState().getBlock();
		}

		int removedBlocks = 0;
		for (int i = start.getX(); i <= end.getX(); i++) {
			for (int j = start.getY(); j <= end.getY(); j++) {
				for (int k = start.getZ(); k <= end.getZ(); k++) {
					if (block == null) {
						if (world.getBlockState(new BlockPos(i, j, k)).getBlock() == Blocks.AIR)
							continue;

						if (world.setBlock(new BlockPos(i, j, k), Blocks.AIR.defaultBlockState(), 54))
							removedBlocks++;
					} else {
						if (world.getBlockState(new BlockPos(i, j, k)).getBlock() == block) {
							if (world.setBlock(new BlockPos(i, j, k), Blocks.AIR.defaultBlockState(), 54))
								removedBlocks++;
						}
					}
				}
			}
		}

		if (hasBlock) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Removed §d" + removedBlocks + "x §e" + block.getRegistryName() + " §7!"), false);
		} else {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Removed §d" + removedBlocks + " §7blocks!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
