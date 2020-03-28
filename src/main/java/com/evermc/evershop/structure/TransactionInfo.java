package com.evermc.evershop.structure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.evermc.evershop.handler.VaultHandler;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.util.SerializableLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * When getting contents of a block:
 *  ((Container)Block.getState()).getInventory().getContents()
 * getState() is 10x slower than all other API, so we cache all Inventories when start a transaction.
 */
public class TransactionInfo{
    final private HashSet<Inventory> shopOut;
    final private HashSet<Inventory> shopIn;
    private Inventory playerInv;
    final private HashMap<ItemStack, Integer> itemsOut;
    final private HashMap<ItemStack, Integer> itemsIn;
    private Player player;
    private OfflinePlayer owner;
    private int price;

    public TransactionInfo(ShopInfo si, Player p){
        this.playerInv = p.getInventory();
        this.player = p;
        this.owner = Bukkit.getOfflinePlayer(PlayerLogic.getPlayerInfo(si.player_id).uuid);
        this.price = si.price;
        // targets
        if (TransactionLogic.targetCount(si.action_id) == 2){
            shopOut = new HashSet<Inventory>();
            shopIn = new HashSet<Inventory>();
            addAllTargets(shopOut, si.getDoubleTargets().get(0));
            addAllTargets(shopIn, si.getDoubleTargets().get(1));
        } else if (si.action_id == TransactionLogic.BUY.id()){
            shopOut = new HashSet<Inventory>();
            shopIn = null;
            addAllTargets(shopOut, si.getAllTargets());
        } else if (si.action_id == TransactionLogic.SELL.id()){
            shopOut = null;
            shopIn = new HashSet<Inventory>();
            addAllTargets(shopIn, si.getAllTargets());
        } else {
            shopOut = null; shopIn = null;
        }
        // items
        if (TransactionLogic.itemsetCount(si.action_id) == 2){
            itemsOut = new HashMap<ItemStack, Integer>();
            itemsIn = new HashMap<ItemStack, Integer>();
            addAllItems(itemsOut, si.getDoubleItems().get(0));
            addAllItems(itemsIn, si.getDoubleItems().get(1));
        } else if (si.action_id == TransactionLogic.BUY.id() || si.action_id == TransactionLogic.IBUY.id() ){
            itemsOut = new HashMap<ItemStack, Integer>();
            addAllItems(itemsOut, si.getAllItems());
            itemsIn = null;
        } else if (si.action_id == TransactionLogic.SELL.id() || si.action_id == TransactionLogic.ISELL.id()){
            itemsOut = null;
            itemsIn = new HashMap<ItemStack, Integer>();
            addAllItems(itemsIn, si.getAllItems());
        } else {
            itemsOut = null; itemsIn = null;
        }
    }

    private void addAllTargets(Set<Inventory> out, Set<SerializableLocation> locs){
        for (SerializableLocation loc : locs){
            Location lo = loc.toLocation();
            BlockState bs = lo.getBlock().getState();
            if (bs instanceof Container)
            out.add(((Container)bs).getInventory());
        }
    }

    private void addAllItems(Map<ItemStack, Integer> out, Set<ItemStack> items){
        for (ItemStack is : items){
            out.put(is, is.getAmount());
        }
    }

    // Only checks if shopOut has itemsOut 
    public boolean shopHasItems(){
        HashMap<ItemStack, Integer> it = new HashMap<ItemStack, Integer>(itemsOut);
        for (Inventory iv : shopOut){
            for (ItemStack is : iv.getContents()){
                if (is == null) continue;
                for (ItemStack its : it.keySet()){
                    if (its.isSimilar(is)){
                        if (it.get(its) > is.getAmount()){
                            it.put(its, it.get(its) - is.getAmount());
                        } else {
                            it.remove(its);
                        }
                        break;
                    }
                }
            }
        }
        return it.size() == 0;
    }

    // Only checks if shopIn has itemsIn 
    public boolean shopCanHold(){
        HashMap<ItemStack, Integer> it = new HashMap<ItemStack, Integer>(itemsIn);
        int emptycount = 0;
        for (Inventory iv : shopIn){
            for (ItemStack is : iv.getContents()){
                if (is == null){
                    emptycount ++;
                    if (emptycount >= it.size()) return true;
                    continue;
                }
                for (ItemStack its : it.keySet()){
                    if (is.getAmount() < is.getMaxStackSize() && its.isSimilar(is)){
                        int reduce = is.getMaxStackSize() - is.getAmount();
                        if (it.get(its) > reduce){
                            it.put(its, it.get(its) - reduce);
                        } else {
                            it.remove(its);
                        }
                        break;
                    }
                }
            }
        }
        return it.size() <= emptycount;
    }

    // Only checks if player has itemsIn 
    public boolean playerHasItems(){
        HashMap<ItemStack, Integer> it = new HashMap<ItemStack, Integer>(itemsIn);
        for (ItemStack is : playerInv.getStorageContents()){
            if (is == null) continue;
            for (ItemStack its : it.keySet()){
                if (its.isSimilar(is)){
                    if (it.get(its) > is.getAmount()){
                        it.put(its, it.get(its) - is.getAmount());
                    } else {
                        it.remove(its);
                    }
                    break;
                }
            }
        }
        return it.size() == 0;
    }

    // Only checks if player has itemsOut 
    public boolean playerCanHold(){
        HashMap<ItemStack, Integer> it = new HashMap<ItemStack, Integer>(itemsOut);
        int emptycount = 0;
        for (ItemStack is : playerInv.getStorageContents()){
            if (is == null){
                emptycount ++;
                if (emptycount >= it.size()) return true;
                continue;
            }
            for (ItemStack its : it.keySet()){
                if (is.getAmount() < is.getMaxStackSize() && its.isSimilar(is)){
                    int reduce = is.getMaxStackSize() - is.getAmount();
                    if (it.get(its) > reduce){
                        it.put(its, it.get(its) - reduce);
                    } else {
                        it.remove(its);
                    }
                    break;
                }
            }
        }
        return it.size() <= emptycount;
    }

    // TODO - tax logic
    public boolean playerHasMoney(){
        return VaultHandler.getEconomy().getBalance(this.player) >= this.price;
    }

    // TODO - tax logic
    public boolean shopHasMoney(){
        return VaultHandler.getEconomy().getBalance(this.owner) >= this.price;
    }
}