package me.ellbristow.ChestBank;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class ChestBankInventoryCloseEvent extends ChestBankInventoryEvent implements ChestBankEvent {

	private static final HandlerList handlers = new HandlerList();
	private Inventory other;
	private static final ChestBankEventType type = ChestBankEventType.Inventory_Close;

	public ChestBankInventoryCloseEvent(Player player, Inventory inventory, Inventory other) {
		super("ChestBankInventoryCloseEvent", player, inventory);
		this.other = other;
	}

	public ChestBankInventoryCloseEvent(Player player, Inventory inventory, Inventory other, Location location) {
		super("ChestBankInventoryCloseEvent", player, inventory, location);
		this.other = other;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public Inventory getBottomInventory() {
		return this.other;
	}

	@Override
	public ChestBankEventType getEventType() {
		return type;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
