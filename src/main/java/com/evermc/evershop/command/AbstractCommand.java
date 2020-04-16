package com.evermc.evershop.command;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

import static com.evermc.evershop.util.TranslationUtil.tr;

public abstract class AbstractCommand {
    private ArrayList<AbstractCommand> children = new ArrayList<AbstractCommand>();
    private String permission;
    private String usage;
    private String name;
    private String[] path;

    public AbstractCommand(String name, String permission, String usage) {
        this.name = name;
        this.permission = permission;
        this.usage = usage;
    }

    public void add(AbstractCommand children){
        this.children.add(children);
        String[] childpath = Arrays.copyOf(path, path.length + 1);
        childpath[path.length] = children.getName();
        children.setPath(childpath);
    }

    public String getName(){
        return this.name;
    }

    public String getPermission(){
        return this.permission;
    }

    public String getUsage(){
        return this.usage;
    }

    public String[] getPath(){
        return this.path;
    }

    public String getFullCommand(){
        String path = "/";
        for (String str : this.path) {
            path = path + str + " ";
        }
        return path;
    }

    public void setPath(String[] path){
        this.path = path;
    }

    public boolean execute(CommandSender sender, String[] args){
        if (!sender.hasPermission(this.permission)){
            return false;
        }
        if (this.children.size() > 0 && args.length > 0 && args[0].length() > 0){
            // has sub-commands
            String[] arg_new = Arrays.copyOfRange(args, 1, args.length);
            for (AbstractCommand sub : this.children){
                if (sub.getName().startsWith(args[0])){
                    if (sub.execute(sender, arg_new)){
                        return true;
                    }
                }
            }
            // not match any of the sub-commands
            this.help(sender, args);
            return true;
        } else {
            // no sub-commands or no arguments provided
            if (sender instanceof Player){
                return this.executeAsPlayer((Player)sender, args);
            } else {
                return this.executeAs(sender, args);
            }
        }
    }

    public void help(CommandSender sender, String[] args) {
        if (args != null && args.length > 0){
            ComponentBuilder msgBuilder = new ComponentBuilder();
            msgBuilder.append("Unknown command: ").color(ChatColor.DARK_RED).bold(true)
                      .append(getFullCommand()).color(ChatColor.DARK_RED).bold(true);
            for (String str : args) {
                msgBuilder.append(str + " ").color(ChatColor.DARK_RED).bold(true);
            }
            sender.spigot().sendMessage(msgBuilder.create());
        }
        help(sender);
    }

    public void help(CommandSender sender) {
        ComponentBuilder msgBuilder = new ComponentBuilder();
        msgBuilder.append("----- ").color(ChatColor.WHITE)
                  .append(tr("EverShop Help", sender)).bold(true).color(ChatColor.GREEN)
                  .append(" -----\n").bold(false).color(ChatColor.WHITE)

                  .append(tr("Command: ", sender)).color(ChatColor.GRAY)
                  .append(getFullCommand()).color(ChatColor.DARK_AQUA)
                  .append("- ").color(ChatColor.GRAY)
                  .append(tr(this.usage, sender)).append("\n").color(ChatColor.YELLOW);

        for (AbstractCommand command: this.children){
            msgBuilder.append(getFullCommand() + command.getName()).color(ChatColor.DARK_AQUA)
                      .append(" - ").color(ChatColor.GRAY)
                      .append(tr(command.getUsage(), sender)).color(ChatColor.WHITE).append("\n");
        }
        sender.spigot().sendMessage(msgBuilder.create());
    }
    
    // ONLY return false when permission check failed
    public abstract boolean executeAsPlayer(Player player, String[] args);
    public abstract boolean executeAs(CommandSender sender, String[] args);
}