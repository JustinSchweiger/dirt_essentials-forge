package net.dirtcraft.mods.dirt_essentials.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class RtpConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLED;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLED_RTPC;
	public static final ForgeConfigSpec.ConfigValue<Integer> RTP_COOLDOWN;
	public static final ForgeConfigSpec.ConfigValue<Integer> RTPC_COOLDOWN;
	public static final ForgeConfigSpec.ConfigValue<Integer> MAX_ATTEMPTS;
	public static final ForgeConfigSpec.ConfigValue<Integer> NETHER_ROOF;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WORLD_LIST;
	public static final ForgeConfigSpec.ConfigValue<Boolean> WORLD_LIST_TYPE;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_BIOMES;

	static {
		BUILDER.comment("Config for Dirt Rtp");
		BUILDER.push("General");

		ENABLED = BUILDER
				.comment(".", "Whether or not the rtp feature is enabled.")
				.define("enabled", true);

		ENABLED_RTPC = BUILDER
				.comment(".", "Whether or not the rtpc feature is enabled.", "RTPC is for rtp to chaos-islands from Draconic Evolution.")
				.define("enabledRtpc", true);

		RTP_COOLDOWN = BUILDER
				.comment(".", "The cooldown for the rtp command in seconds.")
				.defineInRange("rtpCooldown", 30, 0, 86400);

		RTPC_COOLDOWN = BUILDER
				.comment(".", "The cooldown for the rtpc command in seconds.")
				.defineInRange("rtpcCooldown", 1800, 0, 86400);

		MAX_ATTEMPTS = BUILDER
				.comment(".", "The maximum number of attempts to find a safe location.")
				.defineInRange("maxAttempts", 5, 1, 10);

		NETHER_ROOF = BUILDER
				.comment(".", "The maximum height that the rtp command can teleport you to in the nether.")
				.defineInRange("netherRoof", 128, 0, 255);

		BUILDER.pop();
		BUILDER.push("World");
		WORLD_LIST = BUILDER
				.comment(".", "The list of worlds that the rtp command is either blacklisted or whitelisted from.")
				.defineList("worlds", List.of("minecraft:dimension;;;minecraft:overworld"), o -> o instanceof String);

		WORLD_LIST_TYPE = BUILDER
				.comment(".", "Whether or not the world list is a blacklist or whitelist.")
				.define("whitelist", true);

		BLACKLISTED_BIOMES = BUILDER
				.comment(".", "The list of biomes that you cannot rtp to.")
				.defineList("blacklistedBiomes", List.of(
						"minecraft:cold_ocean",
						"minecraft:deep_cold_ocean",
						"minecraft:deep_frozen_ocean",
						"minecraft:deep_lukewarm_ocean",
						"minecraft:deep_ocean",
						"minecraft:deep_warm_ocean",
						"minecraft:frozen_ocean",
						"minecraft:lukewarm_ocean",
						"minecraft:ocean",
						"minecraft:warm_ocean",
						"minecraft:frozen_river",
						"minecraft:river"
				), o -> o instanceof String);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
