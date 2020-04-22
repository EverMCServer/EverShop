package com.evermc.evershop.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.evermc.evershop.util.TranslationUtil.tr;

public class InspectCommand extends AbstractCommand {
    public InspectCommand() {
        super("inspect", "evershop.inspect", "Toggle inspection mode");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        // TODO - inspect mode
        player.sendMessage("TODO");
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        sender.spigot().sendMessage(tr("This command must be executed by players"));
        return true;
    }
}