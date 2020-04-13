package com.evermc.evershop;

import java.util.logging.Level;

import com.evermc.evershop.event.CommandEvent;
import com.evermc.evershop.event.InteractEvent;
import com.evermc.evershop.handler.VaultHandler;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.logic.TranslationLogic;
import com.evermc.evershop.util.RedstoneUtil;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static com.evermc.evershop.util.LogUtil.log;

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
        saveDefaultConfig();
        reloadConfig();
        if (!VaultHandler.setupEconomy() ) {
            log(Level.SEVERE, "Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            log(Level.INFO, "Hooked Vault-" + getServer().getPluginManager().getPlugin("Vault").getDescription().getVersion());
        }
        VaultHandler.setupPermissions();

        Bukkit.getPluginManager().registerEvents(new InteractEvent(this), this);
        Bukkit.getPluginCommand("evershop").setExecutor(new CommandEvent());
        if (!DataLogic.init(this)){
            log(Level.SEVERE, "Fail to start DataLogic! Check your database config.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        PlayerLogic.init(this);
        ShopLogic.init(this);
        TransactionLogic.init(this);
        TranslationLogic.init(this);
        RedstoneUtil.init();
    }  

    public static EverShop getInstance() {
        return EverShop.instance;
    }
}  