package com.evermc.evershop.command;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.structure.ShopInfo;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class SetCommand extends AbstractSetCommand {

    public SetCommand() {
        super("set", "evershop.set", "Set shop attributes", "[shopid]");
        Bukkit.getScheduler().runTaskLater(EverShop.getInstance(), () -> {
            this.add(new SetPermissionCommand());
            this.add(new SetTextCommand());
        }, 1);
    }
    public boolean executeAs(CommandSender sender, String[] args, ShopInfo si){
        return true;
    }

}