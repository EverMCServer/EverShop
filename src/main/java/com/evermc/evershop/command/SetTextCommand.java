package com.evermc.evershop.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.structure.ShopInfo;

public class SetTextCommand extends AbstractSetCommand {

    public SetTextCommand(){
        super("text", "evershop.set.text", "set shop sign text", "<1-4> <text>");
    }

    @Override
    public boolean executeAs(CommandSender sender, String[] args, ShopInfo si){
        if (args.length != 2) {
            return false;
        }
        int line = 0;
        try{
            line = Integer.parseInt(args[0]);
        } catch (Exception e){}
        if (line < 1 || line > 4) {
            return false;
        }
        final String text = ChatColor.stripColor(args[1]);
        final int linen = line - 1;
        if (line == 1) {
            int a = TransactionLogic.getId(text);
            if (a != si.getAction()) {
                sender.sendMessage("Shop type should not change");
                return true;
            }
        }
        Bukkit.getScheduler().runTask(EverShop.getInstance(), () -> {
            Block b = si.getLocation().getBlock();
            Sign sign = (Sign)b.getState();

            if (linen == 0)
                sign.setLine(linen, ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + text);
            else 
                sign.setLine(linen, text);
            sign.update();
        });
        
        return true;
    }
}