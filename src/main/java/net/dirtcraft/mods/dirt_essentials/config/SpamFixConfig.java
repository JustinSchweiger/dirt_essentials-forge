package net.dirtcraft.mods.dirt_essentials.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class SpamFixConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLED;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MESSAGES_TO_FILTER;

	static  {
		BUILDER.push("Config for Dirt Spam Fix");
		ENABLED = BUILDER
				.comment(".", "Whether or not the spam fix feature is enabled.")
				.define("enabled", true);

		MESSAGES_TO_FILTER = BUILDER
				.comment(".", "Messages to filter from the console. You dont have to specify the entire message, just the part that is unique to it.")
				.defineList("messagesToFilter", ArrayList::new, o -> o instanceof String);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
