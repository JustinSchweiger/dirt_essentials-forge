package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.Warp;
import net.dirtcraft.mods.dirt_essentials.manager.WarpManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import org.hibernate.Session;

public class DelwarpCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("delwarp")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.DELWARP))
				.then(Commands.argument("name", StringArgumentType.word())
						.suggests(((context, builder) -> SharedSuggestionProvider.suggest(WarpManager.getWarps().stream().map(Warp::getName), builder)))
						.executes(DelwarpCommand::execute));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		String name = StringArgumentType.getString(commandSourceStackCommandContext, "name");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Warp warp = session.get(Warp.class, name);
			if (warp == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cWarp §e" + name + "§c does not exist!"));
				return Command.SINGLE_SUCCESS;
			}
			session.beginTransaction();
			session.remove(warp);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aWarp §e" + name + "§a has been deleted!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
