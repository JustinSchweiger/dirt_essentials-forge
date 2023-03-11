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
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class EntityzapCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands
				.literal("entityzap")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.ENTITYZAP))
				.then(Commands.argument("world", DimensionArgument.dimension())
						.then(Commands.argument("pos", BlockPosArgument.blockPos())
								.then(Commands.argument("radius", IntegerArgumentType.integer(1, 100))
										.then(Commands.argument("entity", net.minecraft.commands.arguments.EntitySummonArgument.id())
												.suggests((context, builder) -> SharedSuggestionProvider.suggest(Utils.getSummonableEntities(), builder))
												.executes(EntityzapCommand::execute)))));

		dispatcher.register(commandBuilder);
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerLevel level = DimensionArgument.getDimension(commandSourceStackCommandContext, "world");
		BlockPos blockPosArgument = BlockPosArgument.getLoadedBlockPos(commandSourceStackCommandContext, "pos");
		int radius = IntegerArgumentType.getInteger(commandSourceStackCommandContext, "radius");
		ResourceLocation entity = EntitySummonArgument.getSummonableEntity(commandSourceStackCommandContext, "entity");

		List<Entity> entities = level.getEntities(null, new AABB(blockPosArgument.offset(-radius, -radius, -radius), blockPosArgument.offset(radius, radius, radius)));
		int removedEntities = 0;
		for (Entity e : entities){
			if (e.getType().getRegistryName() != null && e.getType().getRegistryName().equals(entity)) {
				e.remove(Entity.RemovalReason.DISCARDED);
				removedEntities++;
			}
		}

		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aRemoved §d" + removedEntities + "x §e" + entity + "§a!"), false);

		return Command.SINGLE_SUCCESS;
	}
}
