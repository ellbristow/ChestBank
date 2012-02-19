package me.ellbristow.ChestBank;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestBankInventoryClickEvent extends ChestBankInventoryEvent implements ChestBankEvent {

	private static final HandlerList handlers = new HandlerList();
	protected ChestBankInventorySlotType type;
	protected ItemStack item;
	protected ItemStack cursor;
	protected int slot;
	protected int convertedSlot;
	protected Result result = Result.DEFAULT;
	protected boolean leftClick;
	protected boolean shift;
	private static final ChestBankEventType eventtype = ChestBankEventType.Inventory_Click;

	public ChestBankInventoryClickEvent(Player player, Inventory inventory, ChestBankInventorySlotType type, ItemStack item, ItemStack cursor, int slot, boolean leftClick, boolean shift) {
		super("ChestBankInventoryClickEvent", player, inventory);
		this.type = type;
		this.item = item;
		this.cursor = cursor;
		this.slot = slot;
		this.convertedSlot = convertSlot(this.slot);
		this.leftClick = leftClick;
		this.shift = shift;
	}

	public ChestBankInventoryClickEvent(Player player, Inventory inventory, ChestBankInventorySlotType type, ItemStack item, ItemStack cursor, int slot, boolean leftClick, boolean shift, Location location) {
		super("ChestBankInventoryClickEvent", player, inventory, location);
		this.type = type;
		this.item = item;
		this.cursor = cursor;
		this.slot = slot;
		this.convertedSlot = convertSlot(this.slot);
		this.leftClick = leftClick;
		this.shift = shift;
	}

	protected ChestBankInventoryClickEvent(String name, Player player, Inventory inventory, ChestBankInventorySlotType type, ItemStack item, ItemStack cursor, int slot, boolean leftClick, boolean shift, Location location) {
		super(name, player, inventory, location);
		this.type = type;
		this.item = item;
		this.cursor = cursor;
		this.slot = slot;
		this.convertedSlot = convertSlot(this.slot);
		this.leftClick = leftClick;
		this.shift = shift;
	}

	@Override
	public void setCancelled(boolean cancel) {
		if (cancel) this.result = Result.DENY;
		super.setCancelled(cancel);
	}

	public Result getResult() {
		return this.result;
	}

	public void setResult(Result result) {
		this.result = result;
		if (result == Result.DENY) {
			setCancelled(true);
		}
	}

	public ChestBankInventorySlotType getSlotType() {
		return this.type;
	}

	public ItemStack getItem() {
		return this.item;
	}

	public void setItem(ItemStack item) {
		if (this.result != Result.ALLOW) {
			throw new UnsupportedOperationException("Can not alter stack contents without allowing any result");
		}
		this.item = item;
	}

	public ItemStack getCursor() {
		return this.cursor;
	}

	public void setCursor(ItemStack cursor) {
		if (this.result != Result.ALLOW) {
			throw new UnsupportedOperationException("Can not alter cursor stack contents without allowing any result");
		}
		this.cursor = cursor;
	}

        public int getSlot() {
		return this.convertedSlot;
	}

	public int getRawSlot() {
		return this.slot;
	}

	public boolean isLeftClick() {
		return leftClick;
	}

	public boolean isShiftClick() {
		return shift;
	}

	protected int convertSlot(int slot) {
		if (getInventory() instanceof ChestBankPlayerInventory) {
			int size = getInventory().getSize();
			//Armour slot
			switch(slot) {
			case 5: return 39;
			case 6: return 38;
			case 7: return 37;
			case 8: return 36;
			}
			//Quickslots
			if (slot >= size) {
				slot -= size;
			}

			return slot;
		}
		return slot;
	}

	@Override
	public ChestBankEventType getEventType() {
		return eventtype;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
