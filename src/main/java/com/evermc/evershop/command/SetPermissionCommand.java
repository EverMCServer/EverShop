package com.evermc.evershop.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.evermc.evershop.util.LogUtil.severe;

import com.evermc.evershop.EverShop;

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

    public boolean executeAsPlayer(Player player, String[] args, int shopid) {
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args, int shopid){
        if (shopid == 0) {
            // should not happen
            severe("Illegal invocation: " + this.getName());
            return true;
        }
        return true;
    }
}
class SetPermissionTypeCommand extends AbstractSetCommand{
    
    public SetPermissionTypeCommand() {
        super("type", "evershop.set.perm", "set permission type", "<none/blacklist/whitelist>");
    }

    public boolean executeAsPlayer(Player player, String[] args, int shopid) {
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args, int shopid){
        return true;
    }
}
class SetPermissionAddCommand extends AbstractSetCommand{
    
    public SetPermissionAddCommand() {
        super("add", "evershop.set.perm", "add user/group to list", "u:<username>/g:<groupname>");
    }

    public boolean executeAsPlayer(Player player, String[] args, int shopid) {
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args, int shopid){
        return true;
    }
}
class SetPermissionRemoveCommand extends AbstractSetCommand{
    
    public SetPermissionRemoveCommand() {
        super("remove", "evershop.set.perm", "remove user/group from list", "u:<username>/g:<groupname>");
    }

    public boolean executeAsPlayer(Player player, String[] args, int shopid) {
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args, int shopid){
        return true;
    }
}
class SetPermissionShowCommand extends AbstractSetCommand{
    
    public SetPermissionShowCommand() {
        super("show", "evershop.set.perm", "show the list");
    }

    public boolean executeAsPlayer(Player player, String[] args, int shopid) {
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args, int shopid){
        return true;
    }
}