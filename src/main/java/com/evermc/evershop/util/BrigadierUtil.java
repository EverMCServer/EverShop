package com.evermc.evershop.util;

import java.lang.reflect.Constructor;

import com.evermc.evershop.EverShop;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import org.bukkit.command.Command;

public class BrigadierUtil {
    private static Class<?> CB_BukkitCommandWrapper;
    private static Class<?> CB_CraftServer;
    private static Constructor<?> CB_BukkitCommandWrapper_Constructer;

    public static void init(){
        try{
            CB_BukkitCommandWrapper = ReflectionUtil.CBClass("command.BukkitCommandWrapper");
            CB_CraftServer = ReflectionUtil.CBClass("CraftServer");
            CB_BukkitCommandWrapper_Constructer = CB_BukkitCommandWrapper.getConstructor(CB_CraftServer, Command.class);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void register(Command command){
        try{
            SuggestionProvider<?> wrapper = (SuggestionProvider<?>) CB_BukkitCommandWrapper_Constructer.newInstance(EverShop.getInstance().getServer(), command);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}