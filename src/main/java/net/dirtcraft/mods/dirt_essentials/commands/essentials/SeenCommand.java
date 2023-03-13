package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SeenCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("seen")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SEEN))
				.then(Commands.argument("player", StringArgumentType.word())
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder))
						.executes(SeenCommand::execute)));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		UUID player = PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player"));
		if (player == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player);

			printSeen(commandSourceStackCommandContext, player, dirtPlayer);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static void printSeen(CommandContext<CommandSourceStack> commandSourceStackCommandContext, UUID targetUuid, DirtPlayer dirtPlayer) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer target = DirtEssentials.SERVER.getPlayerList().getPlayer(targetUuid);

		TextComponent uuidComponent = new TextComponent("§7" + targetUuid);
		uuidComponent.setStyle(uuidComponent.getStyle()
				.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, targetUuid.toString()))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§7Click to copy UUID"))));

		TextComponent ipComponent = new TextComponent("§7" + dirtPlayer.getLastKnownIp());
		ipComponent.setStyle(ipComponent.getStyle()
				.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, dirtPlayer.getLastKnownIp()))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§7Click to copy IP"))));

		TextComponent locationComponent = new TextComponent("§7(§b" + dirtPlayer.getLastKnownLocation().getX() + "§7, §b" + dirtPlayer.getLastKnownLocation().getY() + "§7, §b" + dirtPlayer.getLastKnownLocation().getZ() + "§7)");
		if (target == null) {
			locationComponent.setStyle(locationComponent.getStyle()
					.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tppos " + dirtPlayer.getLastKnownLocation().getX() + " " + dirtPlayer.getLastKnownLocation().getY() + " " + dirtPlayer.getLastKnownLocation().getZ() + " " + dirtPlayer.getLastKnownLocation().getLevel().location()))
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(
							"§6World§7: §b" + dirtPlayer.getLastKnownLocation().getLevel().registry() + " §7| §a" + dirtPlayer.getLastKnownLocation().getLevel().location() + "\n\n" +
									"§3Click to teleport to player"
					))));
		} else {
			locationComponent.setStyle(locationComponent.getStyle()
					.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + target.getName().getString()))
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(
							"§6World§7: §b" + dirtPlayer.getLastKnownLocation().getLevel().registry() + " §7| §a" + dirtPlayer.getLastKnownLocation().getLevel().location() + "\n\n" +
									"§3Click to teleport to player"
					))));
		}

		String lastSeenString = "";
		if (target == null) {
			LocalDateTime lastSeen = dirtPlayer.getLeaveDate();
			LocalDateTime now = LocalDateTime.now();

			long difference = lastSeen.until(now, ChronoUnit.SECONDS);
			long days = difference / 86400;
			difference %= 86400;
			long hours = difference / 3600;
			difference %= 3600;
			long minutes = difference / 60;
			difference %= 60;
			long seconds = difference;

			StringBuilder builder = new StringBuilder();
			if (days == 1)
				builder.append("§b").append(days).append(" §7day ");
			if (days > 1)
				builder.append("§b").append(days).append(" §7days ");
			if (hours == 1)
				builder.append("§b").append(hours).append(" §7hour ");
			if (hours > 1)
				builder.append("§b").append(hours).append(" §7hours ");
			if (minutes == 1)
				builder.append("§b").append(minutes).append(" §7minute ");
			if (minutes > 1)
				builder.append("§b").append(minutes).append(" §7minutes ");
			if (seconds == 1)
				builder.append("§b").append(seconds).append(" §7second");
			if (seconds > 1)
				builder.append("§b").append(seconds).append(" §7seconds");

			lastSeenString = builder.toString();
		}

		source.sendSuccess(new TextComponent(""), false);
		if (target == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Player §6" + dirtPlayer.getUsername() + " §7is §coffline §7since:"), false);
			source.sendSuccess(new TextComponent("  §3⌚ " + lastSeenString), false);
		} else {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Player §6" + dirtPlayer.getUsername() + " §7is §aonline"), false);
		}

		if (PermissionHandler.hasPermission(source, ChatPermissions.STAFF)) {
			source.sendSuccess(new TextComponent(""), false);
			source.sendSuccess(new TextComponent("§6UUID§7: ").append(uuidComponent), false);
			source.sendSuccess(new TextComponent("§6IP§7: ").append(ipComponent), false);
			source.sendSuccess(new TextComponent("§6Location§7: ").append(locationComponent), false);
		}

		source.sendSuccess(new TextComponent(""), false);
	}
}
