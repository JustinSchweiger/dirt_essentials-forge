package net.dirtcraft.mods.dirt_essentials.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class EssentialsConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Double> DEFAULT_BALANCE;
	public static final ForgeConfigSpec.ConfigValue<Boolean> STAFF_JOIN_FULL;
	public static final ForgeConfigSpec.ConfigValue<Integer> MAX_PLAYERS;
	public static final ForgeConfigSpec.ConfigValue<String> FULL_MESSAGE;
	public static final ForgeConfigSpec.ConfigValue<String> BROADCAST_PREFIX;
	public static final ForgeConfigSpec.ConfigValue<String> NICKNAME_PREFIX;
	public static final ForgeConfigSpec.ConfigValue<String> ECONOMY_CHARACTER;
	public static final ForgeConfigSpec.ConfigValue<Integer> BALTOP_SIZE;
	public static final ForgeConfigSpec.ConfigValue<Integer> AFK_TIME;
	public static final ForgeConfigSpec.ConfigValue<Integer> AFK_KICK_TIME;
	public static final ForgeConfigSpec.ConfigValue<Boolean> AFK_KICK;
	public static final ForgeConfigSpec.ConfigValue<String> AFK_KICK_MESSAGE;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BACK_WORLDS;
	public static final ForgeConfigSpec.ConfigValue<Boolean> BACK_WORLDS_WHITELIST;
	public static final ForgeConfigSpec.ConfigValue<Integer> DEFAULT_HOME_LIMIT;
	public static final ForgeConfigSpec.ConfigValue<Integer> HOME_COOLDOWN;
	public static final ForgeConfigSpec.ConfigValue<Integer> HOMES_SIZE;
	public static final ForgeConfigSpec.ConfigValue<Boolean> COMMAND_LISTENER_ENABLED;


	static  {
		BUILDER.comment("Config for Dirt Essentials");
		BUILDER.push("General");

		BROADCAST_PREFIX = BUILDER
				.comment(".", "The prefix to use for broadcasts")
				.define("broadcastPrefix", "  §c§lDirtCraft §8» §7");

		NICKNAME_PREFIX = BUILDER
				.comment(".", "The prefix character to use for nicknames")
				.define("nicknamePrefix", "~");

		COMMAND_LISTENER_ENABLED = BUILDER
				.comment(".", "Whether the command listener should be enabled")
				.define("commandListenerEnabled", true);

		BUILDER.pop();
		BUILDER.push("Afk");

		AFK_TIME = BUILDER
				.comment(".", "After how many seconds of inactivity should a player be marked as AFK")
				.defineInRange("afkTime", 300, 0, Integer.MAX_VALUE);

		AFK_KICK = BUILDER
				.comment(".", "Whether players should be kicked when they are AFK for too long")
				.define("afkKick", true);

		AFK_KICK_TIME = BUILDER
				.comment(".", "After how many seconds of AFK should a player be kicked", "This is after they are marked as AFK")
				.defineInRange("afkKickTime", 300, 0, Integer.MAX_VALUE);

		AFK_KICK_MESSAGE = BUILDER
				.comment(".", "The message to display when a player is kicked for being AFK for too long")
				.define("afkKickMessage", "§cYou have been kicked for being AFK for too long!");

		BUILDER.pop();
		BUILDER.push("Back");

		BACK_WORLDS = BUILDER
				.comment(".", "The list of worlds that players can use /back in")
				.defineList("backWorlds", List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"), o -> o instanceof String);

		BACK_WORLDS_WHITELIST = BUILDER
				.comment(".", "Whether the list of worlds is a whitelist or a blacklist")
				.define("backWorldsWhitelist", true);

		BUILDER.pop();
		BUILDER.push("Connection");

		MAX_PLAYERS = BUILDER
				.comment(".", "The maximum amount of players allowed on the server", "Set the limit in server.properties to 999!")
				.defineInRange("maxPlayers", 999, 1, 999);

		STAFF_JOIN_FULL = BUILDER
				.comment(".", "Whether staff players can join the server when it is full")
				.define("staffJoinFull", true);

		FULL_MESSAGE = BUILDER
				.comment(".", "The message to display when the server is full")
				.define("fullMessage", "§4The Server is currently full!\n\n§cPlease try again later!");

		BUILDER.pop();
		BUILDER.push("Economy");

		DEFAULT_BALANCE = BUILDER
				.comment(".", "The default balance for new players")
				.defineInRange("defaultBalance", 1000.0, 0.0, Double.POSITIVE_INFINITY);

		ECONOMY_CHARACTER = BUILDER
				.comment(".", "The character to use for the economy")
				.define("economyCharacter", "$");

		BALTOP_SIZE = BUILDER
				.comment(".", "The amount of players to display in /baltop")
				.defineInRange("baltopSize", 10, 1, 20);

		BUILDER.pop();
		BUILDER.push("Home");

		DEFAULT_HOME_LIMIT = BUILDER
				.comment(".", "The default amount of homes a player can have")
				.defineInRange("defaultHomeLimit", 1, 1, 999);

		HOME_COOLDOWN = BUILDER
				.comment(".", "The cooldown in seconds for /home")
				.defineInRange("homeCooldown", 60, 0, Integer.MAX_VALUE);

		HOMES_SIZE = BUILDER
				.comment(".", "The amount of homes to display in /homes")
				.defineInRange("homesSize", 10, 1, 20);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
