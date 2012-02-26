package me.ellbristow.ChestBank;

import net.minecraft.server.InventoryLargeChest;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

class ChestBankCloseEvent extends ChestBankInvEvent implements ChestBankEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private static final ChestBankEventType type = ChestBankEventType.INVENTORY_OPEN;
    public static ChestBank plugin;
    
    public ChestBankCloseEvent (Player player, ChestBank instance) {
        super(player);
        plugin = instance;
        this.player = player;
    }
    
    @Override
    public ChestBankEventType getEventType() {
        return type;
    }
    
    public InventoryLargeChest getInventory() {
        return plugin.chestAccounts.get(this.player.getName());
    }
    
    public Inventory getPlayerInventory() {
        return player.getInventory();
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
