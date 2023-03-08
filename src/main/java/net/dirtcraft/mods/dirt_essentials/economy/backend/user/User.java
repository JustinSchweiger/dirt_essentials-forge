package net.dirtcraft.mods.dirt_essentials.economy.backend.user;

import net.dirtcraft.mods.dirt_essentials.economy.backend.transaction.Transaction;
import net.minecraft.network.chat.Component;

/**
 * The base user.
 */
public interface User {

	/**
	 * Should return a display name that is meant for end-user viewing.
	 * @return A display name for the User.
	 */
	String getDisplayName();
	double getBalance();
	Component getFormattedBalance();

	boolean hasAmount(double amount);

	Transaction.Response resetBalance();

	Transaction.Response setBalance(double amount);

	Transaction.Response sendTo(User user, double amount);

	Transaction.Response depositMoney(double amount, String reason);

	Transaction.Response withdrawMoney(double amount, String reason);
}
