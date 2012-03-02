package me.ellbristow.ChestBank;

import java.util.Map;
import java.util.Set;
import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.TileEntityChest;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;

public class ChestPlayerListener implements Listener {
	
    public static ChestBank plugin;

    public ChestPlayerListener (ChestBank instance) {
        plugin = instance;
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerInteract (PlayerInteractEvent event) {
        if (!event.isCancelled()) { 
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Block block = event.getClickedBlock();
                if (block.getTypeId() == 54 && plugin.isNetworkBank(block)) {
                    Player player = event.getPlayer();
                    if (!player.hasPermission("chestbank.use.networks")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to use network ChestBanks!");
                    }
                    else {
                        String network = plugin.getNetwork(block);
                        DoubleChestInventory inv = plugin.chestAccounts.get(network + ">>" + player.getName());
                        if (inv != null) {
                            plugin.openInvs.put(player.getName(), network);
                            player.openInventory(inv);
                        } else {
                            inv = new CraftInventoryDoubleChest(new InventoryLargeChest(player.getName(), new TileEntityChest(), new TileEntityChest()));
                            plugin.chestAccounts.put(network + ">>" + player.getName(), inv);
                            plugin.setAccounts(plugin.chestAccounts);
                            plugin.openInvs.put(player.getName(), network);
                            player.openInventory(inv);
                        }
                    }
                    event.setCancelled(true);
                } else if (block.getTypeId() == 54 && plugin.isBankBlock(block)) {
                    Player player = event.getPlayer();
                    if (!player.hasPermission("chestbank.use")) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to use ChestBanks!");
                    }
                    else {
                        DoubleChestInventory inv = plugin.chestAccounts.get(player.getName());
                        if (inv.getContents().length != 0) {
                            plugin.openInvs.put(player.getName(), "");
                            player.openInventory(inv);
                        } else {
                            inv = new CraftInventoryDoubleChest(new InventoryLargeChest(player.getName(), new TileEntityChest(), new TileEntityChest()));
                            plugin.chestAccounts.put(player.getName(), inv);
                            plugin.setAccounts(plugin.chestAccounts);
                            plugin.openInvs.put(player.getName(), "");
                            player.openInventory(inv);
                        }
                    }
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onInventoryClose (InventoryCloseEvent event) {
        Player player = (Player)event.getPlayer();
        if (plugin.openInvs != null && plugin.openInvs.containsKey(player.getName())) {
            String network = plugin.openInvs.get(event.getPlayer().getName());
            plugin.openInvs.remove(event.getPlayer().getName());
            DoubleChestInventory inv = (DoubleChestInventory)event.getInventory();
            int allowed = getAllowedSlots(player);
            if (getUsedSlots(inv) > allowed) {
                player.sendMessage(ChatColor.RED + "Sorry! You may only use " + ChatColor.WHITE + allowed + ChatColor.RED + " ChestBank slot(s)!");
                inv = trimExcess(player, inv);
                player.sendMessage(ChatColor.RED + "Excess items have been returned to you!");
                if (network.equals("")) {
                    plugin.chestAccounts.put(player.getName(), inv);
                } else {
                    plugin.chestAccounts.put(network + ">>" + player.getName(), inv);
                }
                plugin.setAccounts(plugin.chestAccounts);
            } else {
                plugin.setAccounts(plugin.chestAccounts);
            }
            player.sendMessage(ChatColor.GRAY + "ChestBank Inventory Saved!");
        }
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
        DoubleChestInventory newInv = new CraftInventoryDoubleChest(new InventoryLargeChest(player.getName(), new TileEntityChest(), new TileEntityChest()));
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