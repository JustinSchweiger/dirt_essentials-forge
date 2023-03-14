package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;

public class TopCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("top")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TOP))
				.executes(TopCommand::execute));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		int y = player.getLevel().getHeight(Heightmap.Types.MOTION_BLOCKING, player.getBlockX(), player.getBlockZ());

		PlayerTeleportEvent event = new PlayerTeleportEvent(player, player.getX(), y + 2, player.getZ());
		MinecraftForge.EVENT_BUS.post(event);

		player.teleportTo(player.getX(), y + 2, player.getZ());
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You have been teleported to the §ahighest block above you§7!"), false);

		return Command.SINGLE_SUCCESS;
	}
}
