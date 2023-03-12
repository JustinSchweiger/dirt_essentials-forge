package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;

public class ListautobroadcastsCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("listautobroadcasts")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.LISTAUTOBROADCASTS))
				.executes(ListautobroadcastsCommand::execute));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();

		if (EssentialsConfig.AUTOBROADCASTS.get().isEmpty()) {
			commandSourceStackCommandContext.getSource().sendFailure(new TextComponent("§cThere are no autobroadcasts!"));
			return Command.SINGLE_SUCCESS;
		}
		source.sendSuccess(new TextComponent("§8-"), false);
		for (String broadcastString : EssentialsConfig.AUTOBROADCASTS.get()) {
			EssentialsConfig.Autobroadcast broadcast = EssentialsConfig.Autobroadcast.deserialize(broadcastString);

			TextComponent message = new TextComponent(
					String.join("\n", broadcast.getLines())
			);

			if (broadcast.getAction() != null && !broadcast.getClickValue().equalsIgnoreCase("")) {
				message.setStyle(message.getStyle()
						.withClickEvent(new ClickEvent(broadcast.getAction(), broadcast.getClickValue())));
			}

			if (!broadcast.getHoverEventText().equalsIgnoreCase("")) {
				message.setStyle(message.getStyle()
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(broadcast.getHoverEventText()))));
			}

			source.sendSuccess(message, false);
			source.sendSuccess(new TextComponent("§8-"), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
