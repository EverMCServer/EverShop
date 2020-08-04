package com.evermc.evershop.api.event;

import com.evermc.evershop.api.ShopType;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * Called when a player try to create a shop 
 * 
 * <pre>
 * If the event is cancelled / set result to DENY, the creation will be rejected.
 * If the event is set to DEFAULT, the regular permission(evershop.create.*, evershop.multiworld, evershop.create.price) check will take place.
 * If the event is set to ALLOW, all permission check will be skipped.
 * </pre>
 */
public class CreateShopEvent extends EverShopEvent{
    
    private ShopType shopType;
    private Sign sign;
    private int price;

    public CreateShopEvent(Player player, ShopType shopType, Sign sign, int price) {
        super(player);
        this.shopType = shopType;
        this.sign = sign;
        this.price = price;
    }

    public ShopType getType() {
        return this.shopType;
    }

    public Sign getSign() {
        return this.sign;
    }

    public int getPrice() {
        return this.price;
    }

}