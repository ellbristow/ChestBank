package me.ellbristow.ChestBank;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface ChestBankCraftingInventory extends Inventory {

    public ItemStack getResult();

    public ItemStack[] getMatrix();

    public void setResult(ItemStack newResult);

    public void setMatrix(ItemStack[] contents);
}
