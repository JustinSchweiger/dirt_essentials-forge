package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class KillCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("kill")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.KILL))
				.executes(context -> kill(context, false, ImmutableList.of(context.getSource().getEntityOrException())))
				.then(Commands.argument("targets", EntityArgument.entities())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.KILL_OTHERS))
						.executes(context -> kill(context, true, EntityArgument.getEntities(context, "targets")))));
	}

	private static int kill(CommandContext<CommandSourceStack> context, boolean isOther, Collection<? extends Entity> targets) {
		CommandSourceStack source = context.getSource();
		if (source.getEntity() == null && !isOther) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		for (Entity entity : targets) {
			entity.kill();
		}

		if (isOther) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7Killed §d" + targets.size() + "§7 entities!"), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
