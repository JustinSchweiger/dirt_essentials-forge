package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.MsgManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;

public class SocialspyCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("socialspy")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SOCIALSPY))
				.executes(SocialspyCommand::execute));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();

		if (MsgManager.isSocialSpyEnabled(player.getUUID())) {
			MsgManager.disableSocialSpy(player.getUUID());
			try (Session session = HibernateUtil.getSessionFactory().openSession()) {
				DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
				dirtPlayer.setSocialSpyEnabled(false);
				session.beginTransaction();
				session.persist(dirtPlayer);
				session.getTransaction().commit();
			}
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Socialspy is now §cdisabled§7!"), false);
		} else {
			MsgManager.enableSocialSpy(player.getUUID());
			try (Session session = HibernateUtil.getSessionFactory().openSession()) {
				DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
				dirtPlayer.setSocialSpyEnabled(true);
				session.beginTransaction();
				session.persist(dirtPlayer);
				session.getTransaction().commit();
			}
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Socialspy is now §aenabled§7!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
