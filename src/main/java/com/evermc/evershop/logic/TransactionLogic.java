package com.evermc.evershop.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.database.SQLDataSource;

public class TransactionLogic {

    private SQLDataSource SQL;
    private Map<String, Integer> actions = new HashMap<String, Integer>();
    private final String [] default_actions = {"buy", "sell", "aaa"}; 

    public TransactionLogic(EverShop plugin){
        
        this.SQL = plugin.getDataLogic().getSQL();

        for (int i = 0; i < default_actions.length; i ++){
            actions.put(default_actions[i], i + 1);
            Object con = plugin.getConfig().get("evershop.alias." + default_actions[i]);
            if (con == null) continue;
            if (con instanceof String){
                actions.put((String)con, i + 1);
            } else if (con instanceof List<?>){
                for (Object o : (List<?>) con){
                    if (o instanceof String){
                        actions.put((String)o, i + 1);
                    }
                }
            }
        }
    }

    public int getActionType(String action){
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
}