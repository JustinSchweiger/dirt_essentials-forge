package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.data.entites.Note;
import net.dirtcraft.mods.dirt_essentials.manager.NoteManager;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import org.hibernate.Session;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class NoteCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("note")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.NOTE))
				.then(Commands.literal("add")
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder))
								.then(Commands.argument("note", StringArgumentType.greedyString())
										.executes(context -> addNote(context, PlayerManager.getUuid(StringArgumentType.getString(context, "player")), StringArgumentType.getString(context, "note"))))))
				.then(Commands.literal("list")
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder))
								.executes(context -> listNotes(context, PlayerManager.getUuid(StringArgumentType.getString(context, "player"))))))
				.then(Commands.literal("remove")
						.then(Commands.argument("player", StringArgumentType.word())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder))
								.then(Commands.argument("index", StringArgumentType.word())
										.suggests((context, builder) -> SharedSuggestionProvider.suggest(NoteManager.getNoteIndices(PlayerManager.getUuid(StringArgumentType.getString(context, "player"))), builder))
										.executes(context -> removeNote(context, PlayerManager.getUuid(StringArgumentType.getString(context, "player")), StringArgumentType.getString(context, "index")))))));
	}

	private static int listNotes(CommandContext<CommandSourceStack> context, UUID player) {
		CommandSourceStack source = context.getSource();
		if (player == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThat player does not exist!"));
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player);
			if (dirtPlayer.getNotes().size() == 0) {
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThat player has no notes!"), false);
				return Command.SINGLE_SUCCESS;
			}

			source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aNotes for §6" + PlayerManager.getUsername(player) + "§7:"), false);
			source.sendSuccess(new TextComponent(""), false);
			dirtPlayer.getNotes().forEach(note -> {
				TextComponent noteComponent = new TextComponent("§7[§d" + note.getId() + "§7]§a: §6" + note.getNote());
				noteComponent.setStyle(noteComponent.getStyle()
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§6Added by§7: " + note.getAddedBy() + "\n§6Added on§7: §3" + note.getAdded().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " §7at §e" + note.getAdded().format(DateTimeFormatter.ofPattern("HH:mm"))))));

				TextComponent removeComponent = new TextComponent("  §7[§c✕§7] ");
				removeComponent.setStyle(removeComponent.getStyle()
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§cClick to remove this note!")))
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/note remove " + PlayerManager.getUsername(player) + " " + note.getId())));

				source.sendSuccess(removeComponent.append(noteComponent), false);
			});
			source.sendSuccess(new TextComponent(""), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static int removeNote(CommandContext<CommandSourceStack> context, UUID player, String index) {
		CommandSourceStack source = context.getSource();
		if (player == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThat player does not exist!"));
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player);
			Note note = dirtPlayer.getNotes().stream().filter(n -> n.getId() == Integer.parseInt(index)).findFirst().orElse(null);
			if (note == null) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThat note does not exist!"));
				return Command.SINGLE_SUCCESS;
			}

			dirtPlayer.getNotes().remove(note);

			session.beginTransaction();
			session.remove(note);
			session.persist(dirtPlayer);
			session.getTransaction().commit();
		}

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSuccessfully removed note §d" + index + " §afrom §6" + PlayerManager.getUsername(player) + "§a!"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int addNote(CommandContext<CommandSourceStack> context, UUID player, String noteString) {
		CommandSourceStack source = context.getSource();
		if (player == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cThat player does not exist!"));
			return Command.SINGLE_SUCCESS;
		}

		String addedBy = source.getEntity() == null ? "§4CONSOLE" : "§4" + source.getEntity().getName().getString();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player);
			Note note = new Note(dirtPlayer, "§7" + noteString, addedBy);
			dirtPlayer.getNotes().add(note);

			session.beginTransaction();
			session.persist(note);
			session.persist(dirtPlayer);
			session.getTransaction().commit();
		}

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aSuccessfully added note to §6" + PlayerManager.getUsername(player) + "§a!"), false);
		return Command.SINGLE_SUCCESS;
	}
}
