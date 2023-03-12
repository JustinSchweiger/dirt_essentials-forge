package net.dirtcraft.mods.dirt_essentials.config;

import lombok.Getter;
import net.minecraft.network.chat.ClickEvent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
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
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HELP;
	public static final ForgeConfigSpec.ConfigValue<Integer> AUTOBROADCAST_DELAY;
	public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AUTOBROADCASTS;

	static {
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
		BUILDER.push("Help");

		HELP = BUILDER
				.comment(
						".",
						"The help entries to display in /help",
						"Each entry is defined by:",
						"  - name: The name of the help entry. This acts as a unique identifier.",
						"  - title: The title of the help entry. This is displayed in the help menu.",
						"  - lines: The lines of the help entry. Only supports plain text right now."
				)
				.defineList("helpEntries", List.of(
						new Help("home", "§6Homes",
								List.of(
										"§7Make sure to set your home using §3/sethome §7once you find a nice place to settle down!",
										"§7To return to your home you can use §3/home [name]§7.",
										"",
										"§7You can list/delete your homes using §3/homes §7or §3/delhome <name>§7."
								)
						).serialize()
				), o -> o instanceof String);

		BUILDER.pop();
		BUILDER.push("AutoBroadcast");

		AUTOBROADCAST_DELAY = BUILDER
				.comment(".", "The delay in seconds between each broadcast.")
				.defineInRange("autobroadcastDelay", 300, 0, Integer.MAX_VALUE);

		AUTOBROADCASTS = BUILDER
				.comment(
						".",
						"The automatic broadcasts to display.",
						"Each entry is defined by:",
						"  - clickEventAction: The action to execute when clicking on the message. Possible values are 'open_url', 'run_command', 'suggest_command' and 'copy_to_clipboard'.",
						"  - clickValue: The string to open/run/suggest/copy when clicking on the message.",
						"  - hoverEventText: The text to display when hovering over the message.",
						"  - lines: The lines of the message."
				)
				.defineList("helpEntries", List.of(
						new Autobroadcast(
								ClickEvent.Action.OPEN_URL,
								"https://discord.gg/dirtcraft",
								"§3Click to join our Discord!",
								List.of(
										"  ",
										"    §8< §3ℹ §8>",
										"  §7If you ever find yourself in need of §ehelp",
										"  §7and no §cstaff §7are online, you can always join",
										"  our §3Discord server §7and ask for help there!",
										"  ",
										"  §bIt's also a great place to meet other players and have fun!",
										"  "
								)
						).serialize()
				), o -> o instanceof String);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}

	public static class Autobroadcast {
		@Getter
		private final ClickEvent.Action action;

		@Getter
		private final String clickValue;

		@Getter
		private final String hoverEventText;

		@Getter
		private final List<String> lines;

		public Autobroadcast(ClickEvent.Action action, String clickValue, String hoverEventText, List<String> lines) {
			this.action = action;
			this.clickValue = clickValue;
			this.hoverEventText = hoverEventText;
			this.lines = lines;
		}

		public static Autobroadcast deserialize(String serialized) {
			String[] parts = serialized.split(";;;;");
			ClickEvent.Action action = null;

			if (!parts[0].equalsIgnoreCase(""))
				action = ClickEvent.Action.valueOf(parts[0]);

			String clickValue = parts[1];
			String hoverEventText = parts[2];
			List<String> lines = Arrays.asList(parts[3].split(";;"));

			return new Autobroadcast(action, clickValue, hoverEventText, lines);
		}

		public String serialize() {
			return action.name() + ";;;;" + clickValue + ";;;;" + hoverEventText + ";;;;" + String.join(";;", lines);
		}
	}

	public static class Help {
		@Getter
		private final String name;

		@Getter
		private final String title;

		@Getter
		private final List<String> lines;

		public Help(String name, String title, List<String> lines) {
			this.name = name;
			this.title = title;
			this.lines = lines;
		}

		public static Help deserialize(String serialized) {
			String[] parts = serialized.split(";;;;");
			String name = parts[0];
			String title = parts[1];
			List<String> lines = Arrays.asList(parts[2].split(";"));

			return new Help(name, title, lines);
		}

		public String serialize() {
			return name + ";;;;" + title + ";;;;" + String.join(";", lines);
		}

		public static Help get(String name) {
			return deserialize(HELP.get()
					.stream()
					.map(s -> (String) s)
					.filter(s -> s.startsWith(name + ";;;;"))
					.findFirst()
					.orElse(null));
		}
	}
}
