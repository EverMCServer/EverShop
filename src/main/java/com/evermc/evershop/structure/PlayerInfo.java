package com.evermc.evershop.structure;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

public class PlayerInfo {
    public int id;
    public UUID uuid;
    public String name;
    public boolean advanced;
    public CopyOnWriteArraySet<Location> reg1;
    public CopyOnWriteArraySet<Location> reg2;
    public boolean reg_is_container;

    public String toString(){
        return "PlayerInfo{id:" + id + ", uuid:" + uuid + ", name:" + name + ", advanced:" + advanced + "}";
    }

    public void removeRegs(){
        this.reg1.clear();
        this.reg2.clear();
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
}
