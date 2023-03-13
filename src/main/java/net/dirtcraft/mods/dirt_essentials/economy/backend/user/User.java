package net.dirtcraft.mods.dirt_essentials.economy.backend.user;

import net.dirtcraft.mods.dirt_essentials.economy.backend.transaction.Transaction;

/**
 * The base user.
 */
public interface User {
	String getUsername();

	double getBalance();

	String getFormattedBalance();

	boolean hasAmount(double amount);

	Transaction.Response resetBalance();

	Transaction.Response setBalance(double amount);

	Transaction.Response depositMoney(double amount);

	Transaction.Response withdrawMoney(double amount);
}
