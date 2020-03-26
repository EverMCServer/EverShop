package com.evermc.evershop.structure;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.Location;

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
}
