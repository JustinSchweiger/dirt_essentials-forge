package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
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

public class HomebalanceCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("homebalance")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HOMEBALANCE))
				.then(Commands.literal("set")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HOMEBALANCE_SET))
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.then(Commands.argument("amount", IntegerArgumentType.integer(0))
										.executes(HomebalanceCommand::set))))
				.then(Commands.literal("give")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HOMEBALANCE_GIVE))
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.then(Commands.argument("amount", IntegerArgumentType.integer(1))
										.executes(HomebalanceCommand::give))))
				.then(Commands.literal("remove")
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HOMEBALANCE_REMOVE))
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests(((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder)))
								.then(Commands.argument("amount", IntegerArgumentType.integer(1))
										.executes(HomebalanceCommand::remove))));

		dispatcher.register(Commands
				.literal("homebal")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HOMEBALANCE))
				.redirect(dispatcher.register(commandBuilder))
		);
	}

	private static int remove(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		UUID target = PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player"));
		int amount = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "amount");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, target);
			if (player == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
				return Command.SINGLE_SUCCESS;
			}

			player.setHomeAmount(player.getHomeAmount() - amount);
			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Removed §d" + amount + "§7 from §6" + player.getUsername() + "§7's home balance."), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int give(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		UUID target = PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player"));
		int amount = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "amount");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, target);
			if (player == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
				return Command.SINGLE_SUCCESS;
			}

			player.setHomeAmount(player.getHomeAmount() + amount);
			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Added §d" + amount + "§7 to §6" + player.getUsername() + "§7's home balance."), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int set(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		UUID target = PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player"));
		int amount = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "amount");
		if (amount < 0) {
			amount = 0;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer player = session.get(DirtPlayer.class, target);
			if (player == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
				return Command.SINGLE_SUCCESS;
			}

			player.setHomeAmount(amount);
			session.beginTransaction();
			session.persist(player);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Set §6" + player.getUsername() + "§7's home balance to §d" + amount + "§7."), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
