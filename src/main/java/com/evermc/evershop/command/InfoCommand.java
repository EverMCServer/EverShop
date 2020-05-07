package com.evermc.evershop.command;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.util.SerializableLocation;

import static com.evermc.evershop.util.TranslationUtil.send;
import static com.evermc.evershop.util.TranslationUtil.tr;
import static com.evermc.evershop.util.TranslationUtil.show_location;

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
            if (si == null){
                send("shop not found", player);
                return;
            }
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
        if (si == null){
            send("shop not found", player);
            return;
        }
        ComponentBuilder builder = new ComponentBuilder("");
        PlayerInfo pi = PlayerLogic.getPlayerInfo(si.getOwnerId());
        builder.append("EverShop // ").color(ChatColor.LIGHT_PURPLE)
               .append(tr("Shop #%1$s Infomation", player, si.getId())).bold(true).color(ChatColor.WHITE)
               .append("\nEverShop // ").bold(false).color(ChatColor.LIGHT_PURPLE)
               .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE)
               .append(tr("Owner", player)).color(ChatColor.DARK_AQUA)
               .append(": ").color(ChatColor.DARK_AQUA)
               .append(pi.getName()).color(ChatColor.YELLOW)
                   .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(pi==null?"Unknown":pi.getUUID().toString()).create()))
                   .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, pi==null?"Unknown":pi.getUUID().toString()))
               .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE)
               .append(tr("Type", player)).color(ChatColor.DARK_AQUA)
               .append(": ").color(ChatColor.DARK_AQUA)
               .append(TransactionLogic.getName(si.getAction())).color(ChatColor.YELLOW)
               .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE)
               .append(tr("Price", player)).color(ChatColor.DARK_AQUA)
               .append(": ").color(ChatColor.DARK_AQUA)
               .append("$" + si.getPrice()).color(ChatColor.YELLOW)
               .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE)
               .append(tr("Location", player)).color(ChatColor.DARK_AQUA)
               .append(": ").color(ChatColor.DARK_AQUA)
               .append(show_location(SerializableLocation.toLocation(si.getWorldID(), si.getX(), si.getY(), si.getZ()), player)).color(ChatColor.GREEN)
               .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE)
               .append(tr("Creation Time", player)).color(ChatColor.DARK_AQUA)
               .append(": ").color(ChatColor.DARK_AQUA)
               .append(si.getEpochString()).color(ChatColor.WHITE)
               .append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
        if (si.getTargetIn().size() > 0) {
            builder.append(tr("TargetIn Containers", player)).color(ChatColor.DARK_AQUA)
                   .append(": ").color(ChatColor.DARK_AQUA);
            for (SerializableLocation loc : si.getTargetIn()) {
                builder.append("[").color(ChatColor.LIGHT_PURPLE)
                       .append(show_location(loc.toLocation(), player)).color(ChatColor.GREEN)
                       .append("] ").color(ChatColor.LIGHT_PURPLE);
            }
            builder.append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
        }
        if (si.getTargetOut().size() > 0) {
            builder.append(tr("TargetOut Containers", player)).color(ChatColor.DARK_AQUA)
                   .append(": ").color(ChatColor.DARK_AQUA);
            for (SerializableLocation loc : si.getTargetOut()) {
                builder.append("[").color(ChatColor.LIGHT_PURPLE)
                       .append(show_location(loc.toLocation(), player)).color(ChatColor.GREEN)
                       .append("] ").color(ChatColor.LIGHT_PURPLE);
            }
            builder.append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
        }
        if (si.getItemIn().size() > 0) {
            builder.append(tr("ItemsIn", player)).color(ChatColor.DARK_AQUA)
                   .append(": ").color(ChatColor.DARK_AQUA);
            for (ItemStack item : si.getItemIn()) {
                builder.append(tr(item))
                       .append(", ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            }
            builder.append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
        }
        if (si.getItemOut().size() > 0) {
            builder.append(tr("ItemsOut", player)).color(ChatColor.DARK_AQUA)
                   .append(": ").color(ChatColor.DARK_AQUA);
            for (ItemStack item : si.getItemOut()) {
                builder.append(tr(item))
                       .append(", ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            }
            builder.append("\n", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
        }
        player.spigot().sendMessage(builder.create());
    }
}