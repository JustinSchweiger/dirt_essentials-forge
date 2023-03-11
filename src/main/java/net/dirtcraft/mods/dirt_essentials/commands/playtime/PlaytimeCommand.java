package net.dirtcraft.mods.dirt_essentials.commands.playtime;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.config.PlaytimeConfig;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.TimeUnit;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.PlaytimeManager;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.permissions.PlaytimePermissions;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlaytimeCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> argumentBuilder = Commands
				.literal("dirtplaytime")
				.requires(source -> PermissionHandler.hasPermission(source, PlaytimePermissions.BASE))
				.then(Commands.literal("check")
						.requires(source -> PermissionHandler.hasPermission(source, PlaytimePermissions.CHECK))
						.executes(PlaytimeCommand::check)
						.then(Commands.argument("player", EntityArgument.player())
								.requires(source -> PermissionHandler.hasPermission(source, PlaytimePermissions.CHECK_OTHER))
								.executes(PlaytimeCommand::checkOther)))
				.then(Commands.literal("top")
						.requires(source -> PermissionHandler.hasPermission(source, PlaytimePermissions.TOP))
						.executes(context -> top(context, 1))
						.then(Commands.argument("page", IntegerArgumentType.integer(1))
								.executes(context -> top(context, IntegerArgumentType.getInteger(context, "page")))))
				.then(Commands.literal("add")
						.requires(source -> PermissionHandler.hasPermission(source, PlaytimePermissions.ADD))
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("time", IntegerArgumentType.integer(0))
										.then(Commands.literal("s").executes(context -> add(context, TimeUnit.SECONDS)))
										.then(Commands.literal("m").executes(context -> add(context, TimeUnit.MINUTES)))
										.then(Commands.literal("h").executes(context -> add(context, TimeUnit.HOURS))))))
				.then(Commands.literal("remove")
						.requires(source -> PermissionHandler.hasPermission(source, PlaytimePermissions.REMOVE))
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("time", IntegerArgumentType.integer(0))
										.then(Commands.literal("s").executes(context -> remove(context, TimeUnit.SECONDS)))
										.then(Commands.literal("m").executes(context -> remove(context, TimeUnit.MINUTES)))
										.then(Commands.literal("h").executes(context -> remove(context, TimeUnit.HOURS))))))
				.then(Commands.literal("set")
						.requires(source -> PermissionHandler.hasPermission(source, PlaytimePermissions.SET))
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("time", IntegerArgumentType.integer(0))
										.then(Commands.literal("s").executes(context -> set(context, TimeUnit.SECONDS)))
										.then(Commands.literal("m").executes(context -> set(context, TimeUnit.MINUTES)))
										.then(Commands.literal("h").executes(context -> set(context, TimeUnit.HOURS))))))
				.then(Commands.literal("reset")
						.requires(source -> PermissionHandler.hasPermission(source, PlaytimePermissions.RESET))
						.then(Commands.argument("player", EntityArgument.player())
								.executes(PlaytimeCommand::reset)));

		dispatcher.register(argumentBuilder);

		dispatcher.register(Commands.literal("playtime").requires(source -> PermissionHandler.hasPermission(source, PlaytimePermissions.BASE)).redirect(dispatcher.register(argumentBuilder)));
	}

	private static int reset(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
			if (dirtPlayer == null) {
				source.sendFailure(new TextComponent(Strings.PLAYTIME_PREFIX + "§cThat player has never joined the server!"));
				return Command.SINGLE_SUCCESS;
			}

			PlaytimeConfig.Rank currentRank = PlaytimeManager.getRank(dirtPlayer.getCurrentPath());

			dirtPlayer.setTimePlayed(0);
			dirtPlayer.setCurrentPath(PlaytimeManager.getFirstRank().getName());
			dirtPlayer.setTimesJoined(0);
			session.beginTransaction();
			session.merge(dirtPlayer);
			session.getTransaction().commit();

			DirtEssentials.SERVER.getCommands().performCommand(DirtEssentials.SERVER.createCommandSourceStack(), "lp user " + player.getGameProfile().getName() + " parent remove " + currentRank.getName() + " " + DirtEssentials.LUCKPERMS.getServerName());
			source.sendSuccess(new TextComponent(Strings.PLAYTIME_PREFIX + "§7Successfully reset §6" + player.getName().getString() + "§7's playtime!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int set(CommandContext<CommandSourceStack> commandSourceStackCommandContext, TimeUnit timeUnit) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");
		int time = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "time");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
			if (dirtPlayer == null) {
				source.sendFailure(new TextComponent(Strings.PLAYTIME_PREFIX + "§cThat player has never joined the server!"));
				return Command.SINGLE_SUCCESS;
			}

			long timeToSet = TimeUnit.convertToSeconds(time, timeUnit);
			dirtPlayer.setTimePlayed(timeToSet);
			session.beginTransaction();
			session.merge(dirtPlayer);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.PLAYTIME_PREFIX + "§7Successfully set §6" + dirtPlayer.getUsername() + "§7's playtime to §3" + time + " " + timeUnit.name().toLowerCase()), false);
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int remove(CommandContext<CommandSourceStack> commandSourceStackCommandContext, TimeUnit timeUnit) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");
		int time = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "time");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
			if (dirtPlayer == null) {
				source.sendFailure(new TextComponent(Strings.PLAYTIME_PREFIX + "§cThat player has never joined the server!"));
				return Command.SINGLE_SUCCESS;
			}

			long timeToRemove = TimeUnit.convertToSeconds(time, timeUnit);
			dirtPlayer.setTimePlayed(dirtPlayer.getTimePlayed() - timeToRemove);
			session.beginTransaction();
			session.merge(dirtPlayer);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.PLAYTIME_PREFIX + "§7Successfully removed §3" + time + " " + (time > 1 ? TimeUnit.getString(timeUnit) + "s" : TimeUnit.getString(timeUnit)) + " §7from §6" + player.getName().getString() + "§7's playtime!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int add(CommandContext<CommandSourceStack> commandSourceStackCommandContext, TimeUnit timeUnit) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");
		int time = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "time");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
			if (dirtPlayer == null) {
				source.sendFailure(new TextComponent(Strings.PLAYTIME_PREFIX + "§cThat player has never joined the server!"));
				return Command.SINGLE_SUCCESS;
			}

			long timeToAdd = TimeUnit.convertToSeconds(time, timeUnit);
			dirtPlayer.setTimePlayed(dirtPlayer.getTimePlayed() + timeToAdd);
			session.beginTransaction();
			session.merge(dirtPlayer);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.PLAYTIME_PREFIX + "§7Added §3" + time + " " + (time > 1 ? TimeUnit.getString(timeUnit) + "s" : TimeUnit.getString(timeUnit)) + " §7to §6" + dirtPlayer.getUsername() + "§7's playtime!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int top(CommandContext<CommandSourceStack> commandSourceStackCommandContext, int page) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			List<DirtPlayer> players = session.createQuery("FROM DirtPlayer ORDER BY timePlayed DESC", DirtPlayer.class).list();

			int maxPage = (int) Math.ceil(players.size() / (double) PlaytimeConfig.LEADERBOARD_SIZE.get());
			int start = (page - 1) * PlaytimeConfig.LEADERBOARD_SIZE.get();
			int end = page * PlaytimeConfig.LEADERBOARD_SIZE.get();
			if (end > players.size())
				end = players.size();

			source.sendSuccess(new TextComponent(""), false);
			source.sendSuccess(new TextComponent(Strings.PLAYTIME_PREFIX + "§bTop §3" + (start + 1) + " §c- §3" + end + " §bplayers playtime:"), false);
			source.sendSuccess(new TextComponent(""), false);

			for (int i = start; i < end; i++) {
				DirtPlayer player = players.get(i);
				String builder = Utils.formatTimePlayed(player);

				TextComponent component = new TextComponent("  §a" + (i + 1) + ". §b" + player.getUsername());
				HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(
						"§bPlaytime §3» §7" + builder + "\n" +
								"§bTimes Joined §3» §7" + player.getTimesJoined() + "\n" +
								"§bFirst Joined §3» §7" + player.getFirstJoined().format(DateTimeFormatter.ofPattern("dd. MMMM yyyy")) + " §3at §7" + player.getFirstJoined().format(DateTimeFormatter.ofPattern("HH:mm"))
				));

				component.setStyle(component.getStyle().withHoverEvent(hoverEvent));
				source.sendSuccess(component, false);
			}

			source.sendSuccess(new TextComponent(""), false);
			source.sendSuccess(Utils.getPaginator(page, maxPage, "/playtime top"), false);
			source.sendSuccess(new TextComponent(""), false);
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int checkOther(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");
		DirtPlayer dirtPlayer = DirtPlayer.get(player);
		showPlaytime(source, dirtPlayer, true);
		return Command.SINGLE_SUCCESS;
	}

	private static int check(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.PLAYTIME_PREFIX + "You must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		DirtPlayer player = DirtPlayer.get(source.getPlayerOrException());
		showPlaytime(source, player, false);

		return Command.SINGLE_SUCCESS;
	}

	private static void showPlaytime(CommandSourceStack source, DirtPlayer player, boolean isOtherCheck) {
		PlaytimeConfig.Rank currentRank = PlaytimeManager.getRank(player.getCurrentPath());
		PlaytimeConfig.Rank nextRank = PlaytimeManager.getRank(currentRank.getNextRank());

		String builder = Utils.formatTimePlayed(player);

		source.sendSuccess(new TextComponent(""), false);
		if (isOtherCheck) {
			source.sendSuccess(new TextComponent(Strings.PLAYTIME_PREFIX + "§b" + player.getUsername() + "§3's Playtime:"), false);
		} else {
			source.sendSuccess(new TextComponent(Strings.PLAYTIME_PREFIX + "§3Your Playtime:"), false);
		}

		source.sendSuccess(new TextComponent(""), false);
		source.sendSuccess(new TextComponent("§bPlaytime §3» §7" + builder), false);
		source.sendSuccess(new TextComponent("§bTimes Joined §3» §7" + player.getTimesJoined()), false);
		source.sendSuccess(new TextComponent("§bFirst Joined §3» §7" + player.getFirstJoined().format(DateTimeFormatter.ofPattern("dd. MMMM yyyy")) + " §3at §7" + player.getFirstJoined().format(DateTimeFormatter.ofPattern("HH:mm"))), false);
		source.sendSuccess(new TextComponent(""), false);

		String currentGroupPrefix = PermissionHandler.getGroupPrefix(currentRank.getName());
		if (currentGroupPrefix.isBlank())
			currentGroupPrefix = currentRank.getName();

		String nextGroupPrefix = PermissionHandler.getGroupPrefix(nextRank.getName());
		if (nextGroupPrefix.isBlank())
			nextGroupPrefix = nextRank.getName();

		if (nextRank != null) {
			int remainingHours = (int) Math.floor((nextRank.getTimeRequirement() - player.getTimePlayed()) / 3600F);
			int remainingMinutes = (int) Math.floor((nextRank.getTimeRequirement() - player.getTimePlayed() - remainingHours * 3600F) / 60F);
			int remainingSeconds = (int) Math.floor((nextRank.getTimeRequirement() - player.getTimePlayed()) % 60F);

			StringBuilder remainingTimeBuilder = new StringBuilder();
			if (remainingHours == 1)
				remainingTimeBuilder.append(remainingHours).append(" hour ");
			if (remainingHours > 1)
				remainingTimeBuilder.append(remainingHours).append(" hours ");
			if (remainingMinutes == 1)
				remainingTimeBuilder.append(remainingMinutes).append(" minute ");
			if (remainingMinutes > 1)
				remainingTimeBuilder.append(remainingMinutes).append(" minutes ");
			if (remainingSeconds == 1)
				remainingTimeBuilder.append(remainingSeconds).append(" second");
			if (remainingSeconds > 1)
				remainingTimeBuilder.append(remainingSeconds).append(" seconds");

			source.sendSuccess(new TextComponent(currentGroupPrefix + " §3» " + nextGroupPrefix + " §3in §7" + remainingTimeBuilder), false);
		} else {
			source.sendSuccess(new TextComponent(currentGroupPrefix + " §3» §7No more ranks!"), false);
		}

		source.sendSuccess(new TextComponent(""), false);
	}
}
