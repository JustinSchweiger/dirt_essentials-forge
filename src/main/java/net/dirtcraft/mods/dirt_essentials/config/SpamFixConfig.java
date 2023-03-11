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
		BUILDER.comment("Config for Dirt Spam Fix");
		BUILDER.push("General");
		ENABLED = BUILDER
				.comment(".", "Whether or not the spam fix feature is enabled.")
				.define("enabled", true);

		MESSAGES_TO_FILTER = BUILDER
				.comment(".", "Messages to filter from the console. You dont have to specify the entire message, just the part that is unique to it.")
				.defineList("messagesToFilter", List.of("Ambiguity between arguments"), o -> o instanceof String);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
