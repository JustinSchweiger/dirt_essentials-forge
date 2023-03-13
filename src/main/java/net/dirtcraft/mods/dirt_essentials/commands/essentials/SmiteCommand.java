package net.dirtcraft.mods.dirt_essentials.commands.essentials;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dirtcraft.mods.dirt_essentials.permissions.EssentialsPermissions;
import net.dirtcraft.mods.dirt_essentials.permissions.PermissionHandler;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

public class SmiteCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands
				.literal("smite")
				.requires(source -> PermissionHandler.hasPermission(source, EssentialsPermissions.SMITE))
				.then(Commands.argument("player", EntityArgument.player())
						.executes(SmiteCommand::execute)));
	}

	private static int execute(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
		CommandSourceStack source = commandSourceStackCommandContext.getSource();
		ServerPlayer player = EntityArgument.getPlayer(commandSourceStackCommandContext, "player");

		EntityType.LIGHTNING_BOLT.spawn(player.getLevel(), null, null, player.blockPosition(), MobSpawnType.COMMAND, false, false);
		player.connection.send(new ClientboundCustomSoundPacket(new ResourceLocation("minecraft:entity.lightning_bolt.thunder"), player.getSoundSource(), new Vec3(player.getX(), player.getY(), player.getZ()), 1.0F, 1.0F));
		player.connection.send(new ClientboundCustomSoundPacket(new ResourceLocation("minecraft:entity.lightning_bolt.impact"), player.getSoundSource(), new Vec3(player.getX(), player.getY(), player.getZ()), 1.0F, 1.0F));


		source.sendSuccess(new TextComponent(Strings.ESSENTIALS_PREFIX + "§aYou have smited §6" + player.getName().getString() + "§a!"), false);

		return Command.SINGLE_SUCCESS;
	}
}
