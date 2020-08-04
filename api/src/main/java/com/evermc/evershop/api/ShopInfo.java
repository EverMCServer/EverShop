package com.evermc.evershop.api;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface ShopInfo {
    
    /**
     * Gets internal id of the shop
     * 
     * @return Shop ID
     */
    public int getId();

    /**
     * Gets shop creation time (unix timestamp, in seconds)
     * 
     * @return Shop creation time
     */
    public int getEpoch();

    /**
     * Gets shop transaction type
     * 
     * @return Shop type
     */
    public ShopType getShopType();

    /**
     * Gets shop sign location
     * 
     * @return Shop sign location
     */
    public Location getLocation();

    /**
     * Gets shop owner
     * 
     * @return Shop owner
     */
    public PlayerInfo getShopOwner();

    /**
     * Gets shop price
     * 
     * @return Shop price
     */
    public int getPrice();

    /**
     * Gets shop trading items
     *  BUY, IBUY:      itemOut = ShopItem,           itemIn = empty
     *  SELL, ISELL :   itemOut = empty,              itemIn = ShopItem,      
     *  TRADE, ITRADE:  itemOut = OwnerGive/UserGet,  itemIn = UserGive/OwnerGet,    
     *  Other:          itemOut = empty,              itemIn = empty,      
     * 
     * @return immutable set of ItemStack
     */
    public Set<ItemStack> getShopItemsOut();
    public Set<ItemStack> getShopItemsIn();

    
    /**
     *  BUY, Redstone:  targetOut = ShopItemChest/Redstone,targetIn = empty
     *  SELL:           targetOut = empty,                 targetIn = SellItemChest,      
     *  TRADE:          targetOut = OwnerGiveChest,        targetIn = OwnerGetChest,    
     *  Other:          targetOut = empty,                 targetIn = empty,      
     * @return immutable set of Location
     */
    public Set<Location> getShopTargetsOut();
    public Set<Location> getShopTargetsIn();

    /**
     * Gets shop extra infomation
     * 
     * @return Shop extra info
     */
    public ExtraInfo getExtraInfo();

    public interface ExtraInfo {

        /**
         * Gets shop permission type
         * 
         * @return one of [DISABLED, BLACKLIST, WHITELIST]
         */
        public String getPermissionType();

        /**
         * Gets shop permission player list
         * 
         * @return Players in the permission list
         */
        public List<? extends PlayerInfo> getPermissionUserInfo();

        /**
         * Gets shop permission group list
         * 
         * @return Group names in the permission list
         */
        public List<String> getPermissionGroups();

        /**
         * Gets redstone shop active dutation, in ticks
         * 
         * @return Shop duration
         */
        public int getDuration();
    }
}