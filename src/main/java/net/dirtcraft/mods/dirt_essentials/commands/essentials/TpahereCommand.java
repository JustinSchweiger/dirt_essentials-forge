package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.data.TeleportRequest;
import net.dirtcraft.mods.dirt_essentials.manager.TeleportManager;
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

import java.time.LocalDateTime;

public class TpahereCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("tpahere")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TPAHERE))
				.then(Commands.argument("player", EntityArgument.player())
						.executes(context -> tpa(context.getSource(), EntityArgument.getPlayer(context, "player")))));
	}

	private static int tpa(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();

		if (player.getUUID().equals(target.getUUID())) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou can't send a teleport request to yourself!"));
			return Command.SINGLE_SUCCESS;
		}

		if (TeleportManager.hasTeleportsDisabled(player.getUUID())) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou have teleport requests disabled! Enable them with §d/tptoggle§c."));
			return Command.SINGLE_SUCCESS;
		}

		if (TeleportManager.hasOutgoingRequestToTarget(player.getUUID(), target.getUUID(), TeleportRequest.TeleportType.TPAHERE)) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou already have an active teleport request to §6" + target.getName().getString() + "§c!"));
			return Command.SINGLE_SUCCESS;
		}

		TextComponent targetComponent = new TextComponent("§d" + player.getGameProfile().getName() + " §7has requested you to teleport to them.");
		TextComponent acceptComponent = new TextComponent("§7[§2✔ §aAccept§7]");
		acceptComponent.setStyle(acceptComponent.getStyle()
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§aClick to accept the teleport request."))));
		TextComponent denyComponent = new TextComponent("§7[§4✘ §cDeny§7]");
		denyComponent.setStyle(denyComponent.getStyle()
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§cClick to deny the teleport request."))));

		target.sendMessage(new TextComponent(""), Util.NIL_UUID);
		target.sendMessage(targetComponent, Util.NIL_UUID);
		target.sendMessage(new TextComponent(""), Util.NIL_UUID);
		target.sendMessage(new TextComponent("  ").append(acceptComponent).append(new TextComponent(" ")).append(denyComponent), Util.NIL_UUID);
		target.sendMessage(new TextComponent(""), Util.NIL_UUID);

		player.sendMessage(new TextComponent(""), Util.NIL_UUID);
		player.sendMessage(new TextComponent("§7Teleport request sent to §6" + target.getGameProfile().getName() + "§7!"), Util.NIL_UUID);
		player.sendMessage(new TextComponent("§7Use §c/tpacancel §7to cancel the request!"), Util.NIL_UUID);
		player.sendMessage(new TextComponent(""), Util.NIL_UUID);
		player.sendMessage(new TextComponent("§8§oThe request will automatically get canceled in §b§o30 seconds§8§o!"), Util.NIL_UUID);
		player.sendMessage(new TextComponent(""), Util.NIL_UUID);

		TeleportRequest outgoingRequest = new TeleportRequest(player.getUUID(), target.getUUID(), LocalDateTime.now(), TeleportRequest.Type.OUTGOING, TeleportRequest.TeleportType.TPAHERE);
		TeleportRequest incomingRequest = new TeleportRequest(player.getUUID(), target.getUUID(), LocalDateTime.now(), TeleportRequest.Type.INCOMING, TeleportRequest.TeleportType.TPAHERE);
		TeleportManager.addTeleportRequest(player, outgoingRequest);
		TeleportManager.addTeleportRequest(target, incomingRequest);

		return Command.SINGLE_SUCCESS;
	}
}
