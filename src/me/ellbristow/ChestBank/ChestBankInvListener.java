package me.ellbristow.ChestBank;

import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;

public class ChestBankInvListener implements Listener {
    
    public static ChestBank plugin;
            
    public ChestBankInvListener (ChestBank instance) {
        plugin = instance;
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onInventoryOpen(ChestBankOpenEvent event) {
        DoubleChestInventory inv = event.getInventory();
        Player player = event.getPlayer();
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onInventoryClose(ChestBankCloseEvent event) {
        String networkName = event.getNetwork();
        DoubleChestInventory inv = event.getInventory();
        Player player = event.getPlayer();
        int allowed = getAllowedSlots(player);
        if (getUsedSlots(inv) > allowed) {
            player.sendMessage(ChatColor.RED + "Sorry! You may only use " + ChatColor.WHITE + allowed + ChatColor.RED + " ChestBank slot(s)!");
            inv = trimExcess(player, inv);
            player.sendMessage(ChatColor.RED + "Excess items have been returned to you!");
            if (networkName.equals("")) {
                plugin.chestAccounts.put(player.getName(), inv);
            } else {
                plugin.chestAccounts.put(networkName + ">>" + player.getName(), inv);
            }
            plugin.setAccounts(plugin.chestAccounts);
        } else {
            plugin.setAccounts(plugin.chestAccounts);
        }
        event.getPlayer().sendMessage(ChatColor.GRAY + "ChestBank Inventory Saved!");
    }

    private int getUsedSlots(DoubleChestInventory inv) {
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
        int limit = 54;
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
    
    private DoubleChestInventory trimExcess(Player player, DoubleChestInventory inv) {
        int allowed = getAllowedSlots(player);
        int newInvCount = 0;
        DoubleChestInventory newInv = new CraftInventoryDoubleChest(new CraftInventory(null), new CraftInventory(null));
        for (ItemStack stack : inv.getContents()) {
            if (stack != null) {
                if (newInvCount < allowed) {
                    newInv.setItem(newInvCount, stack);
                    newInvCount++;
                } else {
                    int id = stack.getTypeId();
                    int amount = stack.getAmount();
                    short damage = (short)stack.getDurability();
                    org.bukkit.inventory.ItemStack result = new org.bukkit.inventory.ItemStack(id, amount, damage);
                    Map<Enchantment, Integer> enchantments = stack.getEnchantments();
                    if (!enchantments.isEmpty()) {
                        Set<Enchantment> keys = enchantments.keySet();
                        for (int i = 0; i < enchantments.size(); i++) {
                            Enchantment ench = keys.iterator().next();
                            int enchLvl = enchantments.get(ench);
                            result.addEnchantment(ench, enchLvl);
                        }
                    }
                    player.getInventory().addItem(result);
                }
            }
        }
        return newInv;
    }
}
