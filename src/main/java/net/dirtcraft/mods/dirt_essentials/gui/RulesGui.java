package net.dirtcraft.mods.dirt_essentials.gui;

import net.dirtcraft.mods.dirt_essentials.database.DirtPlayer;
import net.dirtcraft.mods.dirt_essentials.database.Rule;
import net.dirtcraft.mods.dirt_essentials.gui.inv.SeparateInventory;
import net.dirtcraft.mods.dirt_essentials.gui.inv.ServerOnlyScreenHandler;
import net.dirtcraft.mods.dirt_essentials.manager.RulesManager;
import net.dirtcraft.mods.dirt_essentials.util.Strings;
import net.dirtcraft.mods.dirt_essentials.util.Utils;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public class RulesGui extends ServerOnlyScreenHandler {
	protected RulesGui(int syncId, Inventory playerInventory, int rows) {
		super(syncId, playerInventory, rows);
	}

	public static void openRulesGui(Player player) {
		MenuProvider fac = new MenuProvider() {
			@Override
			public AbstractContainerMenu createMenu(int syncId, @NotNull Inventory inv, @NotNull Player player) {
				int rulesCount = RulesManager.getRulesCount();

				int rows = 0;
				if (rulesCount > 0)
					rows = 2;
				if (rulesCount > 9)
					rows = 3;
				if (rulesCount > 18)
					rows = 4;
				if (rulesCount > 27)
					rows = 5;
				if (rulesCount > 36)
					rows = 6;

				return new RulesGui(syncId, inv, rows);
			}

			@Override
			public @NotNull Component getDisplayName() {
				return new TextComponent("§cDirtCraft §4Rules");
			}
		};
		player.openMenu(fac);
	}

	@Override
	protected void fillInventoryWith(Player player, SeparateInventory inv) {
		if (!(player instanceof ServerPlayer))
			return;

		Map<Integer, Rule> rules = RulesManager.getRules();
		if (rules == null)
			return;
		for (Map.Entry<Integer, Rule> entry : rules.entrySet()) {
			int slot = entry.getKey();
			Rule rule = entry.getValue();

			ItemStack item = createRuleGuiItem(entry.getKey(), rule, player);
			inv.updateStack(slot, item);
		}

		for (int i = 0; i < (rows * 9); i++) {
			if (inv.getActualStack(i).isEmpty()) {
				inv.updateStack(i, new ItemStack(Items.GRAY_STAINED_GLASS_PANE).setHoverName(new TextComponent(" ")));
			}
		}

		ItemStack acceptItem = new ItemStack(Items.LIME_TERRACOTTA);
		acceptItem.setHoverName(new TextComponent("§aAccept Rules §2✔"));
		Utils.addLore(acceptItem, " ");
		Utils.addLore(acceptItem, "§7Click to accept the rules.");
		Utils.addLore(acceptItem, "§8§oYou will have to accept them again after any updates.");
		inv.updateStack(rows * 9 - 5, acceptItem);
	}

	private ItemStack createRuleGuiItem(int index, Rule rule, Player player) {
		ItemStack item = new ItemStack(Items.RED_TERRACOTTA);
		item.setHoverName(new TextComponent("§7∙ §cRule §e#" + (index + 1) + " §7∙"));
		Utils.addLore(item, " ");
		Utils.addLore(item, rule.getMessage());

		if (DirtPlayer.get(player).isStaff()) {
			Utils.addLore(item, " ");
			Utils.addLore(item, "§7Last Updated: §e" + rule.getLastEditDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " §7at §e" + rule.getLastEditDate().format(DateTimeFormatter.ofPattern("HH:mm")));
			Utils.addLore(item, "§7Added: §e" + rule.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " §7at §e" + rule.getCreationDate().format(DateTimeFormatter.ofPattern("HH:mm")));
			Utils.addLore(item, " ");
			Utils.addLore(item, "§7Added by: §3" + rule.getCreator().getUsername());
		}

		return item;
	}

	@Override
	protected boolean isRightSlot(int slot) {
		return slot == rows * 9 - 5;
	}

	@Override
	protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
		if (index == rows * 9 - 5) {
			RulesManager.acceptRules(player);
			player.sendMessage(new TextComponent(Strings.RULES_PREFIX + "§7You have accepted the rules! §aHave fun playing!"), Util.NIL_UUID);
			player.connection.send(new ClientboundCustomSoundPacket(new ResourceLocation("minecraft:entity.player.levelup"), SoundSource.PLAYERS, new Vec3(player.getX(), player.getY(), player.getZ()), 1.0F, 1.0F));
			player.closeContainer();
			return true;
		}

		return false;
	}
}
