package com.evermc.evershop.logic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.database.SQLDataSource;
import com.evermc.evershop.util.LogUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerLogic {
    
    private EverShop plugin;
    private SQLDataSource SQL;
    private Map<UUID, PlayerInfo> cachedPlayers;
    
    public PlayerLogic(EverShop plugin){
        this.plugin = plugin;
        this.cachedPlayers = new ConcurrentHashMap<UUID, PlayerInfo>();
        this.SQL = plugin.getDataLogic().getSQL();
    }

    public int getPlayer(Player p){
        return getPlayerInfo(p).id;
    }

    public PlayerInfo getPlayerInfo(int playerid){
        for (PlayerInfo pi : cachedPlayers.values()){
            if (pi.id == playerid) return pi;
        }
        return null;
    }

    /**
     * get playerinfo from @param player 
     * if currrent name is different with cached name, update the cache
     * cache PlayerInfo when log in, to avoid blocking
     */
    public PlayerInfo getPlayerInfo(Player p){
        UUID uuid = p.getUniqueId();
        String name = p.getDisplayName();
        if (cachedPlayers.containsKey(uuid)){
            // cache hit, no block
            PlayerInfo player = cachedPlayers.get(uuid);
            if (!player.name.equals(name)){
                // update cache and db
                player.name = name;
                cachedPlayers.put(uuid, player);
                fetchPlayer(p);
            }
            return player;
        } else {
            return fetchPlayerSync(p);
        }

    }

    public boolean isAdvanced(Player p){
        return getPlayerInfo(p).advanced;
    }

    private PlayerInfo fetchPlayerSync(Player p){

        UUID uuid = p.getUniqueId();
        String name = p.getDisplayName();

        String query = "INSERT INTO `" + SQL.getPrefix() + "player` (`name`, `uuid`) VALUES ('" + name + "', '" + uuid + "') ON DUPLICATE KEY UPDATE `name` = VALUES(name)";
        SQL.exec(query);

        query = "SELECT * FROM `" + SQL.getPrefix() + "player` WHERE uuid = '" + uuid + "'";
        Object[] result = SQL.queryFirst(query, 4);
        if (result == null){
            LogUtil.log(Level.SEVERE, "Error in fetchPlayer.");
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

        LogUtil.log(Level.INFO, "Load " + pi);

        return pi;
    }

    private void fetchPlayer(Player p){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            fetchPlayerSync(p);
        });
    }
}