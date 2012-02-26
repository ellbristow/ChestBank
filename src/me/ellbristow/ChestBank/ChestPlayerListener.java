package me.ellbristow.ChestBank;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.TileEntityChest;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
                        EntityPlayer ePlayer;
                        ePlayer = ((CraftPlayer) player).getHandle();
                        String network = plugin.getNetwork(block);
                        InventoryLargeChest inv = plugin.chestAccounts.get(network + ">>" + player.getName());
                        if (inv != null) {
                            ePlayer.a(inv);
                        } else {
                            inv = new InventoryLargeChest(player.getName(), new TileEntityChest(), new TileEntityChest());
                            plugin.chestAccounts.put(network + ">>" + player.getName(), inv);
                            plugin.setAccounts(plugin.chestAccounts);
                            ePlayer.a(inv);
                        }
                        ChestBankOpenEvent e = new ChestBankOpenEvent(player, plugin);
                        plugin.getServer().getPluginManager().callEvent(e);
                    }
                    event.setCancelled(true);
                }
                if (!event.isCancelled()) {
                    if (block.getTypeId() == 54 && plugin.isBankBlock(block)) {
                        Player player = event.getPlayer();
                        if (!player.hasPermission("chestbank.use")) {
                            player.sendMessage(ChatColor.RED + "You do not have permission to use ChestBanks!");
                        }
                        else {
                            EntityPlayer ePlayer;
                            ePlayer = ((CraftPlayer) player).getHandle();
                            InventoryLargeChest inv = plugin.chestAccounts.get(player.getName());
                            if (inv != null) {
                                ePlayer.a(inv);
                            } else {
                                inv = new InventoryLargeChest(player.getName(), new TileEntityChest(), new TileEntityChest());
                                plugin.chestAccounts.put(player.getName(), inv);
                                plugin.setAccounts(plugin.chestAccounts);
                                ePlayer.a(inv);
                            }
                            ChestBankOpenEvent e = new ChestBankOpenEvent(player, plugin);
                            plugin.getServer().getPluginManager().callEvent(e);
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerLeave (PlayerQuitEvent event) {
        if (plugin.openInvs != null && plugin.openInvs.contains(event.getPlayer().getName())) {
            plugin.openInvs.remove(event.getPlayer().getName());
            ChestBankCloseEvent e = new ChestBankCloseEvent(event.getPlayer(), plugin);
            plugin.getServer().getPluginManager().callEvent(e);
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerMove (PlayerMoveEvent event) {
        if (plugin.openInvs != null && plugin.openInvs.contains(event.getPlayer().getName())) {
            plugin.openInvs.remove(event.getPlayer().getName());
            ChestBankCloseEvent e = new ChestBankCloseEvent(event.getPlayer(), plugin);
            plugin.getServer().getPluginManager().callEvent(e);
        }
    }
    
    @EventHandler (priority = EventPriority.NORMAL)
    public void onInventoryOpen(ChestBankOpenEvent event) {
        plugin.openInvs.add(event.getPlayer().getName());
    }
    
    
}
