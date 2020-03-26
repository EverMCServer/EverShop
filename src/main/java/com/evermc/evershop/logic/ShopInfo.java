package com.evermc.evershop.logic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.util.LogUtil;
import com.evermc.evershop.util.SerializableLocation;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

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
    Set<SerializableLocation> targets;
    Set<ItemStack> items;
    String perm;
    
    public ShopInfo(EverShop plugin, int action_id, int player_id, Location loc, int price, Set<Location> targets, Set<ItemStack> items, String perm){
        this(0, (int)(System.currentTimeMillis()/1000), action_id, player_id, DataLogic.getWorldId(loc.getWorld()), 
        loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), price, null, items, perm);
        this.targets = new HashSet<SerializableLocation>();
        for (Location loca : targets){
            this.targets.add(new SerializableLocation(plugin,loca));
        }
    }

    public ShopInfo(EverShop plugin, int action_id, Player p, Location loc, int price, Set<Location> targets, Set<ItemStack> items, String perm){
        this(plugin, action_id, PlayerLogic.getPlayer(p), loc, price, targets, items, perm);
    }

    public ShopInfo(int id, int epoch, int action_id, int player_id, int world_id, int x, int y, int z, int price, byte[] targets, byte[] items, String perm){

        this(id, epoch, action_id, player_id, world_id, x, y, z, price, (Set<SerializableLocation>)null, null, perm);
        try{
            BukkitObjectInputStream in  = new BukkitObjectInputStream(new ByteArrayInputStream(targets));
            Set<SerializableLocation> _targets = new HashSet<SerializableLocation>();
            Object obj = in.readObject();
            if (obj instanceof Set<?>){
                for (Object k : (Set<?>)obj){
                    if (k instanceof SerializableLocation){
                        _targets.add((SerializableLocation)k);
                    }
                }
            }
            if (_targets.size() == 0){
                LogUtil.log(Level.WARNING, "Error when reading ShopInfo! shopid = "+id);
                return;
            }
            this.targets = _targets;
            Set<ItemStack> _items = new HashSet<ItemStack>();
            in.close();
            in  = new BukkitObjectInputStream(new ByteArrayInputStream(items));
            obj = in.readObject();
            if (obj instanceof Set<?>){
                for (Object k : (Set<?>)obj){
                    if (k instanceof ItemStack){
                        _items.add((ItemStack)k);
                    }
                }
            }
            if (_items.size() == 0){
                LogUtil.log(Level.WARNING, "Error when reading ShopInfo! shopid = "+id);
                return;
            }
            this.items = _items;
        }catch (IOException e){
            LogUtil.log(Level.WARNING, "Error when reading ShopInfo! shopid = "+id);
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            LogUtil.log(Level.WARNING, "Error when reading ShopInfo! shopid = "+id);
            e.printStackTrace();
        }
    }

    public ShopInfo(int id, int epoch, int action_id, int player_id, int world_id, int x, int y, int z, int price, Set<SerializableLocation> targets, Set<ItemStack> items, String perm){
        this.id = id;
        this.epoch = epoch;
        this.action_id = action_id;
        this.player_id = player_id;
        this.world_id = world_id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.price = price;
        this.targets = targets;
        this.items = items;
        this.perm = perm;
    }

    public String toString(){
        return "ShopInfo{id=" + this.id +", epoch=" + this.epoch + ", action_id=" + this.action_id + ", player_id=" + this.player_id + ", world_id=" + this.world_id
         + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", price=" + this.price + ", targets=" + this.targets + ", items=" + this.items + ", perm=" + this.perm;
    }
}
