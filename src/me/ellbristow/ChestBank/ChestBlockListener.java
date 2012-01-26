package me.ellbristow.ChestBank;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;

public class ChestBlockListener implements Listener {
	
	public static ChestBank plugin;
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public ChestBlockListener (ChestBank instance) {
		plugin = instance;
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onBlockBreak (BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getTypeId() == 54) {
			// Chest Broken
			if (plugin.isBankBlock(block)) {
				event.getPlayer().sendMessage(ChatColor.RED + "This is a ChestBank and cannot be destroyed!");
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onBlockIgnite (BlockIgniteEvent event) {
		Block block = event.getBlock();
		if (block.getTypeId() == 54) {
			// Chest Broken
			if (plugin.isBankBlock(block)) {
				if (event.getCause().equals(IgniteCause.FLINT_AND_STEEL)) {
					event.getPlayer().sendMessage(ChatColor.RED + "This is a ChestBank and is fireproof!");
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onBlockPlace (BlockPlaceEvent event) {
		Block block = event.getBlock();
		if (block.getTypeId() == 54) {
			int blockX = block.getX();
			int blockY = block.getY();
			int blockZ = block.getZ();
			if (plugin.isBankBlock(block.getWorld().getBlockAt(blockX + 1, blockY, blockZ)) || plugin.isBankBlock(block.getWorld().getBlockAt(blockX - 1, blockY, blockZ)) || plugin.isBankBlock(block.getWorld().getBlockAt(blockX, blockY, blockZ + 1)) || plugin.isBankBlock(block.getWorld().getBlockAt(blockX, blockY, blockZ - 1))) {
				Player player = event.getPlayer();
				if (player.hasPermission("chestbank.create")) {
					String bankList = plugin.banksConfig.getString("banks", "");
					String[] bankSplit = bankList.split(";");
					String newBankList = "";
					for (String bankBlock : bankSplit) {
						if (newBankList != "") {
							newBankList += ";";
						}
						String[] blockLoc = bankBlock.split(":");
						int oldBlockX = Integer.parseInt(blockLoc[0]);
						int oldBlockY = Integer.parseInt(blockLoc[1]);
						int oldBlockZ = Integer.parseInt(blockLoc[2]);
						newBankList += oldBlockX + ":" + oldBlockY + ":" + oldBlockZ;
						if (oldBlockX + 1 == blockX || oldBlockX - 1 == blockX || oldBlockZ + 1 == blockZ || oldBlockZ - 1 == blockZ) {
							newBankList += ":" + blockX + ":" + blockY + ":" + blockZ;
						}
						else if (blockLoc.length == 6) {
							oldBlockX = Integer.parseInt(blockLoc[3]);
							oldBlockY = Integer.parseInt(blockLoc[4]);
							oldBlockZ = Integer.parseInt(blockLoc[5]);
							newBankList += ":" + oldBlockX + ":" + oldBlockY + ":" + oldBlockZ;
						}
					}
					plugin.banksConfig.set("banks", newBankList);
					plugin.saveChestBanks();
					player.sendMessage(ChatColor.GOLD + "Chest added to ChestBank!");
				}
			}
		}
	}
}
