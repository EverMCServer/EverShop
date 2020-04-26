package com.evermc.evershop.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.structure.TransactionInfo;
import com.evermc.evershop.util.LogUtil;
import com.evermc.evershop.util.RedstoneUtil;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;

import static com.evermc.evershop.util.TranslationUtil.send;
import static com.evermc.evershop.util.TranslationUtil.tr;

public enum TransactionLogic {
    
    //
    // Transaction type enum
    // type(id, location_count, item_set_count)
    BUY(1, 1, 1),
    SELL(2, 1, 1),
    TRADE(3, 2, 2),
    IBUY(4, 0, 1),
    ISELL(5, 0, 1),
    ITRADE(6, 0, 2),
    ISLOT(7, 0, 0),
    TOGGLE(8, 1, 0),
    DEVICE(9, 1, 0),
    DEVICEON(10, 1, 0),
    DEVICEOFF(11, 1, 0)
    ;

    private int index;
    private int location_count;
    private int item_set_count;

    TransactionLogic(int index, int location_count, int item_set_count){
        this.index = index;
        this.location_count = location_count;
        this.item_set_count = item_set_count;
    }
    
    private static Map<Integer, TransactionLogic> map = new HashMap<Integer, TransactionLogic>();
    private static Map<String, Integer> actions = new HashMap<String, Integer>();
    private static Map<Integer, String> actionstr = new HashMap<Integer, String>();

    public static void init(EverShop plugin){

        for (TransactionLogic tl : TransactionLogic.values()){
            actions.put(tl.name(), tl.index);
            map.put(tl.index, tl);
            Object con = plugin.getConfig().get("evershop.alias." + tl.name());
            if (con == null) {
                actionstr.put(tl.index, tl.name());
                continue;
            }
            if (con instanceof String){
                actions.put((String)con, tl.index);
                actionstr.put(tl.index, (String)con);
            } else if (con instanceof List<?>){
                for (Object o : (List<?>) con){
                    if (o instanceof String){
                        actions.put((String)o, tl.index);
                        if (!actionstr.containsKey(tl.index)){
                            actionstr.put(tl.index, (String)o);
                        }
                    }
                }
            }
            if (!actionstr.containsKey(tl.index)){
                actionstr.put(tl.index, tl.name());
            }
        }
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
        String ret = "";
        line = ChatColor.stripColor(line);
        int i = line.length() - 1;
        while (i >= 0 && !Character.isDigit(line.charAt(i))) i--;
        for (;i >= 0 && Character.isDigit(line.charAt(i)); i--){
            ret = line.charAt(i) + ret;
        }
        if ("".equals(ret)) return 0;
        if (line.charAt(i) == '-')
            return -Integer.parseInt(ret);
        else
            return Integer.parseInt(ret);
    }

    public static boolean isContainerShop(int action){
        return TransactionLogic.getEnum(action).item_set_count > 0;
    }

    public static int targetCount(int action){
        return TransactionLogic.getEnum(action).location_count;
    }

    public static int itemsetCount(int action){
        return TransactionLogic.getEnum(action).item_set_count;
    }

    public static TransactionLogic getEnum(int action){
        return map.get(action);
    }

    public static String getName(int action){
        return actionstr.get(action);
    }

    public int id(){
        return this.index;
    }

    public static void doTransaction(ShopInfo si, Player p){
        TransactionInfo ti = new TransactionInfo(si, p);
        si.setSignState(ti.shopHasItems());
        BaseComponent[] ite;
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
            send("you have %1$s %2$s for %3$s!", p, tr("BUY_AS_USER", p), ite[0], ite[1]);
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
            send("you have %1$s %2$s for %3$s!", p, tr("SELL_AS_USER", p), ite[0], ite[1]);
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
            send("you have %1$s %2$s for %3$s!", p, tr("BUY_AS_USER", p), ite[0], ite[1]);
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
            send("you have %1$s %2$s for %3$s!", p, tr("SELL_AS_USER", p), ite[0], ite[1]);
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
            send("you have %1$s %2$s for %3$s!", p, tr("TRADE_AS_USER", p), ite[0], ite[1]);
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
            send("you have %1$s %2$s for %3$s!", p, tr("TRADE_AS_USER", p), ite[0], ite[1]);
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
            send("you have %1$s %2$s for %3$s!", p, tr("TOGGLE_AS_USER", p), "", "$" + si.getPrice());
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
            send("you have %1$s %2$s for %3$s!", p, tr("DEVICEON_AS_USER", p), "", "$" + si.getPrice());
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
            send("you have %1$s %2$s for %3$s!", p, tr("DEVICEOFF_AS_USER", p), "", "$" + si.getPrice());
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
            send("you have %1$s %2$s for %3$s!", p, tr("DEVICE_AS_USER", p), "", "$" + si.getPrice());
            break;

            default:
            LogUtil.log(Level.SEVERE, "Not Implemented!");
        }
    }
}