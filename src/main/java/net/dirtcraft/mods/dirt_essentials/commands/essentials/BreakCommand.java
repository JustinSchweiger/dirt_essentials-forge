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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public class BreakCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("break")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.BREAK))
				.executes(BreakCommand::execute);

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = source.getEntity() == null;

		if (isConsole) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		BlockHitResult result = (BlockHitResult) source.getEntity().pick(10, 0, false);
		BlockPos pos = result.getBlockPos();
		ServerLevel level = source.getLevel();

		if (level.getBlockState(pos).getBlock() == Blocks.AIR) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThere is no block to break!"));
			return Command.SINGLE_SUCCESS;
		}

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Block broken!"), false);
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 50);

		return Command.SINGLE_SUCCESS;
	}
}
