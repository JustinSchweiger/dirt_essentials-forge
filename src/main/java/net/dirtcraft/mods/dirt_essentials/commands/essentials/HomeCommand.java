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
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.hibernate.Session;

public class HomeCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("home")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HOME))
				.executes(HomeCommand::defaultHome)
				.then(Commands.argument("name", StringArgumentType.word())
						.executes(context -> home(context, StringArgumentType.getString(context, "name"))));

		dispatcher.register(commandBuilder);
	}

	private static int defaultHome(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		home(commandSourceStackCommandContext, "home");

		return Command.SINGLE_SUCCESS;
	}

	private static int home(CommandContext<CommandSourceStack> commandSourceStackCommandContext, String name) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();

		if (!HomeManager.hasHomes(player.getUUID())) {
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "You don't have any homes!"), Util.NIL_UUID);
			return Command.SINGLE_SUCCESS;
		}

		if (HomeManager.isOnCooldown(player.getUUID())) {
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "You can use the command again in §b" + HomeManager.getCooldown(player.getUUID()) + " §7seconds!"), Util.NIL_UUID);
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
			Home home = dirtPlayer.getHome(name);

			ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(home.getLocation()));
			ServerLevel level = DirtEssentials.SERVER.getLevel(dimension);
			if (level == null) {
				player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThe home you are trying to access is in a world that no longer exists!"), Util.NIL_UUID);
				return Command.SINGLE_SUCCESS;
			}

			PlayerTeleportEvent event = new PlayerTeleportEvent(player, home.getX(), home.getY(), home.getZ());
			MinecraftForge.EVENT_BUS.post(event);

			player.teleportTo(level, home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch());
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Teleported to home §e" + home.getName()), Util.NIL_UUID);
		}

		return Command.SINGLE_SUCCESS;
	}
}
