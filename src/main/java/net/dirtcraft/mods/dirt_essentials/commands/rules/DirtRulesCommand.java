package net.dirtcraft.mods.dirt_essentials.commands.rules;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.database.Rule;
import net.dirtcraft.mods.dirt_essentials.gui.RulesGui;
import net.dirtcraft.mods.dirt_essentials.manager.RulesManager;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.permissions.RulePermissions;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DirtRulesCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> argumentBuilder = Commands
				.literal("dirtrules")
				.requires(source -> PermissionHandler.hasPermission(source, RulePermissions.BASE))
				.then(Commands.literal("add")
						.requires(source -> PermissionHandler.hasPermission(source, RulePermissions.ADD))
						.then(Commands.argument("rule", StringArgumentType.greedyString())
								.executes(DirtRulesCommand::add)))
				.then(Commands.literal("remove")
						.requires(source -> PermissionHandler.hasPermission(source, RulePermissions.REMOVE))
						.then(Commands.argument("index", IntegerArgumentType.integer(1))
								.suggests(DirtRulesCommand::suggestRuleIds)
								.executes(DirtRulesCommand::remove)))
				.then(Commands.literal("edit")
						.requires(source -> PermissionHandler.hasPermission(source, RulePermissions.EDIT))
						.then(Commands.argument("index", IntegerArgumentType.integer(1))
								.suggests(DirtRulesCommand::suggestRuleIds)
								.then(Commands.argument("rule", StringArgumentType.greedyString())
										.executes(DirtRulesCommand::edit))))
				.then(Commands.literal("list")
						.requires(source -> PermissionHandler.hasPermission(source, RulePermissions.LIST))
						.executes(DirtRulesCommand::list));

		dispatcher.register(argumentBuilder);
	}

	public static void registerRulesAlias(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> argumentBuilder = Commands
				.literal("rules")
				.requires(source -> PermissionHandler.hasPermission(source, RulePermissions.LIST))
				.executes(DirtRulesCommand::list);

		dispatcher.register(argumentBuilder);
	}

	private static int list(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "You must be a player to use this command!"), true);
			return Command.SINGLE_SUCCESS;
		}

		int rulesCount = RulesManager.getRulesCount();
		if (rulesCount == 0) {
			source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "§cThere are no rules to display!"), true);
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = (ServerPlayer) source.getEntity();
		RulesGui.openRulesGui(player);
		return Command.SINGLE_SUCCESS;
	}

	private static int edit(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		int ruleId = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "index");
		String ruleString = StringArgumentType.getString(commandSourceStackCommandContext, "rule");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			Rule rule = session.get(Rule.class, ruleId);
			if (rule == null) {
				source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "§cRule not found!"), true);
				return Command.SINGLE_SUCCESS;
			}
			rule.setMessage(ruleString);
			rule.setLastEditDate(LocalDateTime.now());
			session.merge(rule);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "§7Rule §c" + ruleId + "§7 has been updated!"), true);
			RulesManager.rulesChanged();
		} catch (Exception e) {
			source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "§cAn error occurred while updating rule §3" + ruleId + "§c!"), true);
			e.printStackTrace();
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int remove(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		int ruleId = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "index");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			Rule rule = session.get(Rule.class, ruleId);
			if (rule == null) {
				source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "§cRule not found!"), true);
				return Command.SINGLE_SUCCESS;
			}
			session.remove(rule);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "§7Rule §c" + ruleId + "§7 has been removed!"), true);
			RulesManager.rulesChanged();
		} catch (Exception e) {
			source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "§cAn error occurred while removing rule §3" + ruleId + "§c!"), true);
			e.printStackTrace();
		}

		return Command.SINGLE_SUCCESS;
	}

	private static CompletableFuture<Suggestions> suggestRuleIds(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Collection<Rule> rules = session.createQuery("from Rule", Rule.class).list();
			for (Rule rule : rules) {
				suggestionsBuilder.suggest(String.valueOf(rule.getId()));
			}
		} catch (Exception ignored) {}

		return suggestionsBuilder.buildFuture();
	}

	private static int add(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		String ruleString = Utils.formatColorString(StringArgumentType.getString(commandSourceStackCommandContext, "rule"));
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "You must be a player to use this command!"), true);
			return Command.SINGLE_SUCCESS;
		}

		int rulesCount = RulesManager.getRulesCount();
		if (rulesCount == 45) {
			source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "§7The server has reached the maximum amount of rules!"), true);
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DirtPlayer player = session.get(DirtPlayer.class, source.getEntity().getUUID());

			Rule rule = new Rule(player, ruleString);
			session.persist(rule);
			source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "§7Rule §asuccessfully §7added!"), true);
			session.getTransaction().commit();
			RulesManager.rulesChanged();
		} catch (Exception e) {
			e.printStackTrace();
			source.sendSuccess(new TextComponent(Strings.RULES_PREFIX + "§7There was an §cerror §7adding your rule!"), true);
		}

		return Command.SINGLE_SUCCESS;
	}
}
