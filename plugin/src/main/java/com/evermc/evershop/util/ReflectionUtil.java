package com.evermc.evershop.util;

import org.bukkit.Bukkit;

public class ReflectionUtil {

    private static final String SERVER_VERSION = getServerVersion();

    private static String getServerVersion() {
        Class<?> server = Bukkit.getServer().getClass();
        if (!server.getSimpleName().equals("CraftServer")) {
            return ".";
        }
        if (server.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
            return ".";
        } else {
            String version = server.getName().substring("org.bukkit.craftbukkit".length());
            return version.substring(0, version.length() - "CraftServer".length());
        }
    }

    public static Class<?> NMSClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server" + SERVER_VERSION + className);
    }

    public static Class<?> CBClass(String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit" + SERVER_VERSION + className);
    }
}