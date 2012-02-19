package me.ellbristow.ChestBank;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;

public abstract class ChestBankInventoryEvent extends Event implements Cancellable {

        protected Inventory inventory;
	protected Player player;
	protected boolean cancelled;
	protected Location location = null;

	public ChestBankInventoryEvent(String event, Player player, Inventory inventory) {
		super(event);
		this.player = player;
		this.inventory = inventory;
	}

	public ChestBankInventoryEvent(String event, Player player, Inventory inventory, Location location) {
		super(event);
		this.player = player;
		this.inventory = inventory;
		this.location = location;
	}

	public Player getPlayer() {
		return player;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
