package com.evermc.evershop;

import java.util.logging.Level;

import com.evermc.evershop.event.InteractEvent;
import com.evermc.evershop.handler.VaultHandler;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.util.LogUtil;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EverShop extends JavaPlugin {

    private static EverShop instance;
    @Override      
    public void onDisable(){  

    } 
    @Override  
    public void onEnable(){  
        instance = this;
        if(!this.getDataFolder().exists()){
            this.getDataFolder().mkdir();
        }
        if (!VaultHandler.setupEconomy() ) {
            LogUtil.log(Level.SEVERE, "Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        VaultHandler.setupPermissions();

        Bukkit.getPluginManager().registerEvents(new InteractEvent(this), this);
        if (!DataLogic.init(this)){
            LogUtil.log(Level.SEVERE, "Fail to start DataLogic! Check your database config.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        new PlayerLogic(this);
        new ShopLogic(this);
        new TransactionLogic(this);
    }  

    public static EverShop getInstance() {
        return EverShop.instance;
    }
}  