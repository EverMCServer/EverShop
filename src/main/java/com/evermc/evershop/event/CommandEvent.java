package com.evermc.evershop.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class CommandEvent implements CommandExecutor, TabCompleter {
    
    private String[][] base_commands = {
        {"advanced", "evershop.advanced", "toggle advance mode"},
        {"info",     "evershop.info",     "view shop info"},
        {"inspect",  "evershop.inspect",  "toggle inspect mode"},
        {"list",     "evershop.list",     "list shops"},
        {"log",      "evershop.info",     "show shop transaction logs"},
        {"reload",   "evershop.admin.op", "reload the plugin"},
        {"set",      "evershop.set",      "set shop attributes"}
    };
    private String[][] set_commands = {
        {"permission", "evershop.set.perm", "set shop permission"},
        {"text",       "evershop.set.text", "set sign text"},
        {"price",      "evershop.set.price","set shop price"},
        {"time",       "evershop.set.time", "set redstone poweron time"}
    };
    private String[][] set_perm_commands = {
        {"type",       "evershop.set.perm", "set permission type"},
        {"deny",       "evershop.set.perm", "set deny"},
        {"allow",      "evershop.set.perm", "set allow"}
    };

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage("HI!");
        return true;
    }

    // use naive if-else check since few commands available
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] _args){

        // discards spaces in args
        ArrayList<String> alargs = new ArrayList<String>();
        for (int i = 0; i < _args.length - 1; i ++) {
            if (_args[i].length() > 0){
                alargs.add(_args[i]);
            }
        }
        alargs.add(_args[_args.length - 1]);
        String[] args = alargs.toArray(new String[alargs.size()]);

        ArrayList<String> ret = new ArrayList<String>();

        if (args.length == 0){
            for (String[] t : base_commands){
                if (sender.hasPermission(t[1])){
                    ret.add(t[0]);
                }
            }
            return ret;
        } else if (args.length == 1){
            for (String[] t : base_commands){
                if (t[0].startsWith(args[0]) && sender.hasPermission(t[1])){
                    ret.add(t[0]);
                }
            }
            return ret;
        } else if (args.length == 2){
            if ("list".startsWith(args[0])){
                return null; // show online player list
            } else if ("set".startsWith(args[0])){
                if (args[1].length() == 0) {
                    for (String[] t : set_commands){
                        if (sender.hasPermission(t[1])){
                            ret.add(t[0]);
                        }
                    }
                    return ret;
                } else if (Character.isDigit(args[1].charAt(0))){
                    return ret; //entered shopid
                } else {
                    for (String[] t : set_commands){
                        if (t[0].startsWith(args[1]) && sender.hasPermission(t[1])){
                            ret.add(t[0]);
                        }
                    }
                    return ret;
                }
            } else {
                return ret;
            }
        } else if (args.length == 3){
            if ("set".startsWith(args[0])){
                if (Character.isDigit(args[1].charAt(0))){
                    for (String[] t : set_commands){
                        if ((args[2].length() == 0 || t[0].startsWith(args[2])) && sender.hasPermission(t[1])){
                            ret.add(t[0]);
                        }
                    }
                    return ret;
                } else if ("permission".startsWith(args[1])){
                    for (String[] t : set_perm_commands){
                        if ((args[2].length() == 0 || t[0].startsWith(args[2])) && sender.hasPermission(t[1])){
                            ret.add(t[0]);
                        }
                    }
                    return ret;
                }
            }
        } else if (args.length == 4){
            if ("set".startsWith(args[0]) && Character.isDigit(args[1].charAt(0)) && "permission".startsWith(args[2])){
                for (String[] t : set_perm_commands){
                    if ((args[3].length() == 0 || t[0].startsWith(args[3])) && sender.hasPermission(t[1])){
                        ret.add(t[0]);
                    }
                }
                return ret;
            }
        }
        return ret;
    }
}