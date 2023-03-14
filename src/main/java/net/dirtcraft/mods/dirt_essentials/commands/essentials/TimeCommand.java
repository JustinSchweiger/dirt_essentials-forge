package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

public class TimeCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("time")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TIME))
				.then(Commands.literal("add")
						.then(Commands.argument("amount to add", IntegerArgumentType.integer(1))
								.executes(context -> addTime(context, IntegerArgumentType.getInteger(context, "amount to add"), null))
								.then(Commands.argument("world", DimensionArgument.dimension())
										.executes(context -> addTime(context, IntegerArgumentType.getInteger(context, "amount to add"), DimensionArgument.getDimension(context, "world"))))))
				.then(Commands.literal("set")
						.then(Commands.literal("day")
								.executes(context -> setTime(context, 0, null)))
						.then(Commands.literal("noon")
								.executes(context -> setTime(context, 6000, null)))
						.then(Commands.literal("night")
								.executes(context -> setTime(context, 13000, null)))
						.then(Commands.literal("midnight")
								.executes(context -> setTime(context, 18000, null)))
						.then(Commands.argument("time", IntegerArgumentType.integer(0, 24000))
								.executes(context -> setTime(context, IntegerArgumentType.getInteger(context, "time"), null))
								.then(Commands.argument("world", DimensionArgument.dimension())
										.executes(context -> setTime(context, IntegerArgumentType.getInteger(context, "time"), DimensionArgument.getDimension(context, "world")))))));
	}

	private static int setTime(CommandContext<CommandSourceStack> context, int time, ServerLevel dimension) {
		CommandSourceStack source = context.getSource();
		if (source.getEntity() == null && dimension == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		if (dimension == null)
			dimension = source.getLevel();

		dimension.setDayTime(time);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Time set to §b" + time + " §7in world §e" + dimension.dimension().location() + "§7!"), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int addTime(CommandContext<CommandSourceStack> context, int time, ServerLevel dimension) {
		CommandSourceStack source = context.getSource();
		if (source.getEntity() == null && dimension == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		if (dimension == null)
			dimension = source.getLevel();

		dimension.setDayTime(dimension.getDayTime() + time);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Time added by §b" + time + " §7in world §e" + dimension.dimension().location() + "§7!"), false);

		return Command.SINGLE_SUCCESS;
	}
}
