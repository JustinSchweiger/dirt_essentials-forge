package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class NickCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("nick")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.NICK))
				.then(Commands.literal("set")
						.then(Commands.argument("nickname", StringArgumentType.greedyString())
								.executes(context -> setNick(context.getSource(), StringArgumentType.getString(context, "nickname"), context.getSource().getPlayerOrException(), false))
								.then(Commands.argument("player", EntityArgument.player())
										.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.NICK_OTHERS))
										.executes(context -> setNick(context.getSource(), StringArgumentType.getString(context, "nickname"), EntityArgument.getPlayer(context, "player"), true)))))
				.then(Commands.literal("reset")
						.executes(context -> resetNick(context.getSource(), context.getSource().getPlayerOrException(), false))
						.then(Commands.argument("player", EntityArgument.player())
								.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.NICK_OTHERS))
								.executes(context -> resetNick(context.getSource(), EntityArgument.getPlayer(context, "player"), true)))));
	}

	private static int resetNick(CommandSourceStack source, ServerPlayer player, boolean isOther) {
		player.setCustomName(null);

		if (isOther)
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Reset nickname for §d" + player.getGameProfile().getName()), false);
		else
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Reset nickname"), false);

		return Command.SINGLE_SUCCESS;
	}

	private static int setNick(CommandSourceStack source, String nickname, ServerPlayer player, boolean isOther) {
		String formattedNick = "§7" + EssentialsConfig.NICKNAME_PREFIX.get() + Utils.formatColorString(nickname);

		player.setCustomName(new TextComponent(formattedNick));

		if (isOther)
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Set nickname to " + formattedNick + " §7for §d" + player.getGameProfile().getName()), false);
		else
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Set nickname to " + formattedNick), false);

		return Command.SINGLE_SUCCESS;
	}
}
