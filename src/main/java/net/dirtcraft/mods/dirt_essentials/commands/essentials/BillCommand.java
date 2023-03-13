package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

public class BillCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("bill")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.BILL))
				.then(Commands.argument("target", EntityArgument.player())
						.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
								.executes(BillCommand::execute)));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = !(source.getEntity() instanceof ServerPlayer);

		if (isConsole) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = (ServerPlayer) source.getEntity();
		ServerPlayer target = EntityArgument.getPlayer(commandSourceStackCommandContext, "target");

		if (player.getUUID().equals(target.getUUID())) {
			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "You can't bill yourself!"), Util.NIL_UUID);
			return Command.SINGLE_SUCCESS;
		}

		double amount = DoubleArgumentType.getDouble(commandSourceStackCommandContext, "amount");

		TextComponent billMessage = new TextComponent(Strings.ESSENTIALS_PREFIX + "You have been billed by §6" + player.getGameProfile().getName() + " §7for " + DirtPlayer.getFormattedBalance(amount) + "§!");
		TextComponent billButton = new TextComponent("§7[§a✔ Pay the bill§7]");
		HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§aClick to pay the bill"));
		ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pay " + player.getGameProfile().getName() + " " + amount);
		billButton.setStyle(billButton.getStyle().withHoverEvent(hoverEvent).withClickEvent(clickEvent));

		target.sendMessage(billMessage, player.getUUID());
		target.sendMessage(new TextComponent(""), player.getUUID());
		target.sendMessage(billButton, player.getUUID());

		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "You have billed §6" + target.getGameProfile().getName() + "§7 for " + DirtPlayer.getFormattedBalance(amount) + "§!"), Util.NIL_UUID);

		return Command.SINGLE_SUCCESS;
	}
}
