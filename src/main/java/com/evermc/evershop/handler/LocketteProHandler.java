package com.evermc.evershop.handler;

import com.evermc.evershop.EverShop;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.crafter.mc.lockettepro.LocketteProAPI;

import static com.evermc.evershop.util.LogUtil.info;
import static com.evermc.evershop.util.LogUtil.warn;

public class LocketteProHandler {

    private static boolean enabled = false;
    private static int restrict_type = 0;

    public static void init(EverShop plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("LockettePro") == null) {
            info("LockettePro not found");
            return;
        }
        enabled = true;
        reload(plugin);
        info("Hooked LockettePro-" + plugin.getServer().getPluginManager().getPlugin("LockettePro").getDescription().getVersion());
    }

    public static void reload(EverShop plugin) {
        String type = plugin.getConfig().getString("evershop.lockettepro.restrict_link");
        info("LockettePro restriction type: " + type);
        if ("none".equals(type)) {
            enabled = false;
        } else if ("user".equals(type)) {
            restrict_type = 1;
        } else if ("owner".equals(type)) {
            restrict_type = 2;
        } else if ("all".equals(type)) {
            restrict_type = 3;
        } else {
            warn("Unknown LockettePro restrict type: " + type + ", must be one of: [none, user, owner, all]");
            enabled = false;
        }
    }

    public static boolean canAccessChest(Player p, Location loc) {
        if (!enabled) {
            return true;
        }
        Block b = loc.getBlock();
        switch(restrict_type) {
            case 1:
                return LocketteProAPI.isUser(b, p);
            case 2:
                return LocketteProAPI.isOwner(b, p);
            case 3:
                return !LocketteProAPI.isProtected(b);
            default:
                return true;
        }
    }
}