package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.minecraft.server.level.ServerLevel;

import java.util.Locale;

public class GcManager {
	private static long startTime;
	private static final long[] UNLOADED = new long[] {0};

	public static void init() {
		startTime = System.currentTimeMillis();
	}

	public static float getUsedMemory() {
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024F / 1024F;
	}

	public static float getMaxMemory() {
		return Runtime.getRuntime().maxMemory() / 1024F / 1024F;
	}

	public static double getOverallTps() {
		double meanTickTime = mean(DirtEssentials.SERVER.tickTimes) * 1.0E-6D;

		return Math.min(1000.0/meanTickTime, 20);
	}

	public static double getOverallTickTime() {
		return mean(DirtEssentials.SERVER.tickTimes) * 1.0E-6D;
	}

	public static double getTps(ServerLevel level) {
		long[] times = DirtEssentials.SERVER.getTickTime(level.dimension());

		if (times == null)
			times = UNLOADED;

		double worldTickTime = mean(times) * 1.0E-6D;

		return Math.min(1000.0 / worldTickTime, 20);
	}

	public static double getMeanTickTime(ServerLevel level) {
		long[] times = DirtEssentials.SERVER.getTickTime(level.dimension());

		if (times == null)
			times = UNLOADED;

		return mean(times) * 1.0E-6D;
	}

	private static long mean(long[] values)
	{
		long sum = 0L;
		for (long v : values)
			sum += v;
		return sum / values.length;
	}

	public static String getUptime() {
		long uptime = System.currentTimeMillis() - startTime;
		long hours = (uptime % 86400000) / 3600000;
		long minutes = (uptime % 3600000) / 60000;
		long seconds = (uptime % 60000) / 1000;

		StringBuilder builder = new StringBuilder();
		if (hours > 1) builder.append("§b").append(hours).append(" §7hours ");
		if (hours == 1) builder.append("§b").append(hours).append(" §7hour ");
		if (minutes > 1) builder.append("§b").append(minutes).append(" §7minutes ");
		if (minutes == 1) builder.append("§b").append(minutes).append(" §7minute ");
		if (seconds > 1) builder.append("§b").append(seconds).append(" §7seconds");
		if (seconds == 1) builder.append("§b").append(seconds).append(" §7second");

		return builder.toString();
	}

	public static String formatTps(double tps) {
		String tpsString;
		if (tps >= 18)
			tpsString = String.format(Locale.US, "§a%.2f", tps);
		else if (tps >= 14)
			tpsString = String.format(Locale.US, "§e%.2f", tps);
		else
			tpsString = String.format(Locale.US, "§c%.2f", tps);

		return tpsString;
	}

	public static String formatTickTime(double tickTime) {
		String tickTimeString;
		if (tickTime <= 50)
			tickTimeString = String.format(Locale.US, "§a%.3f §7ms", tickTime);
		else if (tickTime <= 75)
			tickTimeString = String.format(Locale.US, "§e%.3f §7ms", tickTime);
		else
			tickTimeString = String.format(Locale.US, "§c%.3f §7ms", tickTime);

		return tickTimeString;
	}
}
