package com.evermc.evershop.command;

import com.evermc.evershop.structure.ShopInfo;

import org.bukkit.command.CommandSender;

public class SetCommand extends AbstractSetCommand {

    public SetCommand() {
        super("set", "evershop.set", "Set shop attributes", "[shopid]");
        this.add(new SetPermissionCommand());
        this.add(new SetTextCommand());
        this.add(new SetPriceCommand());
        this.add(new SetDurationCommand());
    }
    public boolean executeAs(CommandSender sender, String[] args, ShopInfo si){
        return true;
    }

}