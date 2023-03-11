package net.dirtcraft.mods.dirt_essentials.permissions;

import net.dirtcraft.mods.dirt_essentials.DirtEssentials;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PermissionHandler {
	public static boolean hasPermission(UUID uuid, String node) {
		return getUser(uuid).getCachedData().getPermissionData().checkPermission(node).asBoolean();
	}

	public static boolean hasPermission(CommandSourceStack source, String node) {
		if (source.getEntity() == null) return true;

		return hasPermission(source.getEntity().getUUID(), node);
	}

	private static User getUser(UUID uuid) {
		return DirtEssentials.LUCKPERMS.getUserManager().getUser(uuid);
	}

	public static String getPrimaryGroup(UUID uuid) {
		Group group = DirtEssentials.LUCKPERMS.getGroupManager().getGroup(getUser(uuid).getPrimaryGroup());
		if (group == null) return "";

		return group.getName();
	}

	public static String getPrefix(UUID uuid) {
		return getUser(uuid).getCachedData().getMetaData().getPrefix();
	}

	public static String getGroupPrefix(String groupString) {
		Group group = DirtEssentials.LUCKPERMS.getGroupManager().getGroup(groupString);
		if (group == null) return "";

		return group.getCachedData().getMetaData().getPrefix();
	}
}
