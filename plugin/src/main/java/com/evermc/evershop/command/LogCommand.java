package com.evermc.evershop.command;

import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.structure.ExtraInfoImpl;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.util.TranslationUtil;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.HashMap;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.api.ShopType;

import static com.evermc.evershop.util.TranslationUtil.send;
import static com.evermc.evershop.util.TranslationUtil.tr;
import static com.evermc.evershop.util.TranslationUtil.show_date;

public class LogCommand extends AbstractCommand {
    public LogCommand() {
        super("log", "evershop.info", "Show shop transaction logs", "[shopid [page]]");
    }
    public boolean executeAsPlayer(Player player, String[] args) {
        if (args.length == 0){
            Block b = player.getTargetBlockExact(3);
            if (ShopLogic.isShopSign(b)) {
                int shopid = ShopLogic.getShopId((Sign)b.getState());
                if (shopid != 0) {
                    show_log(player, shopid, 1);
                    return true;
                }
            }
            send("please look at a actived shop sign", player);
        } else {
            try{
                int shopid = Integer.parseInt(args[0]);
                int page = 1;
                if (args.length > 1) {
                    page = Integer.parseInt(args[1]);
                }
                show_log(player, shopid, page);
            } catch (Exception e){
                send("Invalid argument: %1$s", player, String.join(" ",args));
                this.help(player);
            }
        }
        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        if (args.length == 0){
            sender.sendMessage("use '" + this.getFullCommand() + "<shopid>'");
        } else {
            try{
                int shopid = Integer.parseInt(args[0]);
                int page = 1;
                if (args.length > 1) {
                    page = Integer.parseInt(args[1]);
                }
                show_log(sender, shopid, page);
            } catch (Exception e){
                send("Invalid argument: %1$s", sender, String.join(" ",args));
                this.help(sender);
            } 
        }
        return true;
    }

    private void show_log(final CommandSender player, final int shopid, final int page){
        Bukkit.getScheduler().runTaskAsynchronously(EverShop.getInstance(), ()->{
            final ShopInfo si = DataLogic.getShopInfo(shopid);
            if (si == null){
                send("shop not found", player);
                return;
            }
            Bukkit.getScheduler().runTask(EverShop.getInstance(), ()->{
                if (!player.hasPermission("evershop.info.others") && player instanceof Player && si.getOwnerId() != PlayerLogic.getPlayerId((Player)player)){
                    send("no permission", player);
                    return;
                } else {
                    show_log(player, si, page);
                    return;
                }
            });
        });
    }

    private void show_log(final CommandSender player, final ShopInfo si, int page){
        if (si == null){
            send("shop not found", player);
            return;
        }
        boolean isSlotShop = (si.getAction() == ShopType.SLOT.id() || si.getAction() == ShopType.ISLOT.id() || si.getAction() == ShopType.ITEMISLOT.id());
        int countAll = DataLogic.getTransactionCount(si.getId(), isSlotShop);
        if (countAll == 0) {
            send("no logs", player);
            return;
        }
        DataLogic.TransactionLog[] data = DataLogic.getTransaction(si.getId(), page, isSlotShop);
        HashMap<String,ItemStack> itemmap = null;
        if (isSlotShop) {
            itemmap = ExtraInfoImpl.slotItemMap(si.getItemOut());
        }
        ComponentBuilder builder = new ComponentBuilder("");
        builder.append("EverShop // ").color(TranslationUtil.command_color)
               .append(tr("Shop #%1$s Transactions", player, si.getId())).bold(true).color(ChatColor.WHITE)
               .append("\nEverShop // ").bold(false).color(TranslationUtil.command_color)
               .append(tr("Showing %1$s results Page %2$s of %3$s", player, countAll, page, countAll/10+1)).color(ChatColor.GRAY)
               .append("\n").color(ChatColor.WHITE);
        if (data != null) {
            for (int i = 0; i < data.length; i++){
                PlayerInfo pi = PlayerLogic.getPlayerInfo(data[i].player_id);
                builder.append(" + ").color(ChatColor.GREEN)
                       .append(String.format("[%1$s] ", countAll - (page-1)*10 - i)).color(ChatColor.GRAY)
                       .append((pi==null?"Unknown":pi.getName())).color(TranslationUtil.command_color)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(pi==null?"Unknown":pi.getUUID().toString())))
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, pi==null?"Unknown":pi.getUUID().toString()))
                       .append(" ", ComponentBuilder.FormatRetention.NONE);
                if (isSlotShop) {
                    builder.append(tr("win", player)).color(ChatColor.WHITE)
                           .append(String.format(" %d ", data[i].amount)).color(ChatColor.YELLOW)
                           .append(tr(itemmap.get(data[i].itemkey), false)).color(ChatColor.YELLOW);
                } else {
                    builder.append(tr("accessed", player)).color(ChatColor.WHITE)
                           .append(" ").color(ChatColor.WHITE)
                           .append(tr("%1$s times", player, data[i].count)).color(TranslationUtil.command_color);
                }
                builder.append(" ").color(ChatColor.WHITE)
                       .append(show_date(data[i].time, player)).color(ChatColor.GRAY)
                       .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            }
        }
        if (player instanceof Player) {
            builder.append("  ").color(ChatColor.GRAY);
            if (page > 1) {
                builder.append("[<< Prev]").color(TranslationUtil.command_color)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{tr("Click to view the previous page", player)})))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/es log " + si.getId() + " " + (page-1)))
                       .append(" ", ComponentBuilder.FormatRetention.NONE);
            } else {
                builder.append("[<< Prev] ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY);
            }
            builder.append("| ").color(ChatColor.WHITE);
            if (page < countAll/10+1) {
                builder.append("[Next >>]").color(TranslationUtil.command_color)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{tr("Click to view the next page", player)})))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/es log " + si.getId() + " " + (page+1)))
                       .append(" | ", ComponentBuilder.FormatRetention.NONE);
            } else {
                builder.append("[Next >>]").color(ChatColor.GRAY)
                       .append(" | ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            }
            builder.append("[Enter Page]").color(TranslationUtil.command_color)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{tr("Click to enter page number", player)})))
                            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/es log " + si.getId() + " "))
                   .append(" ", ComponentBuilder.FormatRetention.NONE);
        }

        player.spigot().sendMessage(builder.create());
    }
}