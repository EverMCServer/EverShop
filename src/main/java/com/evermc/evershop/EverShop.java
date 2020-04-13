package com.evermc.evershop;

import com.evermc.evershop.event.CommandEvent;
import com.evermc.evershop.event.InteractEvent;
import com.evermc.evershop.handler.VaultHandler;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.util.RedstoneUtil;
import com.evermc.evershop.util.TranslationUtil;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static com.evermc.evershop.util.LogUtil.info;
import static com.evermc.evershop.util.LogUtil.severe;

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
            severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            info("Hooked Vault-" + getServer().getPluginManager().getPlugin("Vault").getDescription().getVersion());
        }
        VaultHandler.setupPermissions();

        Bukkit.getPluginManager().registerEvents(new InteractEvent(this), this);
        Bukkit.getPluginCommand("evershop").setExecutor(new CommandEvent());
        if (!DataLogic.init(this)){
            severe("Fail to start DataLogic! Check your database config.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        PlayerLogic.init(this);
        ShopLogic.init(this);
        TransactionLogic.init(this);
        TranslationUtil.init(this);
        RedstoneUtil.init();
    }  

    public static EverShop getInstance() {
        return EverShop.instance;
    }
}  