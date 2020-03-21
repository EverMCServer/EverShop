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
}
