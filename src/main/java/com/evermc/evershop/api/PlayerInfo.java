package com.evermc.evershop.api;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

import org.jetbrains.annotations.NotNull;

public interface PlayerInfo {

    /**
     * Gets the internal player id
     * 
     * @return Player ID
     */
    public int getId();

    /**
     * Gets the recorded player UUID
     * 
     * @return Player UUID
     */
    public UUID getUUID();

    /**
     * Gets the latest known name of the player
     * 
     * @return Player name
     */
    public String getName();
    
    /**
     * Returns if this player is in advanced mode
     * 
     * @return true if the player is in advanced mode
     */
    public boolean isAdvanced();

    /**
     * Gets the locations the player selected with {@link org.bukkit.event.block.Action#LEFT_CLICK_BLOCK}
     * 
     * @return An immutable Set of {@link org.bukkit.Location}
     */
    @NotNull
    public Set<Location> getRegLeftClick();

    /**
     * Gets the locations the player selected with {@link org.bukkit.event.block.Action#RIGHT_CLICK_BLOCK}
     * 
     * @return An immutable Set of {@link org.bukkit.Location}
     */
    @NotNull
    public Set<Location> getRegRightClick();
}