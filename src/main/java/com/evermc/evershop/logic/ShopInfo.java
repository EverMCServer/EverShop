package com.evermc.evershop.logic;

import java.util.HashSet;

import com.evermc.evershop.EverShop;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopInfo {
    int id;
    int epoch;
    int action_id;
    int player_id;
    int world_id;
    int x;
    int y;
    int z;
    int price;
    HashSet<Location> targets;
    HashSet<ItemStack> items;
    
    public ShopInfo(EverShop plugin, int action_id, int player_id, Location loc, int price, HashSet<Location> targets, HashSet<ItemStack> items){
        this.id = 0;
        this.epoch = (int)(System.currentTimeMillis()/1000);
        this.action_id = action_id;
        this.player_id = player_id;
        this.world_id = plugin.getDataLogic().getWorldId(loc.getWorld());
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.price = price;
        this.targets = targets;
        this.items = items;
    }

    public ShopInfo(EverShop plugin, int action_id, Player p, Location loc, int price, HashSet<Location> targets, HashSet<ItemStack> items){
        this(plugin, action_id, plugin.getPlayerLogic().getPlayer(p), loc, price, targets, items);
    }

}