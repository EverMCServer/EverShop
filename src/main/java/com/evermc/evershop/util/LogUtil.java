package com.evermc.evershop.util;

import com.evermc.evershop.EverShop;

import java.util.logging.Level;

public enum LogUtil { ;
    public static void log(Level level, String message, Throwable t) {
        EverShop.getInstance().getLogger().log(level, message, t);
    }

    public static void log(Level level, String message) {
        EverShop.getInstance().getLogger().log(level, message);
    }
    public static void warn(String message) {
        EverShop.getInstance().getLogger().log(Level.WARNING, message);
    }
    public static void info(String message) {
        EverShop.getInstance().getLogger().log(Level.INFO, message);
    }
    public static void severe(String message) {
        EverShop.getInstance().getLogger().log(Level.SEVERE, message);
    }
}
