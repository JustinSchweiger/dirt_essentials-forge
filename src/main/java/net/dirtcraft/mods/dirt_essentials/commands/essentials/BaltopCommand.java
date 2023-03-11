package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import org.hibernate.Session;

import java.util.List;

public class BaltopCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("baltop")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.BALTOP))
				.executes(context -> execute(context, 1))
				.then(Commands.argument("page", IntegerArgumentType.integer(1))
						.executes(context -> execute(context, IntegerArgumentType.getInteger(context, "page"))));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext, int page) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			List<DirtPlayer> players = session.createQuery("FROM DirtPlayer ORDER BY balance DESC", DirtPlayer.class).list();

			int maxPage = (int) Math.ceil(players.size() / (double) EssentialsConfig.BALTOP_SIZE.get());
			int start = (page - 1) * EssentialsConfig.BALTOP_SIZE.get();
			int end = page * EssentialsConfig.BALTOP_SIZE.get();
			if (end > players.size())
				end = players.size();

			source.sendSuccess(new TextComponent(""), false);
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§6Top §a" + (start + 1) + " §c- §a" + end + " §6players balance:"), false);
			source.sendSuccess(new TextComponent(""), false);
			source.sendSuccess(new TextComponent("§eServers Total Balance§7: "+ DirtPlayer.getFormattedBalance(players.stream().mapToDouble(DirtPlayer::getBalance).sum())), false);
			source.sendSuccess(new TextComponent(""), false);

			for (int i = start; i < end; i++) {
				DirtPlayer player = players.get(i);

				TextComponent component = new TextComponent("  §a" + (i + 1) + ". §b" + player.getUsername() + "§7: " + player.getFormattedBalance());

				source.sendSuccess(component, false);
			}

			source.sendSuccess(new TextComponent(""), false);
			source.sendSuccess(Utils.getPaginator(page, maxPage, "/baltop"), false);
			source.sendSuccess(new TextComponent(""), false);
		}
		return Command.SINGLE_SUCCESS;
	}
}
