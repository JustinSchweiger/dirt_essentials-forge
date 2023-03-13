package net.dirtcraft.mods.dirt_essentials.data;

import lombok.Getter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class Location {
	@Getter
	private final ResourceKey<Level> level;
	@Getter
	private final double x;
	@Getter
	private final double y;
	@Getter
	private final double z;
	@Getter
	private final float yaw;
	@Getter
	private final float pitch;

	public Location(ResourceKey<Level> level, double x, double y, double z, float yaw, float pitch) {
		this.level = level;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}
}
