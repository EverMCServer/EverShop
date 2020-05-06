package com.evermc.evershop.command;

import org.bukkit.command.CommandSender;

import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.structure.ShopInfo;

import static com.evermc.evershop.util.TranslationUtil.send;

public class SetPriceCommand extends AbstractSetCommand {

    public SetPriceCommand(){
        super("price", "evershop.set.price", "set shop price", "<price>");
    }

    @Override
    public boolean executeAs(CommandSender sender, String[] args, ShopInfo si){
        if (args.length != 1) {
            return false;
        }
        int price;
        try{
            price = Integer.parseInt(args[0]);
        } catch (Exception e){
            return false;
        }
        if (!TransactionLogic.getEnum(si.getAction()).name().contains("TRADE") && price < 0) {
           price = -price; 
        }
        si.setPrice(price);
        send("Price set to %1$s", sender, price);
        return true;
    }
}