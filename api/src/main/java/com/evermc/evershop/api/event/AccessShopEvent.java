package com.evermc.evershop.api.event;

import com.evermc.evershop.api.ShopInfo;

import org.bukkit.entity.Player;

/**
 * Called when a player try to access a shop (with right click)
 * 
 * <pre>
 * If the event is cancelled / set result to DENY, the access will be rejected.
 * If the event is set to DEFAULT, the regular permission check will take place.
 * If the event is set to ALLOW, permission check will be skipped.
 * </pre>
 */
public class AccessShopEvent extends EverShopEvent{

    private ShopInfo shopInfo;

    public AccessShopEvent(Player player, ShopInfo shopInfo) {
        super(player);
        this.shopInfo = shopInfo;
    }

    public ShopInfo getShopInfo() {
        return this.shopInfo;
    }
}