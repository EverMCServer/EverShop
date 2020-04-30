package com.evermc.evershop.command;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;

import static com.evermc.evershop.util.TranslationUtil.send;

public class ListCommand extends AbstractCommand {
    public ListCommand() {
        super("list", "evershop.list", "list shops", "[player] [page]");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        int page = 0;
        String playerName = "";
        if (args.length == 0){
            playerName = player.getUniqueId().toString();
        } else if (args.length == 1){
            if (Pattern.matches("\\d+", args[0])){
                playerName = player.getUniqueId().toString();
                try{
                    page = Integer.parseInt(args[0]);
                } catch (Exception e){}
            } else {
                if (player.hasPermission("evershop.list.others")){
                    playerName = args[0];
                } else {
                    return false;
                } 
            }
         } else {
            if (player.hasPermission("evershop.list.others")){
                playerName = args[0];
            } else {
                return false;
            }
            try{
                page = Integer.parseInt(args[1]);
            } catch (Exception e){
                return false;
            }
        }
        show_list(player, playerName, page);
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        String playerName = "";
        int page = 0;
        if (args.length == 0){
            sender.sendMessage("use '" + this.getFullCommand() + "<name/uuid> [page]'");
            return true;
        } else if (args.length == 1){
            if (Pattern.matches("\\d+", args[0])){
                sender.sendMessage("use '" + this.getFullCommand() + "<name/uuid> [page]'");
                return true;
            } else {
                if (sender.hasPermission("evershop.list.others")){
                    playerName = args[0];
                } else {
                    return false;
                } 
            }
         } else {
            if (sender.hasPermission("evershop.list.others")){
                playerName = args[0];
            } else {
                return false;
            }
            try{
                page = Integer.parseInt(args[1]);
            } catch (Exception e){
                return false;
            }
        }
        show_list(sender, playerName, page);
        return true;
    }
    
    private void show_list(final CommandSender sender, final String player, int _page){
        final PlayerInfo pi;
        UUID uuid = null;
        try{
            uuid = UUID.fromString(player); 
        } catch (IllegalArgumentException e){}
        if (uuid == null){
            pi = PlayerLogic.getPlayerInfo(player);
            if (pi == null) {
                send("No player named %1$s found!", sender, player);
                return;
            }
        } else {
            pi = PlayerLogic.getPlayerInfo(uuid);
            if (pi == null) {
                send("No uuid %1$s found!", sender, uuid.toString());
                return;
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(EverShop.getInstance(), () -> {
            int count = DataLogic.getShopListLength(pi);
            if (count == 0){
                Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                    send("shop not found", sender);
                });
                return;
            }
            int page = _page;
            if (_page*10 >= count) page = (count-1)/10;
            ShopInfo[] sis = DataLogic.getShopList(pi.getId(), page);
            final ArrayList<String> msg = new ArrayList<String>();
            msg.add("===== " + pi.getName() + "'s shops =====");
            msg.add("Showing page " + (page+1) + " of " + ((count-1)/10+1));
            for (ShopInfo si : sis){
                msg.add(" #" + si.getId() + "  " + TransactionLogic.getName(si.getAction()) + " shop, at "
                     + DataLogic.getWorld(si.getWorldID()).getName() + ":" + si.getX() + "," + si.getY() + "," + si.getZ() + " rev=" + si.getRev());
            }
            Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                for (String s:msg) sender.sendMessage(s);
                for (ShopInfo si : sis){
                    Block b = DataLogic.getWorld(si.getWorldID()).getBlockAt(si.getX(), si.getY(), si.getZ());
                    if (!ShopLogic.isShopSign(b)){
                        DataLogic.removeShop(si.getId());
                    }
                }
            });
        });
    }
}