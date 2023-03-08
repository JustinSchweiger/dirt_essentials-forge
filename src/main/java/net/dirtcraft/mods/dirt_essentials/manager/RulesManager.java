package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.data.entites.Rule;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RulesManager {
	private static int tickCounter = 0;
	public static int reminderInterval = 1;
	public static int getRulesCount() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.createQuery("SELECT COUNT(*) FROM Rule", Number.class).getSingleResult().intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}

	public static Map<Integer, Rule> getRules() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			List<Rule> rules = session.createQuery("FROM Rule", Rule.class).list();
			Map<Integer, Rule> ruleMap = new HashMap<>();
			for (int i = 0; i < rules.size(); i++) {
				ruleMap.put(i, rules.get(i));
			}
			return ruleMap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void acceptRules(ServerPlayer player) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
			dirtPlayer.setRulesAccepted(true);
			session.merge(dirtPlayer);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void rulesChanged() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			session.createQuery("UPDATE DirtPlayer SET rulesAccepted = false", null).executeUpdate();
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event) {
		if (DirtEssentials.SERVER == null)
			return;

		if (tickCounter != reminderInterval * 20) {
			tickCounter++;
			return;
		}
		tickCounter = 0;

		if (getRulesCount() == 0) return;

		List<ServerPlayer> onlinePlayers = DirtEssentials.SERVER.getPlayerList().getPlayers();
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			List<DirtPlayer> players = session.createQuery("FROM DirtPlayer WHERE rulesAccepted = false", DirtPlayer.class).list();
			for (DirtPlayer player : players) {
				for (ServerPlayer onlinePlayer : onlinePlayers) {
					if (onlinePlayer.getUUID().equals(player.getUuid())) {
						onlinePlayer.sendMessage(new TextComponent(Strings.RULES_PREFIX + "§cRemember to accept the rules using §4/rules§c!"), Util.NIL_UUID);
						onlinePlayer.connection.send(new ClientboundCustomSoundPacket(new ResourceLocation("minecraft:block.anvil.place"), SoundSource.PLAYERS, new Vec3(onlinePlayer.getX(), onlinePlayer.getY(), onlinePlayer.getZ()), 1.0f, 1.0f));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
