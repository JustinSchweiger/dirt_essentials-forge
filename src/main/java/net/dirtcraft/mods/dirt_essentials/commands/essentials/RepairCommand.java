package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RepairCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("repair")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.REPAIR))
				.executes(RepairCommand::execute));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		ItemStack stack = player.getMainHandItem();

		if (stack.isEmpty()) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be holding an item to repair!"));
			return Command.SINGLE_SUCCESS;
		}

		if (!stack.isDamageableItem()) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThis item cannot be repaired because it has no durability!"));
			return Command.SINGLE_SUCCESS;
		}

		if (!stack.isDamaged()) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThis item is already fully repaired!"));
			return Command.SINGLE_SUCCESS;
		}

		List<ResourceLocation> repairListItems = EssentialsConfig.REPAIR_LIST.get().stream().map(ResourceLocation::new).toList();
		boolean isBlacklist = EssentialsConfig.REPAIR_LIST_BLACKLIST.get();

		if (isBlacklist) {
			if (repairListItems.contains(stack.getItem().getRegistryName())) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cRepairment of this item is not allowed!"));
				return Command.SINGLE_SUCCESS;
			}
		} else {
			if (!repairListItems.contains(stack.getItem().getRegistryName())) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cRepairment of this item is not allowed!"));
				return Command.SINGLE_SUCCESS;
			}
		}

		stack.setDamageValue(0);

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aYour item has been repaired!"), false);

		return Command.SINGLE_SUCCESS;
	}
}
