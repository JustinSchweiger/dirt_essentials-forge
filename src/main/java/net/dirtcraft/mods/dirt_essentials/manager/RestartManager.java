package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.Main;
import net.dirtcraft.mods.dirt_essentials.config.RestartConfig;
import net.dirtcraft.mods.dirt_essentials.data.TimeUnit;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class RestartManager {
	private static boolean running = false;
	private static long restartTime;
	private static long remainingTime;
	private static boolean isPaused = false;
	private static int tickCounter = 0;
	private static final List<String> notificationsSent = new ArrayList<>();

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event) {
		if (!running)
			return;

		if (Main.SERVER == null)
			return;

		if (tickCounter != 20) {
			tickCounter++;
			return;
		}
		tickCounter = 0;

		if (getTime() >= restartTime) {
			running = false;
			restart();
		}

		switch ((int) getTimeLeft()) {
			case 1800 -> notifyPlayers("30 minutes");
			case 900 -> notifyPlayers("15 minutes");
			case 600 -> notifyPlayers("10 minutes");
			case 300 -> notifyPlayers("5 minutes");
			case 180 -> notifyPlayers("3 minutes");
			case 60 -> notifyPlayers("1 minute");
			case 30 -> notifyPlayers("30 seconds");
			case 10 -> notifyPlayers("10 seconds");
			case 5 -> notifyPlayers("5 seconds");
			case 4 -> notifyPlayers("4 seconds");
			case 3 -> notifyPlayers("3 seconds");
			case 2 -> notifyPlayers("2 seconds");
			case 1 -> notifyPlayers("1 second");
		}
	}

	private static void notifyPlayers(String timeLeft) {
		if (notificationsSent.contains(timeLeft))
			return;

		notificationsSent.add(timeLeft);

		for (ServerPlayer player : Main.SERVER.getPlayerList().getPlayers()) {
			player.sendMessage(new TextComponent(Strings.RESTART_PREFIX + "§b⌚ §c" + timeLeft + "§7!"), Util.NIL_UUID);
			player.connection.send(new ClientboundCustomSoundPacket(new ResourceLocation("minecraft:block.note_block.bell"), SoundSource.AMBIENT, new Vec3(player.getX(), player.getY(), player.getZ()), 1.0F, 1.0F));
			player.connection.send(new ClientboundSetTitleTextPacket(new TextComponent("§7Server restart in")));
			player.connection.send(new ClientboundSetSubtitleTextPacket(new TextComponent("§c" + timeLeft + "§7!")));
			player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 10));
		}
	}

	public static void startTimer() {
		int interval = RestartConfig.RESTART_INTERVAL.get();
		TimeUnit unit = RestartConfig.RESTART_TIME_UNIT.get();

		restartTime = getTime() + TimeUnit.convertToSeconds(interval, unit);
		notificationsSent.clear();
		running = true;
	}

	public static void startTimer(int interval, TimeUnit unit) {
		restartTime = getTime() + TimeUnit.convertToSeconds(interval, unit);
		notificationsSent.clear();
		running = true;
		isPaused = false;
	}

	public static void restart() {
		for (ServerPlayer player : Main.SERVER.getPlayerList().getPlayers()) {
			player.connection.disconnect(new TextComponent(RestartConfig.RESTART_MESSAGE.get()));
		}

		Main.SERVER.halt(false);
	}

	public static void pauseTimer() {
		running = false;
		remainingTime = restartTime - getTime();
		isPaused = true;
	}

	public static void resumeTimer() {
		isPaused = false;
		restartTime = getTime() + remainingTime;
		running = true;
	}

	public static boolean isPaused() {
		return isPaused;
	}

	public static long getTimeLeft() {
		return restartTime - getTime();
	}

	private static long getTime() {
		return System.currentTimeMillis() / 1000L;
	}
}
