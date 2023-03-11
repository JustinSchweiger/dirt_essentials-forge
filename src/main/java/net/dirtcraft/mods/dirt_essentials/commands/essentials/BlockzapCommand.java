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
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public class BlockzapCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("blockzap")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.BLOCKZAP))
				.then(Commands.argument("world", DimensionArgument.dimension())
						.then(Commands.argument("pos", BlockPosArgument.blockPos())
								.executes(BlockzapCommand::execute)));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerLevel level = DimensionArgument.getDimension(commandSourceStackCommandContext, "world");
		BlockPos pos = BlockPosArgument.getLoadedBlockPos(commandSourceStackCommandContext, "pos");

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aRemoved §e" + level.getBlockState(pos).getBlock().getRegistryName() + "§a at §b" + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "§a in §d" + level.dimension().location() + "§a!"), false);
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 50);

		return Command.SINGLE_SUCCESS;
	}
}
