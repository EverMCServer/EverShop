package com.evermc.evershop.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.evermc.evershop.util.TranslationUtil.send;

import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.structure.PlayerInfo;

public class ClearCommand extends AbstractCommand {
    public ClearCommand() {
        super("clear", "evershop", "remove all current selections");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        PlayerInfo p = PlayerLogic.getPlayerInfo(player);
        p.removeRegs();
        send("You have removed all your selections", player);
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        send("This command must be executed by players", sender);
        return true;
    }
}