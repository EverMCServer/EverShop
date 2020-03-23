package com.evermc.evershop.logic;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Location;

public class PlayerInfo {
    int id;
    UUID uuid;
    String name;
    boolean advanced;
    HashSet<Location> reg1;
    HashSet<Location> reg2;
    boolean reg_is_container;

    public String toString(){
        return "PlayerInfo{id:" + id + ", uuid:" + uuid + ", name:" + name + ", advanced:" + advanced + "}";
    }

    public void removeRegs(){
        this.reg1.clear();
        this.reg2.clear();
    }
}
