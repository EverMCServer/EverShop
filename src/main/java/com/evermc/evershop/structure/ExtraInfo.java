package com.evermc.evershop.structure;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import com.evermc.evershop.handler.VaultHandler;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.structure.PermissionInfo.Type;
import com.evermc.evershop.util.NBTUtil;
import com.google.gson.Gson;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.evermc.evershop.util.LogUtil.severe;

public class ExtraInfo {

    PermissionInfo perm;
    int rc_ticks;
    HashMap<String, String> slot;

    public ExtraInfo(){
        this.perm = new PermissionInfo();
        this.rc_ticks = 20;
        this.slot = null;
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
    public void initSlot(Set<ItemStack> items) {
        this.slot = new HashMap<String,String>();
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            e.printStackTrace();
            severe("ExtraInfo: Hash not supported!");
            return;
        }
        for (ItemStack item : items) {
            int amount = item.getAmount();
            ItemStack it = item.clone();
            it.setAmount(1);
            byte[] re = messageDigest.digest(NBTUtil.toNBTString(it).getBytes(StandardCharsets.UTF_8));
            this.slot.put(byteToHex(re), "1:" + amount);
        }
    }
    public int slotPossibilityAll() {
        int ret = 0;
        for (String posa:this.slot.values()){
            for (String pos:posa.split(";")){
                try{
                    ret += Integer.parseInt(pos.split(":")[1]);
                }catch(Exception e){
                    e.printStackTrace();
                    severe("ExtraInfo: failed to decode slot possibility data");
                }
            }
        }
        return ret;
    }
    public Entry<String,Integer> slotGetAt(int place){
        Iterator<Entry<String,String>> slots = this.slot.entrySet().iterator();
        Entry<String,String> entry = null;
        int amount = 0;
        while(slots.hasNext()){
            entry = slots.next();
            String posa = entry.getValue();
            for (String pos:posa.split(";")){
                try{
                    amount = Integer.parseInt(pos.split(":")[0]);
                    place -= Integer.parseInt(pos.split(":")[1]);
                }catch(Exception e){
                    e.printStackTrace();
                    severe("ExtraInfo: failed to decode slot possibility data");
                }
                if (place <= 0) return new AbstractMap.SimpleEntry<String, Integer>(entry.getKey(), amount);
            }
        }
        if (entry == null) {
            severe("ExtraInfo: failed to decode slot possibility data: Null entry!");
            return null;
        }
        else  return new AbstractMap.SimpleEntry<String, Integer>(entry.getKey(), amount);
    }
    public HashMap<String,ItemStack> slotItemMap(Set<ItemStack> items){
        MessageDigest messageDigest;
        HashMap<String,ItemStack> ret = new HashMap<String,ItemStack>();
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            e.printStackTrace();
            severe("ExtraInfo: Hash not supported!");
            return null;
        }
        for (ItemStack item : items) {
            ItemStack it = item.clone();
            it.setAmount(1);
            byte[] re = messageDigest.digest(NBTUtil.toNBTString(it).getBytes(StandardCharsets.UTF_8));
            ret.put(byteToHex(re), item);
        }
        return ret;
    }
    public boolean slotSetPossibility(ItemStack item, String possibility) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            e.printStackTrace();
            severe("ExtraInfo: Hash not supported!");
            return false;
        }
        for (String pos:possibility.split(";")){
            int amount = 0, possi = 0;
            try{
                amount = Integer.parseInt(pos.split(":")[0]);
                possi = Integer.parseInt(pos.split(":")[1]);
            }catch(Exception e){}
            if (amount == 0 || possi == 0){
                return false;
            }
            if (amount > item.getMaxStackSize()){
                return false;
            }
        }
        ItemStack it = item.clone();
        it.setAmount(1);
        byte[] re = messageDigest.digest(NBTUtil.toNBTString(it).getBytes(StandardCharsets.UTF_8));
        this.slot.put(byteToHex(re), possibility);
        return true;
    }
    private static String byteToHex(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        String temp;
        for (byte b : bytes){
            temp = Integer.toHexString(b & 0xFF);
            if(temp.length() == 1)builder.append(0);
            builder.append(temp);
        }
        return builder.toString();
    }
    public int slotGetMaxAmount(String key) {
        String possibility = this.slot.get(key);
        int maxAmount = 0;
        for (String pos:possibility.split(";")){
            int amount = 0;
            try{
                amount = Integer.parseInt(pos.split(":")[0]);
            }catch(Exception e){}
            if (amount > maxAmount) maxAmount = amount;
        }
        return maxAmount;
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