package com.evermc.evershop.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.util.TranslationUtil;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import static com.evermc.evershop.util.TranslationUtil.tr;

public abstract class AbstractCommand {
    protected ArrayList<AbstractCommand> children = new ArrayList<AbstractCommand>();
    protected String permission;
    protected String usage;
    protected String name;
    protected String parameters;
    protected String[] path;

    public AbstractCommand(String name, String permission, String usage, String parameters) {
        this.name = name;
        this.permission = permission;
        this.usage = usage;
        this.parameters = parameters;
    }

    public AbstractCommand(String name, String permission, String usage) {
        this.name = name;
        this.permission = permission;
        this.usage = usage;
    }

    public void add(AbstractCommand children){
        this.children.add(children);
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

    public String getParameters(){
        return this.parameters;
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

    public boolean execute(CommandSender sender, String[] args, String cmd){
        if (!sender.hasPermission(this.permission)){
            return false;
        }
        if (this.children.size() > 0 && args.length > 0 && args[0].length() > 0){
            // has sub-commands
            String[] arg_new = Arrays.copyOfRange(args, 1, args.length);
            for (AbstractCommand sub : this.children){
                if (sub.getName().startsWith(args[0].toLowerCase())){
                    if (sub.execute(sender, arg_new, cmd)){
                        return true;
                    }
                }
            }
            // not match any of the sub-commands
            this.help(sender, args, cmd);
            return true;
        } else if (this.children.size() == 0){
            // no sub-commands
            boolean ret = true;
            if (sender instanceof Player){
                ret = this.executeAsPlayer((Player)sender, args);
            } else {
                ret = this.executeAs(sender, args);
            }
            if (!ret){
                ComponentBuilder builder = new ComponentBuilder("");
                builder.append(tr("Command: ", sender)).color(ChatColor.GRAY)
                       .append(getFullCommand()).color(TranslationUtil.command_color);
                if (getParameters() != null) {
                    builder.append(getParameters() + " ").color(TranslationUtil.command_parameter_color);
                }
                builder.append("- ").color(ChatColor.GRAY)
                       .append(tr(this.usage, sender)).color(ChatColor.YELLOW);
                sender.spigot().sendMessage(builder.create());
            }
            return true;
        } else {
            this.help(sender, args, cmd);
            return true;
        }
    }

    public List<String> tablist(CommandSender sender, String[] args, String cmd) {
        String[] params = null;
        if (this.parameters != null) {
            params = this.parameters.replaceAll("[\\[\\]<>]", "").split(" ");
        }
        if (!sender.hasPermission(this.permission)){
            return new ArrayList<String>();
        }
        if (args.length > 1) {
            if (this.children.size() == 0) {
                if (params != null && args.length <= params.length) {
                    return Arrays.asList(params[args.length-1].split("/"));
                }
                return new ArrayList<String>();
            }
            String[] arg_new = Arrays.copyOfRange(args, 1, args.length);
            for (AbstractCommand sub : this.children){
                if (sub.getName().startsWith(args[0])){
                    return sub.tablist(sender, arg_new, cmd);
                }
            }
            return new ArrayList<String>();
        } else if (args.length == 1) {
            ArrayList<String> ret = new ArrayList<String>();
            if (this.children.size() > 0) {
                for (AbstractCommand sub : this.children){
                    if (sub.getName().startsWith(args[0]) && sender.hasPermission(sub.permission)){
                        ret.add(sub.getName());
                    }
                }       
                return ret;     
            } else {
                if (this.parameters != null && args[0].length() == 0){
                    return Arrays.asList(this.parameters);
                }
                if (params != null && args.length <= params.length) {
                    if ("name".equals(params[args.length-1])) {
                        return PlayerLogic.tabCompleter(args[args.length-1]);
                    } else {
                        return Arrays.asList(params[args.length-1].split("/"));
                    }
                }
                return ret;
            }
        } else {
            ArrayList<String> ret = new ArrayList<String>();
            if (this.children.size() > 0) {
                for (AbstractCommand sub : this.children){
                    ret.add(sub.getName());
                }
            } else {
                if (this.parameters != null){
                    ret.add(this.parameters);
                }
            }
            return ret;
        }
    }

    public void help(CommandSender sender, String[] args, String cmd) {
        if (args != null && args.length > 0){
            ComponentBuilder msgBuilder = new ComponentBuilder("");
            msgBuilder.append("Unknown command: ").color(ChatColor.DARK_RED).bold(true)
                      .append(cmd).color(ChatColor.DARK_RED).bold(true);
            sender.spigot().sendMessage(msgBuilder.create());
        }
        help(sender);
    }

    public void help(CommandSender sender) {
        help(sender, 1);
    }

    public void help(CommandSender sender, int _page) {

        List<AbstractCommand> commands = this.children.stream().filter(cmd->sender.hasPermission(cmd.permission)).collect(Collectors.toList());
        int count = commands.size();
        if (count == 0) {
            return;
        }
        int totalpage = (count - 1)/10 + 1;
        int page = _page > totalpage? totalpage:_page;
        ComponentBuilder builder = new ComponentBuilder("");
        builder.append(tr("EverShop // ", sender)).color(TranslationUtil.title_color)
               .append(tr("EverShop Help", sender)).bold(true).color(ChatColor.WHITE)
               .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE)
               .append(tr("EverShop // ", sender)).color(TranslationUtil.title_color)
               .append(tr("Command: ", sender)).color(ChatColor.GRAY)
               .append(getFullCommand()).color(TranslationUtil.command_color);
        if (getParameters() != null) {
            builder.append(getParameters() + " ").color(TranslationUtil.command_parameter_color);
        }
        builder.append("- ").color(ChatColor.GRAY)
               .append(tr(this.usage, sender)).color(ChatColor.YELLOW)
               .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
        if (totalpage > 1) {
            builder.append(tr("EverShop // ", sender)).color(TranslationUtil.title_color)
            .append(tr("Showing %1$s results Page %2$s of %3$s", sender, count, page, totalpage)).bold(false).color(ChatColor.GRAY)
            .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
        }

        for (int i = (page-1)*10; i < page*10 && i < count; i++){
            AbstractCommand command = commands.get(i);
            builder.append(getFullCommand()).color(TranslationUtil.command_color)
                   .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, getFullCommand() + command.getName() + " "))
                   .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{tr("Click to input this command", sender)})));
            if (getParameters() != null) {
                builder.append(getParameters() + " ").color(TranslationUtil.command_parameter_color);
            } 
            builder.append(command.getName()).color(TranslationUtil.command_color);
            if (command.getParameters() != null) {
                builder.append(" " + command.getParameters()).color(TranslationUtil.command_parameter_color);
            }
            builder.append(" - ").color(ChatColor.GRAY)
                      .append(tr(command.getUsage(), sender)).color(ChatColor.WHITE).append("\n");
        }
        if (sender instanceof Player && totalpage > 1) {
            builder.append("  ").color(ChatColor.GRAY);
            if (page > 1) {
                builder.append("[<< Prev]").color(TranslationUtil.title_color)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{tr("Click to view the previous page", sender)})))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/es help " + (page-1)))
                       .append(" ", ComponentBuilder.FormatRetention.NONE);
            } else {
                builder.append("[<< Prev] ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY);
            }
            builder.append("| ").color(ChatColor.WHITE);
            if (page < totalpage) {
                builder.append("[Next >>]").color(TranslationUtil.title_color)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{tr("Click to view the next page", sender)})))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/es help " + (page+1)))
                       .append(" | ", ComponentBuilder.FormatRetention.NONE);
            } else {
                builder.append("[Next >>]").color(ChatColor.GRAY)
                       .append(" | ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            }
            builder.append("[Enter Page]").color(TranslationUtil.title_color)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{tr("Click to enter page number", sender)})))
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/es help "))
                   .append(" ", ComponentBuilder.FormatRetention.NONE);
        } else if (totalpage > 1) {
            builder.append("Use /is help <page> to view more");
        }
        sender.spigot().sendMessage(builder.create());
    }
    
    protected static void setPath(AbstractCommand root) {
        for (AbstractCommand cmd : root.children) {
            String[] childpath = Arrays.copyOf(root.path, root.path.length + 1);
            childpath[root.path.length] = cmd.getName();
            cmd.setPath(childpath);
            if (cmd.children.size() > 0) {
                setPath(cmd);
            }
        }
    }
    
    // ONLY return false when permission check failed
    public boolean executeAsPlayer(Player player, String[] args) {
        return this.executeAs(player, args);
    }
    public abstract boolean executeAs(CommandSender sender, String[] args);
}