package com.evermc.evershop.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpCommand extends AbstractCommand {
    
    private EverShopCommand parent;

    public HelpCommand(EverShopCommand parent) {
        super("help", "evershop", "Show EverShop help");
        this.parent = parent;
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        parent.help(player);
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        parent.help(sender);
        return true;
    }
}