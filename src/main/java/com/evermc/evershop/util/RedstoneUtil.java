package com.evermc.evershop.util;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.type.Switch;

import static com.evermc.evershop.util.LogUtil.severe;

import java.lang.reflect.Method;

public class RedstoneUtil {

    private static Object NMS_World = null;
    private static Method NMS_World_applyPhysics = null;
    private static Method CB_Block_getNMS = null;
    private static Method CB_Block_getPosition = null;
    private static Method NMS_Block_getBlock = null;

    public static void applyPhysics(Block lever){
        BlockFace bf = getAttachedFace(lever);
        if (bf == null) {
            return;
        }
        Block attached = lever.getRelative(bf);
        updateBlock(attached);
    }
    public static void updateBlock(Block bukkitBlock){

        try{

            if (CB_Block_getNMS == null) {
                CB_Block_getNMS = bukkitBlock.getClass().getMethod("getNMS");
            }
            Object iBlockData = CB_Block_getNMS.invoke(bukkitBlock);

            if (CB_Block_getPosition == null) {
                CB_Block_getPosition = bukkitBlock.getClass().getMethod("getPosition");
            }
            Object nmsPosition = CB_Block_getPosition.invoke(bukkitBlock);

            if (NMS_Block_getBlock == null) {
                NMS_Block_getBlock = iBlockData.getClass().getMethod("getBlock");
            }
            Object nmsBlock = NMS_Block_getBlock.invoke(iBlockData);

            Class<?> nmsBlockClass = nmsBlock.getClass();
            while (!nmsBlockClass.getName().endsWith("Block")){
                nmsBlockClass = nmsBlockClass.getSuperclass();
            }
            if (NMS_World == null){
                NMS_World = bukkitBlock.getWorld().getClass().getMethod("getHandle").invoke(bukkitBlock.getWorld());
                NMS_World_applyPhysics = NMS_World.getClass().getSuperclass().getMethod("applyPhysics",nmsPosition.getClass(), nmsBlockClass);
            }
            
            NMS_World_applyPhysics.invoke(NMS_World, nmsPosition, nmsBlock);
            
        } catch (Exception e){
            e.printStackTrace();
            String bukkitVersion = Bukkit.getBukkitVersion();
            severe("RedstoneUtil:updateBlock() Unsupported version: " + bukkitVersion);
        }
    }
    
    public static BlockFace getAttachedFace(Block block){
        if (block.getBlockData() instanceof Switch){
            return getAttachedFace((Switch)block.getBlockData());
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static BlockFace getAttachedFace(Switch switc) {
        try{
            if (switc.getAttachedFace() == AttachedFace.CEILING){
                return BlockFace.UP;
            } else if (switc.getAttachedFace() == AttachedFace.FLOOR){
                return BlockFace.DOWN;
            } else {
                return switc.getFacing().getOppositeFace();
            }
        }catch(NoSuchMethodError e){
            if (switc.getFace() == org.bukkit.block.data.type.Switch.Face.CEILING){
                return BlockFace.UP;
            } else if (switc.getFace() == org.bukkit.block.data.type.Switch.Face.FLOOR){
                return BlockFace.DOWN;
            } else {
                return switc.getFacing().getOppositeFace();
            }
        }
    }
}

