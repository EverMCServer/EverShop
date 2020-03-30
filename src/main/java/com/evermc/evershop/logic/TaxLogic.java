package com.evermc.evershop.logic;

import com.evermc.evershop.handler.VaultHandler;

import org.bukkit.OfflinePlayer;

public class TaxLogic {
    
    // TODO - tax logic
    public static boolean playerHasMoney(OfflinePlayer player, double price){
        return VaultHandler.getEconomy().getBalance(player) >= price;
    }
}