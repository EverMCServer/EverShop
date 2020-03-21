package com.evermc.evershop.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.action.ActionType;
import com.evermc.evershop.database.SQLDataSource;
import com.evermc.evershop.util.LogUtil;

class ShopInfo{
    int id;
    int epoch;
    ActionType action_id;
    int player_id;
    int world_id;
    int x;
    int y;
    int z;
    
}

public class ShopLogic {

    private EverShop plugin;
    private SQLDataSource SQL;

    public ShopLogic(EverShop plugin){
        this.plugin = plugin;
        this.SQL = plugin.getDataLogic().getSQL();
    }

}