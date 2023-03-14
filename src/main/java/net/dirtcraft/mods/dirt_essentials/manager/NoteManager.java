package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NoteManager {
	public static List<String> getNoteIndices(UUID player) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player);
			if (dirtPlayer == null) return new ArrayList<>();

			return dirtPlayer.getNotes().stream().map(note -> String.valueOf(note.getId())).toList();
		}
	}
}
