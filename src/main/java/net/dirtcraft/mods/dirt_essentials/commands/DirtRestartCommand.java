package net.dirtcraft.mods.dirt_essentials.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.Main;
import net.dirtcraft.mods.dirt_essentials.manager.RestartManager;
import net.dirtcraft.mods.dirt_essentials.data.TimeUnit;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class DirtRestartCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> argumentBuilder = Commands
				.literal("dirtrestart")
				.requires(source -> source.hasPermission(4))
				.then(Commands.literal("now")
						.executes(DirtRestartCommand::now))
				.then(Commands.literal("in")
						.then(Commands.argument("time", IntegerArgumentType.integer(1))
								.then(Commands.literal("s").executes(context -> in(context, TimeUnit.SECONDS)))
								.then(Commands.literal("m").executes(context -> in(context, TimeUnit.MINUTES)))
								.then(Commands.literal("h").executes(context -> in(context, TimeUnit.HOURS)))))
				.then(Commands.literal("pause")
						.executes(DirtRestartCommand::pause))
				.then(Commands.literal("resume")
						.executes(DirtRestartCommand::resume))
				.then(Commands.literal("time")
						.executes(DirtRestartCommand::time));

		dispatcher.register(Commands.literal("restart").redirect(dispatcher.register(argumentBuilder)));
	}

	private static int time(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		if (RestartManager.isPaused()) {
			commandSourceStackCommandContext.getSource().sendSuccess(new TextComponent(Strings.RESTART_PREFIX + "§cThe restart timer is currently paused!"), false);
			return Command.SINGLE_SUCCESS;
		}

		long timeLeft = RestartManager.getTimeLeft();
		int hours = (int) Math.floor(timeLeft / 3600F);
		int minutes = (int) Math.floor((timeLeft - (hours * 3600)) / 60F);
		int seconds = (int) Math.floor(timeLeft % 60F);

		StringBuilder timeBuilder = new StringBuilder();
		if (hours > 0)
			timeBuilder.append("§c").append(hours).append("§7h ");
		if (minutes > 0)
			timeBuilder.append("§c").append(minutes).append("§7m ");
		if (seconds > 0)
			timeBuilder.append("§c").append(seconds).append("§7s");

		commandSourceStackCommandContext.getSource().sendSuccess(new TextComponent(Strings.RESTART_PREFIX + "§7The server will restart in " + timeBuilder.toString().trim() + "!"), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int resume(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		if (!RestartManager.isPaused()) {
			commandSourceStackCommandContext.getSource().sendSuccess(new TextComponent(Strings.RESTART_PREFIX + "§cThe restart timer is not paused!"), false);
			return Command.SINGLE_SUCCESS;
		}

		RestartManager.resumeTimer();
		commandSourceStackCommandContext.getSource().sendSuccess(new TextComponent(Strings.RESTART_PREFIX + "§aThe restart timer has been resumed!"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int pause(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		if (RestartManager.isPaused()) {
			commandSourceStackCommandContext.getSource().sendSuccess(new TextComponent(Strings.RESTART_PREFIX + "§cThe restart timer is already paused!"), false);
			return Command.SINGLE_SUCCESS;
		}

		RestartManager.pauseTimer();
		commandSourceStackCommandContext.getSource().sendSuccess(new TextComponent(Strings.RESTART_PREFIX + "§aThe restart timer has been paused!"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int in(CommandContext<CommandSourceStack> commandSourceStackCommandContext, TimeUnit timeUnit) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		int time = commandSourceStackCommandContext.getArgument("time", Integer.class);
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		StringBuilder inBuilder = new StringBuilder();
		if (time > 1 && timeUnit == TimeUnit.HOURS)
			inBuilder.append(time).append(" hours");
		else if (time > 1 && timeUnit == TimeUnit.MINUTES)
			inBuilder.append(time).append(" minutes");
		else if (time > 1 && timeUnit == TimeUnit.SECONDS)
			inBuilder.append(time).append(" seconds");
		else if (time == 1 && timeUnit == TimeUnit.HOURS)
			inBuilder.append(time).append(" hour");
		else if (time == 1 && timeUnit == TimeUnit.MINUTES)
			inBuilder.append(time).append(" minute");
		else if (time == 1 && timeUnit == TimeUnit.SECONDS)
			inBuilder.append(time).append(" second");

		Main.SERVER.getPlayerList().getPlayers().forEach(player -> player.sendMessage(
				new TextComponent(
						Strings.RESTART_PREFIX +
								"§7New restart queued by §b" + (isConsole ? "CONSOLE" : ((ServerPlayer) source.getEntity()).getGameProfile().getName()) + "§7! " +
								"The server will now restart in §c" + inBuilder),
				Util.NIL_UUID
		));

		RestartManager.startTimer(time, timeUnit);

		return Command.SINGLE_SUCCESS;
	}

	private static int now(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		RestartManager.restart();
		return Command.SINGLE_SUCCESS;
	}
}
