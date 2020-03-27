package com.evermc.evershop.database;

import java.io.File;

import com.evermc.evershop.EverShop;

import org.bukkit.configuration.ConfigurationSection;

import org.sqlite.SQLiteDataSource;

public class LiteDataSource extends SQLDataSource {
    public LiteDataSource(ConfigurationSection config){
        this.config = config;
        this.prefix = config.getString("prefix");
        SQLiteDataSource sl = new SQLiteDataSource();
        sl.setUrl("jdbc:sqlite:" + EverShop.getInstance().getDataFolder().getAbsolutePath() + File.separator + config.getString("filename"));
        this.ds = sl;
    }
    public String INSERT_IGNORE(){
        return "INSERT OR IGNORE ";
    }

    public String ON_DUPLICATE(String col){
        return "ON CONFLICT(" + col + ") DO UPDATE SET ";
    }

    public String CONCAT(String s1, String s2){
        return s1 + " || " + s2;
    }
}