package me.ellbristow.ChestBank;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.Enchantment;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.ItemStack;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.TileEntityChest;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestBank extends JavaPlugin {
	
	public static ChestBank plugin;
	public final Logger logger = Logger.getLogger("Minecraft");
	public FileConfiguration banksConfig = null;
	private File bankFile = null;
	public final ChestBlockListener blockListener = new ChestBlockListener(this);
	public ChestPlayerListener chestListener;
	public HashMap<String, InventoryLargeChest> chestBanks;

	@Override
	public void onDisable () {
		setChests(chestBanks);
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info( "[" + pdfFile.getName() + "] is now disabled.");
	}

	@Override
	public void onEnable () {
		PluginDescriptionFile pdfFile = this.getDescription();
		PluginManager pm = getServer().getPluginManager();
		logger.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is now enabled." );
		chestListener = new ChestPlayerListener(this);
		pm.registerEvents(blockListener, this);
		pm.registerEvents(chestListener, this);
		banksConfig = getChestBanks();
		saveChestBanks();
		chestBanks = getChests();
		int autosaveInterval = 5 * 3000;
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				setChests(chestBanks);
				logger.fine("[ChestBank] auto-saved ChestBanks");
			}
		}, autosaveInterval, autosaveInterval);
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
				String bankList = banksConfig.getString("banks", "");
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
				banksConfig.set("banks", bankList);
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
				String bankList = banksConfig.getString("banks");
				String[] bankSplit = bankList.split(";");
				if (bankSplit.length == 0 || bankSplit.length == 1) {
					banksConfig.set("banks", "");
				}
				else {
					String newBankList = "";
					for (String chestBank : bankSplit) {
						String[] bankLoc = chestBank.split(":");
						if (bankLoc.length == 3) {
							int blockX = Integer.parseInt(bankLoc[0]);
							int blockY = Integer.parseInt(bankLoc[1]);
							int blockZ = Integer.parseInt(bankLoc[2]);
							if (blockX != block.getX() || blockY != block.getY() || blockZ != block.getZ()) {
								if (newBankList != "") {
									newBankList += ";";
								}
								newBankList += blockX + ":" + blockY + ":" + blockZ;
							}
						}
						else {
							int blockX = Integer.parseInt(bankLoc[0]);
							int blockY = Integer.parseInt(bankLoc[1]);
							int blockZ = Integer.parseInt(bankLoc[2]);
							int blockA = Integer.parseInt(bankLoc[3]);
							int blockB = Integer.parseInt(bankLoc[4]);
							int blockC = Integer.parseInt(bankLoc[5]);
							if (!(blockX == block.getX() && blockY == block.getY() && blockZ == block.getZ()) && !(blockA == block.getX() && blockB == block.getY() && blockC == block.getZ())) {
								if (newBankList != "") {
									newBankList += ";";
								}
								newBankList += blockX + ":" + blockY + ":" + blockZ + ":" + blockA + ":" + blockB + ":" + blockC;
							}
						}
					}
					banksConfig.set("banks", newBankList);
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
			OfflinePlayer target = getServer().getOfflinePlayer(args[1]);
			if (target.hasPlayedBefore()) {
				EntityPlayer ePlayer;
				ePlayer = ((CraftPlayer) player).getHandle();
				InventoryLargeChest lc = chestBanks.get(target.getName());
				ePlayer.a(lc);
			}
			else {
				player.sendMessage(ChatColor.RED + target.getName() + " does not have a ChestBank account!");
				return true;
			}
		}
		return false;
	}
	
	public boolean isBankBlock (Block block) {
		// Check if the block is a ChestBank
		String bankList = banksConfig.getString("banks", "");
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
	
	public HashMap<String, InventoryLargeChest> getChests() {
		HashMap<String, InventoryLargeChest> chests = new HashMap<String, InventoryLargeChest>();
		ConfigurationSection chestSection = banksConfig.getConfigurationSection("accounts");
		if (chestSection != null) {
			Set<String> fileChests = chestSection.getKeys(false);
			if (fileChests != null) {
				for (String playerName : fileChests) {
					InventoryLargeChest returnInv = new InventoryLargeChest(playerName, new TileEntityChest(), new TileEntityChest());
					String[] chestInv = banksConfig.getString("accounts." + playerName).split(";");
					int i = 0;
					for (String items : chestInv) {
						String[] item = items.split(":");
						int i0 = Integer.parseInt(item[0]);
						int i1 = Integer.parseInt(item[1]);
						short i2 = Short.parseShort(item[2]);
						if(i0 != 0) {
							ItemStack stack = new ItemStack(i0, i1, i2);
							if (item.length == 4) {
								String[] enchArray = item[3].split(",");
								for (String ench : enchArray) {
									String[] bits = ench.split("~");
									int enchId = Integer.parseInt(bits[0]);
									int enchLvl = Integer.parseInt(bits[1]);
									stack.addEnchantment(Enchantment.byId[enchId], enchLvl);
								}
							}
							returnInv.setItem(i, stack);
						}
						i++;
					}
					chests.put(playerName, returnInv);
				}
			}
		}
		return chests;
	}
	
	public void setChests(HashMap<String, InventoryLargeChest> chests) {
		Set<String> chestKeys = chests.keySet();
		for (String key : chestKeys) {
			InventoryLargeChest chest = chests.get(key);
			String chestInv = "";
			for (ItemStack item : chest.getContents()) {
				chestInv += ";";
				if (item != null) {
					int itemID = item.id;
					int itemCount = item.count;
					int itemDamage = item.getData();
					chestInv += itemID + ":" + itemCount + ":" + itemDamage;
					if (item.hasEnchantments()) {
						NBTTagList itemEnch = item.getEnchantments();
						chestInv += ":";
						String enchList = "";
						for (int i = 0; i < itemEnch.size(); i++) {
							NBTTagCompound ench = (NBTTagCompound) itemEnch.get(i);
							short enchId = ench.getShort("id");
							short enchLvl = ench.getShort("lvl");
							enchList += "," + enchId + "~" + enchLvl;
						}
						chestInv += enchList.replaceFirst(",", "");
					}
				}
				else {
					chestInv += "0:0:0";
				}
			}
			banksConfig.set("accounts." + key, chestInv.replaceFirst(";", ""));
		}
		saveChestBanks();
	}
	
	public void loadChestBanks() {
		if (bankFile == null) {
			bankFile = new File(getDataFolder(),"chests.yml");
		}
		banksConfig = YamlConfiguration.loadConfiguration(bankFile);
	}
	
	public FileConfiguration getChestBanks() {
		if (banksConfig == null) {
			loadChestBanks();
		}
		return banksConfig;
	}
	
	public void saveChestBanks() {
		if (banksConfig == null || bankFile == null) {
			return;
		}
		try {
			banksConfig.save(bankFile);
		} catch (IOException ex) {
			this.logger.log(Level.SEVERE, "Could not save " + bankFile, ex );
		}
	}
}
