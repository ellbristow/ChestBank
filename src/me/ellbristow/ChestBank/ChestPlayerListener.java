package me.ellbristow.ChestBank;

import java.util.logging.Logger;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.TileEntityChest;

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
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block block = event.getClickedBlock();
			if (block.getTypeId() == 54 && plugin.isBankBlock(block)) {
				Player player = event.getPlayer();
				EntityPlayer ePlayer;
				CraftPlayer cPlayer;
				cPlayer = (CraftPlayer) player;
				ePlayer = cPlayer.getHandle();
				TileEntityChest ch1 = new  TileEntityChest ();
				TileEntityChest ch2 = new  TileEntityChest ();
				InventoryLargeChest lc = new InventoryLargeChest("ChestBank", ch1, ch2);
				ePlayer.a(lc);
				event.setCancelled(true);
			}
		}
	}
}
