package com.evermc.evershop.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.evermc.evershop.util.TranslationUtil.tr;

import java.util.ArrayList;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.util.SerializableLocation;

public class InfoCommand extends AbstractCommand {
    public InfoCommand() {
        super("info", "evershop.info", "view shop info");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        if (args == null || args.length == 0){
            show_info(player);
        } else {
            try{
                int shopid = Integer.parseInt(args[0]);
                show_info(player, shopid);
            } catch (Exception e){
                player.sendMessage("Invalid shopid: " + args[0]);
            }
        }

        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        if (args == null || args.length == 0){
            sender.spigot().sendMessage(tr("use '" + this.getFullCommand() + "[shopid]'"));
        } else {
            try{
                int shopid = Integer.parseInt(args[0]);
                show_info(sender, shopid);
            } catch (Exception e){
                sender.sendMessage("Invalid shopid: " + args[0]);
            }
        }
        return true;
    }

    private void show_info(final Player player){
        Block b = player.getTargetBlockExact(3);
        if (b != null && b.getState() != null && b.getState() instanceof Sign){
            Sign sign = (Sign)b.getState();
            if (sign.getLine(0).length() > 0 && (int)sign.getLine(0).charAt(0) == 167){
                final Location loc = b.getLocation();
                Bukkit.getScheduler().runTaskAsynchronously(EverShop.getInstance(), ()->{
                    final ShopInfo si = DataLogic.getShopInfo(loc);
                    Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                        if (!player.hasPermission("evershop.info.others") && si.getOwnerId() != PlayerLogic.getPlayerId(player)){
                            player.sendMessage("no permission");
                            return;
                        } else {
                            show_info(player, si);
                            return;
                        }
                    });
                });
                return;
            }
        }
        player.sendMessage("please look at a actived shop sign");
    }

    private void show_info(final CommandSender player, final int shopid){
        Bukkit.getScheduler().runTaskAsynchronously(EverShop.getInstance(), ()->{
            final ShopInfo si = DataLogic.getShopInfo(shopid);
            Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                if (!player.hasPermission("evershop.info.others") && player instanceof Player && si.getOwnerId() != PlayerLogic.getPlayerId((Player)player)){
                    player.sendMessage("no permission");
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
            player.sendMessage("shop not found");
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