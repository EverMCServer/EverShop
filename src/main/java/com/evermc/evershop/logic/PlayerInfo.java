package com.evermc.evershop.logic;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

public class PlayerInfo {
    int id;
    UUID uuid;
    String name;
    boolean advanced;
    Set<Location> reg1;
    Set<Location> reg2;
    boolean reg_is_container;
    public String toString(){
        return "PlayerInfo{id:" + id + ", uuid:" + uuid + ", name:" + name + ", advanced:" + advanced + "}";
    }
}
