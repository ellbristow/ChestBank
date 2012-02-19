package me.ellbristow.ChestBank;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestBankInventoryPlayerClickEvent extends ChestBankInventoryClickEvent {

	private static final HandlerList handlers = new HandlerList();
	public ChestBankInventoryPlayerClickEvent(Player player, Inventory inventory, ChestBankInventorySlotType type, ItemStack item, ItemStack cursor, int slot, boolean leftClick, boolean shift, Location location) {
		super("ChestBankInventoryPlayerClickEvent", player, inventory, type, item, cursor, slot, leftClick, shift, location);
	}

	@Override
	protected int convertSlot(int slot) {
		return slot;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
