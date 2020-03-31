package com.evermc.evershop.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.structure.TransactionInfo;
import com.evermc.evershop.util.LogUtil;

import org.bukkit.entity.Player;

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
        switch(getEnum(ti.getAction())){
            case BUY:
            if (!ti.shopHasItems()){
                p.sendMessage("sold out");
                break;
            }
            if (!ti.playerCanHold()){
                p.sendMessage("cannot hold");
                break;
            }
            if (!ti.playerHasMoney()){
                p.sendMessage("insufficient money");
                break;
            }
            ti.shopRemoveItems();
            ti.playerGiveItems();
            ti.playerPayMoney();
            ti.shopGiveMoney();
            DataLogic.recordTransaction(si.id, PlayerLogic.getPlayer(p));
            p.sendMessage("you have bought " + ti.getItemsOut() + "!");
            break;

            case SELL:
            if (!ti.playerHasItems()){
                p.sendMessage("not enough items");
                break;
            }
            if (!ti.shopCanHold()){
                p.sendMessage("shop cannot hold");
                break;
            }
            if (!ti.shopHasMoney()){
                p.sendMessage("insufficient money");
                break;
            }
            ti.playerRemoveItems();
            ti.shopGiveItems();
            ti.shopPayMoney();
            ti.playerGiveMoney();
            DataLogic.recordTransaction(si.id, PlayerLogic.getPlayer(p));
            p.sendMessage("you have sell " + ti.getItemsIn() + "!");
            break;

            case IBUY:
            if (!ti.playerCanHold()){
                p.sendMessage("cannot hold");
                break;
            }
            if (!ti.playerHasMoney()){
                p.sendMessage("insufficient money");
                break;
            }
            ti.playerGiveItems();
            ti.playerPayMoney();
            DataLogic.recordTransaction(si.id, PlayerLogic.getPlayer(p));
            p.sendMessage("you have bought " + ti.getItemsOut() + "!");
            break;

            case ISELL:
            if (!ti.playerHasItems()){
                p.sendMessage("not enough items");
                break;
            }
            ti.playerRemoveItems();
            ti.playerGiveMoney();
            DataLogic.recordTransaction(si.id, PlayerLogic.getPlayer(p));
            p.sendMessage("you have sell " + ti.getItemsIn() + "!");
            break;
            default:
            LogUtil.log(Level.SEVERE, "Not Implemented!");
        }
    }
}