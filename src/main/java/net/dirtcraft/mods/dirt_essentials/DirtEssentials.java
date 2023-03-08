package net.dirtcraft.mods.dirt_essentials;

import com.mojang.logging.LogUtils;
import net.dirtcraft.mods.dirt_essentials.commands.DirtRestartCommand;
import net.dirtcraft.mods.dirt_essentials.commands.DirtRulesCommand;
import net.dirtcraft.mods.dirt_essentials.commands.GlobalCommand;
import net.dirtcraft.mods.dirt_essentials.commands.StaffCommand;
import net.dirtcraft.mods.dirt_essentials.config.RestartConfig;
import net.dirtcraft.mods.dirt_essentials.config.RulesConfig;
import net.dirtcraft.mods.dirt_essentials.listeners.OnPlayerLoggedIn;
import net.dirtcraft.mods.dirt_essentials.listeners.OnServerChat;
import net.dirtcraft.mods.dirt_essentials.manager.RestartManager;
import net.dirtcraft.mods.dirt_essentials.manager.RulesManager;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

import java.io.File;
import java.util.logging.Level;

@Mod(Strings.MOD_ID)
public class DirtEssentials {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static MinecraftServer SERVER = null;
	public static File DIRT_MODS_DIR = null;
	public static LuckPerms LUCKPERMS = null;

	public DirtEssentials() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);
		registerConfigs();
		enableFeatures();

		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(OnServerChat::event);
		MinecraftForge.EVENT_BUS.addListener(OnPlayerLoggedIn::event);
		MinecraftForge.EVENT_BUS.addListener(this::serverStartedEvent);
		MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStartEvent);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void registerConfigs() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RestartConfig.SPEC, "dirt_restart-server.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RulesConfig.SPEC, "dirt_rules-server.toml");
	}

	@SubscribeEvent
	public void serverStartedEvent(final ServerStartedEvent event) {
		SERVER = event.getServer();
		File serverDir = SERVER.getServerDirectory();
		DIRT_MODS_DIR = new File(serverDir, "dirt_mods");
		if (!DIRT_MODS_DIR.exists()) {
			DIRT_MODS_DIR.mkdir();
			LOGGER.info("» Created dirt_mods directory.");
		}

		if (RestartConfig.ENABLED.get()) {
			RestartManager.startTimer();
		}
	}

	@SubscribeEvent
	public void serverAboutToStartEvent(final ServerAboutToStartEvent event) {
		LUCKPERMS = LuckPermsProvider.get();
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		if (RestartConfig.ENABLED.get())
			DirtRestartCommand.register(event.getDispatcher());

		if (RulesConfig.ENABLED.get()) {
			DirtRulesCommand.register(event.getDispatcher());
			DirtRulesCommand.registerRulesAlias(event.getDispatcher());
		}

		GlobalCommand.register(event.getDispatcher());
		StaffCommand.register(event.getDispatcher());
	}

	private void enableFeatures() {
		if (RestartConfig.ENABLED.get()) {
			MinecraftForge.EVENT_BUS.addListener(RestartManager::tick);
			LOGGER.info("» Restart feature enabled.");
		}

		if (RulesConfig.ENABLED.get()) {
			RulesManager.reminderInterval = RulesConfig.REMINDER_INTERVAL.get();
			MinecraftForge.EVENT_BUS.addListener(RulesManager::tick);
			LOGGER.info("» Rules feature enabled.");
		}
	}
}
