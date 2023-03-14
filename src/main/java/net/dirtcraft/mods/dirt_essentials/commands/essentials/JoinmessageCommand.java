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

public class JoinmessageCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("joinmessage")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.JOINMESSAGE))
				.then(Commands.literal("set")
						.then(Commands.argument("message", StringArgumentType.greedyString())
								.executes(context -> setJoinMessage(context, StringArgumentType.getString(context, "message"), false)))
						.then(Commands.argument("player", StringArgumentType.word())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.JOINMESSAGE_OTHERS))
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.then(Commands.argument("message", StringArgumentType.greedyString())
										.executes(context -> setJoinMessage(context, StringArgumentType.getString(context, "message"), true)))))
				.then(Commands.literal("clear")
						.executes(context -> clearJoinMessage(context, false))
						.then(Commands.argument("player", StringArgumentType.word())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.JOINMESSAGE_OTHERS))
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.executes(context -> clearJoinMessage(context, true))))
				.then(Commands.literal("preview")
						.executes(context -> previewJoinMessage(context, false))
						.then(Commands.argument("player", StringArgumentType.word())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.JOINMESSAGE_OTHERS))
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.executes(context -> previewJoinMessage(context, true)))));
	}

	private static int previewJoinMessage(CommandContext<CommandSourceStack> commandSourceStackCommandContext, boolean isOther) {
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
			if (player.getCustomJoinMessage().isBlank()) {
				if (isOther)
					source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§6" + player.getUsername() + "§7 does not have a custom join message!"));
				else
					source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou do not have a custom join message!"));
			} else {
				if (isOther) {
					source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§6" + player.getUsername() + "§7's custom join message:"), false);
					source.sendSuccess(new TextComponent(""), false);
					source.sendSuccess(JLManager.getJoinMessage(player.getCustomJoinMessage(), player.getUsername(), false, PermissionHandler.hasPermission(target, ChatPermissions.STAFF)), false);
					source.sendSuccess(new TextComponent(""), false);
				} else {
					source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Your custom join message: §d"), false);
					source.sendSuccess(new TextComponent(""), false);
					source.sendSuccess(JLManager.getJoinMessage(player.getCustomJoinMessage(), player.getUsername(), false, PermissionHandler.hasPermission(target, ChatPermissions.STAFF)), false);
					source.sendSuccess(new TextComponent(""), false);
				}
			}
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int clearJoinMessage(CommandContext<CommandSourceStack> commandSourceStackCommandContext, boolean isOther) {
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
			player.setCustomJoinMessage("");

			if (isOther)
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSuccessfully §7cleared §d" + player.getUsername() + "§7's join message!"), false);
			else
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Your custom join message has been §ccleared§7!"), false);

			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int setJoinMessage(CommandContext<CommandSourceStack> commandSourceStackCommandContext, String message, boolean isOther) {
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
			player.setCustomJoinMessage(message);

			if (isOther)
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSuccessfully §7set §d" + player.getUsername() + "§7's join message!"), false);
			else
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Your custom join message has been set §asuccessfully§7!"), false);

			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
		}

		return Command.SINGLE_SUCCESS;
	}
}
