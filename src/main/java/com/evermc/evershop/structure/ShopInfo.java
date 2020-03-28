package com.evermc.evershop.structure;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.util.LogUtil;
import com.evermc.evershop.util.SerializableLocation;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

public class ShopInfo {
    public int id;
    public int epoch;
    public int action_id;
    public int player_id;
    public int world_id;
    public int x;
    public int y;
    public int z;
    public int price;
    public Object targets;
    public Object items;
    public String extra;

    // database -> ShopInfo
    public ShopInfo(Object[] data){

        byte[] targets;
        byte[] items;

        try{
            this.id = (int) data[0];
            this.epoch = (int) data[1];
            this.action_id = (int) data[2];
            this.player_id = (int) data[3];
            this.world_id = (int) data[4];
            this.x = (int) data[5];
            this.y = (int) data[6];
            this.z = (int) data[7];
            this.price = (int) data[8];
            targets = (byte[]) data[9];
            items = (byte[]) data[10];
            this.extra = (String) data[11];
        }catch(Exception e){
            LogUtil.log(Level.SEVERE, "Internal error: fail to cast data. Database corruption?");
            e.printStackTrace();
            this.id = 0;
            return;
        }
        try{
            // handle targets
            BukkitObjectInputStream in  = new BukkitObjectInputStream(new ByteArrayInputStream( targets ));
            if (TransactionLogic.targetCount(this.action_id) == 1){
                Object obj = in.readObject();
                HashSet<SerializableLocation> _targets = new HashSet<SerializableLocation>();
                if (obj instanceof Set<?>){
                    for (Object k : (Set<?>)obj){
                        if (k instanceof SerializableLocation){
                            _targets.add((SerializableLocation)k);
                        }
                    }
                }
                if (_targets.size() == 0){
                    LogUtil.log(Level.SEVERE, "No targets found in shop " + this.id + ", data corruption?");
                    this.id = 0;
                    return;
                }
                this.targets = _targets;
            } else if (TransactionLogic.targetCount(this.action_id) == 2){
                HashSet<SerializableLocation> set1 = new HashSet<SerializableLocation>();
                HashSet<SerializableLocation> set2 = new HashSet<SerializableLocation>();
                Object obj = in.readObject();
                if (obj instanceof ArrayList<?>){
                    ArrayList<?> k = (ArrayList<?>) obj;
                    if ((k.get(0) instanceof HashSet<?>) && (k.get(1) instanceof HashSet<?>)){
                        for (Object it : (HashSet<?>)k.get(0)){
                            if (it instanceof SerializableLocation){
                                set1.add((SerializableLocation)it);
                            }
                        }
                        for (Object it : (HashSet<?>)k.get(1)){
                            if (it instanceof SerializableLocation){
                                set2.add((SerializableLocation)it);
                            }
                        }
                    }
                }
                if (set1.size() == 0 || set2.size() == 0){
                    LogUtil.log(Level.SEVERE, "No items found in shop " + this.id + ", data corruption?");
                    this.id = 0;
                    return;
                }
                ArrayList<HashSet<SerializableLocation>> _targets = new ArrayList<HashSet<SerializableLocation>>();
                _targets.add(set1);
                _targets.add(set2);
                this.targets = _targets;
            }
            in.close();
            // handle items
            in  = new BukkitObjectInputStream(new ByteArrayInputStream( items ));
            if (TransactionLogic.itemsetCount(this.action_id) == 1){
                Object obj = in.readObject();
                HashSet<ItemStack> _items = new HashSet<ItemStack>();
                if (obj instanceof Set<?>){
                    for (Object k : (Set<?>)obj){
                        if (k instanceof ItemStack){
                            _items.add((ItemStack)k);
                        }
                    }
                }
                if (_items.size() == 0){
                    LogUtil.log(Level.SEVERE, "No items found in shop " + this.id + ", data corruption?");
                    this.id = 0;
                    return;
                }
                this.items = _items;
            } else if (TransactionLogic.itemsetCount(this.action_id) == 2){
                HashSet<ItemStack> set1 = new HashSet<ItemStack>();
                HashSet<ItemStack> set2 = new HashSet<ItemStack>();
                Object obj = in.readObject();
                if (obj instanceof ArrayList<?>){
                    ArrayList<?> k = (ArrayList<?>) obj;
                    if ((k.get(0) instanceof HashSet<?>) && (k.get(1) instanceof HashSet<?>)){
                        for (Object it : (HashSet<?>)k.get(0)){
                            if (it instanceof ItemStack){
                                set1.add((ItemStack)it);
                            }
                        }
                        for (Object it : (HashSet<?>)k.get(1)){
                            if (it instanceof ItemStack){
                                set2.add((ItemStack)it);
                            }
                        }
                    }
                }
                if (set1.size() == 0 || set2.size() == 0){
                    LogUtil.log(Level.SEVERE, "No items found in shop " + this.id + ", data corruption?");
                    this.id = 0;
                    return;
                }
                ArrayList<HashSet<ItemStack>> _items = new ArrayList<HashSet<ItemStack>>();
                _items.add(set1);
                _items.add(set2);
                this.items = _items;
            }
            
        }catch(Exception e){
            LogUtil.log(Level.WARNING, "Error when reading ShopInfo! shopid = "+id);
            e.printStackTrace();
        }
    }

    public ShopInfo(int action_id, PlayerInfo pi, Location shoploc, int price){
        this.id = 0;
        this.epoch = (int)(System.currentTimeMillis()/1000);
        this.action_id = action_id;
        this.player_id = pi.id;
        this.world_id = DataLogic.getWorldId(shoploc.getWorld());
        this.x = shoploc.getBlockX();
        this.y = shoploc.getBlockY();
        this.z = shoploc.getBlockZ();
        if (action_id != TransactionLogic.ITRADE.id() && action_id != TransactionLogic.TRADE.id())
            price = Math.abs(price);
        this.price = price;
        // TODO - init extra info when creating shopinfo
        this.extra = "";
        if (TransactionLogic.targetCount(action_id) == 1){
            HashSet<SerializableLocation> _targets = new HashSet<SerializableLocation>();
            for (Location loca : pi.reg1){
                _targets.add(new SerializableLocation(loca));
            }
            for (Location loca : pi.reg2){
                _targets.add(new SerializableLocation(loca));
            }
            this.targets = _targets;
        } else if (TransactionLogic.targetCount(action_id) == 2){
            ArrayList<HashSet<SerializableLocation>> _targets = new ArrayList<HashSet<SerializableLocation>>();
            HashSet<SerializableLocation> set1 = new HashSet<SerializableLocation>();
            for (Location loca : pi.reg1){
                set1.add(new SerializableLocation(loca));
            }
            HashSet<SerializableLocation> set2 = new HashSet<SerializableLocation>();
            for (Location loca : pi.reg2){
                set2.add(new SerializableLocation(loca));
            }
            _targets.add(set1);
            _targets.add(set2);
            this.targets = _targets;
        }
        if (TransactionLogic.itemsetCount(action_id) == 1){
            HashSet<ItemStack> _items = new HashSet<ItemStack>();
            _items.addAll(ShopLogic.getReg1(pi));
            this.items = _items;
        } else if (TransactionLogic.itemsetCount(action_id) == 2){
            ArrayList<HashSet<ItemStack>> _items = new ArrayList<HashSet<ItemStack>>();
            _items.add(ShopLogic.getReg1(pi));
            _items.add(ShopLogic.getReg2(pi));
            this.items = _items;
        }
    }

    @SuppressWarnings("unchecked")
    public HashSet<SerializableLocation> getAllTargets(){
        if (TransactionLogic.targetCount(this.action_id) == 1){
            HashSet<SerializableLocation> ret = new HashSet<SerializableLocation>();
            ret.addAll((HashSet<SerializableLocation>)this.targets);
            return ret;
        } else if (TransactionLogic.targetCount(this.action_id) == 2){
            HashSet<SerializableLocation> ret = new HashSet<SerializableLocation>();
            ret.addAll((HashSet<SerializableLocation>)((ArrayList<HashSet<SerializableLocation>>)this.targets).get(0));
            ret.addAll((HashSet<SerializableLocation>)((ArrayList<HashSet<SerializableLocation>>)this.targets).get(1));
            return ret;
        }else{
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<HashSet<SerializableLocation>> getDoubleTargets(){
        if (TransactionLogic.targetCount(this.action_id) == 2)
            return (ArrayList<HashSet<SerializableLocation>>)this.targets;
        else 
            return null;
    }

    @SuppressWarnings("unchecked")
    public HashSet<ItemStack> getAllItems(){
        System.out.println("getAllItems: "+this.items);
        if (TransactionLogic.itemsetCount(this.action_id) == 1){
            HashSet<ItemStack> ret = new HashSet<ItemStack>();
            ret.addAll((HashSet<ItemStack>)this.items);
            return ret;
        } else if (TransactionLogic.itemsetCount(this.action_id) == 2){
            HashSet<ItemStack> ret = new HashSet<ItemStack>();
            ret.addAll((HashSet<ItemStack>)((ArrayList<HashSet<ItemStack>>)this.items).get(0));
            ret.addAll((HashSet<ItemStack>)((ArrayList<HashSet<ItemStack>>)this.items).get(1));
            return ret;
        }else{
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<HashSet<ItemStack>> getDoubleItems(){
        if (TransactionLogic.itemsetCount(this.action_id) == 2)
            return (ArrayList<HashSet<ItemStack>>)this.items;
        else 
            return null;
    }

    public String toString(){
        return "ShopInfo{id=" + this.id +", epoch=" + this.epoch + ", action_id=" + this.action_id + ", player_id=" + this.player_id + ", world_id=" + this.world_id
         + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", price=" + this.price + ", targets=" + this.targets + ", items=" + this.items + ", extra=" + this.extra + "}";
    }
}
