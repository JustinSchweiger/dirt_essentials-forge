package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.config.EssentialsConfig;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.entites.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.data.entites.Home;
import net.dirtcraft.mods.dirt_essentials.manager.HomeManager;
import net.dirtcraft.mods.dirt_essentials.manager.PlayerManager;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.hibernate.Session;

import java.util.List;
import java.util.UUID;

public class HomesCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("homes")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HOMES))
				.executes(context -> execute(context, 1, false))
				.then(Commands.argument("page", IntegerArgumentType.integer(1))
						.executes(context -> execute(context, IntegerArgumentType.getInteger(context, "page"), false)))
				.then(Commands.argument("target", StringArgumentType.string())
						.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.HOMES_OTHERS))
						.suggests((context, builder) -> SharedSuggestionProvider.suggest(PlayerManager.getAllUsernames(), builder))
						.executes(context -> execute(context, 1, true))
						.then(Commands.argument("page", IntegerArgumentType.integer(1))
								.executes(context -> execute(context, IntegerArgumentType.getInteger(context, "page"), true))));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> context, int page, boolean isOther) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		UUID target = isOther ? PlayerManager.getUuid(StringArgumentType.getString(context, "target")) : source.getPlayerOrException().getUUID();

		if (!HomeManager.hasHomes(target)) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "You do not have any homes!"));
			return Command.SINGLE_SUCCESS;
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			DirtPlayer dirtPlayer = session.get(DirtPlayer.class, target);
			List<Home> homes = dirtPlayer.getHomes();

			int maxPage = (int) Math.ceil(homes.size() / (double) EssentialsConfig.HOMES_SIZE.get());
			int start = (page - 1) * EssentialsConfig.HOMES_SIZE.get();
			int end = page * EssentialsConfig.HOMES_SIZE.get();
			if (end > homes.size())
				end = homes.size();

			source.sendSuccess(new TextComponent(""), false);
			if (isOther) {
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§6" + dirtPlayer.getUsername() + " currently has §e" + homes.size() + " §c/ §e" + dirtPlayer.getHomeAmount() + " §7homes used:"), false);
			} else {
				source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "You currently have §e" + homes.size() + " §c/ §e" + dirtPlayer.getHomeAmount() + " §7homes used:"), false);
			}
			source.sendSuccess(new TextComponent(""), false);

			for (int i = start; i < end; i++) {
				Home home = homes.get(i);

				TextComponent deleteComponent = new TextComponent("§7[§c✕§7]");
				if (isOther) {
					deleteComponent.setStyle(deleteComponent.getStyle()
							.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/otherhome " + StringArgumentType.getString(context, "target") + " delete " + home.getName()))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§cClick to delete this players home!"))));
				} else {
					deleteComponent.setStyle(deleteComponent.getStyle()
							.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/delhome " + home.getName()))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§cClick to delete this home!"))));
				}

				TextComponent homeComponent = new TextComponent("§6" + home.getName());
				if (isOther) {
					homeComponent.setStyle(homeComponent.getStyle()
							.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/otherhome " + StringArgumentType.getString(context, "target") + " teleport " + home.getName()))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(
									"§6World§7: §b" + home.getRegistry() + " | " + home.getLocation() + "\n" +
											"§6Coords§7: §b" + Utils.formatCoordinates(home.getX(), home.getY(), home.getZ()) + "\n" +
											"\n" +
											"§aClick to teleport to this players home!"
							))));
				} else {
					homeComponent.setStyle(homeComponent.getStyle()
							.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home " + home.getName()))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(
									"§6World§7: §b" + home.getRegistry() + " | " + home.getLocation() + "\n" +
											"§6Coords§7: §b" + Utils.formatCoordinates(home.getX(), home.getY(), home.getZ()) + "\n" +
											"\n" +
											"§aClick to teleport to this home!"
							))));
				}

				MutableComponent finalComponent = new TextComponent("  §9- ");

				if (isOther) {
					if (PermissionHandler.hasPermission(source, EssentialsPermissions.DELHOME_OTHERS)) {
						finalComponent.append(deleteComponent).append(new TextComponent(" "));
					}
				} else {
					if (PermissionHandler.hasPermission(source, EssentialsPermissions.DELHOME)) {
						finalComponent.append(deleteComponent).append(new TextComponent(" "));
					}
				}

				finalComponent.append(homeComponent);
				source.sendSuccess(finalComponent, false);
			}

			source.sendSuccess(new TextComponent(""), false);
			if (isOther) {
				source.sendSuccess(Utils.getPaginator(page, maxPage, "/homes " + StringArgumentType.getString(context, "target")), false);
			} else {
				source.sendSuccess(Utils.getPaginator(page, maxPage, "/homes"), false);
			}
			source.sendSuccess(new TextComponent(""), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
