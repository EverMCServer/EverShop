package com.evermc.evershop.command;

import com.evermc.evershop.logic.PlayerLogic;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.evermc.evershop.util.TranslationUtil.send;
import static com.evermc.evershop.util.TranslationUtil.tr;

public class AdvancedCommand extends AbstractCommand {
    public AdvancedCommand() {
        super("advanced", "evershop.advanced", "Toggle advance mode");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        if (PlayerLogic.isAdvanced(player)){
            PlayerLogic.setAdvanced(player, false);
            send("Advanced mode: %1$s", player, tr("off", player));
        } else {
            PlayerLogic.setAdvanced(player, true);
            send("Advanced mode: %1$s", player, tr("on", player));
        }
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        send("This command must be executed by players", sender);
        return true;
    }
}