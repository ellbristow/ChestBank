package me.ellbristow.ChestBank;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ChestBlockListener implements Listener {
	
	public static ChestBank plugin;
	
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
                        String blockWorld = block.getWorld().getName();
			int blockX = block.getX();
			int blockY = block.getY();
			int blockZ = block.getZ();
                        if (plugin.isNetworkBank(block.getWorld().getBlockAt(blockX + 1, blockY, blockZ)) || plugin.isNetworkBank(block.getWorld().getBlockAt(blockX - 1, blockY, blockZ)) || plugin.isNetworkBank(block.getWorld().getBlockAt(blockX, blockY, blockZ + 1)) || plugin.isNetworkBank(block.getWorld().getBlockAt(blockX, blockY, blockZ - 1))) {
                            Player player = event.getPlayer();
                            if (player.hasPermission("chestbank.create.networks")) {
                                String network = "";
                                String bankNames = plugin.banksConfig.getString("networks.names", "");
                                for (String bankName : bankNames.split(":")) {
                                    String bankLocs = plugin.banksConfig.getString("networks." + bankName + ".locations", "");
                                    for (String bankLoc : bankLocs.split(";")) {
                                        String[] loc = bankLoc.split(":");
                                        String bankWorld = loc[0];
                                        int bankX = Integer.parseInt(loc[1]);
                                        int bankY = Integer.parseInt(loc[2]);
                                        int bankZ = Integer.parseInt(loc[3]);
                                        if (blockWorld.equals(bankWorld) && ((blockX + 1 == bankX && blockY == bankY && blockZ == bankZ) || (blockX - 1 == bankX && blockY == bankY && blockZ == bankZ) || (blockX == bankX && blockY == bankY && blockZ + 1 == bankZ) || (blockX == bankX && blockY == bankY && blockZ -1 == bankZ ))) {
                                            network = bankName;
                                        }
                                    }
                                }
                                ConfigurationSection networkBank = plugin.banksConfig.getConfigurationSection("networks." + network);
                                String locsList = networkBank.getString("locations", "");
                                String[] bankSplit = locsList.split(";");
                                String newBankList = "";
                                for (String bankBlock : bankSplit) {
                                        if (!newBankList.equals("")) {
                                                newBankList += ";";
                                        }
                                        String[] blockLoc = bankBlock.split(":");
                                        String oldBlockWorld = blockLoc[0];
                                        int oldBlockX = Integer.parseInt(blockLoc[1]);
                                        int oldBlockY = Integer.parseInt(blockLoc[2]);
                                        int oldBlockZ = Integer.parseInt(blockLoc[3]);
                                        newBankList += oldBlockWorld + ":" + oldBlockX + ":" + oldBlockY + ":" + oldBlockZ;
                                        if (oldBlockWorld.equals(blockWorld) && (oldBlockX + 1 == blockX || oldBlockX - 1 == blockX || oldBlockZ + 1 == blockZ || oldBlockZ - 1 == blockZ)) {
                                                newBankList += ":" + blockX + ":" + blockY + ":" + blockZ;
                                        }
                                        else if (blockLoc.length == 6) {
                                                oldBlockX = Integer.parseInt(blockLoc[4]);
                                                oldBlockY = Integer.parseInt(blockLoc[5]);
                                                oldBlockZ = Integer.parseInt(blockLoc[6]);
                                                newBankList += ":" + oldBlockX + ":" + oldBlockY + ":" + oldBlockZ;
                                        }
                                }
                                plugin.banksConfig.set("networks." + network + ".locations", newBankList);
                                plugin.saveChestBanks();
                                player.sendMessage(ChatColor.GOLD + "ChestBank added to " + ChatColor.WHITE + network + ChatColor.GOLD + " Network!");
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission to place a chest next to a network Chestbank!");
                                event.setCancelled(true);
                            }
                            return;
                        }
			if (plugin.isBankBlock(block.getWorld().getBlockAt(blockX + 1, blockY, blockZ)) || plugin.isBankBlock(block.getWorld().getBlockAt(blockX - 1, blockY, blockZ)) || plugin.isBankBlock(block.getWorld().getBlockAt(blockX, blockY, blockZ + 1)) || plugin.isBankBlock(block.getWorld().getBlockAt(blockX, blockY, blockZ - 1))) {
				Player player = event.getPlayer();
				if (player.hasPermission("chestbank.create")) {
					String bankList = plugin.banksConfig.getString("banks", "");
					String[] bankSplit = bankList.split(";");
					String newBankList = "";
					for (String bankBlock : bankSplit) {
						if (!newBankList.equals("")) {
							newBankList += ";";
						}
						String[] blockLoc = bankBlock.split(":");
                                                boolean hasWorld = false;
                                                try {
                                                    Integer.parseInt(blockLoc[0]);
                                                } catch (NumberFormatException nfe) {
                                                    hasWorld = true;
                                                } finally {
                                                    if (!hasWorld) {
                                                        if (blockLoc.length == 6) {
                                                            blockLoc[6] = blockLoc[5];
                                                            blockLoc[5] = blockLoc[4];
                                                            blockLoc[4] = blockLoc[3];
                                                        }
                                                        blockLoc[3] = blockLoc[2];
                                                        blockLoc[2] = blockLoc[1];
                                                        blockLoc[1] = blockLoc[0];
                                                        blockLoc[0] = block.getWorld().getName();
                                                    }
                                                }
                                                String oldBlockWorld = blockLoc[0];
						int oldBlockX = Integer.parseInt(blockLoc[1]);
						int oldBlockY = Integer.parseInt(blockLoc[2]);
						int oldBlockZ = Integer.parseInt(blockLoc[3]);
						newBankList += oldBlockWorld + ":" + oldBlockX + ":" + oldBlockY + ":" + oldBlockZ;
						if (oldBlockWorld.equals(blockWorld) && (oldBlockX + 1 == blockX || oldBlockX - 1 == blockX || oldBlockZ + 1 == blockZ || oldBlockZ - 1 == blockZ)) {
							newBankList += ":" + blockX + ":" + blockY + ":" + blockZ;
						}
						else if (blockLoc.length == 6) {
							oldBlockX = Integer.parseInt(blockLoc[4]);
							oldBlockY = Integer.parseInt(blockLoc[5]);
							oldBlockZ = Integer.parseInt(blockLoc[6]);
							newBankList += ":" + oldBlockX + ":" + oldBlockY + ":" + oldBlockZ;
						}
					}
					plugin.banksConfig.set("banks", newBankList);
					plugin.saveChestBanks();
					player.sendMessage(ChatColor.GOLD + "Chest added to ChestBank!");
				} else {
                                    player.sendMessage(ChatColor.RED + "You do not have permission to place a chest next to a Chestbank!");
                                    event.setCancelled(true);
                                }
			}
		}
	}
        
        @EventHandler (priority = EventPriority.NORMAL)
	public void onBlockExplode (EntityExplodeEvent event) {
            List<Block> blocks = event.blockList();
            int index = 0;
            Collection<Block> saveBanks = new HashSet<Block>();
            for (Iterator<Block> it = blocks.iterator(); it.hasNext();) {
                Block block = it.next();
                if (plugin.isBankBlock(block)) {
                    saveBanks.add(block);
                }
                index++;
            }
            if (!saveBanks.isEmpty()) {
                    event.blockList().removeAll(saveBanks);
            }
        }
}
