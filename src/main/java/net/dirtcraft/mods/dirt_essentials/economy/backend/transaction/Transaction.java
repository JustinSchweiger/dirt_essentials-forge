package net.dirtcraft.mods.dirt_essentials.economy.backend.transaction;

import net.dirtcraft.mods.dirt_essentials.economy.backend.user.User;
import net.minecraft.world.entity.player.Player;

public interface Transaction {

	/**
	 * @return The {@link User} involved in the transaction
	 */
	User getUser();

	/**
	 * @return A message that can either be sent to the {@link Player} or used for logging purposes.
	 */
	String getMessage();

	/**
	 * @return return the {@link Response} of the transaction. Implementations can further add their own Responses.
	 */
	Response getTransactionResponse();

	/**
	 * @return return the {@link Type} of transaction. Implementations can further add their own Types.
	 */
	Type getTransactionType();

	interface Response {
		Response SUCCESS = () -> "success";
		Response FAIL = () -> "fail";
		Response INSUFFICIENT_FUNDS = () -> "insufficient_funds";

		String type();
	}

	interface Type {
		Type DEPOSIT = () -> "deposit";
		Type WITHDRAW = () -> "withdraw";
		Type SET = () -> "set";
		Type RESET = () -> "reset";
		Type SEND = () -> "send";

		String type();
	}
}
