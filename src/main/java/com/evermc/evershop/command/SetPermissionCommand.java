package com.evermc.evershop.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.util.Iterator;
import java.util.UUID;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.structure.ExtraInfo;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;

import static com.evermc.evershop.util.TranslationUtil.send;
import static com.evermc.evershop.util.TranslationUtil.tr;

public class SetPermissionCommand extends AbstractSetCommand {

    public SetPermissionCommand(){
        super("permission", "evershop.set.perm", "set shop permission");
        Bukkit.getScheduler().runTaskLater(EverShop.getInstance(), () -> {
            this.add(new SetPermissionTypeCommand());
            this.add(new SetPermissionAddCommand());
            this.add(new SetPermissionRemoveCommand());
            this.add(new SetPermissionShowCommand());
        }, 1);
    }

    @Override
    public boolean executeAs(CommandSender sender, String[] args, ShopInfo si){
        return true;
    }
}
class SetPermissionTypeCommand extends AbstractSetCommand{
    
    public SetPermissionTypeCommand() {
        super("type", "evershop.set.perm", "set permission type", "<none/blacklist/whitelist>");
    }

    public boolean executeAs(CommandSender sender, String[] args, ShopInfo si){
        if (args.length != 1) {
            return false;
        }
        ExtraInfo ei = si.getExtraInfo();
        if(ei.permissionType(args[0].charAt(0))) {
            send("Permission type set to %1$s", sender, tr(ei.getPermissionType(), sender));
            return true;
        }
        return false;
    }
}
class SetPermissionAddCommand extends AbstractSetCommand{
    
    public SetPermissionAddCommand() {
        super("add", "evershop.set.perm", "add user/group to list", "u:<username>/g:<groupname>");
    }

    @Override
    public boolean executeAs(CommandSender sender, String[] args, ShopInfo si){
        if (args.length != 1) {
            return false;
        }
        ExtraInfo ei = si.getExtraInfo();
        if ("DISABLED".equals(ei.getPermissionType())) {
            send("Set permission type first", sender);
            return true;
        }
        if (args[0].startsWith("u:")) {
            if (!ei.permissionUserAdd(args[0].substring(2))) {
                send("No player named %1$s found!", sender, args[0].substring(2));
                return false;
            }
            send("Player %1$s added to the list!", sender, args[0].substring(2));
            return true;
        } else if (args[0].startsWith("g:")) {
            if (!ei.permissionGroupAdd(args[0].substring(2))) {
                send("No group named %1$s found!", sender, args[0].substring(2));
                return false;
            }
            send("Group %1$s added to the list!", sender, args[0].substring(2));
            return true;
        } else {
            return false;
        }
    }
}
class SetPermissionRemoveCommand extends AbstractSetCommand{
    
    public SetPermissionRemoveCommand() {
        super("remove", "evershop.set.perm", "remove user/group from list", "u:<username>/g:<groupname>");
    }

    @Override
    public boolean executeAs(CommandSender sender, String[] args, ShopInfo si){
        if (args.length != 1) {
            return false;
        }
        ExtraInfo ei = si.getExtraInfo();
        if ("DISABLED".equals(ei.getPermissionType())) {
            send("Set permission type first", sender);
            return true;
        }
        if (args[0].startsWith("u:")) {
            if (!ei.permissionUserRemove(args[0].substring(2))) {
                send("Player %1$s is not in the list!", sender, args[0].substring(2));
                return false;
            }
            send("Player %1$s removed from the list!", sender, args[0].substring(2));
            return true;
        } else if (args[0].startsWith("g:")) {
            if (!ei.permissionGroupRemove(args[0].substring(2))) {
                send("Group %1$s is not in the list!", sender, args[0].substring(2));
                return false;
            }
            send("Group %1$s removed from the list!", sender, args[0].substring(2));
            return true;
        } else {
            return false;
        }
    }
}
class SetPermissionShowCommand extends AbstractSetCommand{
    
    public SetPermissionShowCommand() {
        super("show", "evershop.set.perm", "show the access control info");
    }

    @Override
    public boolean executeAs(CommandSender sender, String[] args, ShopInfo si){
        ExtraInfo ei = si.getExtraInfo();
        ComponentBuilder msgBuilder = new ComponentBuilder("");
        msgBuilder.append("EverShop // ").color(ChatColor.LIGHT_PURPLE)
                  .append(tr("Shop #%1$s Access Control info", sender, si.getId())).bold(true).color(ChatColor.WHITE)
                  .append("\nEverShop // ").bold(false).color(ChatColor.LIGHT_PURPLE)
                  .append("\n").color(ChatColor.WHITE)
                  .append(tr("Type", sender)).color(ChatColor.LIGHT_PURPLE)
                  .append(": ").color(ChatColor.LIGHT_PURPLE)
                  .append(tr(ei.getPermissionType(), sender)).color(ChatColor.WHITE)
                  .append("\n").color(ChatColor.WHITE);
        if (!"DISABLED".equals(ei.getPermissionType())) {
            msgBuilder.append("Users: ").color(ChatColor.LIGHT_PURPLE);
            if (ei.getPermissionUsers().size() == 0) {
                msgBuilder.append("<empty>").color(ChatColor.WHITE);
            } else {
                Iterator<UUID> it = ei.getPermissionUsers().iterator();
                while (it.hasNext()) {
                    UUID uuid = it.next();
                    PlayerInfo pi = PlayerLogic.getPlayerInfo(uuid);
                    msgBuilder.append((pi==null?"<Unknown>":pi.getName())).color(ChatColor.YELLOW)
                              .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(uuid.toString()).create()))
                              .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, uuid.toString()));
                    if (it.hasNext()) {
                        msgBuilder.append(", ").color(ChatColor.WHITE);
                    }
                }
            }
            msgBuilder.append("\nGroups: ").color(ChatColor.LIGHT_PURPLE);
            if (ei.getPermissionGroups().size() == 0) {
                msgBuilder.append("<empty>").color(ChatColor.WHITE);
            } else {
                Iterator<String> it = ei.getPermissionGroups().iterator();
                while (it.hasNext()) {
                    String gr = it.next();
                    msgBuilder.append(gr).color(ChatColor.YELLOW)
                              .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, gr));
                    if (it.hasNext()) {
                        msgBuilder.append(", ").color(ChatColor.WHITE);
                    }
                }
            }
        }
        sender.spigot().sendMessage(msgBuilder.create());
        return true;
    }
}