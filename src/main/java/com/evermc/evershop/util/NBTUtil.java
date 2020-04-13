package com.evermc.evershop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import static com.evermc.evershop.util.LogUtil.severe;

public class NBTUtil {

    private static Method CB_CraftItemStack_asNMSCopy = null;
    private static Method NMS_ItemStack_save = null;
    private static Method NMS_NBTTagCompound_toString = null;

    private static Class<?> CB_CraftItemStack = null;
    private static Class<?> NMS_NBTTagCompound = null;
    private static Class<?> NMS_ItemStack = null;

    private static Constructor<?> NMS_NBTTagCompound_Constructer = null;

    public static void init(){
        try{

            CB_CraftItemStack = ReflectionUtil.CBClass("inventory.CraftItemStack");
            NMS_NBTTagCompound = ReflectionUtil.NMSClass("NBTTagCompound");
            NMS_ItemStack = ReflectionUtil.NMSClass("ItemStack");

            CB_CraftItemStack_asNMSCopy = CB_CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
            NMS_ItemStack_save = NMS_ItemStack.getMethod("save", NMS_NBTTagCompound);
            NMS_NBTTagCompound_toString = NMS_NBTTagCompound.getMethod("toString");

            NMS_NBTTagCompound_Constructer = NMS_NBTTagCompound.getConstructor();

        } catch (Exception e){
            e.printStackTrace();
            String bukkitVersion = Bukkit.getBukkitVersion();
            severe("NBTUtil:init() Unsupported version: " + bukkitVersion);
        }
    }

    public static String toNBTString(ItemStack it){
        ItemStack Bukkit_itemstack;
        if (it == null){
            return "";
        }
        Bukkit_itemstack = it.clone();
        Bukkit_itemstack.setAmount(1);
        try{
            Object NMS_ItemStack_Object = CB_CraftItemStack_asNMSCopy.invoke(CB_CraftItemStack, Bukkit_itemstack);
            Object NMS_NBTTagCompound_Object = NMS_NBTTagCompound_Constructer.newInstance();
            NMS_ItemStack_save.invoke(NMS_ItemStack_Object, NMS_NBTTagCompound_Object);
            return (String)NMS_NBTTagCompound_toString.invoke(NMS_NBTTagCompound_Object);
        } catch (Exception e){
            e.printStackTrace();
            String bukkitVersion = Bukkit.getBukkitVersion();
            severe("NBTUtil:init() Unsupported version: " + bukkitVersion);
            return "";
        }
    }
}