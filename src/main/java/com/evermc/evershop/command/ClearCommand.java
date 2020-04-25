package com.evermc.evershop.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.evermc.evershop.util.TranslationUtil.tr;

import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.structure.PlayerInfo;

public class ClearCommand extends AbstractCommand {
    public ClearCommand() {
        super("clear", "evershop.clear", "remove all current selections");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        PlayerInfo p = PlayerLogic.getPlayerInfo(player);
        p.removeRegs();
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        sender.spigot().sendMessage(tr("This command must be executed by players"));
        return true;
    }
}