package com.evermc.evershop.command;

import com.evermc.evershop.EverShop;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.evermc.evershop.util.TranslationUtil.send;

public class ReloadCommand extends AbstractCommand {
    public ReloadCommand() {
        super("reload", "evershop.admin.op", "reload the plugin");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        EverShop.getInstance().reload();
        send("EverShop reloaded", player);
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        EverShop.getInstance().reload();
        send("EverShop reloaded", sender);
        return true;
    }
}