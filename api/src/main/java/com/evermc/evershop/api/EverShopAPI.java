package com.evermc.evershop.api;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface EverShopAPI extends Plugin{
    public PlayerInfo getPlayerInfo(Player player);
}