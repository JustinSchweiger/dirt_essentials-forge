package net.dirtcraft.mods.dirt_essentials.data.entites;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import net.dirtcraft.mods.dirt_essentials.data.HibernateUtil;
import net.dirtcraft.mods.dirt_essentials.data.TimeUnit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import org.hibernate.Session;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Kit {
	@Id
	@Getter
	private String name;

	@Getter
	private int cooldown;

	@Getter
	private TimeUnit timeUnit;

	@Getter
	@OneToMany
	private List<KitTracker> trackers;

	@Column(length = 999999)
	private String items;

	public Kit() {
	}

	public Kit(String name, int cooldown, TimeUnit timeUnit, List<ItemStack> items) {
		this.name = name;
		this.cooldown = cooldown;
		this.timeUnit = timeUnit;
		this.trackers = new ArrayList<>();
		this.items = serialize(items);
	}

	@Nullable
	public static Kit get(String name) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.get(Kit.class, name);
		}
	}

	public static String serialize(List<ItemStack> items) {
		StringBuilder builder = new StringBuilder();

		for (ItemStack item : items) {
			builder.append(item.serializeNBT());
			builder.append(";;;;");
		}

		return builder.toString();
	}

	public static List<ItemStack> deserialize(String items) {
		List<ItemStack> list = new ArrayList<>();

		for (String itemString : items.split(";;;;")) {
			try {
				CompoundTag tag = TagParser.parseTag(itemString);
				ItemStack stack = ItemStack.of(tag);
				list.add(stack);
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public List<ItemStack> getItems() {
		return deserialize(items);
	}
}
