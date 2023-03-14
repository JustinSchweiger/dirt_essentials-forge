package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.Warp;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import org.hibernate.Session;

public class SetwarpCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("setwarp")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SETWARP))
				.then(Commands.argument("name", StringArgumentType.word())
						.executes(SetwarpCommand::execute));

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

		if (player.getMainHandItem().getItem() == Items.AIR) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be holding an item to set a warp!"));
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Warp warp = session.get(Warp.class, name);
			if (warp == null) {
				warp = new Warp(name, player.getMainHandItem(), player.getLevel().dimension(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
				session.beginTransaction();
				session.persist(warp);
				session.getTransaction().commit();
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aWarp §e" + name + "§a has been created!"), false);
			} else {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cWarp §e" + name + "§c already exists!"));
			}
		}

		return Command.SINGLE_SUCCESS;
	}
}
