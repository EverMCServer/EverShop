package com.evermc.evershop.structure;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.evermc.evershop.handler.VaultHandler;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.structure.PermissionInfo.Type;
import com.evermc.evershop.util.NBTUtil;
import com.google.gson.Gson;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.evermc.evershop.util.LogUtil.severe;

public class ExtraInfoImpl implements com.evermc.evershop.api.ShopInfo.ExtraInfo{

    private PermissionInfo perm;
    private int rc_ticks;
    private HashMap<String, String> slot;

    public ExtraInfoImpl(){
        this.perm = new PermissionInfo();
        this.rc_ticks = 20;
        this.slot = null;
    }
    public String toJSON(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    public static ExtraInfoImpl fromJson(String json){
        Gson gson = new Gson();
        return gson.fromJson(json, ExtraInfoImpl.class);
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
        PlayerInfo pi = PlayerLogic.getPlayerInfo(uuid);
        if (pi == null) {
            return false;
        }
        int player = pi.getId();
        if (this.perm.users.contains(player)) {
            return true;
        }
        this.perm.users.add(player);
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
        PlayerInfo pi = PlayerLogic.getPlayerInfo(uuid);
        if (pi == null) {
            return false;
        }
        int player = pi.getId();
        return this.perm.users.remove((Integer)player);
    }
    public boolean permissionGroupRemove(String group) {
        return this.perm.groups.remove(group);
    }
    public String getPermissionType(){
        return this.perm.type.name();
    }
    public List<Integer> getPermissionUsers(){
        return this.perm.users;
    }
    public List<PlayerInfo> getPermissionUserInfo(){
        return this.perm.users.stream().map(i->PlayerLogic.getPlayerInfo(i)).collect(Collectors.toList());
    }
    public List<String> getPermissionGroups(){
        return this.perm.groups;
    }
    public boolean checkPermission(Player p) {
        PlayerInfo pi = PlayerLogic.getPlayerInfo(p);
        if (pi == null) {
            return false;
        }
        Integer playerid = pi.getId();
        switch (this.perm.type) {
            case DISABLED:
                return true;
            case WHITELIST:
                if (this.perm.users.contains(playerid)) {
                    return true;
                }
                for (String group : this.perm.groups) {
                    if (VaultHandler.playerInGroup(p, group)) {
                        return true;
                    }
                }
                return false;
            case BLACKLIST:
            if (this.perm.users.contains(playerid)) {
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
    public HashMap<String, String> getSlotPossibilityMap(){
        return this.slot;
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
    public static String getItemKey(ItemStack item){
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            e.printStackTrace();
            severe("ExtraInfo: Hash not supported!");
            return null;
        }
        ItemStack it = item.clone();
        it.setAmount(1);
        byte[] re = messageDigest.digest(NBTUtil.toNBTString(it).getBytes(StandardCharsets.UTF_8));
        return byteToHex(re);
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
                if (place < 0) return new AbstractMap.SimpleEntry<String, Integer>(entry.getKey(), amount);
            }
        }
        if (entry == null) {
            severe("ExtraInfo: failed to decode slot possibility data: Null entry!");
            return null;
        }
        else  return new AbstractMap.SimpleEntry<String, Integer>(entry.getKey(), amount);
    }
    public static HashMap<String,ItemStack> slotItemMap(Set<ItemStack> items){
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
            int amount = 0;
            int possi = 0;
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
    protected Type type;
    protected ArrayList<Integer> users;
    protected ArrayList<String> groups;
    public PermissionInfo(){
        this.type = Type.DISABLED;
        this.users = new ArrayList<Integer>();
        this.groups = new ArrayList<String>();
    }
}