package net.dirtcraft.mods.dirt_essentials.config;

import lombok.Getter;
import net.dirtcraft.mods.dirt_essentials.data.TimeUnit;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class PlaytimeConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLED;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ANNOUNCE_RANKUPS_IN_CHAT;
	public static final ForgeConfigSpec.ConfigValue<Boolean> ANNOUNCE_RANKUPS_IN_TITLE;
	public static final ForgeConfigSpec.ConfigValue<Integer> LEADERBOARD_SIZE;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RANKS;

	static {
		BUILDER.comment("Config for Dirt Playtime");
		BUILDER.push("General");
		ENABLED = BUILDER
				.comment(".", "Whether or not the playtime feature is enabled.")
				.define("enabled", true);

		ANNOUNCE_RANKUPS_IN_CHAT = BUILDER
				.comment(".", "Whether or not to announce rankups in chat.")
				.define("announceRankupsInChat", true);

		ANNOUNCE_RANKUPS_IN_TITLE = BUILDER
				.comment(".", "Whether or not to announce rankups in title.")
				.define("announceRankupsInTitle", true);

		LEADERBOARD_SIZE = BUILDER
				.comment(".", "The size of the leaderboard.")
				.defineInRange("leaderboardSize", 10, 1, 20);

		BUILDER.pop();
		BUILDER.push("Ranks");
		RANKS = BUILDER
				.comment(
						".",
						"The ranks that will be given to players when the server restarts.",
						"Each rank is defined by:",
						"  - name: The name of the rank. Must be unique.",
						"  - displayName: The name of the rank that will be displayed in chat.",
						"  - prerequisite: The name of the rank that must be achieved before this rank can be achieved.",
						"  - nextRank: The name of the rank that will be achieved after this rank.",
						"  - time: The amount of time that must be played before this rank can be achieved.",
						"  - timeUnit: The unit of time that the time is measured in. Can be: SECONDS, MINUTES, HOURS",
						"  - money: The amount of money that will be given to the player when they achieve this rank.",
						"  - commands: A list of commands that will be executed when the player achieves this rank. Use {PLAYER} to refer to the player."
				)
				.defineList("ranks", List.of(
						new Rank("starter", "", "beginner", 0, TimeUnit.SECONDS, 0, List.of("")).serialize(),
						new Rank("beginner", "starter", "amateur", 30, TimeUnit.MINUTES, 500, List.of("")).serialize(),
						new Rank("amateur", "beginner", "citizen", 12, TimeUnit.HOURS, 1000, List.of("")).serialize(),
						new Rank("citizen", "amateur", "experienced", 24, TimeUnit.HOURS, 2000, List.of("")).serialize(),
						new Rank("experienced", "citizen", "master", 24 * 3, TimeUnit.HOURS, 4000, List.of("")).serialize(),
						new Rank("master", "experienced", "veteran", 24 * 5, TimeUnit.HOURS, 8000, List.of("")).serialize(),
						new Rank("veteran", "master", "", 24 * 7, TimeUnit.HOURS, 16000, List.of("")).serialize()
				), o -> o instanceof String);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}

	public static class Rank {
		@Getter private final String name;
		@Getter private final String prerequisite;
		@Getter private final String nextRank;
		@Getter private final int time;
		@Getter private final TimeUnit timeUnit;
		@Getter private final int money;
		@Getter private final List<String> commands;

		public Rank(String name, String prerequisite, String nextRank, int time, TimeUnit timeUnit, int money, List<String> commands) {
			this.name = name;
			this.prerequisite = prerequisite;
			this.nextRank = nextRank;
			this.time = time;
			this.timeUnit = timeUnit;
			this.money = money;
			this.commands = commands;
		}

		public String serialize() {
			return String.join(",",
					name,
					prerequisite,
					nextRank,
					String.valueOf(time),
					timeUnit.name(),
					String.valueOf(money),
					String.join(";", commands)
			);
		}

		public static Rank deserialize(String string) {
			String[] split = string.split(",");
			if (split.length == 6)
				return new Rank(
						split[0],
						split[1],
						split[2],
						Integer.parseInt(split[3]),
						TimeUnit.valueOf(split[4]),
						Integer.parseInt(split[5]),
						List.of()
				);

			return new Rank(
					split[0],
					split[1],
					split[2],
					Integer.parseInt(split[3]),
					TimeUnit.valueOf(split[4]),
					Integer.parseInt(split[5]),
					List.of(split[6].split(";"))
			);
		}

		public long getTimeRequirement() {
			return TimeUnit.convertToSeconds(time, timeUnit);
		}
	}
}
