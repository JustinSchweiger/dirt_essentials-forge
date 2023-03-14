package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.Warp;
import net.dirtcraft.mods.dirt_essentials.manager.WarpManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;

public class EditwarpiconCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("editwarpicon")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.EDIT_WARP))
				.then(Commands.argument("warp", StringArgumentType.word())
						.suggests(((context, builder) -> SharedSuggestionProvider.suggest(WarpManager.getWarps().stream().map(Warp::getName), builder)))
						.executes(EditwarpiconCommand::execute));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		String name = StringArgumentType.getString(commandSourceStackCommandContext, "warp");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Warp warp = session.get(Warp.class, name);
			if (warp == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cWarp §e" + name + "§c does not exist!"));
				return Command.SINGLE_SUCCESS;
			}

			session.beginTransaction();
			warp.setItem(player.getMainHandItem());
			session.persist(warp);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSuccessfully edited warp §e" + name + "§a!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
