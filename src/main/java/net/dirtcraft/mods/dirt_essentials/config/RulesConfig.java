package net.dirtcraft.mods.dirt_essentials.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RulesConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLED;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ACCEPT_AGAIN_ON_CHANGE;
	public static final ForgeConfigSpec.ConfigValue<Integer> REMINDER_INTERVAL;

	static  {
		BUILDER.comment("Config for Dirt Rules");
		BUILDER.push("general");

		ENABLED = BUILDER
				.comment("#.", "Whether or not the restart feature is enabled.")
				.define("enabled", true);

		ACCEPT_AGAIN_ON_CHANGE = BUILDER
				.comment("#.", "Whether or not players will have to accept the rules again if they change.")
				.define("acceptAgainOnChange", true);

		REMINDER_INTERVAL = BUILDER
				.comment("#.", "How often the rules reminder will be sent to players in seconds.")
				.defineInRange("reminderInterval", 30, 10, 1800);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
