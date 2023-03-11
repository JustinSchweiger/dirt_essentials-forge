package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import org.hibernate.Session;

import java.util.UUID;

public class EcoCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("eco")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ECO))
				.then(Commands.literal("set")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ECO_SET))
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
										.executes(EcoCommand::set))))
				.then(Commands.literal("give")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ECO_GIVE))
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
										.executes(EcoCommand::give))))
				.then(Commands.literal("remove")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ECO_REMOVE))
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
										.executes(EcoCommand::remove))));

		dispatcher.register(Commands
				.literal("economy")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ECO))
				.redirect(dispatcher.register(commandBuilder))
		);
	}

	private static int set(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		UUID target = PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player"));
		double amount = DoubleArgumentType.getDouble(commandSourceStackCommandContext, "amount");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, target);
			if (player == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
				return Command.SINGLE_SUCCESS;
			}

			player.setBalance(amount);
			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Set §6" + player.getUsername() + "§7's balance to " + DirtPlayer.getFormattedBalance(amount)), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int give(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		UUID target = PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player"));
		double amount = DoubleArgumentType.getDouble(commandSourceStackCommandContext, "amount");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, target);
			if (player == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
				return Command.SINGLE_SUCCESS;
			}

			player.setBalance(player.getBalance() + amount);
			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Added " + DirtPlayer.getFormattedBalance(amount) + " §7to §6" + player.getUsername()), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int remove(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		UUID target = PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player"));
		double amount = DoubleArgumentType.getDouble(commandSourceStackCommandContext, "amount");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, target);
			if (player == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
				return Command.SINGLE_SUCCESS;
			}

			player.setBalance(player.getBalance() - amount);
			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Removed " + DirtPlayer.getFormattedBalance(amount) + " §7from §6" + player.getUsername()), false);
		}

		return Command.SINGLE_SUCCESS;
	}

}
