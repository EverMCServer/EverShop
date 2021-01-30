package com.evermc.evershop.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.api.ShopType;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.TaxLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.util.RedstoneUtil;
import com.evermc.evershop.util.SerializableLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static com.evermc.evershop.util.LogUtil.severe;

/**
 * When getting contents of a block:
 *  ((Container)Block.getState()).getInventory().getContents()
 * getState() is 10x slower than all other API, so we cache all Inventories when start a transaction.
 */
public class TransactionInfo{
    private HashSet<Inventory> shopOut;
    private HashSet<Inventory> shopIn;
    private Inventory playerInv;
    private HashMap<ItemStack, Integer> itemsOut;
    private HashMap<ItemStack, Integer> itemsIn;
    private Player player;
    private OfflinePlayer owner;
    private int price;
    private int action_id;
    private HashSet<Location> rsComponents;
    private int rsDuration;
    private ExtraInfoImpl extra;
    private HashMap<String,ItemStack> slotMap;

    public TransactionInfo(ShopInfo si, Player p){
        this.playerInv = p.getInventory();
        this.player = p;
        this.owner = PlayerLogic.getOfflinePlayer(si.getOwnerId());
        this.price = si.getPrice();
        this.action_id = si.getAction();
        this.rsDuration = si.getExtraInfo().getDuration();
        
        if (TransactionLogic.isRedStoneShop(si.getAction())){
            //redstone components
            HashSet<SerializableLocation> locs = si.getTargetAll();
            rsComponents = new HashSet<Location>();
            for (SerializableLocation lo : locs){
                rsComponents.add(lo.toLocation());
            }
        } else {
            if (si.getItemOut() != null) {
                this.itemsOut = new HashMap<ItemStack, Integer>();
                addAllItems(this.itemsOut, si.getItemOut());
            }
            if (si.getItemIn() != null) {
                this.itemsIn = new HashMap<ItemStack, Integer>();
                addAllItems(this.itemsIn, si.getItemIn());
            }
            if (si.getTargetOut() != null) {
                this.shopOut = new HashSet<Inventory>();
                addAllTargets(this.shopOut, si.getTargetOut());
            }
            if (si.getTargetIn() != null) {
                this.shopIn = new HashSet<Inventory>();
                addAllTargets(this.shopIn, si.getTargetIn());
            }
            if (si.getAction() == ShopType.DONATEHAND.id() || si.getAction() == ShopType.DISPOSE.id()) {
                this.itemsIn = new HashMap<ItemStack, Integer>();
                ItemStack it = p.getInventory().getItemInMainHand();
                this.itemsIn.put(it, it.getAmount());
            }
        }
        if (si.getAction() == ShopType.ISLOT.id() || si.getAction() == ShopType.ITEMISLOT.id() || si.getAction() == ShopType.SLOT.id()){
            this.extra = si.getExtraInfo();
            this.slotMap = ExtraInfoImpl.slotItemMap(si.getItemOut());
        }
    }
    
    public boolean isOwner(){
        return this.owner.getUniqueId().equals(this.player.getUniqueId());
    }

    public Player getOnlineOwner(){
        return this.owner.getPlayer();
    }

    public String getPlayerName(){
        return this.player.getName();
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
        if (this.action_id != ShopType.BUY.id() && this.action_id != ShopType.TRADE.id()){
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

    // used in slot
    public boolean shopHasSlotItems(){
        if (this.action_id != ShopType.SLOT.id() && this.action_id != ShopType.ISLOT.id() && this.action_id != ShopType.ITEMISLOT.id()){
            return true;
        }
        HashMap<ItemStack, Integer> it = new HashMap<ItemStack, Integer>();
        for (Entry<String,ItemStack> entry: this.slotMap.entrySet()){
            it.put(entry.getValue(), extra.slotGetMaxAmount(entry.getKey()));
        }
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

    private int countSize(HashMap<ItemStack, Integer> items) {
        int count = 0;
        for (Entry<ItemStack,Integer> entry : items.entrySet()) {
            ItemStack it = entry.getKey();
            int amount = entry.getValue();
            if (amount == 0) {
                continue;
            }
            count += (amount-1)/it.getMaxStackSize() + 1;
        } 
        return count;
    }
    // Only checks if shopIn has itemsIn 
    public boolean shopCanHold(){
        if (this.action_id != ShopType.SELL.id()
        && this.action_id != ShopType.TRADE.id()
        && this.action_id != ShopType.DONATEHAND.id()){
            severe("TransactionInfo: Illegal invocation");
            return false;
        }
        HashMap<ItemStack, Integer> it = new HashMap<ItemStack, Integer>(itemsIn);
        int emptycount = 0;
        for (Inventory iv : shopIn){
            for (ItemStack is : iv.getContents()){
                if (is == null){
                    emptycount ++;
                    if (emptycount >= countSize(it)) return true;
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
        return countSize(it) <= emptycount;
    }

    // Only checks if player has itemsIn 
    public boolean playerHasItems(){
        if (this.action_id != ShopType.SELL.id() 
        && this.action_id != ShopType.ISELL.id() 
        && this.action_id != ShopType.ITRADE.id() 
        && this.action_id != ShopType.TRADE.id()
        && this.action_id != ShopType.ITEMISLOT.id() ){
            severe("TransactionInfo: Illegal invocation");
            return false;
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

    // used in slot
    public boolean playerHasEmptyInv(){
        if (this.action_id != ShopType.ISLOT.id() 
        && this.action_id != ShopType.SLOT.id() 
        && this.action_id != ShopType.ITEMISLOT.id()){
            severe("TransactionInfo: Illegal invocation");
            return false;
        }
        for (ItemStack is : playerInv.getStorageContents()){
            if (is == null) return true;
        }
        return false;
    }

    // Only checks if player has itemsOut 
    public boolean playerCanHold(){
        if (this.action_id != ShopType.BUY.id()
         && this.action_id != ShopType.IBUY.id()
          && this.action_id != ShopType.TRADE.id()
          && this.action_id != ShopType.ITRADE.id()){
            severe("TransactionInfo: Illegal invocation");
            return false;
        }
        HashMap<ItemStack, Integer> it = new HashMap<ItemStack, Integer>(itemsOut);
        int emptycount = 0;
        for (ItemStack is : playerInv.getStorageContents()){
            if (is == null){
                emptycount ++;
                if (emptycount >= countSize(it)) return true;
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
        return countSize(it) <= emptycount;
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
        if (this.action_id != ShopType.BUY.id() && this.action_id != ShopType.TRADE.id()){
            severe("TransactionInfo: Illegal invocation");
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
    }

    public void shopRemoveItems(ItemStack item){
        if (this.action_id != ShopType.SLOT.id()){
            severe("TransactionInfo: Illegal invocation");
            return;
        }
        HashMap<ItemStack, Integer> it = new HashMap<ItemStack, Integer>();
        it.put(item, item.getAmount());
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
    }

    public void playerRemoveItems(){
        if (this.action_id != ShopType.SELL.id()
            && this.action_id != ShopType.ISELL.id()
            && this.action_id != ShopType.TRADE.id()
            && this.action_id != ShopType.ITRADE.id()
            && this.action_id != ShopType.DONATEHAND.id()
            && this.action_id != ShopType.DISPOSE.id()
            && this.action_id != ShopType.ITEMISLOT.id() ){
            severe("TransactionInfo: Illegal invocation");
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
    }

    public void shopGiveItems(){
        if (this.action_id != ShopType.SELL.id()
        && this.action_id != ShopType.TRADE.id()
        && this.action_id != ShopType.DONATEHAND.id()){
            severe("TransactionInfo: Illegal invocation");
            return;
        }
        Collection<ItemStack> items = new ArrayList<ItemStack>();
        for (Entry<ItemStack,Integer> entry : this.itemsIn.entrySet()){
            ItemStack it = entry.getKey();
            int amount = entry.getValue();
            while(amount > it.getMaxStackSize()) {
                ItemStack tt = it.clone();
                tt.setAmount(it.getMaxStackSize());
                items.add(tt);
                amount -= it.getMaxStackSize();
            }
            if (amount > 0) {
                ItemStack tt = it.clone();
                tt.setAmount(amount);
                items.add(tt);
            }
        }
        for (Inventory iv : shopIn){
            HashMap<Integer, ItemStack> ret = 
                iv.addItem(items.toArray(new ItemStack[items.size()]));
            if (ret.size() == 0)return;
            items = ret.values();
        }
        if (items.size() != 0) {
            severe("shopGiveItems():"+items);
        }
    }

    public void playerGiveItems(){
        if (this.action_id != ShopType.BUY.id() && this.action_id != ShopType.IBUY.id() && this.action_id != ShopType.TRADE.id() && this.action_id != ShopType.ITRADE.id()){
            severe("TransactionInfo: Illegal invocation");
            return;
        }
        Collection<ItemStack> items = new ArrayList<ItemStack>();
        for (Entry<ItemStack,Integer> entry : this.itemsOut.entrySet()){
            ItemStack it = entry.getKey();
            int amount = entry.getValue();
            while(amount > it.getMaxStackSize()) {
                ItemStack tt = it.clone();
                tt.setAmount(it.getMaxStackSize());
                items.add(tt);
                amount -= it.getMaxStackSize();
            }
            if (amount > 0) {
                ItemStack tt = it.clone();
                tt.setAmount(amount);
                items.add(tt);
            }
        }
        HashMap<Integer, ItemStack> ret = 
            this.playerInv.addItem(items.toArray(new ItemStack[items.size()]));
        if (ret.size() != 0){
            severe("playerGiveItems():"+ret);
        }
    }

    public ItemStack playerGiveSlot(){
        if (this.action_id != ShopType.SLOT.id() && this.action_id != ShopType.ISLOT.id() && this.action_id != ShopType.ITEMISLOT.id()){
            severe("TransactionInfo: Illegal invocation");
            return null;
        }
        int possi = extra.slotPossibilityAll();
        int place = new Random().nextInt(possi);
        Entry<String, Integer> ret = extra.slotGetAt(place);
        ItemStack item = this.slotMap.get(ret.getKey());
        item.setAmount(ret.getValue());
        HashMap<Integer, ItemStack> retval = 
            this.playerInv.addItem(item);
        if (retval.size() != 0){
            severe("playerGiveSlot():"+ret);
        }
        return item;
    }

    private static int abs(int k){
        return k<0?(-k):(k);
    }

    public void playerPayMoney(){
        TaxLogic.withdraw(this.player, abs(this.price));
    }

    public void playerGiveMoney(){
        TaxLogic.deposit(this.player, abs(this.price));
    }

    public void shopPayMoney(){
        TaxLogic.withdraw(this.owner, abs(this.price));
    }

    public void shopGiveMoney(){
        TaxLogic.deposit(this.owner, abs(this.price));
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

    public void toggleRS(){
        for (final Location lo : this.rsComponents){
            BlockData bs = lo.getBlock().getBlockData();
            if (bs instanceof Powerable){
                final Powerable sw = (Powerable)bs;
                if (lo.getBlock().getType().name().endsWith("_BUTTON")){
                    sw.setPowered(true);
                    lo.getBlock().setBlockData(sw);
                    RedstoneUtil.applyPhysics(lo.getBlock());
                    Bukkit.getScheduler().runTaskLater(EverShop.getInstance(), () -> {
                        BlockData bsn = lo.getBlock().getBlockData();
                        if (bsn instanceof Powerable){
                            ((Powerable)bsn).setPowered(false);
                            lo.getBlock().setBlockData(bsn);
                            RedstoneUtil.applyPhysics(lo.getBlock());
                        }
                    }, this.rsDuration);
                } else {
                    sw.setPowered(!sw.isPowered());
                    lo.getBlock().setBlockData(sw);
                    RedstoneUtil.applyPhysics(lo.getBlock());
                }
            }
        }
    }

    public void turnOnRS(){
        for (final Location lo : this.rsComponents){
            BlockData bs = lo.getBlock().getBlockData();
            if (bs instanceof Powerable){
                final Powerable sw = (Powerable)bs;
                if (lo.getBlock().getType().name().endsWith("_BUTTON")){
                    sw.setPowered(true);
                    lo.getBlock().setBlockData(sw);
                    RedstoneUtil.applyPhysics(lo.getBlock());
                    Bukkit.getScheduler().runTaskLater(EverShop.getInstance(), () -> {
                        BlockData bsn = lo.getBlock().getBlockData();
                        if (bsn instanceof Powerable){
                            ((Powerable)bsn).setPowered(false);
                            lo.getBlock().setBlockData(bsn);
                            RedstoneUtil.applyPhysics(lo.getBlock());
                        }
                    }, this.rsDuration);
                } else {
                    sw.setPowered(true);
                    lo.getBlock().setBlockData(sw);
                    RedstoneUtil.applyPhysics(lo.getBlock());
                }
            }
        }
    }

    public void turnOffRS(){
        for (final Location lo : this.rsComponents){
            BlockData bs = lo.getBlock().getBlockData();
            if (bs instanceof Powerable){
                final Powerable sw = (Powerable)bs;
                sw.setPowered(false);
                lo.getBlock().setBlockData(sw);
                RedstoneUtil.applyPhysics(lo.getBlock());
            }
        }
    }

    public void turnOnDurationRS(){
        for (final Location lo : this.rsComponents){
            BlockData bs = lo.getBlock().getBlockData();
            if (bs instanceof Powerable){
                final Powerable sw = (Powerable)bs;
                sw.setPowered(true);
                lo.getBlock().setBlockData(sw);
                RedstoneUtil.applyPhysics(lo.getBlock());
                Bukkit.getScheduler().runTaskLater(EverShop.getInstance(), () -> {
                    BlockData bsn = lo.getBlock().getBlockData();
                    if (bsn instanceof Powerable){
                        ((Powerable)bsn).setPowered(false);
                        lo.getBlock().setBlockData(bsn);
                        RedstoneUtil.applyPhysics(lo.getBlock());
                    }
                }, this.rsDuration);
            }
        }
    }

    public void shopDispose(){
        if (this.action_id != ShopType.DISPOSE.id()){
            severe("TransactionInfo: Illegal invocation");
            return;
        }
        if (this.shopIn.size() != 1) {
            severe("shopDispose(): target count != 1");
            return;
        }
        if (this.getItemsIn().size() != 1) {
            severe("shopDispose(): item count != 1");
            return;
        }
        Inventory iv = this.shopIn.iterator().next();
        ItemStack it = this.getItemsIn().iterator().next();
        int k = iv.firstEmpty();
        if (k == iv.getSize() - 1) {
            iv.setItem(0, null);
            iv.setItem(k, it);
            return;
        }
        if (k == -1) {
            iv.setItem(0, it);
            iv.setItem(1, null);
            return;
        } else {
            iv.setItem(k, it);
            iv.setItem(k+1, null);
        }
    }
}