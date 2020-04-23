package com.evermc.evershop.command;

import com.evermc.evershop.EverShop;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCommand extends AbstractSetCommand {

    public SetCommand() {
        super("set", "evershop.set", "Set shop attributes", "[shopid]");
        Bukkit.getScheduler().runTaskLater(EverShop.getInstance(), () -> {
            this.add(new SetPermissionCommand());
        }, 1);
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        return true;
    }
    public boolean executeAsPlayer(Player player, String[] args, int shopid) {
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args, int shopid){
        return true;
    }

}