package com.evermc.evershop.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import static com.evermc.evershop.util.LogUtil.severe;

public class NBTUtil {

    private static Method CB_CraftItemStack_asBukkitCopy = null;
    private static Method CB_CraftItemStack_asNMSCopy = null;
    private static Method NMS_ItemStack_save = null;
    private static Method NMS_ItemStack_fromNBT = null;
    private static Method NMS_NBTTagCompound_toString = null;
    private static Method NMS_MojangsonParser_parse = null;

    private static Class<?> CB_CraftItemStack = null;
    private static Class<?> NMS_NBTTagCompound = null;
    private static Class<?> NMS_ItemStack = null;
    private static Class<?> NMS_MojangsonParser = null;

    private static Constructor<?> NMS_NBTTagCompound_Constructer = null;

    public static void init(){
        try{

            CB_CraftItemStack = ReflectionUtil.CBClass("inventory.CraftItemStack");
            NMS_NBTTagCompound = ReflectionUtil.NMSClass("NBTTagCompound");
            NMS_ItemStack = ReflectionUtil.NMSClass("ItemStack");
            NMS_MojangsonParser = ReflectionUtil.NMSClass("MojangsonParser");

            CB_CraftItemStack_asBukkitCopy = CB_CraftItemStack.getMethod("asBukkitCopy", NMS_ItemStack);
            CB_CraftItemStack_asNMSCopy = CB_CraftItemStack.getMethod("asNMSCopy", ItemStack.class);
            NMS_ItemStack_save = NMS_ItemStack.getMethod("save", NMS_NBTTagCompound);
            NMS_ItemStack_fromNBT = NMS_ItemStack.getMethod("a", NMS_NBTTagCompound);
            NMS_NBTTagCompound_toString = NMS_NBTTagCompound.getMethod("toString");
            NMS_MojangsonParser_parse = NMS_MojangsonParser.getMethod("parse", String.class);

            NMS_NBTTagCompound_Constructer = NMS_NBTTagCompound.getConstructor();

        } catch (Exception e){
            e.printStackTrace();
            String bukkitVersion = Bukkit.getBukkitVersion();
            severe("NBTUtil:init() Unsupported version: " + bukkitVersion);
        }
    }
    /**
     * Convert bukkit ItemStack to NMS NBT string. 
     * Note that the the amount of output nbt item will be set to 1, since the item will be considered as invalid 
     * when the amount is bigger than the max stack size
     */ 
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

    public static ItemStack toItemStack(String nbt){
        try{
            Object NMS_NBTTagCompound_Object = NMS_MojangsonParser_parse.invoke(NMS_MojangsonParser, nbt);
            Object NMS_ItemStack_Object = NMS_ItemStack_fromNBT.invoke(NMS_ItemStack, NMS_NBTTagCompound_Object);
            return (ItemStack)CB_CraftItemStack_asBukkitCopy.invoke(CB_CraftItemStack, NMS_ItemStack_Object);
        }catch (Exception e){
            e.printStackTrace();
            String bukkitVersion = Bukkit.getBukkitVersion();
            severe("NBTUtil:init() Unsupported version: " + bukkitVersion);
            return null;
        }
    }

    public static byte[] serialize(Collection<ItemStack> itemOut, Collection<ItemStack> itemIn){
        HashMap<?,?>[] result = new HashMap<?,?>[2];
        HashMap<String, Integer> resultOut = new HashMap<String, Integer>();
        HashMap<String, Integer> resultIn = new HashMap<String, Integer>();
        for (ItemStack i : itemOut){
            resultOut.put(toNBTString(i), i.getAmount());
        }
        for (ItemStack i : itemIn){
            resultIn.put(toNBTString(i), i.getAmount());
        }
        result[0] = resultOut;
        result[1] = resultIn;
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(out);
            outputStream.writeObject(result);
            byte [] bytes = out.toByteArray();
            outputStream.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            severe("NBTUtil: Failed to serialize itemstack.");
            return null ;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static HashSet<ItemStack>[] deserialize(byte[] data){
        HashSet<?>[] result = new HashSet<?>[2];
        HashSet<ItemStack> resultOut = new HashSet<ItemStack>();
        HashSet<ItemStack> resultIn = new HashSet<ItemStack>();
        try{
            ObjectInputStream in  = new ObjectInputStream(new ByteArrayInputStream(data));
            Object obj = in.readObject();
            if (!(obj instanceof HashMap<?,?>[])){
                throw new Exception("Not a HashMap array");
            }
            HashMap<String, Integer> itemOut = ((HashMap<String, Integer>[])obj)[0];
            HashMap<String, Integer> itemIn = ((HashMap<String, Integer>[])obj)[1];
            for (Entry<String, Integer> item: itemOut.entrySet()){
                ItemStack temp = toItemStack(item.getKey());
                temp.setAmount(item.getValue());
                resultOut.add(temp);
            }
            for (Entry<String, Integer> item: itemIn.entrySet()){
                ItemStack temp = toItemStack(item.getKey());
                temp.setAmount(item.getValue());
                resultIn.add(temp);
            }
            result[0] = resultOut;
            result[1] = resultIn;
            return (HashSet<ItemStack>[])result;
        } catch (Exception e) {
            e.printStackTrace();
            severe("NBTUtil: Failed to deserialize itemstack.");
            return null ;
        }

    }
}