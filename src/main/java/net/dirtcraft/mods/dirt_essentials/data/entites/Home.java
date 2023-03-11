package net.dirtcraft.mods.dirt_essentials.data.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

@Entity
public class Home {
	@Id
	@Getter
	private String name;

	@Id
	@ManyToOne
	@Getter
	private DirtPlayer owner;

	@Getter
	private String registry;

	@Getter
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

	public Home() {}

	public Home(String name, DirtPlayer owner, ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch) {
		this.name = name;
		this.owner = owner;
		this.registry = dimension.registry().toString();
		this.location = dimension.location().toString();
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}
}
