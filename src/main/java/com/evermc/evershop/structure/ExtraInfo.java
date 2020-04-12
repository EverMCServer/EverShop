package com.evermc.evershop.structure;

import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.Gson;

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