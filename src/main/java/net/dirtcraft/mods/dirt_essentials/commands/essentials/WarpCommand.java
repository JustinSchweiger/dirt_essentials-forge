package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.Warp;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.dirtcraft.mods.dirt_essentials.manager.WarpManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.hibernate.Session;

public class WarpCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("warp")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.WARP))
				.then(Commands.argument("warp", StringArgumentType.word())
						.suggests(((context, builder) -> SharedSuggestionProvider.suggest(WarpManager.getWarps().stream().map(Warp::getName), builder)))
						.executes(WarpCommand::execute));

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
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cWarp not found!"));
				return Command.SINGLE_SUCCESS;
			}

			ResourceKey<Level> dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(warp.getLocation()));
			ServerLevel level = DirtEssentials.SERVER.getLevel(dim);
			if (level == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThe warp you are trying to access is in a dimension that no longer exists!"));
				return Command.SINGLE_SUCCESS;
			}

			PlayerTeleportEvent event = new PlayerTeleportEvent(player, warp.getX(), warp.getY(), warp.getZ());
			MinecraftForge.EVENT_BUS.post(event);

			player.teleportTo(level, warp.getX(), warp.getY(), warp.getZ(), player.getYRot(), player.getXRot());
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "Teleported to warp §e" + warp.getName() + "§7!"), Util.NIL_UUID);
		}

		return Command.SINGLE_SUCCESS;
	}
}
