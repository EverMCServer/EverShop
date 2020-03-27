package com.evermc.evershop.handler;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import com.evermc.evershop.EverShop;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHandler {

    private static Permission perms;
    private static Economy econ;

    static {
        perms = null;
        econ = null;
    }

    public static void addPermission(final Player player, final String perk) {
        perms.playerAdd(player, perk);
    }

    public static void removePermission(final Player player, final String perk) {
        perms.playerRemove(player, perk);
    }

    public static boolean hasPermission(final Player player, final String perk) {
        return perms.playerHas(player, perk);
    }

    public static boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> rsp = (RegisteredServiceProvider<Permission>) EverShop.getInstance().getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp.getProvider() != null) {
            perms = rsp.getProvider();
        }
        return perms != null;
    }

    public static boolean setupEconomy() {
        if (EverShop.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>) EverShop.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

}
