package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.manager.GcManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.Collections;

public class GcCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("gc")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.GC))
				.executes(GcCommand::execute);

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		int bars = 40;
		float used = GcManager.getUsedMemory();
		float max = GcManager.getMaxMemory();
		float percent = used / max;
		int usedBars = (int) (percent * bars);
		int freeBars = bars - usedBars;
		String usedBar = String.join("", Collections.nCopies(usedBars, "§e❘"));
		String freeBar = String.join("", Collections.nCopies(freeBars, "§6❘"));

		source.sendSuccess(new TextComponent(""), false);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§bSystem Performance Information§7:"), false);
		source.sendSuccess(new TextComponent(""), false);
		source.sendSuccess(new TextComponent("§cUptime§7:        " + GcManager.getUptime()), false);
		source.sendSuccess(new TextComponent("§cMemory§7:       " + usedBar + freeBar + " §e" + ((int) used) + "MB §7| §6" + ((int) max) + "MB"), false);
		source.sendSuccess(new TextComponent("§cOverall TPS§7: " + GcManager.formatTps(GcManager.getOverallTps())), false);
		source.sendSuccess(new TextComponent("§cOverall Tick§7: " + GcManager.formatTickTime(GcManager.getOverallTickTime())), false);
		source.sendSuccess(new TextComponent(""), false);

		Iterable<ServerLevel> levels = DirtEssentials.SERVER.getAllLevels();
		if (levels.spliterator().getExactSizeIfKnown() > 15 && source.getEntity() != null) {
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cToo many dimensions to display!"), false);
			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cPlease run this command in the console!"), false);
			return Command.SINGLE_SUCCESS;
		}

		source.sendSuccess(new TextComponent("§7▼ §8§oHover for more info! §7▼"), false);
		source.sendSuccess(new TextComponent(""), false);
		for (ServerLevel level : levels) {
			TextComponent worldComponent = new TextComponent("§3▪ §a§o" + level.dimension().registry() + " §r§7| §e§o" + level.dimension().location());
			worldComponent.setStyle(worldComponent.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(
					"§6Mean Tick§7:\n" +
							"  §8▪ " + GcManager.formatTickTime(GcManager.getMeanTickTime(level)) + "\n" +
							"§6Mean TPS§7:\n" +
							"  §8▪ " + GcManager.formatTps(GcManager.getTps(level)) + "\n" +
							"§6Chunks§7:\n" +
							"  §8▪ §d" + level.getChunkSource().getLoadedChunksCount() + "\n" +
							"§6Entities§7:\n" +
							"  §8▪ §d" + level.getAllEntities().spliterator().getExactSizeIfKnown()
			))));

			source.sendSuccess(worldComponent, false);
		}

		source.sendSuccess(new TextComponent(""), false);

		return Command.SINGLE_SUCCESS;
	}
}
