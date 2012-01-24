package me.ellbristow.ChestBank;

import java.util.logging.Logger;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.TileEntityChest;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class ChestPlayerListener extends PlayerListener {
	
	public static ChestBank plugin;
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public ChestPlayerListener (ChestBank instance) {
		plugin = instance;
	}
	
	public void onPlayerInteract (PlayerInteractEvent event) {
		if (!event.isCancelled()) { 
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				Block block = event.getClickedBlock();
				if (block.getTypeId() == 54 && plugin.isBankBlock(block)) {
					Player player = event.getPlayer();
					if (!player.hasPermission("chestbank.use")) {
						player.sendMessage(ChatColor.RED + "You do not have permission to use ChestBanks!");
					}
					else {
						EntityPlayer ePlayer;
						ePlayer = ((CraftPlayer) player).getHandle();
						InventoryLargeChest inv = plugin.chestBanks.get(player.getName());
						if (inv != null) {
							ePlayer.a(inv);
						} else {
							inv = new InventoryLargeChest(player.getName(), new TileEntityChest(), new TileEntityChest());
							plugin.chestBanks.put(player.getName(), inv);
							plugin.setChests(plugin.chestBanks);
							ePlayer.a(inv);
						}
					}
					event.setCancelled(true);
				}
			}
		}
	}
}
