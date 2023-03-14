package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class WeatherCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("weather")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.WEATHER))
				.then(Commands.literal("clear")
						.executes(context -> setClear(context.getSource(), 6000))
						.then(Commands.argument("duration", IntegerArgumentType.integer(0, 1000000))
								.executes(context -> setClear(context.getSource(), IntegerArgumentType.getInteger(context, "duration") * 20))))
				.then(Commands.literal("rain")
						.executes(context -> setRain(context.getSource(), 6000))
						.then(Commands.argument("duration", IntegerArgumentType.integer(0, 1000000))
								.executes(context -> setRain(context.getSource(), IntegerArgumentType.getInteger(context, "duration") * 20))))
				.then(Commands.literal("thunder")
						.executes(context -> setThunder(context.getSource(), 6000))
						.then(Commands.argument("duration", IntegerArgumentType.integer(0, 1000000))
								.executes(context -> setThunder(context.getSource(), IntegerArgumentType.getInteger(context, "duration") * 20)))));
	}

	private static int setClear(CommandSourceStack pSource, int pTime) {
		pSource.getLevel().setWeatherParameters(pTime, 0, false, false);
		pSource.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Set the weather to §9clear"), true);
		return pTime;
	}

	private static int setRain(CommandSourceStack pSource, int pTime) {
		pSource.getLevel().setWeatherParameters(0, pTime, true, false);
		pSource.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Set the weather to §3rain"), true);
		return pTime;
	}

	private static int setThunder(CommandSourceStack pSource, int pTime) {
		pSource.getLevel().setWeatherParameters(0, pTime, true, true);
		pSource.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Set the weather to §cthunder"), true);
		return pTime;
	}
}
