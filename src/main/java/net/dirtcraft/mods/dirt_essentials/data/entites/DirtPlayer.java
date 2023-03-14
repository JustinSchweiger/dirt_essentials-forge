package net.dirtcraft.mods.dirt_essentials.data.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.Location;
import net.dirtcraft.mods.dirt_essentials.economy.backend.transaction.Transaction;
import net.dirtcraft.mods.dirt_essentials.economy.backend.user.User;
import net.dirtcraft.mods.dirt_essentials.manager.PlaytimeManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
public class DirtPlayer implements User {
	@Id
	@Getter
	private UUID uuid;

	@Setter
	private String username;

	@Getter
	@Setter
	private boolean rulesAccepted;

	@Getter
	@Setter
	private boolean isStaff;

	private double balance;

	@Getter
	@Setter
	private long timePlayed;

	@Getter
	@Setter
	private long timesJoined;

	@Getter
	@Setter
	private LocalDateTime firstJoined;

	@Getter
	@Setter
	private LocalDateTime lastJoined;

	@Getter
	@Setter
	private LocalDateTime leaveDate;

	@Getter
	@Setter
	private String currentPath;

	@OneToMany
	@Getter
	private List<KitTracker> kitTrackers;

	@OneToMany
	@Getter
	private List<Home> homes;

	@Getter
	@Setter
	private int homeAmount;

	@Getter
	@Setter
	private boolean flyingWhenLoggedOut;

	@Getter
	@Setter
	private boolean godModeEnabled;

	@Getter
	@Setter
	private String customJoinMessage;

	@Getter
	@Setter
	private String customLeaveMessage;

	@Getter
	@OneToMany
	private List<Note> notes;

	@Getter
	@Setter
	private String lastKnownIp;

	@Getter
	@Setter
	private String lastKnownDimension;

	private String lastKnownLocation;

	@Getter
	@Setter
	private boolean socialSpyEnabled;

	@Getter
	@Setter
	private boolean teleportToSpawnOnNextLogin;

	@Getter
	@Setter
	private boolean autobroadcastsDisabled;

	@Getter
	@Setter
	@Column(columnDefinition = "boolean default false")
	private boolean tpDisabled;

	public DirtPlayer() {}

	public DirtPlayer(UUID uuid) {
		this.uuid = uuid;
		this.username = "Unknown";
		this.rulesAccepted = false;
		this.isStaff = false;
		this.balance = EssentialsConfig.DEFAULT_BALANCE.get();
		this.timePlayed = 0;
		this.timesJoined = 0;
		this.firstJoined = LocalDateTime.now();
		this.lastJoined = null;
		this.leaveDate = null;
		this.currentPath = PlaytimeManager.getFirstRank().getName();
		this.kitTrackers = new ArrayList<>();
		this.homes = new ArrayList<>();
		this.homeAmount = EssentialsConfig.HOMES_SIZE.get();
		this.flyingWhenLoggedOut = false;
		this.godModeEnabled = false;
		this.customJoinMessage = "";
		this.customLeaveMessage = "";
		this.notes = new ArrayList<>();
		this.lastKnownIp = "";
		this.lastKnownDimension = "";
		this.lastKnownLocation = "";
		this.socialSpyEnabled = false;
		this.teleportToSpawnOnNextLogin = false;
		this.autobroadcastsDisabled = false;
		this.tpDisabled = false;
	}

	public void setLastKnownLocation(Location location) {
		this.lastKnownLocation = String.format("%s,%s,%s,%s,%s,%s,%s", location.getLevel().registry(), location.getLevel().location(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	public Location getLastKnownLocation() {
		if (lastKnownLocation.isEmpty()) return null;
		String[] split = lastKnownLocation.split(",");
		return new Location(
				ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation(split[0])), new ResourceLocation(split[1])),
				Double.parseDouble(split[2]),
				Double.parseDouble(split[3]),
				Double.parseDouble(split[4]),
				Float.parseFloat(split[5]),
				Float.parseFloat(split[6])
		);
	}

	public static @NonNull DirtPlayer get(Player player) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
			if (dirtPlayer == null) {
				dirtPlayer = new DirtPlayer(player.getUUID());
				session.persist(dirtPlayer);
			}
			session.getTransaction().commit();

			return dirtPlayer;
		}
	}

	public static @NonNull DirtPlayer get(UUID uuid) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, uuid);
			if (dirtPlayer == null) {
				dirtPlayer = new DirtPlayer(uuid);
				session.persist(dirtPlayer);
			}
			session.getTransaction().commit();

			return dirtPlayer;
		}
	}

	public static String getFormattedBalance(double balance) {
		return "§a" + String.format(Locale.US, "%.2f", balance) + "§e" + EssentialsConfig.ECONOMY_CHARACTER.get();
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public double getBalance() {
		return balance;
	}

	@Override
	public String getFormattedBalance() {
		return "§a" + String.format(Locale.US, "%.2f", balance) + "§e" + EssentialsConfig.ECONOMY_CHARACTER.get();
	}

	@Override
	public boolean hasAmount(double amount) {
		return balance >= amount;
	}

	@Override
	public Transaction.Response resetBalance() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

			balance = EssentialsConfig.DEFAULT_BALANCE.get();
			session.merge(this);
			session.getTransaction().commit();
			return Transaction.Response.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return Transaction.Response.FAIL;
		}
	}

	@Override
	public Transaction.Response setBalance(double amount) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

			if (amount < 0) {
				balance = 0;
			} else {
				balance = amount;
			}
			session.merge(this);
			session.getTransaction().commit();
			return Transaction.Response.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return Transaction.Response.FAIL;
		}
	}

	@Override
	public Transaction.Response depositMoney(double amount) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

			balance += amount;
			session.merge(this);
			session.getTransaction().commit();
			return Transaction.Response.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return Transaction.Response.FAIL;
		}
	}

	@Override
	public Transaction.Response withdrawMoney(double amount) {
		if (!hasAmount(amount)) {
			return Transaction.Response.INSUFFICIENT_FUNDS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

			balance -= amount;
			session.merge(this);
			session.getTransaction().commit();
			return Transaction.Response.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return Transaction.Response.FAIL;
		}
	}

	public Home getHome(String name) {
		for (Home home : homes) {
			if (home.getName().equals(name)) {
				return home;
			}
		}
		return null;
	}

	public void removeHome(Home home) {
		for (Home h : homes) {
			if (h.getName().equals(home.getName())) {
				homes.remove(h);
				return;
			}
		}
	}
}
