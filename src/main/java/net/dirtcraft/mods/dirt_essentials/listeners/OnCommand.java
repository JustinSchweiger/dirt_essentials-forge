package net.dirtcraft.mods.dirt_essentials.listeners;

import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OnCommand {
	private static final Logger LOGGER = LogManager.getLogger(Strings.MOD_ID + "/CommandListener");

	@SubscribeEvent
	public static void event(CommandEvent event) {
		String command = event.getParseResults().getReader().getString();
		if (command.startsWith("/"))
			command = command.substring(1);

		if (event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player) {
			LOGGER.info(String.format("%s issued server command: /%s", player.getGameProfile().getName(), command));
		} else {
			LOGGER.info(String.format("CONSOLE issued server command: /%s", command));
		}
	}
}
