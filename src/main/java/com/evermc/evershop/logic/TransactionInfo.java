package com.evermc.evershop.logic;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.util.SerializableLocation;

import org.bukkit.Location;
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
    private Set<Inventory> shopInv = new HashSet<Inventory>();
    private Inventory playerInv;
    private Set<ItemStack> items;

    public TransactionInfo(EverShop plugin, Set<SerializableLocation> targets, Player p, Set<ItemStack> items){
        this.shopInv = new HashSet<Inventory>();
        this.playerInv = p.getInventory();
        this.items = items;
        for (SerializableLocation loc : targets){
            Location lo = loc.toLocation();
            BlockState bs = lo.getBlock().getState();
            if (bs instanceof Container)
            this.shopInv.add(((Container)bs).getInventory());
        }
    }

    public boolean shopHasItems(){
        Set<ItemStack> it = new CopyOnWriteArraySet<ItemStack>();
        it.addAll(items);
        for (Inventory iv : shopInv){
            for (ItemStack is : iv.getContents()){
                if (is == null) continue;
                for (ItemStack its : it){
                    if (its.isSimilar(is)){
                        if (its.getAmount() > is.getAmount()){
                            its.setAmount(its.getAmount() - is.getAmount());
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

    public boolean shopCanHold(){
        Set<ItemStack> it = new CopyOnWriteArraySet<ItemStack>();
        it.addAll(items);
        int emptycount = 0;
        for (Inventory iv : shopInv){
            for (ItemStack is : iv.getContents()){
                if (is == null){
                    emptycount ++;
                    if (emptycount >= it.size()) return true;
                    continue;
                }
                for (ItemStack its : it){
                    if (is.getAmount() < is.getMaxStackSize() && its.isSimilar(is)){
                        int reduce = is.getMaxStackSize() - is.getAmount();
                        if (its.getAmount() > reduce){
                            its.setAmount(its.getAmount() - reduce);
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

    public boolean playerHasItems(){
        Set<ItemStack> it = new CopyOnWriteArraySet<ItemStack>();
        it.addAll(items);
        for (ItemStack is : playerInv.getStorageContents()){
            if (is == null) continue;
            for (ItemStack its : it){
                if (its.isSimilar(is)){
                    if (its.getAmount() > is.getAmount()){
                        its.setAmount(its.getAmount() - is.getAmount());
                    } else {
                        it.remove(its);
                    }
                    break;
                }
            }
        }
        return it.size() == 0;
    }

    public boolean playerCanHold(){
        Set<ItemStack> it = new CopyOnWriteArraySet<ItemStack>();
        it.addAll(items);
        int emptycount = 0;
        for (ItemStack is : playerInv.getStorageContents()){
            if (is == null){
                emptycount ++;
                continue;
            } 
            for (ItemStack its : it){
                if (is.getAmount() < is.getMaxStackSize() && its.isSimilar(is)){
                    int reduce = is.getMaxStackSize() - is.getAmount();
                    if (its.getAmount() > reduce){
                        its.setAmount(its.getAmount() - reduce);
                    } else {
                        it.remove(its);
                    }
                    break;
                }
            }
        }
        return it.size() <= emptycount;
    }
}