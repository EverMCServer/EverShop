package com.evermc.evershop.structure;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.TaxLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.util.LogUtil;
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
    private int action_id;

    public TransactionInfo(ShopInfo si, Player p){
        this.playerInv = p.getInventory();
        this.player = p;
        this.owner = Bukkit.getOfflinePlayer(PlayerLogic.getPlayerInfo(si.player_id).uuid);
        this.price = si.price;
        this.action_id = si.action_id;
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
        if (this.action_id != TransactionLogic.BUY.id() && this.action_id != TransactionLogic.TRADE.id()){
            return true;
        }
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
        if (this.action_id != TransactionLogic.SELL.id() && this.action_id != TransactionLogic.TRADE.id()){
            return true;
        }
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
        if (this.action_id != TransactionLogic.SELL.id() && this.action_id != TransactionLogic.ISELL.id() && this.action_id != TransactionLogic.ITRADE.id() && this.action_id != TransactionLogic.TRADE.id()){
            return true;
        }
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
        if (this.action_id != TransactionLogic.BUY.id() && this.action_id != TransactionLogic.IBUY.id() && this.action_id != TransactionLogic.TRADE.id()){
            return true;
        }
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

    public boolean playerHasMoney(){
        return TaxLogic.playerHasMoney(this.player, this.price);
    }

    public boolean shopHasMoney(){
        return TaxLogic.playerHasMoney(this.owner, this.price);
    }

    public int getAction(){
        return this.action_id;
    }

    public void shopRemoveItems(){
        if (this.action_id != TransactionLogic.BUY.id() && this.action_id != TransactionLogic.TRADE.id()){
            LogUtil.log(Level.SEVERE, "Not supported.");
            return;
        }
        HashMap<ItemStack, Integer> it = new HashMap<ItemStack, Integer>(itemsOut);
        for (Inventory iv : shopOut){
            ItemStack[] iss = iv.getContents();
            for (int i = 0; i < iss.length; i++){
                ItemStack is = iss[i];
                if (is == null) continue;
                for (ItemStack its : it.keySet()){
                    if (its.isSimilar(is)){
                        if (it.get(its) > is.getAmount()){
                            it.put(its, it.get(its) - is.getAmount());
                            iss[i] = null;
                        } else {
                            if (it.get(its) < is.getAmount())
                                is.setAmount(is.getAmount() - it.get(its));
                            else 
                                iss[i] = null;
                            it.remove(its);
                        }
                        break;
                    }
                }
            }
            iv.setStorageContents(iss);
        }
        return;
    }

    public void playerRemoveItems(){
        if (this.action_id != TransactionLogic.SELL.id() && this.action_id != TransactionLogic.ISELL.id() && this.action_id != TransactionLogic.TRADE.id() && this.action_id != TransactionLogic.ITRADE.id()){
            LogUtil.log(Level.SEVERE, "Not supported.");
            return;
        }
        HashMap<ItemStack, Integer> it = new HashMap<ItemStack, Integer>(itemsIn);
        ItemStack[] iss = playerInv.getStorageContents();
        for (int i = 0; i < iss.length; i++){
            ItemStack is = iss[i];
            if (is == null) continue;
            for (ItemStack its : it.keySet()){
                if (its.isSimilar(is)){
                    if (it.get(its) > is.getAmount()){
                        it.put(its, it.get(its) - is.getAmount());
                        iss[i] = null;
                    } else {
                        if (it.get(its) < is.getAmount())
                            is.setAmount(is.getAmount() - it.get(its));
                        else 
                            iss[i] = null;
                        it.remove(its);
                    }
                    break;
                }
            }
        }
        playerInv.setStorageContents(iss);
        return;
    }

    public void shopGiveItems(){
        if (this.action_id != TransactionLogic.SELL.id() && this.action_id != TransactionLogic.TRADE.id()){
            LogUtil.log(Level.SEVERE, "Not supported.");
            return;
        }
        Collection<ItemStack> items = new HashSet<ItemStack>();
        for (ItemStack is : this.itemsIn.keySet()){
            items.add(is.clone());
        }
        for (Inventory iv : shopIn){
            HashMap<Integer, ItemStack> ret = 
                iv.addItem(items.toArray(new ItemStack[items.size()]));
            if (ret.size() == 0)return;
            items = ret.values();
        }
    }

    public void playerGiveItems(){
        if (this.action_id != TransactionLogic.BUY.id() && this.action_id != TransactionLogic.IBUY.id() && this.action_id != TransactionLogic.TRADE.id() && this.action_id != TransactionLogic.ITRADE.id()){
            LogUtil.log(Level.SEVERE, "Not supported.");
            return;
        }
        Collection<ItemStack> items = new HashSet<ItemStack>();
        for (ItemStack is : this.itemsOut.keySet()){
            items.add(is.clone());
        }
        HashMap<Integer, ItemStack> ret = 
            this.playerInv.addItem(items.toArray(new ItemStack[items.size()]));
        if (ret.size() != 0){
            LogUtil.log(Level.SEVERE, "playerGiveItems():"+ret);
        }
    }

    public void playerPayMoney(){
        TaxLogic.withdraw(this.player, this.price);
    }

    public void playerGiveMoney(){
        if (this.price >= 0)
        TaxLogic.deposit(this.player, this.price);
        else 
        TaxLogic.deposit(this.player, -this.price);
    }

    public void shopPayMoney(){
        TaxLogic.withdraw(this.owner, this.price);
    }

    public void shopGiveMoney(){
        TaxLogic.deposit(this.owner, this.price);
    }

    public int getPrice(){
        return this.price;
    }

    public Set<ItemStack> getItemsOut(){
        return this.itemsOut.keySet();
    }

    public Set<ItemStack> getItemsIn(){
        return this.itemsIn.keySet();
    }
}