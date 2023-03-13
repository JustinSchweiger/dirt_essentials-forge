package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public class GiveCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("give")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GIVE))
				.then(Commands.argument("targets", EntityArgument.players())
						.then(Commands.argument("item", ItemArgument.item())
								.executes(context -> giveItem(context.getSource(), ItemArgument.getItem(context, "item"), EntityArgument.getPlayers(context, "targets"), 1))
								.then(Commands.argument("count", IntegerArgumentType.integer(1))
										.executes(context -> giveItem(context.getSource(), ItemArgument.getItem(context, "item"), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "count")))))));
	}

	private static int giveItem(CommandSourceStack source, ItemInput item, Collection<ServerPlayer> targets, int count) throws CommandSyntaxException {
		int i = item.getItem().getItemStackLimit(new ItemStack(item.getItem()));
		int j = i * 100;
		if (count > j) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "Can't give more than §d" + i * 100 + " §7of §a" + item.createItemStack(count, false).getDisplayName()));
			return 0;
		} else {
			for (ServerPlayer serverplayer : targets) {
				int k = count;

				while (k > 0) {
					int l = Math.min(i, k);
					k -= l;
					ItemStack itemstack = item.createItemStack(l, false);
					serverplayer.getInventory().add(itemstack);
				}
			}

			if (targets.size() == 1) {
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Gave §d" + count + " §a" + item.createItemStack(count, false).getDisplayName().getString() + "§7 to §6" + targets.iterator().next().getDisplayName().getString()), false);
			} else {
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "Gave §d" + count + " §a" + item.createItemStack(count, false).getDisplayName().getString() + "§7 to §6" + targets.size() + "§7 players"), false);
			}

			return targets.size();
		}
	}
}
