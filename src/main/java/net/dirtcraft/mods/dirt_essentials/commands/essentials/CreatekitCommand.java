package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.TimeUnit;
import net.dirtcraft.mods.dirt_essentials.database.Kit;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class CreatekitCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("createkit")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.CREATEKIT))
				.then(Commands.argument("name", StringArgumentType.string())
						.then(Commands.argument("cooldown", IntegerArgumentType.integer(-1))
								.then(Commands.literal("s").executes(context -> execute(context, TimeUnit.SECONDS)))
								.then(Commands.literal("m").executes(context -> execute(context, TimeUnit.MINUTES)))
								.then(Commands.literal("h").executes(context -> execute(context, TimeUnit.HOURS)))));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext, TimeUnit timeUnit) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = source.getEntity() == null;

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		BlockHitResult result = (BlockHitResult) source.getEntity().pick(10, 0, false);
		BlockPos pos = result.getBlockPos();
		ServerLevel level = source.getLevel();
		ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(pos);

		if (chest == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You must be looking at a chest!"), false);
			return Command.SINGLE_SUCCESS;
		}

		if (chest.isEmpty()) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "The chest you are looking at is empty!"), false);
			return Command.SINGLE_SUCCESS;
		}

		String name = StringArgumentType.getString(commandSourceStackCommandContext, "name");
		int cooldown = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "cooldown");

		if (Kit.get(name) != null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "A kit with that name already exists!"), false);
			return Command.SINGLE_SUCCESS;
		}

		int size = chest.getContainerSize();
		List<ItemStack> items = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			ItemStack item = chest.getItem(i);
			if (!item.isEmpty())
				items.add(item);
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Kit kit = new Kit(name, cooldown, timeUnit, items);
			session.beginTransaction();
			session.persist(kit);
			session.getTransaction().commit();
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Kit §e" + name + " §7created successfully!"), false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Command.SINGLE_SUCCESS;
	}
}
