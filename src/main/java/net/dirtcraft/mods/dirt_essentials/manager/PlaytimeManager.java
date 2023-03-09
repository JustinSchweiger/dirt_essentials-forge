package net.dirtcraft.mods.dirt_essentials.manager;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.config.PlaytimeConfig;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.hibernate.Session;

import java.util.LinkedHashSet;

public class PlaytimeManager {
	private static int tickCounter = 0;
	private static long lastTime = 0;
	private static boolean running = false;
	private static final LinkedHashSet<PlaytimeConfig.Rank> ranks = new LinkedHashSet<>();

	public static void init() {
		for (String rankString : PlaytimeConfig.RANKS.get()) {
			PlaytimeConfig.Rank rank = PlaytimeConfig.Rank.deserialize(rankString);
			ranks.add(rank);
		}

		lastTime = getTime();
		running = true;
	}

	public static PlaytimeConfig.Rank getFirstRank() {
		return ranks.stream().filter(rank -> rank.getPrerequisite().isEmpty()).findFirst().orElse(null);
	}

	public static PlaytimeConfig.Rank getRank(String name) {
		return ranks.stream().filter(rank -> rank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event) {
		if (!running)
			return;

		if (DirtEssentials.SERVER == null)
			return;

		if (tickCounter != 20) {
			tickCounter++;
			return;
		}
		tickCounter = 0;

		if (DirtEssentials.SERVER.getPlayerList().getPlayers().isEmpty())
			return;

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();
			for (ServerPlayer player : DirtEssentials.SERVER.getPlayerList().getPlayers()) {
				DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
				dirtPlayer.setTimePlayed(dirtPlayer.getTimePlayed() + (getTime() - lastTime));

				PlaytimeConfig.Rank currentRank = ranks.stream().filter(r -> r.getName().equalsIgnoreCase(dirtPlayer.getCurrentPath())).findFirst().orElse(null);
				PlaytimeConfig.Rank nextRank = ranks.stream().filter(r -> r.getPrerequisite().equalsIgnoreCase(currentRank.getName())).findFirst().orElse(null);

				if (nextRank == null)
					continue;

				if (dirtPlayer.getTimePlayed() >= nextRank.getTimeRequirement()) {
					if (PlaytimeConfig.ANNOUNCE_RANKUPS_IN_CHAT.get()) {
						DirtEssentials.SERVER.getPlayerList().broadcastMessage(
								new TextComponent(Strings.PLAYTIME_PREFIX + "§3" + dirtPlayer.getDisplayName() + " §7just ranked up to " + PermissionHandler.getGroupPrefix(nextRank.getName()) + "§7!"),
								ChatType.CHAT,
								Util.NIL_UUID
						);
					}

					if (PlaytimeConfig.ANNOUNCE_RANKUPS_IN_TITLE.get()) {
						player.connection.send(new ClientboundSetTitleTextPacket(new TextComponent("§bYou just ranked up to")));
						player.connection.send(new ClientboundSetSubtitleTextPacket(new TextComponent(PermissionHandler.getGroupPrefix(nextRank.getName()))));
						player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
					}

					player.connection.send(new ClientboundCustomSoundPacket(new ResourceLocation("minecraft:ui.toast.challenge_complete"), SoundSource.PLAYERS, new Vec3(player.getX(), player.getY(), player.getZ()), 1.0F, 1.0F));

					for (String command : nextRank.getCommands()) {
						DirtEssentials.SERVER.getCommands().performCommand(DirtEssentials.SERVER.createCommandSourceStack(), command.replace("{PLAYER}", player.getGameProfile().getName()));
					}

					if (nextRank.getMoney() > 0) {
						dirtPlayer.setBalance(dirtPlayer.getBalance() + nextRank.getMoney());
						player.sendMessage(new TextComponent(Strings.PLAYTIME_PREFIX + "§7You have been awarded " + DirtPlayer.getFormattedBalance(nextRank.getMoney()).getString() + "§7 for ranking up!"), Util.NIL_UUID);
					}

					dirtPlayer.setCurrentPath(nextRank.getName());
					DirtEssentials.SERVER.getCommands().performCommand(DirtEssentials.SERVER.createCommandSourceStack(), "lp user " + player.getGameProfile().getName() + " parent add " + nextRank.getName() + " " + DirtEssentials.LUCKPERMS.getServerName());

					if (!currentRank.getName().equals(getFirstRank().getName()))
						DirtEssentials.SERVER.getCommands().performCommand(DirtEssentials.SERVER.createCommandSourceStack(), "lp user " + player.getGameProfile().getName() + " parent remove " + currentRank.getName() + " " + DirtEssentials.LUCKPERMS.getServerName());
				}

				session.merge(dirtPlayer);
			}

			session.getTransaction().commit();
		}

		lastTime = getTime();
	}

	private static long getTime() {
		return System.currentTimeMillis() / 1000;
	}
}
