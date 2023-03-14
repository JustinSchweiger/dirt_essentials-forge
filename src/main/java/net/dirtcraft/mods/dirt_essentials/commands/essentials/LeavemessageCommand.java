package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.JLManager;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.permissions.ChatPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import org.hibernate.Session;

import java.util.UUID;

public class LeavemessageCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("leavemessage")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.LEAVEMESSAGE))
				.then(Commands.literal("set")
						.then(Commands.argument("message", StringArgumentType.greedyString())
								.executes(context -> setLeaveMessage(context, StringArgumentType.getString(context, "message"), false)))
						.then(Commands.argument("player", StringArgumentType.word())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.LEAVEMESSAGE_OTHERS))
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.then(Commands.argument("message", StringArgumentType.greedyString())
										.executes(context -> setLeaveMessage(context, StringArgumentType.getString(context, "message"), true)))))
				.then(Commands.literal("clear")
						.executes(context -> clearLeaveMessage(context, false))
						.then(Commands.argument("player", StringArgumentType.word())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.LEAVEMESSAGE_OTHERS))
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.executes(context -> clearLeaveMessage(context, true))))
				.then(Commands.literal("preview")
						.executes(context -> previewLeaveMessage(context, false))
						.then(Commands.argument("player", StringArgumentType.word())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.LEAVEMESSAGE_OTHERS))
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.executes(context -> previewLeaveMessage(context, true)))));
	}

	private static int previewLeaveMessage(CommandContext<CommandSourceStack> commandSourceStackCommandContext, boolean isOther) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null && !isOther) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		UUID target = isOther ? PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player")) : source.getEntity().getUUID();
		if (target == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThat player does not exist!"));
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, target);
			if (player.getCustomLeaveMessage().isBlank()) {
				if (isOther)
					source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§6" + player.getUsername() + "§7 does not have a custom leave message!"));
				else
					source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou do not have a custom leave message!"));
			} else {
				if (isOther) {
					source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§6" + player.getUsername() + "§7's custom leave message:"), false);
					source.sendSuccess(new TextComponent(""), false);
					source.sendSuccess(JLManager.getLeaveMessage(player.getCustomLeaveMessage(), player.getUsername(), PermissionHandler.hasPermission(target, ChatPermissions.STAFF)), false);
					source.sendSuccess(new TextComponent(""), false);
				} else {
					source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Your custom leave message: §d"), false);
					source.sendSuccess(new TextComponent(""), false);
					source.sendSuccess(JLManager.getLeaveMessage(player.getCustomLeaveMessage(), player.getUsername(), PermissionHandler.hasPermission(target, ChatPermissions.STAFF)), false);
					source.sendSuccess(new TextComponent(""), false);
				}
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int clearLeaveMessage(CommandContext<CommandSourceStack> commandSourceStackCommandContext, boolean isOther) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null && !isOther) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		UUID target = isOther ? PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player")) : source.getEntity().getUUID();
		if (target == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThat player does not exist!"));
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, target);
			player.setCustomLeaveMessage("");

			if (isOther)
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSuccessfully §7cleared §d" + player.getUsername() + "§7's leave message!"), false);
			else
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Your custom leave message has been §ccleared§7!"), false);

			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int setLeaveMessage(CommandContext<CommandSourceStack> commandSourceStackCommandContext, String message, boolean isOther) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null && !isOther) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		UUID target = isOther ? PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player")) : source.getEntity().getUUID();
		if (target == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThat player does not exist!"));
			return Command.SINGLE_SUCCESS;
		}

		message = Utils.formatColorString(message);
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, target);
			player.setCustomLeaveMessage(message);

			if (isOther)
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSuccessfully §7set §d" + player.getUsername() + "§7's leave message!"), false);
			else
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Your custom leave message has been set §asuccessfully§7!"), false);

			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
		}

		return Command.SINGLE_SUCCESS;
	}
}
