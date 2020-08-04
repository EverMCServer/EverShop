package com.evermc.evershop.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.evermc.evershop.util.TranslationUtil.send;
import static com.evermc.evershop.util.LogUtil.info;

import java.util.UUID;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;

public class MigrateCommand extends AbstractCommand {
    public MigrateCommand() {
        super("migrate", "evershop.admin.op", "modify user uuid", "<old-uuid> <new-uuid> [force]");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        return executeAs(player, args);
    }
    public boolean executeAs(CommandSender sender, String[] args){
        if (args.length < 2 || args.length > 3) {
            this.help(sender);
            return true;
        }
        final boolean forceMode;
        if (args.length == 3 && args[2].equals("force")) {
            forceMode = true;
        } else {
            forceMode = false;
        }
        UUID _olduuid = null, _newuuid = null;
        try {
            _olduuid = UUID.fromString(args[0]);
            _newuuid = UUID.fromString(args[1]);
        }catch(Exception e){}
        if (_olduuid == null || _newuuid == null) {
            this.help(sender);
            return true;
        }
        if (_olduuid.equals(_newuuid)) {
            send("same UUID, return", sender);
            return true;
        }
        send("start migrating " + _olduuid.toString() + " to " + _newuuid.toString(), sender);
        info("start migrating " + _olduuid.toString() + " to " + _newuuid.toString());
        final UUID olduuid = _olduuid;
        final UUID newuuid = _newuuid;

        Bukkit.getScheduler().runTaskAsynchronously(EverShop.getInstance(), ()->{
            PlayerInfo oldPlayer = PlayerLogic.fetchPlayerSync(olduuid);
            if (oldPlayer == null) {
                send("UUID " + olduuid.toString() + " not found. ", sender);
                info("UUID " + olduuid.toString() + " not found. ");
                return;
            }
            PlayerInfo newPlayer = PlayerLogic.fetchPlayerSync(newuuid);
            if (newPlayer != null) {
                if (forceMode) {
                    send("new UUID " + newuuid.toString() + " exists. Using force mode.", sender);
                    info("new UUID " + newuuid.toString() + " exists. Using force mode.");
                    ShopInfo[] sis = DataLogic.getShopList(newPlayer.getId());
                    for (ShopInfo si : sis) {
                        info("Transferring shop #" + si.getId() + "...");
                        if (!DataLogic.changeOwner(si.getId(), oldPlayer.getId())) {
                            info("Shop #" + si.getId() + " FAILED");
                        }else {
                            info("Shop #" + si.getId() + " SUCCESS");
                        }
                    }
                    if (!PlayerLogic.removePlayer(newPlayer)) {
                        send("error occurred. exit.", sender);
                        info("error occurred. exit.");
                        return;
                    }
                } else {
                    send("new UUID " + newuuid.toString() + " exists. exit.", sender);
                    info("new UUID " + newuuid.toString() + " exists. exit.");
                    return;
                }
            }
            PlayerLogic.removeCachedPlayer(oldPlayer);
            oldPlayer.setUUID(newuuid);
            if (!PlayerLogic.updatePlayer(oldPlayer)) {
                send("error occurred. exit.", sender);
                info("error occurred. exit.");
                return;
            }
            send("Successfully migrated " + olduuid + " -> " + newuuid, sender);
            info("Successfully migrated " + olduuid + " -> " + newuuid);
        });
        return true;
    }
}