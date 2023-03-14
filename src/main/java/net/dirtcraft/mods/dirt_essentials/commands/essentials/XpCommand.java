package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class XpCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("xp")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.XP))
				.then(Commands.literal("add")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000000))
										.then(Commands.literal("levels")
												.executes(context -> addLevels(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"))))
										.then(Commands.literal("points")
												.executes(context -> addPoints(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount")))))))
				.then(Commands.literal("set")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000000))
										.then(Commands.literal("levels")
												.executes(context -> setLevels(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"))))
										.then(Commands.literal("points")
												.executes(context -> setPoints(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount")))))))
				.then(Commands.literal("remove")
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000000))
										.then(Commands.literal("levels")
												.executes(context -> removeLevels(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"))))
										.then(Commands.literal("points")
												.executes(context -> removePoints(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "amount"))))))));
	}

	private static int removePoints(CommandSourceStack source, ServerPlayer player, int amount) {
		player.giveExperiencePoints(-amount);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Removed §d" + amount + " §7points from §6" + player.getName().getString() + "§7!"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int removeLevels(CommandSourceStack source, ServerPlayer player, int amount) {
		player.giveExperienceLevels(-amount);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Removed §d" + amount + " §7levels from §6" + player.getName().getString() + "§7!"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int setPoints(CommandSourceStack source, ServerPlayer player, int amount) {
		player.giveExperienceLevels(-player.experienceLevel);
		player.giveExperiencePoints(amount);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Set §d" + amount + " §7points to §6" + player.getName().getString() + "§7!"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int setLevels(CommandSourceStack source, ServerPlayer player, int amount) {
		player.giveExperienceLevels(-player.experienceLevel);
		player.giveExperienceLevels(amount);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Set §d" + amount + " §7levels to §6" + player.getName().getString() + "§7!"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int addPoints(CommandSourceStack source, ServerPlayer player, int amount) {
		player.giveExperiencePoints(amount);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Added §d" + amount + " §7points to §6" + player.getName().getString() + "§7!"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int addLevels(CommandSourceStack source, ServerPlayer player, int amount) {
		player.giveExperienceLevels(amount);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Added §d" + amount + " §7levels to §6" + player.getName().getString() + "§7!"), false);
		return Command.SINGLE_SUCCESS;
	}
}
