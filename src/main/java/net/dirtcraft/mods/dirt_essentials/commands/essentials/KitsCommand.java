package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.database.Kit;
import net.dirtcraft.mods.dirt_essentials.manager.KitManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class KitsCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("kits")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.KITS))
				.executes(KitsCommand::execute);

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		boolean isConsole = source.getEntity() == null;

		if (isConsole) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();

		List<Kit> kits = KitManager.getAllKitsWithPermission(source);

		player.sendMessage(new TextComponent(""), Util.NIL_UUID);
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aAvailable Kits§7:"), Util.NIL_UUID);
		player.sendMessage(new TextComponent(""), Util.NIL_UUID);

		if (kits.isEmpty()) {
			player.sendMessage(new TextComponent("§8§oThere are no kits claimable for you :("), Util.NIL_UUID);
		} else {
			for (Kit kit : kits) {
				TextComponent showKitComponent = new TextComponent("§7[§3ℹ§7]");
				showKitComponent.setStyle(showKitComponent.getStyle()
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/showkit " + kit.getName()))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§7Click to view §3" + kit.getName() + "§7's contents!"))));

				String cooldownString = kit.getCooldown() < 0 ? "One-Time" : kit.getCooldown() + " " + kit.getTimeUnit().getString() + (kit.getCooldown() == 1 ? "" : "s");

				TextComponent kitComponent;
				if (KitManager.isKitClaimable(player.getUUID(), kit)) {
					kitComponent = new TextComponent("§6" + kit.getName() + " §9- §3Click to claim!");
					kitComponent.setStyle(kitComponent.getStyle()
							.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/kit " + kit.getName()))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§aClick to claim this kit!\n§9Cooldown§7: §3" + cooldownString))));
				} else {
					String timeLeft = KitManager.getTimeLeft(player.getUUID(), kit);
					kitComponent = new TextComponent("§c" + kit.getName() + " §9- " + timeLeft);
					kitComponent.setStyle(kitComponent.getStyle()
							.withHoverEvent(new HoverEvent(
									HoverEvent.Action.SHOW_TEXT,
									new TextComponent(
											kit.getCooldown() < 0 ?
													"§cThis kit is only claimable once!" :
													"§7You have to wait before claiming this kit again!\n§9Cooldown§7: §3" + cooldownString
									)
							))
					);
				}

				MutableComponent finalComponent = new TextComponent("  ");

				if (PermissionHandler.hasPermission(source, EssentialsPermissions.SHOWKIT)) {
					finalComponent.append(showKitComponent);
					finalComponent.append(" ");
				}

				finalComponent.append(kitComponent);

				player.sendMessage(finalComponent, Util.NIL_UUID);
			}
		}

		player.sendMessage(new TextComponent(""), Util.NIL_UUID);

		return Command.SINGLE_SUCCESS;
	}
}
