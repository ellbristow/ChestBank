package me.ellbristow.ChestBank;

import org.bukkit.inventory.PlayerInventory;

public interface ChestBankPlayerInventory extends PlayerInventory, ChestBankCraftingInventory {
	public int getItemInHandSlot();
}
