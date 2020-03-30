package com.evermc.evershop.logic;

import com.evermc.evershop.handler.VaultHandler;

import org.bukkit.OfflinePlayer;

public class TaxLogic {
    
    // TODO - tax logic
    public static boolean playerHasMoney(OfflinePlayer player, double price){
        return VaultHandler.getEconomy().getBalance(player) >= price;
    }

    public static void deposit(OfflinePlayer player, double price){
        VaultHandler.getEconomy().depositPlayer(player, price);
    }

    public static void withdraw(OfflinePlayer player, double price){
        VaultHandler.getEconomy().withdrawPlayer(player, price);
    }
}