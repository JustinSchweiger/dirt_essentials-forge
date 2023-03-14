package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;

public class TrashCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("trash")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.TRASH))
				.executes(TrashCommand::execute));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"), false);
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		player.openMenu(new SimpleMenuProvider((id, inventory, playerEntity) -> ChestMenu.oneRow(id, inventory), new TextComponent("§4Trash §7- §cItems cannot be restored!")));

		return Command.SINGLE_SUCCESS;
	}
}
