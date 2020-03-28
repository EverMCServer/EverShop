package com.evermc.evershop.logic;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.util.LogUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerLogic {
    
    private static EverShop plugin;
    private static Map<UUID, PlayerInfo> cachedPlayers;
    private static Map<Integer, PlayerInfo> cachedPlayerId;
    
    static {
        plugin = null;
        cachedPlayerId = null;
        cachedPlayers = null;
    }

    public static void init(EverShop _plugin){
        plugin = _plugin;
        cachedPlayers = new ConcurrentHashMap<UUID, PlayerInfo>();
        cachedPlayerId = new ConcurrentHashMap<Integer, PlayerInfo>();
        getAllPlayers();
    }

    public static int getPlayer(Player p){
        return getPlayerInfo(p).id;
    }

    public static PlayerInfo getPlayerInfo(int playerid){
        if (cachedPlayerId.containsKey(playerid)){
            return cachedPlayerId.get(playerid);
        } else {
            // this should not happen because all players will be cached at start.
            PlayerInfo pi = fetchPlayerSync(playerid);
            LogUtil.log(Level.WARNING, "fetchPlayerSync: playerid=" + playerid + ", PlayerInfo: " + pi);
            return pi;
        }
    }

    /**
     * get playerinfo from @param player 
     * if currrent name is different with cached name, update the cache
     */
    public static PlayerInfo getPlayerInfo(Player p){
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        if (cachedPlayers.containsKey(uuid)){
            // cache hit, no block
            PlayerInfo player = cachedPlayers.get(uuid);
            if (!player.name.equals(name)){
                // update cache and db
                player.name = name;
                cachedPlayers.put(uuid, player);
                cachedPlayerId.put(player.id, player);
                fetchPlayer(p);
            }
            return player;
        } else {
            // this should not happen because all players will be cached at start.
            PlayerInfo pi = fetchPlayerSync(p);
            LogUtil.log(Level.WARNING, "fetchPlayerSync: Player: [" + p.getUniqueId() + " | " + p.getName() + "], PlayerInfo: " + pi);
            return pi;
        }

    }

    public static boolean isAdvanced(Player p){
        return getPlayerInfo(p).advanced;
    }

    private static PlayerInfo fetchPlayerSync(Player p){

        UUID uuid = p.getUniqueId();
        String name = p.getName();

        String query = "INSERT INTO `" + DataLogic.getPrefix() + "player` (`name`, `uuid`) VALUES ('" + name + "', '" + uuid + "') " + DataLogic.getSQL().ON_DUPLICATE("uuid")+ "`name` = '" + name + "'";
        DataLogic.getSQL().exec(query);

        query = "SELECT * FROM `" + DataLogic.getPrefix() + "player` WHERE uuid = '" + uuid + "'";
        Object[] result = DataLogic.getSQL().queryFirst(query, 4);
        if (result == null){
            LogUtil.log(Level.SEVERE, "Error in fetchPlayer(Player= [" + p.getUniqueId() + " | " + p.getName() + "]).");
            return null;
        }

        PlayerInfo pi = new PlayerInfo();
        pi.id = (Integer)result[0];
        pi.uuid = UUID.fromString((String)result[2]);
        pi.name = (String)result[1];
        if (result[3] instanceof Integer)
            pi.advanced = ((int)result[3]) != 0;
        else
            pi.advanced = (Boolean)result[3];
        pi.reg_is_container = false;
        pi.reg1 = new CopyOnWriteArraySet<Location>();
        pi.reg2 = new CopyOnWriteArraySet<Location>();
        cachedPlayers.put(pi.uuid, pi);
        cachedPlayerId.put(pi.id, pi);

        LogUtil.log(Level.INFO, "Load " + pi);

        return pi;
    }

    private static PlayerInfo fetchPlayerSync(int playerid){

        String query = "SELECT * FROM `" + DataLogic.getPrefix() + "player` WHERE id = '" + playerid + "'";
        Object[] result = DataLogic.getSQL().queryFirst(query, 4);
        if (result == null){
            LogUtil.log(Level.SEVERE, "Error in fetchPlayer(playerid=" + playerid +").");
            return null;
        }

        PlayerInfo pi = new PlayerInfo();
        pi.id = (Integer)result[0];
        pi.uuid = UUID.fromString((String)result[2]);
        pi.name = (String)result[1];
        pi.advanced = (Boolean)result[3];
        pi.reg_is_container = false;
        pi.reg1 = new CopyOnWriteArraySet<Location>();
        pi.reg2 = new CopyOnWriteArraySet<Location>();
        cachedPlayers.put(pi.uuid, pi);
        cachedPlayerId.put(pi.id, pi);

        LogUtil.log(Level.INFO, "Load " + pi);

        return pi;
    }

    private static void fetchPlayer(Player p){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            fetchPlayerSync(p);
        });
    }

    public static void getAllPlayers(){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            String query = "SELECT * FROM `" + DataLogic.getPrefix() + "player`";
            List<Object[]> result = DataLogic.getSQL().query(query, 4);
            if (result == null || result.size() == 0){
                LogUtil.log(Level.SEVERE, "getAllPlayers(): no player found.");
                return;
            }
            
            for (Object[] o : result){
                PlayerInfo pi = new PlayerInfo();
                pi.id = (Integer)o[0];
                pi.uuid = UUID.fromString((String)o[2]);
                pi.name = (String)o[1];
                if (o[3] instanceof Integer)
                    pi.advanced = ((int)o[3]) != 0;
                else
                    pi.advanced = (Boolean)o[3];
                pi.reg_is_container = false;
                pi.reg1 = new CopyOnWriteArraySet<Location>();
                pi.reg2 = new CopyOnWriteArraySet<Location>();
                cachedPlayers.put(pi.uuid, pi);
                cachedPlayerId.put(pi.id, pi);
            }
    
            LogUtil.log(Level.INFO, "Load " + cachedPlayers.size() + " players.");
    
        });
    }
}