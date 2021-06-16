package com.evermc.evershop.util;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.type.Switch;

import static com.evermc.evershop.util.LogUtil.severe;

import java.lang.reflect.Method;

public class RedstoneUtil {

    private static Method CB_Block_getNMS = null;
    private static Method CB_Block_getPosition = null;
    private static Method CB_World_getHandle = null;
    private static Method NMS_IBlockData_getBlock = null;
    private static Method NMS_World_applyPhysics = null;

    private static Class <?> CB_Block = null;
    private static Class <?> CB_World = null;
    private static Class <?> NMS_Block = null;
    private static Class <?> NMS_BlockPosition = null;
    private static Class <?> NMS_IBlockData = null;
    private static Class <?> NMS_World = null;
    
    private static boolean enable = false;

    public static void init(){
        try {
            NMS_Block = ReflectionUtil.NMSClass("Block", "world.level.block.Block");
            NMS_BlockPosition = ReflectionUtil.NMSClass("BlockPosition", "core.BlockPosition");
            NMS_IBlockData = ReflectionUtil.NMSClass("IBlockData", "world.level.block.state.IBlockData");
            NMS_World = ReflectionUtil.NMSClass("World", "world.level.World");

            CB_Block = ReflectionUtil.CBClass("block.CraftBlock");
            CB_World = ReflectionUtil.CBClass("CraftWorld");

            NMS_IBlockData_getBlock = NMS_IBlockData.getMethod("getBlock");
            NMS_World_applyPhysics = NMS_World.getMethod("applyPhysics", NMS_BlockPosition, NMS_Block);

            CB_Block_getNMS = CB_Block.getMethod("getNMS");
            CB_Block_getPosition = CB_Block.getMethod("getPosition");
            CB_World_getHandle = CB_World.getMethod("getHandle");

            enable = true;
        } catch (Exception e){
            e.printStackTrace();
            String bukkitVersion = Bukkit.getBukkitVersion();
            severe("RedstoneUtil:init() Unsupported version: " + bukkitVersion + ", disable restone related shops.");
        }
    }

    public static boolean isEnabled(){
        return enable;
    }

    public static void applyPhysics(Block lever){
        BlockFace bf = getAttachedFace(lever);
        if (bf == null) {
            return;
        }
        Block attached = lever.getRelative(bf);
        updateBlock(attached);
    }
    public static void updateBlock(Block bukkit_Block){
        try{
            Object NMS_World_Object = CB_World_getHandle.invoke(bukkit_Block.getWorld());
            Object NMS_BlockPosition_Object = CB_Block_getPosition.invoke(bukkit_Block);
            Object NMS_IBlockData_Object = CB_Block_getNMS.invoke(bukkit_Block);
            Object NMS_Block_Object = NMS_IBlockData_getBlock.invoke(NMS_IBlockData_Object);
            NMS_World_applyPhysics.invoke(NMS_World_Object, NMS_BlockPosition_Object, NMS_Block_Object);
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

