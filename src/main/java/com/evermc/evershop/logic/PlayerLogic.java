package com.evermc.evershop.logic;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.database.SQLDataSource;
import com.evermc.evershop.util.LogUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class PlayerInfo {
    int id;
    UUID uuid;
    String name;
    boolean advanced;
    Set<Location> reg1;
    Set<Location> reg2;
    public String toString(){
        return "PlayerInfo{id:" + id + ", uuid:" + uuid + ", name:" + name + ", advanced:" + advanced + "}";
    }
}

public class PlayerLogic {
    
    private EverShop plugin;
    private SQLDataSource SQL;
    private Map<UUID, PlayerInfo> cachedPlayers;
    
    public PlayerLogic(EverShop plugin){
        this.plugin = plugin;
        this.cachedPlayers = new ConcurrentHashMap<UUID, PlayerInfo>();
        this.SQL = plugin.getDataLogic().getSQL();
    }

    /**
     * get player id from @param player 
     * if currrent name is different with cached name, update the cache
     * note: this will block thread if not cached, so async!
     * @return playerId
     */
    public int getPlayer(Player p){
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
            return player.id;
        } else {
            return fetchPlayerSync(p);
        }
    }

    private int fetchPlayerSync(Player p){

        UUID uuid = p.getUniqueId();
        String name = p.getDisplayName();

        String query = "INSERT INTO `" + SQL.getPrefix() + "player` (`name`, `uuid`) VALUES ('" + name + "', '" + uuid + "') ON DUPLICATE KEY UPDATE `name` = VALUES(name)";
        SQL.exec(query);

        query = "SELECT * FROM `" + SQL.getPrefix() + "player` WHERE uuid = '" + uuid + "'";
        Object[] result = SQL.queryFirst(query, 4);
        if (result == null){
            LogUtil.log(Level.SEVERE, "Error in fetchPlayer.");
            return 0;
        }

        PlayerInfo pi = new PlayerInfo();
        pi.id = (Integer)result[0];
        pi.uuid = UUID.fromString((String)result[2]);
        pi.name = (String)result[1];
        pi.advanced = (Boolean)result[3];
        cachedPlayers.put(pi.uuid, pi);

        LogUtil.log(Level.INFO, "Load " + pi);

        return 0;
    }

    public void fetchPlayer(Player p){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            fetchPlayerSync(p);
        });
    }
}