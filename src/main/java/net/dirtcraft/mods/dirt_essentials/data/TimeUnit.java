package net.dirtcraft.mods.dirt_essentials.data;

public enum TimeUnit {
	SECONDS,
	MINUTES,
	HOURS;

	public static long convertToSeconds(int time, TimeUnit unit) {
		return switch (unit) {
			case SECONDS -> time;
			case MINUTES -> time * 60L;
			case HOURS -> time * 60L * 60L;
		};
	}
}
