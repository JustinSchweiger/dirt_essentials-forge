package net.dirtcraft.mods.dirt_essentials.data.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.hibernate.Session;
import org.jetbrains.annotations.Nullable;

@Entity
public class Warp {
	@Id
	@Getter
	private String name;

	private String item;

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

	public Warp() {}

	public Warp(String name, ItemStack item, ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch) {
		this.name = name;
		this.item = serialize(item);
		this.registry = dimension.registry().toString();
		this.location = dimension.location().toString();
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	@Nullable
	public static Warp get(String name) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.get(Warp.class, name);
		}
	}

	public ItemStack getItem() {
		return deserialize(item);
	}

	public void setItem(ItemStack item) {
		this.item = serialize(item);
	}

	private String serialize(ItemStack item) {
		return item.serializeNBT().toString();
	}

	private ItemStack deserialize(String item) {
		try {
			return ItemStack.of(TagParser.parseTag(item));
		} catch (Exception e) {
			return ItemStack.EMPTY;
		}
	}
}
