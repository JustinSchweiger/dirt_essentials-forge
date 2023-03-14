package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.manager.AutobroadcastManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;

public class ToggleautobroadcastsCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> commandNode = dispatcher.register(Commands
				.literal("toggleautobroadcasts")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TOGGLEAUTOBROADCASTS))
				.executes(ToggleautobroadcastsCommand::execute));

		dispatcher.register(Commands.literal("toggleab").requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TOGGLEAUTOBROADCASTS)).redirect(commandNode));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();

		if (AutobroadcastManager.areAutobroadcastsDisabled(player.getUUID())) {
			AutobroadcastManager.enableAutobroadcasts(player.getUUID());
			try (Session session = HibernateUtil.getSessionFactory().openSession()) {
				DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
				dirtPlayer.setAutobroadcastsDisabled(false);
				session.beginTransaction();
				session.persist(dirtPlayer);
				session.getTransaction().commit();
			}
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Autobroadcasts are now §aenabled§7!"), false);
		} else {
			AutobroadcastManager.disableAutobroadcasts(player.getUUID());
			try (Session session = HibernateUtil.getSessionFactory().openSession()) {
				DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
				dirtPlayer.setAutobroadcastsDisabled(true);
				session.beginTransaction();
				session.persist(dirtPlayer);
				session.getTransaction().commit();
			}
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Autobroadcasts are now §cdisabled§7!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
