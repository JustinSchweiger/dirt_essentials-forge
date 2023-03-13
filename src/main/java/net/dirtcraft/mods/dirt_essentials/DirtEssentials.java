package net.dirtcraft.mods.dirt_essentials;

import com.mojang.brigadier.CommandDispatcher;
import net.dirtcraft.mods.dirt_essentials.commands.chat.GlobalCommand;
import net.dirtcraft.mods.dirt_essentials.commands.chat.StaffCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.AfkCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.BackCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.BalCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.BaltopCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.BillCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.BlockzapCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.BreakCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.BroadcastCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.CartographytableCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.ClearCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.CraftCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.CreatekitCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.DeletekitCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.DelhomeCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.DelwarpCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.EcoCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.EditwarpiconCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.EnchantCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.EnderchestCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.EntityzapCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.FeedCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.FlyCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.GamemodeCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.GcCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.GodCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.GrindstoneCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.HatCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.HealCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.HelpCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.HomeCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.HomebalanceCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.HomesCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.ItemCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.JoinmessageCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.KillCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.KitCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.KitsCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.LeavemessageCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.ListCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.ListautobroadcastsCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.LoomCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.MsgCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.NbtCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.NickCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.NoteCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.OtherhomeCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.PayCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.RadiuszapCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.RepairCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.ReplyCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.ResetPlayerDataCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.RespectCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.SeenCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.SethomeCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.SetwarpCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.ShowkitCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.WarpCommand;
import net.dirtcraft.mods.dirt_essentials.commands.essentials.WarpsCommand;
import net.dirtcraft.mods.dirt_essentials.commands.playtime.PlaytimeCommand;
import net.dirtcraft.mods.dirt_essentials.commands.restart.DirtRestartCommand;
import net.dirtcraft.mods.dirt_essentials.commands.rtp.RtpCommand;
import net.dirtcraft.mods.dirt_essentials.commands.rtp.RtpcCommand;
import net.dirtcraft.mods.dirt_essentials.commands.rules.DirtRulesCommand;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.config.PlaytimeConfig;
import net.dirtcraft.mods.dirt_essentials.config.RestartConfig;
import net.dirtcraft.mods.dirt_essentials.config.RtpConfig;
import net.dirtcraft.mods.dirt_essentials.config.RulesConfig;
import net.dirtcraft.mods.dirt_essentials.config.SpamFixConfig;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.filter.JavaFilter;
import net.dirtcraft.mods.dirt_essentials.filter.Log4jFilter;
import net.dirtcraft.mods.dirt_essentials.filter.SystemFilter;
import net.dirtcraft.mods.dirt_essentials.listeners.OnCommand;
import net.dirtcraft.mods.dirt_essentials.listeners.OnPlayerLoggedIn;
import net.dirtcraft.mods.dirt_essentials.listeners.OnPlayerLoggedOut;
import net.dirtcraft.mods.dirt_essentials.listeners.OnServerChat;
import net.dirtcraft.mods.dirt_essentials.manager.AfkManager;
import net.dirtcraft.mods.dirt_essentials.manager.BackManager;
import net.dirtcraft.mods.dirt_essentials.manager.GcManager;
import net.dirtcraft.mods.dirt_essentials.manager.GodManager;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.manager.PlaytimeManager;
import net.dirtcraft.mods.dirt_essentials.manager.RestartManager;
import net.dirtcraft.mods.dirt_essentials.manager.RulesManager;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.commands.CommandSourceStack;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod(Strings.MOD_ID)
public class DirtEssentials {
	public static final Logger LOGGER = LogManager.getLogger(Strings.MOD_ID);
	public static MinecraftServer SERVER = null;
	public static File DIRT_MODS_DIR = null;
	public static LuckPerms LUCKPERMS = null;

	public DirtEssentials() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

		registerConfigs();
		enableFeatures();

		MinecraftForge.EVENT_BUS.addListener(OnServerChat::event);
		MinecraftForge.EVENT_BUS.addListener(OnPlayerLoggedIn::event);
		MinecraftForge.EVENT_BUS.addListener(OnPlayerLoggedOut::event);
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::serverStartedEvent);
		MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStartEvent);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void registerConfigs() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RestartConfig.SPEC, "dirt_restart-server.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RulesConfig.SPEC, "dirt_rules-server.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, EssentialsConfig.SPEC, "dirt_essentials-server.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RtpConfig.SPEC, "dirt_rtp-server.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SpamFixConfig.SPEC, "dirt_spamfix-server.toml");
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PlaytimeConfig.SPEC, "dirt_playtime-server.toml");
	}

	@SubscribeEvent
	public void serverStartedEvent(final ServerStartedEvent event) {
		File serverDir = SERVER.getServerDirectory();
		DIRT_MODS_DIR = new File(serverDir, "dirt_mods");
		if (!DIRT_MODS_DIR.exists()) {
			boolean created = DIRT_MODS_DIR.mkdir();
			if (created)
				LOGGER.info("» Created dirt_mods directory.");
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			List<DirtPlayer> players = session.createQuery("from DirtPlayer", DirtPlayer.class).list();
			for (DirtPlayer player : players) {
				PlayerManager.addPlayerData(player.getUuid(), player.getUsername());
			}
		}

		if (RestartConfig.ENABLED.get()) {
			RestartManager.startTimer();
		}

		GcManager.init();
	}

	@SubscribeEvent
	public void serverAboutToStartEvent(final ServerAboutToStartEvent event) {
		SERVER = event.getServer();
		LUCKPERMS = LuckPermsProvider.get();
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		List<String> commandsToRemove = new ArrayList<>();
		commandsToRemove.add("enchant");
		commandsToRemove.add("clear");
		commandsToRemove.add("xp");
		commandsToRemove.add("experience");
		commandsToRemove.add("gamemode");
		commandsToRemove.add("give");
		commandsToRemove.add("help");
		commandsToRemove.add("kick");
		commandsToRemove.add("kill");
		commandsToRemove.add("list");
		commandsToRemove.add("bossbar");
		commandsToRemove.add("item");
		commandsToRemove.add("me");
		commandsToRemove.add("msg");
		commandsToRemove.add("tell");
		commandsToRemove.add("w");
		commandsToRemove.add("say");
		commandsToRemove.add("tp");
		commandsToRemove.add("teleport");
		commandsToRemove.add("time");
		commandsToRemove.add("weather");
		commandsToRemove.add("ban-ip");
		commandsToRemove.add("banlist");
		commandsToRemove.add("ban");
		commandsToRemove.add("pardon");
		commandsToRemove.add("pardon-ip");

		dispatcher.getRoot().getChildren().removeIf(commandNode -> commandsToRemove.contains(commandNode.getName()));

		if (RestartConfig.ENABLED.get()) {
			DirtRestartCommand.register(dispatcher);
		}

		if (RulesConfig.ENABLED.get()) {
			DirtRulesCommand.register(dispatcher);
			DirtRulesCommand.registerRulesAlias(dispatcher);
		}

		if (RtpConfig.ENABLED.get()) {
			RtpCommand.register(dispatcher);
		}

		if (RtpConfig.ENABLED_RTPC.get()) {
			RtpcCommand.register(dispatcher);
		}

		if (PlaytimeConfig.ENABLED.get()) {
			PlaytimeCommand.register(dispatcher);
		}

		// Chat Commands
		GlobalCommand.register(dispatcher);
		StaffCommand.register(dispatcher);

		// Essential Commands
		AfkCommand.register(dispatcher);
		BackCommand.register(dispatcher);
		BalCommand.register(dispatcher);
		BaltopCommand.register(dispatcher);
		BillCommand.register(dispatcher);
		BlockzapCommand.register(dispatcher);
		BreakCommand.register(dispatcher);
		BroadcastCommand.register(dispatcher);
		CartographytableCommand.register(dispatcher);
		ClearCommand.register(dispatcher);
		CraftCommand.register(dispatcher);
		CreatekitCommand.register(dispatcher);
		DelhomeCommand.register(dispatcher);
		DeletekitCommand.register(dispatcher);
		DelwarpCommand.register(dispatcher);
		EcoCommand.register(dispatcher);
		EditwarpiconCommand.register(dispatcher);
		EnchantCommand.register(dispatcher);
		EnderchestCommand.register(dispatcher);
		EntityzapCommand.register(dispatcher);

		FeedCommand.register(dispatcher);
		FlyCommand.register(dispatcher);
		GcCommand.register(dispatcher);
		GamemodeCommand.register(dispatcher);
		GodCommand.register(dispatcher);
		GrindstoneCommand.register(dispatcher);
		HatCommand.register(dispatcher);
		HealCommand.register(dispatcher);
		HelpCommand.register(dispatcher);
		HomebalanceCommand.register(dispatcher);
		HomeCommand.register(dispatcher);
		HomesCommand.register(dispatcher);
		ItemCommand.register(dispatcher);
		JoinmessageCommand.register(dispatcher);
		KillCommand.register(dispatcher);
		KitCommand.register(dispatcher);
		KitsCommand.register(dispatcher);
		LeavemessageCommand.register(dispatcher);
		ListautobroadcastsCommand.register(dispatcher);
		ListCommand.register(dispatcher);
		LoomCommand.register(dispatcher);
		MsgCommand.register(dispatcher);
		NbtCommand.register(dispatcher);
		NickCommand.register(dispatcher);
		NoteCommand.register(dispatcher);
		OtherhomeCommand.register(dispatcher);
		PayCommand.register(dispatcher);
		RadiuszapCommand.register(dispatcher);
		RepairCommand.register(dispatcher);
		ReplyCommand.register(dispatcher);
		ResetPlayerDataCommand.register(dispatcher);
		RespectCommand.register(dispatcher);
		SeenCommand.register(dispatcher);
		SethomeCommand.register(dispatcher);

		SetwarpCommand.register(dispatcher);

		ShowkitCommand.register(dispatcher);

		WarpCommand.register(dispatcher);
		WarpsCommand.register(dispatcher);
	}

	private void enableFeatures() {
		if (EssentialsConfig.COMMAND_LISTENER_ENABLED.get()) {
			MinecraftForge.EVENT_BUS.addListener(OnCommand::event);
			LOGGER.info("» Initialized Command Listener.");
		}

		if (RestartConfig.ENABLED.get()) {
			MinecraftForge.EVENT_BUS.addListener(RestartManager::tick);
			LOGGER.info("» Restart feature enabled.");
		}

		if (RulesConfig.ENABLED.get()) {
			RulesManager.reminderInterval = RulesConfig.REMINDER_INTERVAL.get();
			MinecraftForge.EVENT_BUS.addListener(RulesManager::tick);
			LOGGER.info("» Rules feature enabled.");
		}

		if (RtpConfig.ENABLED.get()) {
			LOGGER.info("» RTP feature enabled.");
		}

		if (RtpConfig.ENABLED_RTPC.get()) {
			LOGGER.info("» RTPC feature enabled.");
		}

		if (SpamFixConfig.ENABLED.get()) {
			JavaFilter.applyFilter();
			Log4jFilter.applyFilter();
			SystemFilter.applyFilter();
			LOGGER.info("» SpamFix feature enabled.");
		}

		if (PlaytimeConfig.ENABLED.get()) {
			MinecraftForge.EVENT_BUS.addListener(PlaytimeManager::tick);
			PlaytimeManager.init();
			LOGGER.info("» Playtime feature enabled.");
		}

		LOGGER.info("» Essentials feature enabled.");
		MinecraftForge.EVENT_BUS.addListener(AfkManager::tick);
		MinecraftForge.EVENT_BUS.addListener(AfkManager::onPlayerJoin);
		MinecraftForge.EVENT_BUS.addListener(AfkManager::onChat);
		MinecraftForge.EVENT_BUS.addListener(AfkManager::onPlayerLeave);
		MinecraftForge.EVENT_BUS.addListener(AfkManager::onPlayerMove);
		MinecraftForge.EVENT_BUS.addListener(BackManager::teleportEvent);
		MinecraftForge.EVENT_BUS.addListener(BackManager::entityDeathEvent);
		MinecraftForge.EVENT_BUS.addListener(BackManager::teleportCommandEvent);
		MinecraftForge.EVENT_BUS.addListener(BackManager::spreadCommandEvent);
		MinecraftForge.EVENT_BUS.addListener(GodManager::onLivingHurt);
		MinecraftForge.EVENT_BUS.addListener(GodManager::onLivingAttack);
	}
}
