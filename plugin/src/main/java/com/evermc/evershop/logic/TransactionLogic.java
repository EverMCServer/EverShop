package com.evermc.evershop.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.api.ShopType;
import com.evermc.evershop.structure.ExtraInfoImpl;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.structure.TransactionInfo;
import com.evermc.evershop.util.LogUtil;
import com.evermc.evershop.util.RedstoneUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.BaseComponent;

import static com.evermc.evershop.util.TranslationUtil.send;
import static com.evermc.evershop.util.TranslationUtil.tr;

public class TransactionLogic {
    
    private static Map<Integer, ShopType> map = new HashMap<Integer, ShopType>();
    private static Map<String, Integer> actions = new HashMap<String, Integer>();
    private static Map<Integer, String> actionstr = new HashMap<Integer, String>();

    public static void init(EverShop plugin){

        for (ShopType tl : ShopType.values()){
            actions.put(tl.name(), tl.id());
            map.put(tl.id(), tl);
            Object con = plugin.getConfig().get("evershop.alias." + tl.name());
            if (con == null) {
                actionstr.put(tl.id(), tl.name());
                continue;
            }
            if (con instanceof String){
                actions.put((String)con, tl.id());
                actionstr.put(tl.id(), (String)con);
            } else if (con instanceof List<?>){
                for (Object o : (List<?>) con){
                    if (o instanceof String){
                        actions.put((String)o, tl.id());
                        if (!actionstr.containsKey(tl.id())){
                            actionstr.put(tl.id(), (String)o);
                        }
                    }
                }
            }
            if (!actionstr.containsKey(tl.id())){
                actionstr.put(tl.id(), tl.name());
            }
        }
    }

    public static void reload(EverShop plugin) {
        map.clear();
        actions.clear();
        actionstr.clear();
        init(plugin);
    }

    public static int getId(String action){
        action = action.toUpperCase();
        if (actions.containsKey(action)){
            return actions.get(action);
        }
        int min = Integer.MAX_VALUE;
        String cur = "";
        for (String s : actions.keySet()){
            if (action.indexOf(s) != -1 && action.indexOf(s) < min){
                cur = s; min = action.indexOf(s);
            }
        }
        if (min != Integer.MAX_VALUE){
            return actions.get(cur);
        }
        return 0;
    }

    public static int getPrice(String line){
        if (line.length() == 0) {
            return 0;
        }
        String ret = "";
        line = ChatColor.stripColor(line);
        int i = line.length() - 1;
        while (i >= 0 && !Character.isDigit(line.charAt(i))) i--;
        for (;i >= 0 && Character.isDigit(line.charAt(i)); i--){
            ret = line.charAt(i) + ret;
        }
        if ("".equals(ret)) return 0;
        if (i >= 0 && line.charAt(i) == '-')
            return -Integer.parseInt(ret);
        else
            return Integer.parseInt(ret);
    }

    public static boolean isContainerShop(int action){
        return TransactionLogic.getEnum(action).item_set_count() > 0 || action == ShopType.DONATEHAND.id() || action == ShopType.DISPOSE.id();
    }

    public static boolean needItemSet(int action) {
        return TransactionLogic.getEnum(action).item_set_count() > 0;
    }

    public static int targetCount(int action){
        return TransactionLogic.getEnum(action).location_count();
    }

    public static int itemsetCount(int action){
        return TransactionLogic.getEnum(action).item_set_count();
    }

    public static boolean isRedStoneShop(int action){
        return action == ShopType.DEVICE.id()
        || action == ShopType.DEVICEON.id()
        || action == ShopType.DEVICEOFF.id()
        || action == ShopType.TOGGLE.id();
    }

    public static ShopType getEnum(int action){
        return map.get(action);
    }

    public static String getName(int action){
        return actionstr.get(action);
    }

    public static void doTransaction(ShopInfo si, Player p){
        TransactionInfo ti = new TransactionInfo(si, p);
        si.setSignState(ti.shopHasItems());
        BaseComponent[] ite;
        ItemStack slotItem;
        switch(getEnum(ti.getAction())){
            case BUY:
            if (!ti.shopHasItems()){
                send("shop sold out", p);
                break;
            }
            if (!ti.playerCanHold()){
                send("player cannot hold", p);
                break;
            }
            if (!ti.playerHasMoney()){
                send("player insufficient money", p);
                break;
            }
            ti.shopRemoveItems();
            ti.playerGiveItems();
            ti.playerPayMoney();
            ti.shopGiveMoney();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            ite = ShopLogic.itemToString(si);
            send("%1$s have bought %2$s for %3$s!", p, tr("You", p), ite[0], ite[1]);
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have bought %2$s for %3$s!", ti.getOnlineOwner(), ti.getPlayerName(), ite[0], ite[1]);
            }
            break;

            case SELL:
            if (!ti.playerHasItems()){
                send("player not enough items", p);
                break;
            }
            if (!ti.shopCanHold()){
                send("shop cannot hold", p);
                break;
            }
            if (!ti.shopHasMoney()){
                send("shop insufficient money", p);
                break;
            }
            ti.playerRemoveItems();
            ti.shopGiveItems();
            ti.shopPayMoney();
            ti.playerGiveMoney();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            ite = ShopLogic.itemToString(si);
            send("%1$s have sold %2$s for %3$s!", p, tr("You", p), ite[0], ite[1]);
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have sold %2$s for %3$s!", ti.getOnlineOwner(), ti.getPlayerName(), ite[0], ite[1]);
            }
            break;

            case IBUY:
            if (!ti.playerCanHold()){
                send("player cannot hold", p);
                break;
            }
            if (!ti.playerHasMoney()){
                send("player insufficient money", p);
                break;
            }
            ti.playerGiveItems();
            ti.playerPayMoney();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            ite = ShopLogic.itemToString(si);
            send("%1$s have bought %2$s for %3$s!", p, tr("You", p), ite[0], ite[1]);
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have bought %2$s for %3$s!", ti.getOnlineOwner(), ti.getPlayerName(), ite[0], ite[1]);
            }
            break;

            case ISELL:
            if (!ti.playerHasItems()){
                send("player not enough items", p);
                break;
            }
            ti.playerRemoveItems();
            ti.playerGiveMoney();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            ite = ShopLogic.itemToString(si);
            send("%1$s have sold %2$s for %3$s!", p, tr("You", p), ite[0], ite[1]);
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have sold %2$s for %3$s!", ti.getOnlineOwner(), ti.getPlayerName(), ite[0], ite[1]);
            }
            break;

            case ITRADE:
            if (!ti.playerHasItems()){
                send("player not enough items", p);
                break;
            }
            if (!ti.playerCanHold()){
                send("player cannot hold", p);
                break;
            }
            if (ti.getPrice() > 0){
                if (!ti.playerHasMoney()){
                    send("player insufficient money", p);
                    break;
                }
            }
            ti.playerRemoveItems();
            ti.playerGiveItems();
            if (ti.getPrice() < 0){
                ti.playerGiveMoney();
            } else if (ti.getPrice() > 0){
                ti.playerPayMoney();
            }
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            ite = ShopLogic.itemToString(si);
            send("%1$s have traded %2$s for %3$s!", p, tr("You", p), ite[0], ite[1]);
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have traded %2$s for %3$s!", ti.getOnlineOwner(), ti.getPlayerName(), ite[0], ite[1]);
            }
            break;

            case TRADE:
            if (!ti.playerHasItems()){
                send("player not enough items", p);
                break;
            }
            if (!ti.shopHasItems()){
                send("shop sold out", p);
                break;
            }
            if (!ti.shopCanHold()){
                send("shop cannot hold", p);
                break;
            }
            if (!ti.playerCanHold()){
                send("player cannot hold", p);
                break;
            }
            if (ti.getPrice() < 0){
                if (!ti.shopHasMoney()){
                    send("shop insufficient money", p);
                    break;
                }
            } else if (ti.getPrice() > 0){
                if (!ti.playerHasMoney()){
                    send("player insufficient money", p);
                    break;
                }
            }
            ti.playerRemoveItems();
            ti.shopRemoveItems();
            ti.playerGiveItems();
            ti.shopGiveItems();
            if (ti.getPrice() < 0){
                ti.shopPayMoney();
                ti.playerGiveMoney();
            } else if (ti.getPrice() > 0){
                ti.playerPayMoney();
                ti.shopGiveMoney();
            }
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            ite = ShopLogic.itemToString(si);
            send("%1$s have traded %2$s for %3$s!", p, tr("You", p), ite[0], ite[1]);
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have traded %2$s for %3$s!", ti.getOnlineOwner(), ti.getPlayerName(), ite[0], ite[1]);
            }
            break;

            case TOGGLE:
            if (!RedstoneUtil.isEnabled()) {
                send("redstone shops disabled", p);
                break;
            }
            if (!ti.playerHasMoney()){
                send("player insufficient money", p);
                break;
            }
            ti.playerPayMoney();
            ti.shopGiveMoney();
            ti.toggleRS();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            send("%1$s have toggled the devices for %2$s!", p, tr("You", p), "$" + si.getPrice());
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have toggled the devices for %2$s!", ti.getOnlineOwner(), ti.getPlayerName(), "$" + si.getPrice());
            }
            break;

            case DEVICEON:
            if (!RedstoneUtil.isEnabled()) {
                send("redstone shops disabled", p);
                break;
            }
            if (!ti.playerHasMoney()){
                send("player insufficient money", p);
                break;
            }
            ti.playerPayMoney();
            ti.shopGiveMoney();
            ti.turnOnRS();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            send("%1$s have activated the devices for %2$s!", p, tr("You", p), "$" + si.getPrice());
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have activated the devices for %2$s!", ti.getOnlineOwner(), ti.getPlayerName(), "$" + si.getPrice());
            }
            break;

            case DEVICEOFF:
            if (!RedstoneUtil.isEnabled()) {
                send("redstone shops disabled", p);
                break;
            }
            if (!ti.playerHasMoney()){
                send("player insufficient money", p);
                break;
            }
            ti.playerPayMoney();
            ti.shopGiveMoney();
            ti.turnOffRS();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            send("%1$s have deactivated the devices for %2$s!", p, tr("You", p), "$" + si.getPrice());
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have deactivated the devices for %2$s!", ti.getOnlineOwner(), ti.getPlayerName(), "$" + si.getPrice());
            }
            break;

            case DEVICE:
            if (!RedstoneUtil.isEnabled()) {
                send("redstone shops disabled", p);
                break;
            }
            if (!ti.playerHasMoney()){
                send("player insufficient money", p);
                break;
            }
            ti.playerPayMoney();
            ti.shopGiveMoney();
            ti.turnOnDurationRS();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            send("%1$s have used the devices for %2$s!", p, tr("You", p), "$" + si.getPrice());
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have used the devices for %2$s!", ti.getOnlineOwner(), ti.getPlayerName(), "$" + si.getPrice());
            }
            break;
            
            case DONATEHAND:
            if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                send("no items in main hand", p);
                break;
            }
            if (!ti.shopCanHold()){
                send("shop cannot hold", p);
                break;
            }
            ti.playerRemoveItems();
            ti.shopGiveItems();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            send("%1$s have donated %2$s!", p, tr("You", p), tr(ti.getItemsIn().iterator().next()));
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have donated %2$s!", ti.getOnlineOwner(), ti.getPlayerName(), tr(ti.getItemsIn().iterator().next()));
            }
            break;

            case DISPOSE:
            if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                break;
            }
            if (si.getTargetIn().size() == 0) {
                ti.playerRemoveItems();
                send("%1$s have disposed %2$s!", p, tr("You", p), tr(ti.getItemsIn().iterator().next()));
                if (!ti.isOwner() && ti.getOnlineOwner() != null){
                    send("%1$s have disposed %2$s!", ti.getOnlineOwner(), ti.getPlayerName(), tr(ti.getItemsIn().iterator().next()));
                }
                break;
            }
            ti.playerRemoveItems();
            ti.shopDispose();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p));
            send("%1$s have disposed %2$s!", p, tr("You", p), tr(ti.getItemsIn().iterator().next()));
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s have disposed %2$s!", ti.getOnlineOwner(), ti.getPlayerName(), tr(ti.getItemsIn().iterator().next()));
            }
            break;

            case ISLOT:
            if (!ti.playerHasEmptyInv()){
                send("player cannot hold", p);
                break;
            }
            if (!ti.playerHasMoney()){
                send("player insufficient money", p);
                break;
            }
            ti.playerPayMoney();
            slotItem = ti.playerGiveSlot();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p), ExtraInfoImpl.getItemKey(slotItem), slotItem.getAmount());
            send("%1$s won %2$s!", p, tr("You", p), tr(slotItem));
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s won %2$s!", ti.getOnlineOwner(), ti.getPlayerName(), tr(slotItem));
            }
            break;

            case ITEMISLOT:
            if (!ti.playerHasEmptyInv()){
                send("player cannot hold", p);
                break;
            }
            if (!ti.playerHasItems()){
                send("player not enough items", p);
                break;
            }
            ti.playerRemoveItems();
            slotItem = ti.playerGiveSlot();
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p), ExtraInfoImpl.getItemKey(slotItem), slotItem.getAmount());
            send("%1$s won %2$s!", p, tr("You", p), tr(slotItem));
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s won %2$s!", ti.getOnlineOwner(), ti.getPlayerName(), tr(slotItem));
            }
            break;

            case SLOT:
            if (!ti.playerHasEmptyInv()){
                send("player cannot hold", p);
                break;
            }
            if (!ti.playerHasMoney()){
                send("player insufficient money", p);
                break;
            }
            if (!ti.shopHasSlotItems()){
                send("shop sold out", p);
                break;
            }
            ti.playerPayMoney();
            ti.shopGiveMoney();
            slotItem = ti.playerGiveSlot();
            ti.shopRemoveItems(slotItem);
            DataLogic.recordTransaction(si.getId(), PlayerLogic.getPlayerId(p), ExtraInfoImpl.getItemKey(slotItem), slotItem.getAmount());
            send("%1$s won %2$s!", p, tr("You", p), tr(slotItem));
            if (!ti.isOwner() && ti.getOnlineOwner() != null){
                send("%1$s won %2$s!", ti.getOnlineOwner(), ti.getPlayerName(), tr(slotItem));
            }
            break;

            default:
            LogUtil.log(Level.SEVERE, "Not Implemented!");
        }
    }
}