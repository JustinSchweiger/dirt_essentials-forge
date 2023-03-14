package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
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
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;

import java.util.UUID;

public class ResetPlayerDataCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralCommandNode<CommandSourceStack> commandBuilder = dispatcher.register(Commands
				.literal("resetplayerdata")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.RESETPLAYERDATA))
				.then(Commands.argument("player", StringArgumentType.word())
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder))
						.executes(context -> resetPlayerData(context, PlayerManager.getUuid(StringArgumentType.getString(context, "player")), false))
						.then(Commands.literal("--confirm")
								.executes(context -> resetPlayerData(context, PlayerManager.getUuid(StringArgumentType.getString(context, "player")), true)))));

		dispatcher.register(Commands.literal("reset").requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.REPLY)).redirect(commandBuilder));
	}

	private static int resetPlayerData(CommandContext<CommandSourceStack> context, UUID player, boolean confirmed) {
		CommandSourceStack source = context.getSource();
		String name = source.getEntity() == null ? "CONSOLE" : source.getEntity().getName().getString();
		if (player == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"));
			return Command.SINGLE_SUCCESS;
		}

		if (!confirmed) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThis will delete all player data associated with this player! It can not be rolled back! Run this command again with §4--confirm §cto confirm!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer target = DirtEssentials.SERVER.getPlayerList().getPlayer(player);
		if (target != null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThis player is currently online! Please kick them before running this command!"));
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player);

			int entriesDeleted = 0;
			session.beginTransaction();
			entriesDeleted += session.createQuery("DELETE FROM Note WHERE player = :player", null).setParameter("player", dirtPlayer).executeUpdate();
			entriesDeleted += session.createQuery("DELETE FROM KitTracker WHERE player = :player", null).setParameter("player", dirtPlayer).executeUpdate();
			entriesDeleted += session.createQuery("DELETE FROM Home WHERE owner = :player", null).setParameter("player", dirtPlayer).executeUpdate();

			session.remove(dirtPlayer);
			entriesDeleted++;
			session.getTransaction().commit();

			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSuccessfully deleted §d" + entriesDeleted + " §aentries!\n§aYou still have to delete their playerdata file manually if you want to completely erase any trace of the player!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
