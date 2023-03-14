package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.dirtcraft.mods.dirt_essentials.database.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.hibernate.Session;

import java.util.UUID;

public class PayCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("pay")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.PAY))
				.then(Commands.argument("player", StringArgumentType.word())
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder))
						.then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
								.executes(PayCommand::execute))));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		UUID target = PlayerManager.getUuid(StringArgumentType.getString(commandSourceStackCommandContext, "player"));
		double amount = DoubleArgumentType.getDouble(commandSourceStackCommandContext, "amount");

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, player.getUUID());
			DirtPlayer dirtTarget = session.get(DirtPlayer.class, target);

			if (!dirtPlayer.hasAmount(amount)) {
				source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou do not have enough money to do that!"));
				return Command.SINGLE_SUCCESS;
			}

			dirtPlayer.withdrawMoney(amount);
			dirtTarget.depositMoney(amount);
			session.beginTransaction();
			session.persist(dirtPlayer);
			session.persist(dirtTarget);
			session.getTransaction().commit();

			player.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§7You have §asuccessfully §7paid " + DirtPlayer.getFormattedBalance(amount) + " §7to §6" + dirtTarget.getUsername()), Util.NIL_UUID);

			ServerPlayer targetPlayer = DirtEssentials.SERVER.getPlayerList().getPlayer(target);
			if (targetPlayer != null)
				targetPlayer.sendMessage(new TextComponent(Strings.ESSENTIALS_PREFIX + "§6" + dirtPlayer.getUsername() + " §7sent you " + DirtPlayer.getFormattedBalance(amount)), Util.NIL_UUID);
		}

		return Command.SINGLE_SUCCESS;
	}
}
