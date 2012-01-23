package me.ellbristow.ChestBank;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestBank extends JavaPlugin {
	
	public static ChestBank plugin;
	public final Logger logger = Logger.getLogger("Minecraft");
	public final ChestBlockListener blockListener = new ChestBlockListener(this);
	public final ChestPlayerListener playerListener = new ChestPlayerListener(this);
	public FileConfiguration chestBanks = null;
	private File bankFile = null;

	@Override
	public void onDisable () {
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info( "[" + pdfFile.getName() + "] is now disabled.");
	}

	@Override
	public void onEnable () {
		PluginDescriptionFile pdfFile = this.getDescription();
		PluginManager pm = getServer().getPluginManager();
		logger.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is now enabled." );
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		chestBanks = getChestBanks();
		saveChestBanks();
	}
	
	public boolean onCommand (CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Sorry! The console can't use this command!");
			return true;
		}
		Player player = (Player) sender;
		if (args.length == 0) {
			// Command list requested
			PluginDescriptionFile pdfFile = this.getDescription();
			if (player.hasPermission("chestbank.create") || player.hasPermission("chestbank.remove") || player.hasPermission("chestbank.see")) {
				player.sendMessage(ChatColor.GOLD + pdfFile.getName() + " version " + pdfFile.getVersion() + " by " + pdfFile.getAuthors());
				if (player.hasPermission("chestbank.create")) {
					player.sendMessage(ChatColor.GOLD + "  /chestbank create " + ChatColor.GRAY + ": Make targetted chest a ChestBank.");
				}
				if (player.hasPermission("chestbank.remove")) {
					player.sendMessage(ChatColor.GOLD + "  /chestbank remove " + ChatColor.GRAY + ": Make targetted ChestBank a chest.");
				}
				if (player.hasPermission("chestbank.see")) {
					player.sendMessage(ChatColor.GOLD + "  /chestbank see [player] " + ChatColor.GRAY + ": View player's ChestBank account.");
				}
				return true;
			}
			else {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
				return false;
			}
		}
		else if (args.length == 1) {
			// Create or Remove ('See' requires more args)
			if (args[0].equalsIgnoreCase("see")) {
				player.sendMessage(ChatColor.RED + "Please specify a player!");
				return false;
			}
			else if (args[0].equalsIgnoreCase("create")) {
				if (!player.hasPermission("chestbank.create")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to create a ChestBank!");
					return true;
				}
				Block block = player.getTargetBlock(null, 4);
				if (block.getTypeId() != 54) {
					player.sendMessage(ChatColor.RED + "You're not looking at a chest!");
					return true;
				}
				if (isBankBlock(block)) {
					player.sendMessage(ChatColor.RED + "That is already a ChestBank!");
					return true;
				}
				String bankList = chestBanks.getString("banks", "");
				if (bankList == "") {
					bankList += block.getX() + ":" + block.getY() + ":" + block.getZ();
				}
				else {
					bankList += ";" + block.getX() + ":" + block.getY() + ":" + block.getZ();
				}
				Block doubleChest = getDoubleChest(block);
				if (doubleChest != null) {
					bankList += ":" + doubleChest.getX() + ":" + doubleChest.getY() + ":" + doubleChest.getZ();
				}
				chestBanks.set("banks", bankList);
				saveChestBanks();
				player.sendMessage(ChatColor.GOLD + "ChestBank created!");
				return true;
			}
			else if (args[0].equalsIgnoreCase("remove")) {
				if (!player.hasPermission("chestbank.remove")) {
					player.sendMessage(ChatColor.RED + "You do not have permission to remove a ChestBank!");
					return true;
				}
				Block block = player.getTargetBlock(null, 4);
				if (!isBankBlock(block)) {
					player.sendMessage(ChatColor.RED + "You're not looking at a ChestBank!");
					return true;
				}
				String bankList = chestBanks.getString("banks");
				String[] bankSplit = bankList.split(";");
				if (bankSplit.length == 0 || bankSplit.length == 1) {
					chestBanks.set("banks", "");
				}
				else {
					String newBankList = "";
					for (String chestBank : bankSplit) {
						String[] bankLoc = chestBank.split(":");
						int blockX = Integer.parseInt(bankLoc[0]);
						int blockY = Integer.parseInt(bankLoc[1]);
						int blockZ = Integer.parseInt(bankLoc[2]);
						if (blockX != block.getX() || blockY != block.getY() || blockZ != block.getZ()) {
							if (newBankList != "") {
								newBankList += ";";
							}
							newBankList += blockX + ":" + blockY + ":" + blockZ;
							if (bankLoc.length == 6) {
								blockX = Integer.parseInt(bankLoc[3]);
								blockY = Integer.parseInt(bankLoc[4]);
								blockZ = Integer.parseInt(bankLoc[5]);
								newBankList += ":" + blockX + ":" + blockY + ":" + blockZ;
							}
						}
					}
				}
				saveChestBanks();
				player.sendMessage(ChatColor.GOLD + "ChestBank removed!");
				return true;
			}
		}
		else if (args.length == 2) {
			if (!args[0].equalsIgnoreCase("see")) {
				return false;
			}
			if (!player.hasPermission("chestbank.see")) {
				player.sendMessage(ChatColor.RED + "You do not have permission to access other players' accounts!");
				return true;
			}
			Block block = player.getTargetBlock(null, 4);
			if (!isBankBlock(block)) {
				player.sendMessage(ChatColor.RED + "You're not looking at a ChestBank!");
				return true;
			}
		}
		return false;
	}
	
	public boolean isBankBlock (Block block) {
		// Check if the block is a ChestBank
		String bankList = chestBanks.getString("banks", "");
		if (bankList != "") {
			String[] bankSplit = bankList.split(";");
			for (String bank : bankSplit) {
				String[] bankCoords = bank.split(":");
				int blockX = Integer.parseInt(bankCoords[0]);
				int blockY = Integer.parseInt(bankCoords[1]);
				int blockZ = Integer.parseInt(bankCoords[2]);
				if (block.getX() == blockX && block.getY() == blockY && block.getZ() == blockZ) {
					return true;
				}
				if (bankCoords.length == 6) {
					blockX = Integer.parseInt(bankCoords[3]);
					blockY = Integer.parseInt(bankCoords[4]);
					blockZ = Integer.parseInt(bankCoords[5]);
					if (block.getX() == blockX && block.getY() == blockY && block.getZ() == blockZ) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public Block getDoubleChest(Block block) {
		int blockX = block.getX();
		int blockY = block.getY();
		int blockZ = block.getZ();
		if (block.getWorld().getBlockAt(blockX + 1, blockY, blockZ).getTypeId() == 54) {
			return block.getWorld().getBlockAt(blockX + 1, blockY, blockZ);
		}
		if (block.getWorld().getBlockAt(blockX - 1, blockY, blockZ).getTypeId() == 54) {
			return block.getWorld().getBlockAt(blockX - 1, blockY, blockZ);
		}
		if (block.getWorld().getBlockAt(blockX , blockY, blockZ + 1).getTypeId() == 54) {
			return block.getWorld().getBlockAt(blockX, blockY, blockZ + 1);
		}
		if (block.getWorld().getBlockAt(blockX , blockY, blockZ - 1).getTypeId() == 54) {
			return block.getWorld().getBlockAt(blockX, blockY, blockZ - 1);
		}
		return null;
	}
	
	public void loadChestBanks() {
		if (bankFile == null) {
			bankFile = new File(getDataFolder(),"accounts.yml");
		}
		chestBanks = YamlConfiguration.loadConfiguration(bankFile);
	}
	
	public FileConfiguration getChestBanks() {
		if (chestBanks == null) {
			loadChestBanks();
		}
		return chestBanks;
	}
	
	public void saveChestBanks() {
		if (chestBanks == null || bankFile == null) {
			return;
		}
		try {
			chestBanks.save(bankFile);
		} catch (IOException ex) {
			this.logger.log(Level.SEVERE, "Could not save " + bankFile, ex );
		}
	}

}
