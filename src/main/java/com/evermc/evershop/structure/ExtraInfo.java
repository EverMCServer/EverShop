package com.evermc.evershop.structure;

import java.util.ArrayList;
import java.util.UUID;

import com.evermc.evershop.handler.VaultHandler;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.structure.PermissionInfo.Type;
import com.google.gson.Gson;

import org.bukkit.entity.Player;

public class ExtraInfo {
    PermissionInfo perm;
    int rc_ticks;
    public ExtraInfo(){
        this.perm = new PermissionInfo();
        this.rc_ticks = 20;
    }
    public String toJSON(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    public static ExtraInfo fromJson(String json){
        Gson gson = new Gson();
        return gson.fromJson(json, ExtraInfo.class);
    }
    public boolean permissionType(char _type) {
        Type type;
        switch (_type) {
            case 'n': type = Type.DISABLED; break;
            case 'w': type = Type.WHITELIST; break;
            case 'b': type = Type.BLACKLIST; break;
            default: return false;
        }
        this.perm.type = type;
        this.perm.users.clear();
        this.perm.groups.clear();
        return true;
    }
    public boolean permissionUserAdd(String user) {
        UUID uuid = null;
        try{
            uuid = UUID.fromString(user); 
        } catch (IllegalArgumentException e){}
        if (uuid == null){
            PlayerInfo pi = PlayerLogic.getPlayerInfo(user);
            if (pi == null) {
                return false;
            }
            uuid = pi.getUUID();
        } 
        if (this.perm.users.contains(uuid)) {
            return true;
        }
        this.perm.users.add(uuid);
        return true;
    }
    public boolean permissionGroupAdd(String group) {
        if (this.perm.groups.contains(group)) {
            return true;
        }
        String[] groups = VaultHandler.getGroups();
        for (String g:groups){
            if (g.equals(group)) {
                this.perm.groups.add(g);
                return true;
            }
        }
        return false;
    }
    public boolean permissionUserRemove(String user) {
        UUID uuid = null;
        try{
            uuid = UUID.fromString(user); 
        } catch (IllegalArgumentException e){}
        if (uuid == null){
            PlayerInfo pi = PlayerLogic.getPlayerInfo(user);
            if (pi == null) {
                return false;
            }
            uuid = pi.getUUID();
        } 
        return this.perm.users.remove(uuid);
    }
    public boolean permissionGroupRemove(String group) {
        return this.perm.groups.remove(group);
    }
    public String getPermissionType(){
        return this.perm.type.name();
    }
    public ArrayList<UUID> getPermissionUsers(){
        return this.perm.users;
    }
    public ArrayList<String> getPermissionGroups(){
        return this.perm.groups;
    }
    public boolean checkPermission(Player p) {
        switch (this.perm.type) {
            case DISABLED:
                return true;
            case WHITELIST:
                if (this.perm.users.contains(p.getUniqueId())) {
                    return true;
                }
                for (String group : this.perm.groups) {
                    if (VaultHandler.playerInGroup(p, group)) {
                        return true;
                    }
                }
                return false;
            case BLACKLIST:
            if (this.perm.users.contains(p.getUniqueId())) {
                return false;
            }
            for (String group : this.perm.groups) {
                if (VaultHandler.playerInGroup(p, group)) {
                    return false;
                }
            }
            return true;
            default:
            return true;
        }
    }
    public void setDuration(int dur) {
        this.rc_ticks = dur;
    }
    public int getDuration() {
        return this.rc_ticks;
    }
}

class PermissionInfo{
    enum Type{
        DISABLED, BLACKLIST, WHITELIST
    }
    Type type;
    ArrayList<UUID> users;
    ArrayList<String> groups;
    public PermissionInfo(){
        this.type = Type.DISABLED;
        this.users = new ArrayList<UUID>();
        this.groups = new ArrayList<String>();
    }
}