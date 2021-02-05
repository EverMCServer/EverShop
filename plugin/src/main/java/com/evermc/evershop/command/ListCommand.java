package com.evermc.evershop.command;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.UUID;
import java.util.regex.Pattern;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.util.TranslationUtil;

import static com.evermc.evershop.util.TranslationUtil.send;
import static com.evermc.evershop.util.TranslationUtil.tr;
import static com.evermc.evershop.util.TranslationUtil.show_location;

public class ListCommand extends AbstractCommand {
    public ListCommand() {
        super("list", "evershop.list", "list shops", "[name] [page]");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        int page = 1;
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
        int page = 1;
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
            final int count = DataLogic.getShopListLength(pi);
            if (count == 0){
                Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                    send("shop not found", sender);
                });
                return;
            }
            int page = _page - 1;
            if (_page*10 >= count) page = (count-1)/10;
            ShopInfo[] sis = DataLogic.getShopList(pi.getId(), page);
            final int showingpage = page + 1;
            Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                String _player;
                if (sender instanceof Player && player.equals(((Player)sender).getUniqueId().toString())) {
                    _player = "";
                } else {
                    _player = player + " ";
                }
                for (ShopInfo si : sis){
                    if (si.getRev() != 0) {
                        continue;
                    }
                    Block b = si.getLocation().getBlock();
                    if (!ShopLogic.isShopSign(b)){
                        DataLogic.removeShop(b.getLocation());
                        si.setRev(-1);
                    }
                }
                ComponentBuilder builder = new ComponentBuilder("");
                builder.append("EverShop // ").color(TranslationUtil.title_color)
                       .append(tr("%1$ss shop list", sender, pi.getName())).bold(true).color(ChatColor.WHITE)
                       .append("\nEverShop // ").bold(false).color(TranslationUtil.title_color)
                       .append(tr("Showing %1$s results Page %2$s of %3$s", sender, count, showingpage, (count-1)/10 + 1)).bold(false).color(ChatColor.GRAY)
                       .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
                for (ShopInfo si : sis){
                    builder.append(" [" + si.getId() + "] ").color(TranslationUtil.command_color)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("click to view shop info")))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/es info " + si.getId()))
                           .append(" ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
                    if (si.getRev() != 0) {
                        builder.color(ChatColor.GRAY).strikethrough(true);
                    } else {
                        builder.color(ChatColor.WHITE);
                    }
                    builder.append(tr("%1$s shop, at %2$s", sender, TransactionLogic.getName(si.getAction()), show_location(si.getLocation(), sender)));
                    if (si.getRev() != 0) {
                        builder.append("[").append(tr("Closed", sender)).append("]");
                    }
                    if (si.getRev() == -1) {
                        builder.append(" Rev: 0");
                    } else {
                        builder.append(" Rev: " + si.getRev());
                    }
                    builder.append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
                }
                if (sender instanceof Player) {
                    builder.append("  ").color(ChatColor.GRAY);
                    if (showingpage > 1) {
                        builder.append("[<< Prev]").color(TranslationUtil.command_color)
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{tr("Click to view the previous page", sender)})))
                                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/es list " + _player + (showingpage-1)))
                               .append(" ", ComponentBuilder.FormatRetention.NONE);
                    } else {
                        builder.append("[<< Prev] ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY);
                    }
                    builder.append("| ").color(ChatColor.WHITE);
                    if (showingpage <= (count-1)/10) {
                        builder.append("[Next >>]").color(TranslationUtil.command_color)
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{tr("Click to view the next page", sender)})))
                                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/es list " + _player + (showingpage+1)))
                               .append(" | ", ComponentBuilder.FormatRetention.NONE);
                    } else {
                        builder.append("[Next >>]").color(ChatColor.GRAY)
                               .append(" | ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
                    }
                    builder.append("[Enter Page]").color(TranslationUtil.command_color)
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{tr("Click to enter page number", sender)})))
                                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/es list " + _player))
                           .append(" ", ComponentBuilder.FormatRetention.NONE);
                }
                sender.spigot().sendMessage(builder.create());
            });
        });
    }
}