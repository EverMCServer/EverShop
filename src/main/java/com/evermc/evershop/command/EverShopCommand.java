package com.evermc.evershop.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class EverShopCommand extends AbstractCommand implements CommandExecutor, TabCompleter {

    public EverShopCommand() {
        super("es", "evershop", "EverShop Commands");
        this.setPath(new String[]{"es"});
        this.add(new AdvancedCommand());
        this.add(new HelpCommand(this));
        this.add(new InfoCommand());
        this.add(new ListCommand());
        this.add(new LogCommand());
        this.add(new SetCommand());
        this.add(new SlotCommand());
        this.add(new ReloadCommand());
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
        String argstring = String.join(" ",args);
        argstring = "/" + label + " " + argstring;
        if (args.length == 0 || !this.execute(sender, args, argstring)){
            this.help(sender, args, argstring);
        }
        return true;
    }

    public boolean executeAsPlayer(Player player, String[] args) {
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] _args){
        // discards spaces in args
        ArrayList<String> alargs = new ArrayList<String>();
        for (int i = 0; i < _args.length; i ++) {
            if (_args[i].length() > 0 || i == _args.length - 1){
                alargs.add(_args[i]);
            }
        }
        String[] args = alargs.toArray(new String[alargs.size()]);
        String argstring = String.join(" ",args);
        argstring = "/" + label + " " + argstring;
        return this.tablist(sender, args, argstring);
    }
}