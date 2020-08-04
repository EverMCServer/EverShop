package com.evermc.evershop.api.event;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Called when a player try to register a block (not signs)
 * 
 * <pre>
 * If the event is cancelled / set result to DENY, the registration will be rejected.
 * If the event is set to DEFAULT, the regular permission (WorldGuard/LockettePro) check will take place.
 * If the event is set to ALLOW, permission check will be skipped.
 * </pre>
 */
public class RegisterBlockEvent extends EverShopEvent {

    private Block block;

    public RegisterBlockEvent(Player player, Block block) {
        super(player);
        this.block = block;
    }
    
    public Block getBlock() {
        return this.block;
    }

    public Location getClickedLocation() {
        return this.block.getLocation();
    }
}