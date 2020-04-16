package com.evermc.evershop.command;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EverShopCommand extends AbstractCommand implements CommandExecutor {

    public EverShopCommand() {
        super("es", "evershop", "EverShop Commands");
        this.setPath(new String[]{"es"});
        this.add(new AdvancedCommand());
        this.add(new HelpCommand(this));
        this.add(new InfoCommand());
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] _args) {
        // discards spaces in args
        ArrayList<String> alargs = new ArrayList<String>();
        for (int i = 0; i < _args.length; i ++) {
            if (_args[i].length() > 0){
                alargs.add(_args[i]);
            }
        }
        String[] args = alargs.toArray(new String[alargs.size()]);
        if (args.length == 0 || !this.execute(sender, args)){
            this.help(sender, args);
        }
        return true;
    }

    public boolean executeAsPlayer(Player player, String[] args) {
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        return true;
    }
}