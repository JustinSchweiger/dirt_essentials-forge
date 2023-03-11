package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.Kit;
import net.dirtcraft.mods.dirt_essentials.manager.KitManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import org.hibernate.Session;

public class DeletekitCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("deletekit")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.DELETEKIT))
				.then(Commands.argument("name", StringArgumentType.string())
						.suggests(((context, builder) -> SharedSuggestionProvider.suggest(KitManager.getAllKits().stream().map(Kit::getName), builder)))
						.executes(DeletekitCommand::execute));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		String name = StringArgumentType.getString(commandSourceStackCommandContext, "name");
		Kit kit = Kit.get(name);

		if (kit == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "A kit with that name doesn't exists!"), false);
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			session.remove(kit);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Kit §e" + name + " §7successfully deleted!"), false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Command.SINGLE_SUCCESS;
	}
}
