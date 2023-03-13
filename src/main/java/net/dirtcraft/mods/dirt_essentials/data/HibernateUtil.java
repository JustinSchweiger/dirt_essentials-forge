package net.dirtcraft.mods.dirt_essentials.data;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.data.entites.Home;
import net.dirtcraft.mods.dirt_essentials.data.entites.Kit;
import net.dirtcraft.mods.dirt_essentials.data.entites.KitTracker;
import net.dirtcraft.mods.dirt_essentials.data.entites.Note;
import net.dirtcraft.mods.dirt_essentials.data.entites.Rule;
import net.dirtcraft.mods.dirt_essentials.data.entites.Spawn;
import net.dirtcraft.mods.dirt_essentials.data.entites.Warp;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
	private static StandardServiceRegistry registry;
	private static SessionFactory sessionFactory;

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			try {
				Configuration configuration = new Configuration()
						.addAnnotatedClass(DirtPlayer.class)
						.addAnnotatedClass(Rule.class)
						.addAnnotatedClass(Kit.class)
						.addAnnotatedClass(KitTracker.class)
						.addAnnotatedClass(Home.class)
						.addAnnotatedClass(Warp.class)
						.addAnnotatedClass(Note.class)
						.addAnnotatedClass(Spawn.class)
						.setProperty("hibernate.connection.driver_class", "org.h2.Driver")
						.setProperty("hibernate.connection.url", "jdbc:h2:" + DirtEssentials.DIRT_MODS_DIR + "/dirt_essentials")
						.setProperty("hibernate.connection.pool_size", "5")
						.setProperty("show_sql", "true")
						.setProperty("hibernate.hbm2ddl.auto", "update")
						.setProperty("hibernate.connection.autocommit", "true");

				registry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
				sessionFactory = configuration.buildSessionFactory(registry);
			} catch (Exception e) {
				e.printStackTrace();
				if (registry != null) {
					StandardServiceRegistryBuilder.destroy(registry);
				}
			}
		}

		return sessionFactory;
	}
}
