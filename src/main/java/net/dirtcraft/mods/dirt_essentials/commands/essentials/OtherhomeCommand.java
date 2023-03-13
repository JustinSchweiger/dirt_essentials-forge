package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.data.entites.Home;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.dirtcraft.mods.dirt_essentials.manager.HomeManager;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.hibernate.Session;

import java.util.UUID;

public class OtherhomeCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("otherhome")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.OTHERHOME))
				.then(Commands.argument("target", StringArgumentType.string())
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder))
						.then(Commands.literal("teleport")
								.then(Commands.argument("home", StringArgumentType.string())
										.suggests((context, builder) -> SharedSuggestionProvider.suggest(HomeManager.getHomes(StringArgumentType.getString(context, "target")), builder))
										.executes(OtherhomeCommand::teleport)))
						.then(Commands.literal("delete")
								.then(Commands.argument("home", StringArgumentType.string())
										.suggests((context, builder) -> SharedSuggestionProvider.suggest(HomeManager.getHomes(StringArgumentType.getString(context, "target")), builder))
										.executes(OtherhomeCommand::delete))));

		dispatcher.register(commandBuilder);
	}

	private static int teleport(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = source.getPlayerOrException();

		UUID target = PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "target"));
		if (target == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"), false);
			return Command.SINGLE_SUCCESS;
		}

		String homeName = StringArgumentType.getString(commandSourceStackCommandContext, "home");
		if (!HomeManager.hasHome(target, homeName)) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "That home does not exist!"), false);
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, target);
			Home home = dirtPlayer.getHome(homeName);
			session.getTransaction().commit();

			ResourceKey<Level> dimension = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation(home.getRegistry())), new ResourceLocation(home.getLocation()));
			ServerLevel level = DirtEssentials.SERVER.getLevel(dimension);
			if (level == null) {
				player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThe home you are trying to access is in a world that no longer exists!"), Util.NIL_UUID);
				return Command.SINGLE_SUCCESS;
			}

			PlayerTeleportEvent event = new PlayerTeleportEvent(player, player.getX(), player.getY(), player.getZ());
			MinecraftForge.EVENT_BUS.post(event);

			player.teleportTo(level, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Teleported to home §e" + home.getName() + " §7from §6" + StringArgumentType.getString(commandSourceStackCommandContext, "target") + "§7!"), Util.NIL_UUID);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int delete(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();

		UUID target = PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "target"));
		if (target == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlayer not found!"), false);
			return Command.SINGLE_SUCCESS;
		}

		String homeName = StringArgumentType.getString(commandSourceStackCommandContext, "home");
		if (!HomeManager.hasHome(target, homeName)) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "That home does not exist!"), false);
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DirtPlayer player = session.get(DirtPlayer.class, target);
			Home home = player.getHome(homeName);

			player.removeHome(home);
			session.persist(player);
			session.remove(home);
			session.getTransaction().commit();
		}

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Deleted home §b" + homeName + " §7from §6" + StringArgumentType.getString(commandSourceStackCommandContext, "target") + "§7!"), true);

		return Command.SINGLE_SUCCESS;
	}
}
