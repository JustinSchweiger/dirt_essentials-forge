package net.dirtcraft.mods.dirt_essentials.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

@Entity
public class Spawn {
	@Id
	@Column(length = 9999)
	private String location;
	@Getter
	private double x;
	@Getter
	private double y;
	@Getter
	private double z;
	@Getter
	private float yaw;
	@Getter
	private float pitch;

	public Spawn() {}

	public Spawn(ResourceKey<Level> world, double x, double y, double z, float yaw, float pitch) {
		this.location = world.location().toString();
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public ResourceKey<Level> getLevel() {
		return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(location));
	}
}
