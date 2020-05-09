package com.evermc.evershop.structure;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import com.evermc.evershop.database.SQLDataSource;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.util.NBTUtil;
import com.evermc.evershop.util.SerializableLocation;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import static com.evermc.evershop.util.LogUtil.severe;

public class ShopInfo {

    // Shop UID
    private int id;

    // Shop creation time
    private int epoch;

    // Shop transaction type
    private int action_id;

    // Shop owner pid
    private int player_id;

    // Shop sign location
    private int world_id;
    private int x;
    private int y;
    private int z;

    // Shop price
    private int price;
    /**
     *  BUY, IBUY:      itemOut = shopitem, itemIn = null
     *  SELL, ISELL :   itemOut = null,     itemIn = shopitem,      
     *  TRADE, ITRADE:  itemOut = itemOut,  itemIn = itemIn,    
     *  Other:          itemOut = null,     itemIn = null,      
     */
    private HashSet<ItemStack> itemOut;
    private HashSet<ItemStack> itemIn;
    /**
     *  BUY, Redstone:  targetOut = shop/Redstone, targetIn = null
     *  SELL:           targetOut = null,          targetIn = shop,      
     *  TRADE:          targetOut = targetOut,     targetIn = targetIn,    
     *  Other:          targetOut = null,          targetIn = null,      
     */
    private HashSet<SerializableLocation> targetOut;
    private HashSet<SerializableLocation> targetIn;

    // Extra Infomation
    private ExtraInfo extra;

    // Shop revision. 
    // Only rev=0 is available shop, others are old shops which share the same location with the current shop.
    // A bigger rev means an older shop
    private int rev;

    // Create a new shop. shopid will be generated after save to the database
    public ShopInfo(int action_id, PlayerInfo pi, Location shoploc, int price){
        this.id = 0;
        this.epoch = (int)(System.currentTimeMillis()/1000);
        this.action_id = action_id;
        this.player_id = pi.getId();
        this.world_id = DataLogic.getWorldId(shoploc.getWorld());
        this.x = shoploc.getBlockX();
        this.y = shoploc.getBlockY();
        this.z = shoploc.getBlockZ();
        if (action_id != TransactionLogic.ITRADE.id() && action_id != TransactionLogic.TRADE.id())
            price = Math.abs(price);
        this.price = price;
        if (action_id == TransactionLogic.DONATEHAND.id() || action_id == TransactionLogic.DISPOSE.id()) {
            this.price = 0;
        }
        this.extra = new ExtraInfo();
        this.rev = 0;
        // items record
        if (TransactionLogic.itemsetCount(action_id) == 0){
            this.itemOut = new HashSet<ItemStack>();
            this.itemIn = new HashSet<ItemStack>();
        } else if (TransactionLogic.itemsetCount(action_id) == 2){
            this.itemOut = pi.getReg1Items();
            this.itemIn = pi.getReg2Items();
        } else if (action_id == TransactionLogic.BUY.id() || action_id == TransactionLogic.IBUY.id() || action_id == TransactionLogic.ISLOT.id() || action_id == TransactionLogic.SLOT.id()){
            this.itemOut = pi.getReg1Items();
            this.itemIn = new HashSet<ItemStack>();
        } else if (action_id == TransactionLogic.SELL.id() || action_id == TransactionLogic.ISELL.id()){
            this.itemOut = new HashSet<ItemStack>();
            this.itemIn = pi.getReg1Items();
        } else {
            severe("ShopInfo: Unimplemented shop type: " + action_id);
        }
        // targets record
        this.setTarget(pi);

        // init slot
        if (action_id == TransactionLogic.ISLOT.id() || action_id == TransactionLogic.SLOT.id()) {
            this.extra.initSlot(this.itemOut);
        }
    }

    // copy a shop.
    public ShopInfo(ShopInfo old, PlayerInfo pi, Location shopLoc) {
        this.id = 0;
        this.epoch = (int)(System.currentTimeMillis()/1000);
        this.action_id = old.getAction();
        this.player_id = old.getOwnerId();
        SerializableLocation sloc = new SerializableLocation(shopLoc);
        this.world_id = sloc.world;
        this.x = sloc.x;
        this.y = sloc.y;
        this.z = sloc.z;
        this.price = old.getPrice();
        this.itemOut = old.getItemOut();
        this.itemIn = old.getItemIn();
        this.setTarget(pi);
        this.extra = old.getExtraInfo();
    }

    public void setTarget(PlayerInfo pi) {
        if (TransactionLogic.targetCount(action_id) == 0){
            this.targetOut = new HashSet<SerializableLocation>();
            this.targetIn = new HashSet<SerializableLocation>();
        } else if (TransactionLogic.targetCount(action_id) == 2){
            this.targetOut = pi.getReg1Loc();
            this.targetIn = pi.getReg2Loc();
        } else if (action_id == TransactionLogic.SELL.id() || action_id == TransactionLogic.DONATEHAND.id() || action_id == TransactionLogic.DISPOSE.id()){
            this.targetOut = new HashSet<SerializableLocation>();
            this.targetIn = pi.getRegsLoc();
        } else {
            this.targetOut = pi.getRegsLoc();
            this.targetIn = new HashSet<SerializableLocation>();
        }
    }

    private ShopInfo(){}

    public static ShopInfo decode(Object[] data){
        ShopInfo result = new ShopInfo();
        SQLDataSource SQL = DataLogic.getSQL();
        try{
            result.id = SQL.getInt(data[0]);
            result.epoch = SQL.getInt(data[1]);
            result.action_id = SQL.getInt(data[2]);
            result.player_id = SQL.getInt(data[3]);
            result.world_id = SQL.getInt(data[4]);
            result.x = SQL.getInt(data[5]);
            result.y = SQL.getInt(data[6]);
            result.z = SQL.getInt(data[7]);
            result.price = SQL.getInt(data[8]);
            HashSet<SerializableLocation>[] targets = SerializableLocation.deserialize((byte[]) data[9]);
            result.targetOut = targets[0];
            result.targetIn = targets[1];
            HashSet<ItemStack>[] items = NBTUtil.deserialize((byte[]) data[10]);
            result.itemOut = items[0];
            result.itemIn = items[1];
            result.extra = ExtraInfo.fromJson((String) data[11]);
            result.rev = SQL.getInt(data[12]);
            return result;
        }catch(Exception e){
            e.printStackTrace();
            severe("Internal error: fail to cast data. Database corruption?");
            return null;
        }
    }

    public void setSignState(boolean avail){
        Location loc = SerializableLocation.toLocation(this.world_id, this.x, this.y, this.z);
        if (!ShopLogic.isShopSign(loc.getBlock())){
            return;
        }
        Sign sign = (Sign)loc.getBlock().getState();
        StringBuilder strBuilder = new StringBuilder(sign.getLine(0));
        if (avail){
            strBuilder.setCharAt(1, ChatColor.DARK_BLUE.getChar());
        }else{
            strBuilder.setCharAt(1, ChatColor.DARK_RED.getChar());
        }
        sign.setLine(0, strBuilder.toString());
        sign.update();
    }

    public String toString(){
        return "ShopInfo{id=" + this.id +", epoch=" + this.epoch + ", action_id=" + this.action_id + ", player_id=" + this.player_id + ", world_id=" + this.world_id
         + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", price=" + this.price + ", targetOut=" + this.targetOut + ", targetIn=" + this.targetIn + ", itemOut="
         + this.itemOut + ", itemIn=" + this.itemIn + ", extra=" + this.extra + ", rev=" + this.rev + "}";
    }

    public int getId(){
        return this.id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getEpoch(){
        return this.epoch;
    }

    public Date getEpochDate(){
        return new Date(((long)this.epoch)*1000);
    }

    public String getEpochString(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date(((long)this.epoch)*1000));
    }

    public int getAction(){
        return this.action_id;
    }

    public int getOwnerId(){
        return this.player_id;
    }

    public int getWorldID(){
        return this.world_id;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    public int getZ(){
        return this.z;
    }

    public Location getLocation(){
        return SerializableLocation.toLocation(this.world_id, this.x, this.y, this.z);
    }

    public int getPrice(){
        return this.price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getExtra(){
        return this.extra.toJSON();
    }

    public ExtraInfo getExtraInfo(){
        return this.extra;
    }

    public int getRev(){
        return this.rev;
    }

    public void setRev(int rev) {
        this.rev = rev;
    }

    public HashSet<ItemStack> getItemOut(){
        return this.itemOut;
    }

    public HashSet<ItemStack> getItemIn(){
        return this.itemIn;
    }

    public HashSet<ItemStack> getItemAll(){
        HashSet<ItemStack> result = new HashSet<ItemStack>();
        result.addAll(this.itemOut);
        result.addAll(this.itemIn);
        return result;
    }

    public HashSet<SerializableLocation> getTargetOut(){
        return this.targetOut;
    }

    public HashSet<SerializableLocation> getTargetIn(){
        return this.targetIn;
    }

    public HashSet<SerializableLocation> getTargetAll(){
        HashSet<SerializableLocation> result = new HashSet<SerializableLocation>();
        result.addAll(this.targetOut);
        result.addAll(this.targetIn);
        return result;
    }
}
