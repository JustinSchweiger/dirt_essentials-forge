package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.events.PlayerTeleportEvent;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

public class TpallCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("tpall")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TPALL))
				.executes(TpallCommand::execute));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer target = source.getPlayerOrException();

		int teleported = 0;
		for (ServerPlayer player : DirtEssentials.SERVER.getPlayerList().getPlayers()) {
			if (target.getUUID().equals(player.getUUID()))
				continue;

			teleported++;
			PlayerTeleportEvent event = new PlayerTeleportEvent(player, target.getX(), target.getY(), target.getZ());
			MinecraftForge.EVENT_BUS.post(event);
			player.teleportTo(target.getLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aYou have been teleported to §6" + target.getGameProfile().getName()), Util.NIL_UUID);
		}

		target.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aTeleported §d" + teleported + " §aplayers to you!"), Util.NIL_UUID);
		return Command.SINGLE_SUCCESS;
	}
}
