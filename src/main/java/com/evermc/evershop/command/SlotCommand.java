package com.evermc.evershop.command;

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

import java.util.Map.Entry;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.logic.DataLogic;
import com.evermc.evershop.logic.PlayerLogic;
import com.evermc.evershop.logic.ShopLogic;
import com.evermc.evershop.logic.TransactionLogic;
import com.evermc.evershop.structure.ExtraInfo;
import com.evermc.evershop.structure.ShopInfo;

import static com.evermc.evershop.util.TranslationUtil.send;
import static com.evermc.evershop.util.TranslationUtil.tr;

public class SlotCommand extends AbstractCommand {
    public SlotCommand() {
        super("slot", "evershop.create.SLOT", "setup slot shop", "[shopid] [set <itemkey> <possibility>]");
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
        } else if (args.length == 1){
            try{
                int shopid = Integer.parseInt(args[0]);
                show_info(player, shopid);
            } catch (Exception e){
                send("Invalid shopid: %1$s", player, args[0]);
            }
        } else if (args.length == 4 && "set".equals(args[1])){
            try{
                int shopid = Integer.parseInt(args[0]);
                modify_info(player, shopid, args[2], args[3]);
            } catch (Exception e){
                send("Invalid shopid: %1$s", player, args[0]);
            }
        } else {
            help(player);
        }

        return true;
    }
    public boolean executeAs(CommandSender sender, String[] args){
        if (args == null || args.length == 0){
            send("use '" + this.getFullCommand() + "<shopid>'", sender);
        } else if (args.length == 1){
            try{
                int shopid = Integer.parseInt(args[0]);
                show_info(sender, shopid);
            } catch (Exception e){
                send("Invalid shopid: %1$s", sender, args[0]);
            }
        } else if (args.length == 4 && "set".equals(args[1])){
            try{
                int shopid = Integer.parseInt(args[0]);
                modify_info(sender, shopid, args[2], args[3]);
            } catch (Exception e){
                send("Invalid shopid: %1$s", sender, args[0]);
            }
        } else {
            help(sender);
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
                if (si.getAction() != TransactionLogic.SLOT.id() && si.getAction() != TransactionLogic.ISLOT.id()) {
                    send("not a slot shop", player);
                    return;
                } else {
                    show_info(player, si);
                    return;
                }
            });
        });
        return;
    }
    private void modify_info(final CommandSender player, final int shopid, String key, String possi){
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
                } else if (si.getAction() != TransactionLogic.SLOT.id() && si.getAction() != TransactionLogic.ISLOT.id()) {
                    send("not a slot shop", player);
                    return;
                } else {
                    modify_info(player, si, key, possi);
                    return;
                }
            });
        });
    }

    private void modify_info(final CommandSender player, final ShopInfo si, String key, String possi){
        ExtraInfo extra = si.getExtraInfo();
        ItemStack item = ExtraInfo.slotItemMap(si.getItemOut()).get(key);
        if (extra.slotSetPossibility(item, possi)) {
            DataLogic.saveShop(si, (a) -> {
                show_info(player, si);
            }, () -> send("ShopInfo save failed", player));
        } else {
            help(player);
            player.spigot().sendMessage(
                new ComponentBuilder("<possibility>").color(ChatColor.LIGHT_PURPLE)
                             .append(" = \n  ").color(ChatColor.WHITE)
                             .append("<amount>").color(ChatColor.GREEN)
                             .append(":").color(ChatColor.YELLOW)
                             .append("<possibility>").color(ChatColor.GREEN)
                             .append(";").color(ChatColor.YELLOW)
                             .append("<amount>").color(ChatColor.GREEN)
                             .append(":").color(ChatColor.YELLOW)
                             .append("<possibility>").color(ChatColor.GREEN)
                             .append("...\n  ").color(ChatColor.WHITE)
                             .append("<amount>").color(ChatColor.LIGHT_PURPLE)
                             .append(" should not exceed the MaxStackSize").color(ChatColor.WHITE)
                             .create());
        }
    }

    private void show_info(final CommandSender player, final ShopInfo si){
        ComponentBuilder builder = new ComponentBuilder("");
        builder.append("EverShop // ").color(ChatColor.LIGHT_PURPLE)
               .append(tr("Slot Shop #%1$s Infomation", player, si.getId())).bold(true).color(ChatColor.WHITE);
        ExtraInfo extra = si.getExtraInfo();
        builder.append("\nEverShop // ").bold(false).color(ChatColor.LIGHT_PURPLE)
               .append(tr("Possibility List: (%1$s possibilitis in total)", player, extra.slotPossibilityAll())).color(ChatColor.YELLOW);

        int count = 0;
        for (Entry<String, ItemStack> entry : ExtraInfo.slotItemMap(si.getItemOut()).entrySet()) {
            count ++;
            builder.append("\n")
                   .append("#" + count + " ").color(ChatColor.LIGHT_PURPLE)
                   .append(tr(entry.getValue(),false))
                   .append(": ").color(ChatColor.WHITE);
            String possibility = extra.getSlotPossibilityMap().get(entry.getKey());
            for (String posi:possibility.split(";")) {
                String amount = posi.split(":")[0];
                String possi = posi.split(":")[1];
                builder.append(tr("[win %1$s: p=%2$s] ", player, amount, possi)).color(ChatColor.WHITE);
            }
            if (player instanceof Player && (player.hasPermission("evershop.info.others") || si.getOwnerId() == PlayerLogic.getPlayerId((Player)player))) {
                builder.append("[+]").color(ChatColor.GREEN)
                     .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/es slot " + si.getId() + " set " + entry.getKey() + " " + possibility))
                     .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{tr("Click to edit", player)}));
            } else if (!(player instanceof Player)){
                builder.append("\n  Key = " + entry.getKey());
            }
        }
        player.spigot().sendMessage(builder.create());
    }
}