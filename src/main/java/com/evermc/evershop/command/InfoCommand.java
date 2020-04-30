package com.evermc.evershop.command;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.util.SerializableLocation;

import static com.evermc.evershop.util.TranslationUtil.send;

public class InfoCommand extends AbstractCommand {
    public InfoCommand() {
        super("info", "evershop.info", "view shop info", "[shopid]");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        if (args == null || args.length == 0){
            Block b = player.getTargetBlockExact(3);
            if (ShopLogic.isShopSign(b)) {
                int shopid = ShopLogic.getShopId((Sign)b.getState());
                if (shopid != 0) {
                    show_info(player, shopid);
                    return true;
                }
            }
            send("please look at a actived shop sign", player);
        } else {
            try{
                int shopid = Integer.parseInt(args[0]);
                show_info(player, shopid);
            } catch (Exception e){
                send("Invalid shopid: %1$s", player, args[0]);
            }
        }

        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        if (args == null || args.length == 0){
            send("use '" + this.getFullCommand() + "<shopid>'", sender);
        } else {
            try{
                int shopid = Integer.parseInt(args[0]);
                show_info(sender, shopid);
            } catch (Exception e){
                send("Invalid shopid: %1$s", sender, args[0]);
            }
        }
        return true;
    }

    private void show_info(final CommandSender player, final int shopid){
        Bukkit.getScheduler().runTaskAsynchronously(EverShop.getInstance(), ()->{
            final ShopInfo si = DataLogic.getShopInfo(shopid);
            Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                if (!player.hasPermission("evershop.info.others") && player instanceof Player && si.getOwnerId() != PlayerLogic.getPlayerId((Player)player)){
                    send("no permission", player);
                    return;
                } else {
                    show_info(player, si);
                    return;
                }
            });
        });
        return;
    }

    private void show_info(final CommandSender player, final ShopInfo si){
        // TODO - tellraw
        if (si == null){
            send("shop not found", player);
            return;
        }
        ArrayList<String> msg = new ArrayList<String>();
        msg.add("===== Shop #" + si.getId() + " Infomation =====");
        msg.add("Owner: " + PlayerLogic.getPlayerName(si.getOwnerId()));
        msg.add("Type: " + TransactionLogic.getName(si.getAction()));
        msg.add("Location: " + SerializableLocation.toLocation(si.getWorldID(), si.getX(), si.getY(), si.getZ()));
        msg.add("Create time: " + si.getEpochString());
        msg.add("Containers: " + si.getTargetAll());
        msg.add("Items: " + si.getItemAll());
        for (String a:msg)player.sendMessage(a);
    }
}