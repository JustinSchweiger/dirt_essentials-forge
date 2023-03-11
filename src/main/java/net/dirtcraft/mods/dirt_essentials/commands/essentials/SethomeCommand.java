package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.data.entites.Home;
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

public class SethomeCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("sethome")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SETHOME))
				.executes(SethomeCommand::override)
						.then(Commands.argument("name", StringArgumentType.word())
								.executes(context -> sethome(context, StringArgumentType.getString(context, "name"))));

		dispatcher.register(commandBuilder);
	}

	private static int override(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		String name = "home";
		sethome(commandSourceStackCommandContext, name);
		return Command.SINGLE_SUCCESS;
	}

	private static int sethome(CommandContext<CommandSourceStack> commandSourceStackCommandContext, String name) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();

		if (!HomeManager.hasAvailableHomes(player.getUUID())) {
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You have reached the maximum amount of homes!"), Util.NIL_UUID);
			return Command.SINGLE_SUCCESS;
		}
		
		if (HomeManager.hasHome(player.getUUID(), name)) {
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You already have a home with that name! Please remove it before using the name again!"), Util.NIL_UUID);
			return Command.SINGLE_SUCCESS;
		}
		
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
			Home home = new Home(name, dirtPlayer, player.getLevel().dimension(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
			dirtPlayer.getHomes().add(home);
			session.persist(home);
			session.persist(dirtPlayer);
			session.getTransaction().commit();
		}
		
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Home §e" + name + " §7has been set!"), Util.NIL_UUID);
		return Command.SINGLE_SUCCESS;
	}
}
