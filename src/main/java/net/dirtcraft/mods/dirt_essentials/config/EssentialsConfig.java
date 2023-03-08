package net.dirtcraft.mods.dirt_essentials.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class EssentialsConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Double> DEFAULT_BALANCE;

	static  {
		BUILDER.comment("Config for Dirt Essentials");
		BUILDER.push("Economy");

		DEFAULT_BALANCE = BUILDER
				.comment("#.", "The default balance for new players")
				.defineInRange("defaultBalance", 1000.0, 0.0, Double.MAX_VALUE);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
