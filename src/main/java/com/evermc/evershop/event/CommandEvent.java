package com.evermc.evershop.event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CommandEvent implements CommandExecutor, TabCompleter {
    
    private String[][] base_commands = {
        {"advanced", "evershop.advanced", "toggle advance mode"},
        {"help",     "evershop",          "show help"},
        {"info",     "evershop.info",     "view shop info", "shopid"},
        {"inspect",  "evershop.inspect",  "toggle inspect mode"},
        {"list",     "evershop.list",     "list shops", "player", "page"},
        {"log",      "evershop.info",     "show shop transaction logs", "shopid", "page"},
        {"reload",   "evershop.admin.op", "reload the plugin"},
        {"set",      "evershop.set",      "set shop attributes", "..."}
    };
    private String[][] set_commands = {
        {"permission", "evershop.set.perm", "set shop permission", "..."},
        {"text",       "evershop.set.text", "set sign text", "1-4", "text"},
        {"price",      "evershop.set.price","set shop price", "price"},
        {"time",       "evershop.set.time", "set redstone poweron time", "time"}
    };
    private String[][] set_perm_commands = {
        {"type",       "evershop.set.perm", "set permission type"},
        {"deny",       "evershop.set.perm", "set deny"},
        {"allow",      "evershop.set.perm", "set allow"}
    };

    // use naive if-else check since few commands available
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] _args) {

        // discards spaces in args
        ArrayList<String> alargs = new ArrayList<String>();
        for (int i = 0; i < _args.length; i ++) {
            if (_args[i].length() > 0){
                alargs.add(_args[i]);
            }
        }
        String[] args = alargs.toArray(new String[alargs.size()]);
        
        if (args.length == 0){
            show_usage(sender);
            return true;
        } else {
            if ("inspect".startsWith(args[0]) && sender.hasPermission("evershop.inspect")){
                // TODO - inspect mode

            } else if ("list".startsWith(args[0]) && sender.hasPermission("evershop.list")){
                int page = 0;
                String player = "";
                if (args.length == 1){
                    if (sender instanceof Player){
                        player = ((Player)sender).getUniqueId().toString();
                    } else {
                        sender.sendMessage("use '" + label + " " + args[0] + " [player]' instead");
                    }
                } else if (args.length == 2){
                    if (Pattern.matches("\\d+", args[1])){
                        if (sender instanceof Player){
                            player = ((Player)sender).getUniqueId().toString();
                            try{
                                page = Integer.parseInt(args[1]);
                            } catch (Exception e){}
                        } else {
                            sender.sendMessage("use '" + label + " " + args[0] + " [player]' instead");
                        }
                    } else {
                        if (sender.hasPermission("evershop.list.others")){
                            player = args[1];
                        } else {
                            show_usage(sender);
                            return true;
                        }
                    }
                } else {
                    if (sender.hasPermission("evershop.list.others")){
                        player = args[1];
                    } else {
                        show_usage(sender);
                    }
                    if (!Pattern.matches("\\d+", args[2])){
                        show_usage(sender);
                        return true;
                    } else {
                        try{
                            page = Integer.parseInt(args[2]);
                        } catch (Exception e){}
                    }
                }
                show_list(sender, player, page);
                return true;
            } else if ("log".startsWith(args[0]) && sender.hasPermission("evershop.info")){
                if (args.length == 1){
                    if (sender instanceof Player){
                        show_log((Player)sender);
                    } else {
                        sender.sendMessage("use '" + label + " " + args[0] + " [shopid]'");
                    }
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
        }
        show_usage(sender, _args);
        return false;
    }

    // use naive if-else check since few commands available
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] _args){

        // discards spaces in args
        ArrayList<String> alargs = new ArrayList<String>();
        for (int i = 0; i < _args.length - 1; i ++) {
            if (_args[i].length() > 0){
                alargs.add(_args[i]);
            }
        }
        alargs.add(_args[_args.length - 1]);
        String[] args = alargs.toArray(new String[alargs.size()]);

        ArrayList<String> ret = new ArrayList<String>();

        if (args.length == 0){
            for (String[] t : base_commands){
                if (sender.hasPermission(t[1])){
                    ret.add(t[0]);
                }
            }
            return ret;
        } else if (args.length == 1){
            for (String[] t : base_commands){
                if (t[0].startsWith(args[0]) && sender.hasPermission(t[1])){
                    ret.add(t[0]);
                }
            }
            return ret;
        } else if (args.length == 2){
            if ("list".startsWith(args[0])){
                return null; // show online player list
            } else if ("set".startsWith(args[0])){
                if (args[1].length() == 0) {
                    for (String[] t : set_commands){
                        if (sender.hasPermission(t[1])){
                            ret.add(t[0]);
                        }
                    }
                    return ret;
                } else if (Character.isDigit(args[1].charAt(0))){
                    return ret; //entered shopid
                } else {
                    for (String[] t : set_commands){
                        if (t[0].startsWith(args[1]) && sender.hasPermission(t[1])){
                            ret.add(t[0]);
                        }
                    }
                    return ret;
                }
            } else {
                return ret;
            }
        } else if (args.length == 3){
            if ("set".startsWith(args[0])){
                if (Character.isDigit(args[1].charAt(0))){
                    for (String[] t : set_commands){
                        if ((args[2].length() == 0 || t[0].startsWith(args[2])) && sender.hasPermission(t[1])){
                            ret.add(t[0]);
                        }
                    }
                    return ret;
                } else if ("permission".startsWith(args[1])){
                    for (String[] t : set_perm_commands){
                        if ((args[2].length() == 0 || t[0].startsWith(args[2])) && sender.hasPermission(t[1])){
                            ret.add(t[0]);
                        }
                    }
                    return ret;
                }
            }
        } else if (args.length == 4){
            if ("set".startsWith(args[0]) && Character.isDigit(args[1].charAt(0)) && "permission".startsWith(args[2])){
                for (String[] t : set_perm_commands){
                    if ((args[3].length() == 0 || t[0].startsWith(args[3])) && sender.hasPermission(t[1])){
                        ret.add(t[0]);
                    }
                }
                return ret;
            }
        }
        return ret;
    }

    private void show_usage(CommandSender sender, String[] cmds){
        String cmd = "";
        for (String a:cmds){
            cmd += a + " ";
        }
        sender.sendMessage("Unknown command: " + cmd);
        show_usage(sender);
    }

    private void show_usage(CommandSender sender){
        ArrayList<String> msg = new ArrayList<String>();
        msg.add("EverShop help:");
        for (String[] t : base_commands){
            if (sender.hasPermission(t[1])){
                String str = ChatColor.AQUA.toString() + t[0] + " " + ChatColor.GREEN.toString();
                for (int i = 3; i < t.length; i ++){
                    str += "[" + t[i] + "] ";
                }
                str += ChatColor.GRAY.toString() + "- " + t[2];
                msg.add(str);
            }
        }
        for (String s:msg){
            sender.sendMessage(s);
        }
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

    private void show_list(final CommandSender sender, final String player, int _page){
        final PlayerInfo pi;
        UUID uuid = null;
        try{
            uuid = UUID.fromString(player); 
        } catch (IllegalArgumentException e){}
        if (uuid == null){
            pi = PlayerLogic.getPlayerInfo(player);
            if (pi == null) {
                sender.sendMessage("No player named " + player + " found!");
                return;
            }
        } else {
            pi = PlayerLogic.getPlayerInfo(uuid);
            if (pi == null) {
                sender.sendMessage("No uuid " + uuid + " found!");
                return;
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(EverShop.getInstance(), () -> {
            int count = DataLogic.getShopListLength(pi);
            if (count == 0){
                Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                    sender.sendMessage("No shops found!");
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
                     + DataLogic.getWorld(si.getWorldID()).getName() + ":" + si.getX() + "," + si.getY() + "," + si.getZ());
            }
            Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                for (String s:msg) sender.sendMessage(s);
                for (ShopInfo si : sis){
                    BlockState bs = DataLogic.getWorld(si.getWorldID()).getBlockAt(si.getX(), si.getY(), si.getZ()).getState();
                    if (!(bs instanceof Sign && ((Sign)bs).getLine(0).length() > 0 && (int)((Sign)bs).getLine(0).charAt(0) == 167)){
                        DataLogic.removeShop(si.getId());
                    }
                }
            });
        });
    }
}