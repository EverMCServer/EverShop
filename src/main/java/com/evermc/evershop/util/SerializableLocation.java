package com.evermc.evershop.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.evermc.evershop.EverShop;

import org.bukkit.Location;

/**
 * The same to the ConfigurationSerializable version of Location
 * This is created to avoid using Bukkit API in async operations.
 * 
 * Just used to save chest/redstone places -> use int, no yaw and pitch
 */
public class SerializableLocation implements Serializable{

    static final long serialVersionUID = 1L;
    public int x;
    public int y;
    public int z;
    public int world;
    public SerializableLocation(EverShop plugin, Location loc){
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.world = plugin.getDataLogic().getWorldId(loc.getWorld());
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("world", this.world);
        data.put("x", this.x);
        data.put("y", this.y);
        data.put("z", this.z);
        return data;
    }

    public Location toLocation(EverShop plugin){
        return new Location(plugin.getDataLogic().getWorld(this.world), this.x, this.y, this.z);
    }

    public String toString(){
        return "SeriLoc{world=" + this.world + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z;
    }
}