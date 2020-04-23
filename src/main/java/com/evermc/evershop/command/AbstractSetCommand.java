package com.evermc.evershop.command;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.evermc.evershop.util.LogUtil.severe;

public abstract class AbstractSetCommand extends AbstractCommand {

    public AbstractSetCommand(String name, String permission, String usage) {
        super(name, permission, usage);
    }
    public AbstractSetCommand(String name, String permission, String usage, String parameters) {
        super(name, permission, usage, parameters);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args, String cmd){
        if ("set".equals(this.name)) {
            if (args.length > 0 && Pattern.matches("\\d+", args[0])) {
                if (args.length > 1){
                    // specify which shop
                    int shopid = 0;
                    try{
                        shopid = Integer.parseInt(args[0]);
                    } catch (Exception e){}
                    if (shopid > 0) {
                        String[] arg_new = Arrays.copyOfRange(args, 1, args.length);
                        return execute(sender, arg_new, cmd, shopid);
                    }
                }
                this.help(sender);
                return true;
            } else if (args.length > 0 && sender instanceof Player) {
                return execute(sender, args, cmd, 0);
            } else {
                this.help(sender);
                return true;
            }
        } else {
            severe("Illegal invocation: " + this.getName());
            return true;
        }
    }

    public boolean execute(CommandSender sender, String[] args, String cmd, int shopid){
        if (!sender.hasPermission(this.permission)){
            return false;
        }
        if (this.children.size() > 0 && args.length > 0 && args[0].length() > 0){
            // has sub-commands
            String[] arg_new = Arrays.copyOfRange(args, 1, args.length);
            for (AbstractCommand sub : this.children){
                if (!(sub instanceof AbstractSetCommand)){
                    continue;
                }
                if (sub.getName().startsWith(args[0])){
                    if (((AbstractSetCommand)sub).execute(sender, arg_new, cmd, shopid)){
                        return true;
                    }
                }
            }
            // not match any of the sub-commands
            this.help(sender, args, cmd);
            return true;
        } else if (this.children.size() == 0) {
            // no sub-commands 
            boolean ret = true;
            if (sender instanceof Player){
                ret = this.executeAsPlayer((Player)sender, args, shopid);
            } else {
                ret = this.executeAs(sender, args, shopid);
            }
            if (!ret){
                this.help(sender, args, cmd);
            }
            return true;
        } else {
            this.help(sender, cmd);
            return true;
        }
    }

    public boolean executeAsPlayer(Player player, String[] args){
        severe("Illegal invocation: " + this.getName());
        return true;
    }

    public boolean executeAs(CommandSender sender, String[] args){
        severe("Illegal invocation: " + this.getName());
        return true;
    }

    public abstract boolean executeAsPlayer(Player player, String[] args, int shopid);
    public abstract boolean executeAs(CommandSender sender, String[] args, int shopid);
}