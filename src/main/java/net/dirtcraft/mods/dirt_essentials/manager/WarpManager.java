package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.Warp;
import org.hibernate.Session;

import java.util.List;

public class WarpManager {
	public static List<Warp> getWarps() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.createQuery("from Warp", Warp.class).list();
		}
	}
}
