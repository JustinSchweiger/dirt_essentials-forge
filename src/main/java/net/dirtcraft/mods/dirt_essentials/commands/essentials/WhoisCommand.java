package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.GodManager;
import net.dirtcraft.mods.dirt_essentials.manager.HomeManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.time.format.DateTimeFormatter;

public class WhoisCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("whois").requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.WHOIS)).then(Commands.argument("player", EntityArgument.player()).executes(WhoisCommand::execute)));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");
		DirtPlayer dirtPlayer = DirtPlayer.get(player);

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aWho is §6" + player.getGameProfile().getName() + "§a:"), false);
		source.sendSuccess(new TextComponent(""), false);
		source.sendSuccess(new TextComponent(" §8- §6Player§7: " + player.getGameProfile().getName()), false);
		source.sendSuccess(new TextComponent(" §8- §6DisplayName§7: " + (player.getCustomName() != null ? player.getCustomName().getString() : player.getDisplayName().getString())), false);

		TextComponent uuidComponent = new TextComponent("§3" + player.getUUID());
		uuidComponent.setStyle(uuidComponent.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, player.getUUID().toString())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§7Click to copy!"))));
		source.sendSuccess(new TextComponent(" §8- §6UUID§7: ").append(uuidComponent), false);

		TextComponent ipComponent = new TextComponent(player.getIpAddress());
		ipComponent.setStyle(ipComponent.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, player.getIpAddress())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§7Click to copy!"))));
		source.sendSuccess(new TextComponent(" §8- §6IP§7: ").append(ipComponent), false);

		source.sendSuccess(new TextComponent(" §8- §6Gamemode§7: " + player.gameMode.getGameModeForPlayer().getName()), false);
		source.sendSuccess(new TextComponent(" §8- §6Health§7: " + player.getHealth()), false);
		source.sendSuccess(new TextComponent(" §8- §6Hunger§7: " + player.getFoodData().getFoodLevel() + " / 20 (" + player.getFoodData().getSaturationLevel() + " saturation)"), false);

		TextComponent locationComponent = new TextComponent("§d" + player.getLevel().dimension().location() + " §7(§b" + player.getBlockX() + "§7, §b" + player.getBlockY() + "§7, §b" + player.getBlockZ() + "§7)");
		locationComponent.setStyle(locationComponent.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getGameProfile().getName())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§7Click to teleport!"))));
		source.sendSuccess(new TextComponent(" §8- §6Location§7: ").append(locationComponent), false);

		source.sendSuccess(new TextComponent(" §8- §6Staff§7: " + (dirtPlayer.isStaff() ? "§atrue" : "§cfalse")), false);
		source.sendSuccess(new TextComponent(" §8- §6Flying§7: " + (player.getAbilities().flying ? "§atrue" : "§cfalse")), false);
		source.sendSuccess(new TextComponent(" §8- §6Money§7: " + dirtPlayer.getFormattedBalance()), false);
		source.sendSuccess(new TextComponent(" §8- §6God Mode§7: " + (GodManager.isGodModeEnabled(player.getUUID()) ? "§atrue" : "§cfalse")), false);
		source.sendSuccess(new TextComponent(" §8- §6Home Limit§7: " + HomeManager.getHomes(player.getGameProfile().getName()).spliterator().getExactSizeIfKnown() + " / " + dirtPlayer.getHomeAmount()), false);
		source.sendSuccess(new TextComponent(" §8- §6Autobroadcasts§7: " + (dirtPlayer.isAutobroadcastsDisabled() ? "§cdisabled" : "§aenabled")), false);
		source.sendSuccess(new TextComponent(" §8- §6First played§7: §9" + dirtPlayer.getFirstJoined().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " §7at §9" + dirtPlayer.getFirstJoined().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))), false);
		source.sendSuccess(new TextComponent(" §8- §6Playtime§7: §e" + Utils.formatTimePlayed(dirtPlayer)), false);
		source.sendSuccess(new TextComponent(""), false);

		return Command.SINGLE_SUCCESS;
	}
}
