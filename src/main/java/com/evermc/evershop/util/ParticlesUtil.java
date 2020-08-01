package com.evermc.evershop.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static long counter = 0;
    public static void init(EverShop plugin){
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            ParticlesUtil.counter ++;
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerInfo pi = PlayerLogic.getPlayerInfo(p);
                Set<Location> reg1 = pi.getReg1().stream().filter(loc -> loc.getWorld().equals(p.getWorld())).collect(Collectors.toSet());
                Set<Location> reg2 = pi.getReg2().stream().filter(loc -> loc.getWorld().equals(p.getWorld())).collect(Collectors.toSet());
                int count = reg1.size() + reg2.size();
                int dustcount = 5;
                float dustsize = 0.5f;
                if (count > 5) {
                    dustcount = 3;
                    dustsize = 0.8f;
                }
                if (count <= 10) {
                    if (counter % 10 != 0) continue;
                    for (Location loc : reg1) {
                        DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 127, 255), dustsize);
                        for (Location lo : getBorder(loc, dustcount)){
                            p.spawnParticle(Particle.REDSTONE, lo, 1, dustOptions);
                        }   
                    }
                    for (Location loc : reg2) {
                        DustOptions dustOptions = new DustOptions(Color.fromRGB(0, 255, 0), dustsize);
                        for (Location lo : getBorder(loc, dustcount)){
                            p.spawnParticle(Particle.REDSTONE, lo, 1, dustOptions);
                        }   
                    }
                } else {
                    int index = (int)counter%count;
                    DustOptions dustOptions;
                    Iterator<Location> it;
                    if (reg1.size() > index) {
                        it = reg1.iterator();
                        dustOptions = new DustOptions(Color.fromRGB(0, 127, 255), dustsize);
                    } else {
                        index -= reg1.size();
                        it = reg2.iterator();
                        dustOptions = new DustOptions(Color.fromRGB(0, 255, 0), dustsize);
                    }
                    while(it.hasNext() && index-- > 0)it.next();
                    if (!it.hasNext()) continue;
                    for (Location lo : getBorder(it.next(), 3)){
                        p.spawnParticle(Particle.REDSTONE, lo, 1, dustOptions);
                    }
                }
            }
        }, 10, 1);
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