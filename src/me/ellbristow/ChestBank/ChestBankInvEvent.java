package me.ellbristow.ChestBank;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class ChestBankInvEvent extends Event implements Cancellable {
    
    private Player player;
    protected boolean cancelled;
    
    public ChestBankInvEvent (Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
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
