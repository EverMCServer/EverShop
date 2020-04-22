package com.evermc.evershop.command;

import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.evermc.evershop.EverShop;

public class LogCommand extends AbstractCommand {
    public LogCommand() {
        super("log", "evershop.info", "Show shop transaction logs");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        if (args.length == 0){
            show_log(player);
        } else {
            try{
                int shopid = Integer.parseInt(args[1]);
                show_log(player, shopid);
            } catch (Exception e){
                player.sendMessage("Invalid shopid: " + args[1]);
            }
        }
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        if (args.length == 0){
            sender.sendMessage("use '" + this.getFullCommand() + " [shopid]'");
        } else {
            try{
                int shopid = Integer.parseInt(args[1]);
                show_log(sender, shopid);
            } catch (Exception e){
                sender.sendMessage("Invalid shopid: " + args[1]);
            } 
        }
        return true;
    }
    private void show_log(final Player player){
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
                            show_log(player, si);
                            return;
                        }
                    });
                });
                return;
            }
        }
        player.sendMessage("please look at a actived shop sign");
    }

    private void show_log(final CommandSender player, final int shopid){
        Bukkit.getScheduler().runTaskAsynchronously(EverShop.getInstance(), ()->{
            final ShopInfo si = DataLogic.getShopInfo(shopid);
            Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                if (!player.hasPermission("evershop.info.others") && player instanceof Player && si.getOwnerId() != PlayerLogic.getPlayerId((Player)player)){
                    player.sendMessage("no permission");
                    return;
                } else {
                    show_log(player, si);
                    return;
                }
            });
        });
        return;
    }

    private void show_log(final CommandSender player, final ShopInfo si){
        // TODO - tellraw
        if (si == null){
            player.sendMessage("shop not found");
            return;
        }
        int[][] re = DataLogic.getTransaction(si.getId());
        if (re == null){
            player.sendMessage("no logs");
            return;
        }
        ArrayList<String> msg = new ArrayList<String>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        msg.add("===== Shop #" + si.getId() + " Transactions =====");
        for (int i = 0; i < re.length; i++){
            PlayerInfo pi = PlayerLogic.getPlayerInfo(re[i][0]);
            msg.add(" - " + pi==null?"Unknown":pi.getName() + " @ " + df.format(new Date(((long)re[i][1])*1000*60)) + " x" + re[i][2]);
        }
        for (String a:msg)player.sendMessage(a);
    }
}