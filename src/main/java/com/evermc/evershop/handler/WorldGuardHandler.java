package com.evermc.evershop.handler;

import com.evermc.evershop.EverShop;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import static com.evermc.evershop.util.LogUtil.info;
import static com.evermc.evershop.util.LogUtil.warn;

public class WorldGuardHandler {

    private static boolean enabled = false;
    private static WorldGuardPlugin pWorldGuardPlugin = null;
    private static WorldGuard pWorldGuard = null;
    private static boolean checkFlag = false;

    public static void init(EverShop plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            info("WorldGuard not found");
            return;
        }
        enabled = true;
        pWorldGuardPlugin = WorldGuardPlugin.inst();
        pWorldGuard = WorldGuard.getInstance();
        reload(plugin);
        info("Hooked WorldGuard-" + plugin.getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion());
    }

    public static void reload(EverShop plugin) {
        String type = plugin.getConfig().getString("evershop.worldguard.restrict_link");
        info("Worldguard restriction type: " + type);
        if (type.equals("none")) {
            enabled = false;
        } else if (type.equals("flag")) {
            checkFlag = true;
        } else if (type.equals("member")) {
            checkFlag = false;
        } else {
            warn("Unknown worldguard restrict type: " + type + ", must be one of: [none, flag, member]");
            enabled = false;
        }
    }

    public static boolean canAccessChest(Player p, Location loc) {
        if (!enabled) {
            return true;
        }
        LocalPlayer localPlayer = pWorldGuardPlugin.wrapPlayer(p);
        if (pWorldGuard.getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld())) {
            return true;
        }
        RegionContainer container = pWorldGuard.getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (checkFlag) {
            return query.testBuild(BukkitAdapter.adapt(loc), localPlayer, Flags.CHEST_ACCESS) && query.testBuild(BukkitAdapter.adapt(loc), localPlayer, Flags.INTERACT);
        } else {
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
            return set.isMemberOfAll(localPlayer) || set.isOwnerOfAll(localPlayer);
        }
    }
}