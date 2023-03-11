package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ItemEnchantmentArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("enchant")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ENCHANT))
				.then(Commands.argument("enchantment", ItemEnchantmentArgument.enchantment())
						.then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
								.executes(EnchantCommand::execute)));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		if (source.getEntity() == null) {
			source.sendFailure(new TextComponent(Strings.ESSENTIALS_PREFIX + "§cYou must be a player to use this command!"));
			return Command.SINGLE_SUCCESS;
		}

		ServerPlayer player = source.getPlayerOrException();
		int level = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "level");
		Enchantment enchantment = ItemEnchantmentArgument.getEnchantment(commandSourceStackCommandContext, "enchantment");

		player.getMainHandItem().enchant(enchantment, level);
		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aEnchanted item with " + enchantment.getFullname(level).getString()), false);

		return Command.SINGLE_SUCCESS;
	}
}
