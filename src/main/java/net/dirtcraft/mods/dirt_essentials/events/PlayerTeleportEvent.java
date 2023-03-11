package net.dirtcraft.mods.dirt_essentials.events;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityTeleportEvent;

public class PlayerTeleportEvent extends EntityTeleportEvent {
	public PlayerTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
		super(entity, targetX, targetY, targetZ);
	}
}
