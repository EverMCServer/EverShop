package com.evermc.evershop.command;

import org.bukkit.command.CommandSender;

import com.evermc.evershop.structure.ShopInfo;

public class SetDurationCommand extends AbstractSetCommand {

    public SetDurationCommand(){
        super("duration", "evershop.set.duration", "set redstone power on duration", "<ticks>");
    }

    @Override
    public boolean executeAs(CommandSender sender, String[] args, ShopInfo si){
        if (args.length != 1) {
            return false;
        }
        int dur;
        try{
            dur = Integer.parseInt(args[0]);
        } catch (Exception e){
            return false;
        }
        if (dur <= 0) {
            return false;
        }
        si.getExtraInfo().setDuration(dur);
        return true;
    }
}