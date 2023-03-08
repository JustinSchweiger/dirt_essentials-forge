package net.dirtcraft.mods.dirt_essentials.data.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.economy.backend.transaction.Transaction;
import net.dirtcraft.mods.dirt_essentials.economy.backend.user.User;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import org.hibernate.Session;

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

	public DirtPlayer() {}

	public DirtPlayer(UUID uuid) {
		this.uuid = uuid;
		this.username = "Unknown";
		this.rulesAccepted = false;
		this.isStaff = false;
		this.balance = EssentialsConfig.DEFAULT_BALANCE.get();
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

	@Override
	public String getDisplayName() {
		return username;
	}

	@Override
	public double getBalance() {
		return balance;
	}

	@Override
	public Component getFormattedBalance() {
		return new TextComponent("§a" + String.format("%.2f", balance) + "§e$");
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

			balance = amount;
			session.merge(this);
			session.getTransaction().commit();
			return Transaction.Response.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return Transaction.Response.FAIL;
		}
	}

	@Override
	public Transaction.Response sendTo(User user, double amount) {
		if (!hasAmount(amount)) {
			return Transaction.Response.INSUFFICIENT_FUNDS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

			balance -= amount;
			user.depositMoney(amount, "Money sent from " + getDisplayName());
			session.merge(this);
			session.getTransaction().commit();
			return Transaction.Response.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return Transaction.Response.FAIL;
		}
	}

	@Override
	public Transaction.Response depositMoney(double amount, String reason) {
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
	public Transaction.Response withdrawMoney(double amount, String reason) {
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
}
