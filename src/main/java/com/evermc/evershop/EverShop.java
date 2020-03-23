package com.evermc.evershop;

import com.evermc.evershop.event.InteractEvent;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EverShop extends JavaPlugin {

    private static EverShop instance;
    private DataLogic dataLogic;
    private PlayerLogic playerLogic;
    private ShopLogic shopLogic;
    private TransactionLogic transactionLogic;

    @Override      
    public void onDisable(){  

    } 
    @Override  
    public void onEnable(){  
        instance = this;
        Bukkit.getPluginManager().registerEvents(new InteractEvent(this), this);
        this.dataLogic = new DataLogic(this);
        this.playerLogic = new PlayerLogic(this);
        this.shopLogic = new ShopLogic(this);
        this.transactionLogic = new TransactionLogic(this);
    }  

    public static EverShop getInstance() {
        return EverShop.instance;
    }

    public DataLogic getDataLogic(){
        return this.dataLogic;
    }

    public PlayerLogic getPlayerLogic(){
        return this.playerLogic;
    }

    public ShopLogic getShopLogic(){
        return this.shopLogic;
    }

    public TransactionLogic getTransactionLogic(){
        return this.transactionLogic;
    }

}  