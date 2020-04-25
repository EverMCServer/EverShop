package com.evermc.evershop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.evermc.evershop.EverShop;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import org.bukkit.command.Command;

public class BrigadierUtil {
    private static Class<?> CB_BukkitCommandWrapper;
    private static Class<?> CB_CraftServer;
    private static Constructor<?> CB_BukkitCommandWrapper_Constructer;
    private static Field CUSTOM_SUGGESTIONS_FIELD;
    private static EverShop plugin;

    public static void init(EverShop _plugin){
        plugin = _plugin;
        try{
            CB_BukkitCommandWrapper = ReflectionUtil.CBClass("command.BukkitCommandWrapper");
            CB_CraftServer = ReflectionUtil.CBClass("CraftServer");
            CB_BukkitCommandWrapper_Constructer = CB_BukkitCommandWrapper.getConstructor(CB_CraftServer, Command.class);
            CUSTOM_SUGGESTIONS_FIELD = ArgumentCommandNode.class.getDeclaredField("customSuggestions");
            CUSTOM_SUGGESTIONS_FIELD.setAccessible(true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void register(Command command){
        try{
            LiteralCommandNode<?> timeCommand = LiteralArgumentBuilder.literal("evershop")
                .then(LiteralArgumentBuilder.literal("list")
                        .then(RequiredArgumentBuilder.argument("page", IntegerArgumentType.integer())))
                .then(LiteralArgumentBuilder.literal("list")
                        .then(RequiredArgumentBuilder.argument("player", StringArgumentType.word()))
                        .then(RequiredArgumentBuilder.argument("page", IntegerArgumentType.integer())))
                .then(LiteralArgumentBuilder.literal("advance")
                ).build();

            SuggestionProvider<?> wrapper = (SuggestionProvider<?>) CB_BukkitCommandWrapper_Constructer.newInstance(plugin.getServer(), command);
            setCustomSuggestionProvider(timeCommand, wrapper);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private static void  setCustomSuggestionProvider(CommandNode<?> node, SuggestionProvider<?> suggestionProvider) {
        if (node instanceof ArgumentCommandNode) {
            ArgumentCommandNode<?, ?> argumentNode = (ArgumentCommandNode<?, ?>) node;
            try {
                CUSTOM_SUGGESTIONS_FIELD.set(argumentNode, suggestionProvider);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // apply recursively to child nodes
        for (CommandNode<?> child : node.getChildren()) {
            setCustomSuggestionProvider(child, suggestionProvider);
        }
    }
}