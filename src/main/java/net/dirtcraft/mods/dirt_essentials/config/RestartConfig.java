package net.dirtcraft.mods.dirt_essentials.config;

import net.dirtcraft.mods.dirt_essentials.data.TimeUnit;
import net.minecraftforge.common.ForgeConfigSpec;

public class RestartConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLED;
	public static final ForgeConfigSpec.ConfigValue<Integer> RESTART_INTERVAL;
	public static final ForgeConfigSpec.ConfigValue<TimeUnit> RESTART_TIME_UNIT;
	public static final ForgeConfigSpec.ConfigValue<String> RESTART_MESSAGE;

	static  {
		BUILDER.push("Config for Dirt Restart");
		BUILDER.comment();
		ENABLED = BUILDER
				.comment("Whether or not the restart feature is enabled.")
				.define("enabled", true);
		BUILDER.comment();
		RESTART_INTERVAL = BUILDER
				.comment("The interval at which the server will restart.")
				.define("restartInterval", 6);
		BUILDER.comment();
		RESTART_TIME_UNIT = BUILDER
				.comment("The time unit for the restart interval.")
				.defineEnum("restartTimeUnit", TimeUnit.HOURS);
		BUILDER.comment();
		RESTART_MESSAGE = BUILDER
				.comment("The message that will be displayed to players when the server restarts.")
				.define("restartMessage", "§cThe server will now restart!\n§7This should only take a few minutes.");

		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
