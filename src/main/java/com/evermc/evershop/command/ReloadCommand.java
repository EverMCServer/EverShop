package com.evermc.evershop.command;

import com.evermc.evershop.EverShop;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand extends AbstractCommand {
    public ReloadCommand() {
        super("reload", "evershop.admin.op", "reload the plugin");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        EverShop.getInstance().reload();
        player.sendMessage("EverShop reloaded");
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        EverShop.getInstance().reload();
        sender.sendMessage("EverShop reloaded");
        return true;
    }
}