package com.evermc.evershop.util;

import java.util.ArrayList;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.structure.PlayerInfo;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;

public class ParticlesUtil {
    public static void init(EverShop plugin){
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerInfo pi = PlayerLogic.getPlayerInfo(p);
                for (Location loc : pi.getReg1()) {
                    DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 127, 255), 0.5f);
                    for (Location lo : getBorder(loc, 5)){
                        p.spawnParticle(Particle.REDSTONE, lo, 1, dustOptions);
                    }   
                }
                for (Location loc : pi.getReg2()) {
                    DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 255, 0), 0.5f);
                    for (Location lo : getBorder(loc, 5)){
                        p.spawnParticle(Particle.REDSTONE, lo, 1, dustOptions);
                    }   
                }
            }
        }, 10, 10);
    }
    private static Location[] getBorder(Location loc, int count) {
        ArrayList<Location> ret = new ArrayList<Location>();
        for (double i = 0; i <= count; i ++) {
            Location l = loc.clone();
            l.add(i/count, 0, 0);
            ret.add(l);
            ret.add(l.clone().add(0, 1, 0));
            ret.add(l.clone().add(0, 1, 1));
            ret.add(l.clone().add(0, 0, 1));
            l = loc.clone();
            l.add(0, i/count, 0);
            ret.add(l);
            ret.add(l.clone().add(1, 0, 0));
            ret.add(l.clone().add(0, 0, 1));
            ret.add(l.clone().add(1, 0, 1));
            l = loc.clone();
            l.add(0, 0, i/count);
            ret.add(l);
            ret.add(l.clone().add(1, 0, 0));
            ret.add(l.clone().add(0, 1, 0));
            ret.add(l.clone().add(1, 1, 0));
        }
        return ret.toArray(new Location[ret.size()]);
    }
}