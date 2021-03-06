package com.evermc.evershop.structure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.evermc.evershop.util.SerializableLocation;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerInfo implements com.evermc.evershop.api.PlayerInfo{
    private int id;
    private UUID uuid;
    private String name;
    private boolean advanced;
    private Set<Location> reg1;
    private Set<Location> reg2;
    private ShopInfo wandShop;
    private boolean reg_is_container;

    public PlayerInfo(int id, UUID uuid, String name, boolean advanced){
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.advanced = advanced;
        this.reg1 = ConcurrentHashMap.newKeySet();
        this.reg2 = ConcurrentHashMap.newKeySet();
        this.reg_is_container = false;
        this.wandShop = null;
    }

    public String toString(){
        return "PlayerInfo{id:" + id + ", uuid:" + uuid + ", name:" + name + ", advanced:" + advanced + "}";
    }

    public void removeRegs(){
        this.reg1.clear();
        this.reg2.clear();
    }

    public void removeWand(){
        this.wandShop = null;
    }

    /**
     * remove duplicated locations, and keeps left block of double chest only
     */
    public void cleanupRegs(){
        if (this.reg_is_container){
            for (Location loc : this.reg1){
                if (!(loc.getBlock().getState() instanceof Container)){
                    this.reg1.remove(loc);
                    continue;
                }
                Inventory inv = ((Container)loc.getBlock().getState()).getInventory();
                if (inv instanceof DoubleChestInventory && ((DoubleChestInventory)inv).getRightSide().getLocation().equals(loc)){
                    this.reg1.remove(loc);
                    this.reg2.remove(loc);
                    this.reg1.add(((DoubleChestInventory)inv).getLeftSide().getLocation());
                    continue;
                }
            }
            for (Location loc : this.reg2){
                if (!(loc.getBlock().getState() instanceof Container)){
                    this.reg2.remove(loc);
                    continue;
                }
                Inventory inv = ((Container)loc.getBlock().getState()).getInventory();
                if (inv instanceof DoubleChestInventory && ((DoubleChestInventory)inv).getRightSide().getLocation().equals(loc)){
                    this.reg1.remove(loc);
                    this.reg2.remove(loc);
                    this.reg2.add(((DoubleChestInventory)inv).getLeftSide().getLocation());
                    continue;
                }
            }
        }
    }

    public int getId(){
        return this.id;
    }

    public UUID getUUID(){
        return this.uuid;
    }

    public void setUUID(UUID uuid){
        this.uuid = uuid;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setWandShop(ShopInfo si) {
        this.wandShop = si;
    }

    public ShopInfo getWandShop(){
        return this.wandShop;
    }

    public boolean isAdvanced(){
        return this.advanced;
    }

    // get advanced as integer 1 or 0
    public int getAdvanced(){
        return this.advanced?1:0;
    }

    public void setAdvanced(boolean advanced){
        this.advanced = advanced;
    }

    public boolean isContainer(){
        return this.reg_is_container;
    }

    public void setContainer(boolean reg_is_container){
        this.reg_is_container = reg_is_container;
    }

    public Set<Location> getReg1(){
        return this.reg1;
    }
    
    public Set<Location> getRegLeftClick() {
        return Collections.unmodifiableSet(this.reg1);
    }

    public Set<Location> getReg2(){
        return this.reg2;
    }
    
    public Set<Location> getRegRightClick() {
        return Collections.unmodifiableSet(this.reg2);
    }
    
    public HashSet<SerializableLocation> getReg1Loc(){
        HashSet<SerializableLocation> result = new HashSet<SerializableLocation>();
        for (Location loc: getReg1()){
            result.add(new SerializableLocation(loc));
        }
        return result;
    }

    public HashSet<SerializableLocation> getReg2Loc(){
        HashSet<SerializableLocation> result = new HashSet<SerializableLocation>();
        for (Location loc: getReg2()){
            result.add(new SerializableLocation(loc));
        }
        return result;
    }

    public HashSet<SerializableLocation> getRegsLoc(){
        HashSet<SerializableLocation> result = new HashSet<SerializableLocation>();
        for (Location loc: getReg1()){
            result.add(new SerializableLocation(loc));
        }
        for (Location loc: getReg2()){
            result.add(new SerializableLocation(loc));
        }
        return result;
    }

    public HashSet<ItemStack> getReg1Items(){
        cleanupRegs();
        HashSet<ItemStack> items = new HashSet<ItemStack>();
        if (isContainer()){
            for (Location loc : getReg1()){
                Inventory inv = ((Container)loc.getBlock().getState()).getInventory();
                for (ItemStack is : inv.getContents()){
                    if (is == null) continue;
                    boolean duplicate = false;
                    for (ItemStack isc : items){
                        if (isc.isSimilar(is)){
                            duplicate = true;
                            isc.setAmount(isc.getAmount() + is.getAmount());
                            break;
                        }
                    }
                    if (!duplicate){
                        items.add(is.clone());
                    }
                }
            }
        }
        return items;
    }

    public HashSet<ItemStack> getReg2Items(){
        cleanupRegs();
        HashSet<ItemStack> items = new HashSet<ItemStack>();
        if (isContainer()){
            for (Location loc : getReg2()){
                Inventory inv = ((Container)loc.getBlock().getState()).getInventory();
                for (ItemStack is : inv.getContents()){
                    if (is == null) continue;
                    boolean duplicate = false;
                    for (ItemStack isc : items){
                        if (isc.isSimilar(is)){
                            duplicate = true;
                            isc.setAmount(isc.getAmount() + is.getAmount());
                            break;
                        }
                    }
                    if (!duplicate){
                        items.add(is.clone());
                    }
                }
            }
        }
        return items;
    }
}
