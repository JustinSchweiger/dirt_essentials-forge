package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.database.Home;
import net.dirtcraft.mods.dirt_essentials.manager.HomeManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;

public class DelhomeCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("delhome")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.DELHOME))
				.then(Commands.argument("name", StringArgumentType.word())
						.executes(DelhomeCommand::execute));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		String name = StringArgumentType.getString(commandSourceStackCommandContext, "name");
		ServerPlayer player = source.getPlayerOrException();

		if (!HomeManager.hasHome(player.getUUID(), name)) {
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "You don't have a home with that name!"), Util.NIL_UUID);
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
			Home home = dirtPlayer.getHome(name);
			dirtPlayer.removeHome(home);

			session.persist(dirtPlayer);
			session.remove(home);
			session.getTransaction().commit();
		}

		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Home §e" + name + " §7deleted!"), Util.NIL_UUID);

		return Command.SINGLE_SUCCESS;
	}
}
