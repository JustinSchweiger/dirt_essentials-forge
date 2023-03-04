package net.dirtcraft.mods.dirt_essentials;

import com.mojang.logging.LogUtils;
import net.dirtcraft.mods.dirt_essentials.commands.DirtRestartCommand;
import net.dirtcraft.mods.dirt_essentials.config.RestartConfig;
import net.dirtcraft.mods.dirt_essentials.listeners.OnPlayerLoggedIn;
import net.dirtcraft.mods.dirt_essentials.listeners.OnServerChat;
import net.dirtcraft.mods.dirt_essentials.manager.RestartManager;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

@Mod(Strings.MOD_ID)
public class Main {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static MinecraftServer SERVER = null;

	public Main() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

		registerConfigs();
		enableFeatures();

		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(OnServerChat::event);
		MinecraftForge.EVENT_BUS.addListener(OnPlayerLoggedIn::event);
		MinecraftForge.EVENT_BUS.addListener(this::setup);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void registerConfigs() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RestartConfig.SPEC, "dirt_restart-server.toml");
	}

	@SubscribeEvent
	public void setup(final ServerStartedEvent event) {
		SERVER = event.getServer();

		if (RestartConfig.ENABLED.get()) {
			RestartManager.startTimer();
		}
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		if (RestartConfig.ENABLED.get())
			DirtRestartCommand.register(event.getDispatcher());
	}

	private void enableFeatures() {
		if (RestartConfig.ENABLED.get()) {
			MinecraftForge.EVENT_BUS.addListener(RestartManager::tick);
			LOGGER.info(">> Restart feature enabled.");
		}
	}
}
