package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.GodManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;


public class GodCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("god")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GOD))
				.executes(GodCommand::self)
				.then(Commands.argument("player", EntityArgument.player())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GOD_OTHERS))
						.executes(GodCommand::others)));
	}

	private static int self(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());

			if (dirtPlayer.isGodModeEnabled() && GodManager.isGodModeEnabled(player.getUUID())) {
				dirtPlayer.setGodModeEnabled(false);
				GodManager.setGodModeEnabled(player.getUUID(), false);
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "God mode is now §cdisabled§7!"), false);
			} else {
				dirtPlayer.setGodModeEnabled(true);
				GodManager.setGodModeEnabled(player.getUUID(), true);
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "God mode is now §aenabled§7!"), false);
			}

			session.beginTransaction();
			session.persist(dirtPlayer);
			session.getTransaction().commit();
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int others(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer target = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, target.getUUID());

			if (dirtPlayer.isGodModeEnabled() && GodManager.isGodModeEnabled(target.getUUID())) {
				dirtPlayer.setGodModeEnabled(false);
				GodManager.setGodModeEnabled(target.getUUID(), false);
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "God mode is now §cdisabled§7 for " + target.getName().getString() + "!"), false);
			} else {
				dirtPlayer.setGodModeEnabled(true);
				GodManager.setGodModeEnabled(target.getUUID(), true);
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "God mode is now §aenabled§7 for " + target.getName().getString() + "!"), false);
			}

			session.beginTransaction();
			session.persist(dirtPlayer);
			session.getTransaction().commit();
		}

		return Command.SINGLE_SUCCESS;
	}
}
