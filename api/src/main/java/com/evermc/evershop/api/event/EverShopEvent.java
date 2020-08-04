package com.evermc.evershop.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base event
 * 
 */
public class EverShopEvent extends Event implements Cancellable{

    private Player player;
    private Result result = Result.DEFAULT;
    private static final HandlerList handlers = new HandlerList();

    public EverShopEvent(Player player) {
        this.player = player;
    }

    public void setCancelled(boolean cancel) {
        if (cancel) {
            this.result = Result.DENY;
        } else if(this.result == Result.DENY) {
            this.result = Result.DEFAULT;
        }
    }

    public boolean isCancelled() {
        return this.result == Result.DENY;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Result getResult() {
        return this.result;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}