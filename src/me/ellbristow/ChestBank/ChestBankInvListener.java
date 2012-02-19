package me.ellbristow.ChestBank;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.event.inventory.InventoryClickEvent;
import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
import org.getspout.spoutapi.event.inventory.InventoryOpenEvent;
import org.getspout.spoutapi.event.inventory.InventoryPlayerClickEvent;

public class ChestBankInvListener implements Listener {
    
    public static ChestBank plugin;
    public HashMap<String, Inventory> status = new HashMap<String, Inventory>();
            
    public ChestBankInvListener (ChestBank instance) {
        plugin = instance;
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        Player player = event.getPlayer();
        if (inv.getName().equals(player.getName())) {
            status.put(player.getName(), inv);
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        Player player = event.getPlayer();
        if (inv.getName().equals(player.getName())) {
            status.remove(player.getName());
        }
        plugin.setChests(plugin.chestBanks);
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        Player player = event.getPlayer();
        if (inv.getName().equals(player.getName())) {
            if (event.getCursor() != null) {
                int usedSlots = getUsedSlots(inv);
                int allowedSlots = getAllowedSlots(player);
                if (usedSlots >= allowedSlots) {
                    player.sendMessage(ChatColor.RED + "Your ChestBank is Full!");
                    player.sendMessage(ChatColor.GRAY + "(Your Slot Limit: " + allowedSlots + ")");
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerInventoryClick(InventoryPlayerClickEvent event) {
        Inventory inv = event.getInventory();
        Player player = event.getPlayer();
        if (status.containsKey(player.getName())) {
            if (event.getCursor() == null) {
                int usedSlots = getUsedSlots(status.get(player.getName()));
                int allowedSlots = getAllowedSlots(player);
                if (usedSlots >= allowedSlots) {
                    player.sendMessage(ChatColor.RED + "Your ChestBank is Full!");
                    player.sendMessage(ChatColor.GRAY + "(Your Slot Limit: " + allowedSlots + ")");
                    event.setCancelled(true);
                }
            }
        }
    }
    
    private int getUsedSlots(Inventory inv) {
        ItemStack[] contents = inv.getContents();
        int count = 0;
        for (ItemStack stack : contents) {
            if (stack != null && stack.getTypeId() != 0) {
                count++;
            }
        }
        return count;
    }
    
    private int getAllowedSlots(Player player) {
        int limit = 1;
        if (player.hasPermission("chestbank.limited.normal")) {
            limit = plugin.limits[0];
        }
        if (player.hasPermission("chestbank.limited.elevated")) {
            limit = plugin.limits[1];
        }
        if (player.hasPermission("chestbank.limited.vip")) {
            limit = plugin.limits[2];
        }
        if (limit > 54) {
            limit = 54;
        }
        return limit;
    }
}
