package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class HatCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("hat")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HAT))
				.executes(HatCommand::execute));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
		ItemStack heldItem = player.getMainHandItem();

		if (helmet.isEmpty()) {
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		} else {
			player.setItemInHand(InteractionHand.MAIN_HAND, helmet);
		}

		player.setItemSlot(EquipmentSlot.HEAD, heldItem);
		player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Your hat is now §d" + heldItem.getItem().getRegistryName()), Util.NIL_UUID);

		return Command.SINGLE_SUCCESS;
	}
}
