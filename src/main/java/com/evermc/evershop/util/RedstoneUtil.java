package com.evermc.evershop.util;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.Switch.Face;

import static com.evermc.evershop.util.LogUtil.log;

import java.util.logging.Level;

public class RedstoneUtil {
    public static void applyPhysics(Block lever){
        BlockFace bf = getAttachedFace(lever);
        if (bf == null) {
            return;
        }
        Block attached = lever.getRelative(bf);
        updateBlock(attached);
    }
    public static void updateBlock(Block b){

        String bukkitVersion = Bukkit.getBukkitVersion();

        if (bukkitVersion.matches("1\\.14\\.[0-9]*-R[0-9]*\\..*")) {
            org.bukkit.craftbukkit.v1_14_R1.CraftWorld cWorld = (org.bukkit.craftbukkit.v1_14_R1.CraftWorld)b.getWorld();
            org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock cBlock = (org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock)b;
            cWorld.getHandle().applyPhysics(cBlock.getPosition(), cBlock.getNMS().getBlock());
        } else {
            log(Level.SEVERE, "RedstoneUtil:updateBlock() Unsupported version: " + bukkitVersion);
        }
    }
    
    public static BlockFace getAttachedFace(Block block){
        if (block.getBlockData() instanceof Switch){
            return getAttachedFace((Switch)block.getBlockData());
        }
        return null;
    }

    public static BlockFace getAttachedFace(Switch switc) {
        if (switc.getFace() == Face.CEILING){
            return BlockFace.UP;
        } else if (switc.getFace() == Face.FLOOR){
            return BlockFace.DOWN;
        } else {
            return switc.getFacing().getOppositeFace();
        }
    }
}

