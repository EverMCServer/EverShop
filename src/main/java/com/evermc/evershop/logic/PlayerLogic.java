package com.evermc.evershop.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.util.LogUtil;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerLogic {
    
    private static EverShop plugin = null;
    private static Map<UUID, PlayerInfo> cachedPlayers = null;
    private static Map<Integer, PlayerInfo> cachedPlayerId = null;
    private static Map<String, PlayerInfo> cachedPlayerName = null;

    public static void init(EverShop _plugin){
        plugin = _plugin;
        cachedPlayers = new ConcurrentHashMap<UUID, PlayerInfo>();
        cachedPlayerId = new ConcurrentHashMap<Integer, PlayerInfo>();
        cachedPlayerName = new ConcurrentHashMap<String, PlayerInfo>();
        getAllPlayers();
    }

    public static int getPlayerId(OfflinePlayer p){
        return getPlayerInfo(p).getId();
    }

    public static boolean isAdvanced(OfflinePlayer p){
        return getPlayerInfo(p).isAdvanced();
    }

    public static String getPlayerName(OfflinePlayer p){
        return getPlayerInfo(p).getName();
    }

    public static String getPlayerName(int playerid){
        return getPlayerInfo(playerid).getName();
    }

    public static UUID getPlayerUUID(int playerid){
        return getPlayerInfo(playerid).getUUID();
    }

    public static OfflinePlayer getOfflinePlayer(int playerid){
        return Bukkit.getOfflinePlayer(getPlayerUUID(playerid));
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

    public static PlayerInfo getPlayerInfo(String playername){
        if (cachedPlayerName.containsKey(playername)){
            return cachedPlayerName.get(playername);
        } else {
            return null;
        }
    }

    public static PlayerInfo getPlayerInfo(UUID uuid){
        if (cachedPlayers.containsKey(uuid)){
            return cachedPlayers.get(uuid);
        } else {
            return null;
        }
    }

    public static void updatePlayerInfo(OfflinePlayer p){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            getPlayerInfo(p);
        });
    }
    
    /**
     * get playerinfo from @param player 
     * if currrent name is different with cached name, update the cache
     */
    public static PlayerInfo getPlayerInfo(OfflinePlayer p){
        UUID uuid = p.getUniqueId();
        String name = p.getName();
        if (cachedPlayers.containsKey(uuid)){
            // cache hit, no block
            PlayerInfo player = cachedPlayers.get(uuid);
            if (!player.getName().equals(name)){
                // update cache and db
                player.setName(name);
                cachedPlayers.put(player.getUUID(), player);
                cachedPlayerId.put(player.getId(), player);
                cachedPlayerName.put(player.getName(), player);
                fetchPlayer(p);
            }
            return player;
        } else {
            PlayerInfo pi = fetchPlayerSync(p);
            LogUtil.log(Level.WARNING, "Add new Player: [" + p.getUniqueId() + " | " + p.getName() + " | ID=" + pi.getId() + "]");
            return pi;
        }

    }

    public static void setAdvanced(final Player p, final boolean advanced){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            PlayerInfo pi = getPlayerInfo(p);
            pi.setAdvanced(advanced);
            String query = "INSERT INTO `" + DataLogic.getPrefix() + "player` (`name`, `uuid`, `advanced`) VALUES ('" + pi.getName() + "', '"
            + pi.getUUID() + "', '" + pi.getAdvanced() + "') " + DataLogic.getSQL().ON_DUPLICATE("uuid")+ "`advanced` = " + pi.getAdvanced();
            DataLogic.getSQL().exec(query);
        });
    }

    public static boolean removePlayer(final PlayerInfo pi){
        removeCachedPlayer(pi);
        String query = "DELETE FROM `" + DataLogic.getPrefix() + "player` WHERE `id` = '" + pi.getId() + "' AND `uuid` = '" + pi.getUUID() + "'";
        int ret = DataLogic.getSQL().exec(query);
        if (ret != 1) {
            LogUtil.severe("Error in removing player " + pi + "; retval = " + ret);
            return false;
        }
        return true;
    }

    public static void removeCachedPlayer(final PlayerInfo pi){
        cachedPlayerId.remove(pi.getId());
        cachedPlayers.remove(pi.getUUID());
        cachedPlayerName.remove(pi.getName());
    }

    public static boolean updatePlayer(final PlayerInfo pi){
        String query = "UPDATE `" + DataLogic.getPrefix() + "player` SET `uuid` = '" + pi.getUUID() + "', `name` = '" + pi.getName() + "' WHERE `id` = '" + pi.getId() + "'";
        int ret = DataLogic.getSQL().exec(query);
        if (ret != 1) {
            LogUtil.severe("Error in updating player " + pi + "; retval = " + ret);
            return false;
        }
        return true;
    }

    private static PlayerInfo fetchPlayerSync(OfflinePlayer p){

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

        boolean advanced;
        if (result[3] instanceof Integer)
            advanced = ((int)result[3]) != 0;
        else
            advanced = (Boolean)result[3];
        PlayerInfo pi = new PlayerInfo((Integer)result[0], UUID.fromString((String)result[2]), (String)result[1], advanced);
        cachedPlayers.put(pi.getUUID(), pi);
        cachedPlayerId.put(pi.getId(), pi);
        cachedPlayerName.put(pi.getName(), pi);
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

        boolean advanced;
        if (result[3] instanceof Integer)
            advanced = ((int)result[3]) != 0;
        else
            advanced = (Boolean)result[3];
        PlayerInfo pi = new PlayerInfo((Integer)result[0], UUID.fromString((String)result[2]), (String)result[1], advanced);
        cachedPlayers.put(pi.getUUID(), pi);
        cachedPlayerId.put(pi.getId(), pi);
        cachedPlayerName.put(pi.getName(), pi);
        LogUtil.log(Level.INFO, "Load " + pi);

        return pi;
    }

    public static PlayerInfo fetchPlayerSync(UUID player){

        String query = "SELECT * FROM `" + DataLogic.getPrefix() + "player` WHERE uuid = '" + player.toString() + "'";
        Object[] result = DataLogic.getSQL().queryFirst(query, 4);
        if (result == null){
            return null;
        }

        boolean advanced;
        if (result[3] instanceof Integer)
            advanced = ((int)result[3]) != 0;
        else
            advanced = (Boolean)result[3];
        PlayerInfo pi = new PlayerInfo((Integer)result[0], UUID.fromString((String)result[2]), (String)result[1], advanced);
        cachedPlayers.put(pi.getUUID(), pi);
        cachedPlayerId.put(pi.getId(), pi);
        cachedPlayerName.put(pi.getName(), pi);
        LogUtil.log(Level.INFO, "Load " + pi);

        return pi;
    }

    private static void fetchPlayer(OfflinePlayer p){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            fetchPlayerSync(p);
        });
    }

    public static void getAllPlayers(){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            String query = "SELECT * FROM `" + DataLogic.getPrefix() + "player`";
            List<Object[]> result = DataLogic.getSQL().query(query, 4);
            if (result == null || result.size() == 0){
                LogUtil.log(Level.INFO, "No player in database.");
                return;
            }
            
            for (Object[] o : result){
                boolean advanced;
                if (o[3] instanceof Integer)
                    advanced = ((int)o[3]) != 0;
                else
                    advanced = (Boolean)o[3];
                PlayerInfo pi = new PlayerInfo((Integer)o[0], UUID.fromString((String)o[2]), (String)o[1], advanced);
                cachedPlayers.put(pi.getUUID(), pi);
                cachedPlayerId.put(pi.getId(), pi);
                cachedPlayerName.put(pi.getName(), pi);
            }
    
            LogUtil.log(Level.INFO, "Load " + cachedPlayers.size() + " players.");
    
        });
    }
    
    public static List<String> tabCompleter(String startWith) {
        // show offline player names when:
        //   1. startWith.length() >= 2
        //   2. no match in online players
        if (startWith == null || startWith.length() == 0) {
            return Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).collect(Collectors.toList());
        }
        if (startWith.length() >= 2) {
            return tabCompleterOfflinePlayer(startWith.toLowerCase());
        } else {
            List<String> ret = Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).filter(name -> name.toLowerCase().startsWith(startWith.toLowerCase())).collect(Collectors.toList());
            if (ret.size() == 0) {
                return tabCompleterOfflinePlayer(startWith.toLowerCase());
            } else {
                return ret;
            }
        }
    }

    public static List<String> tabCompleterOfflinePlayer(String startWith) {
        ArrayList<String> ret = new ArrayList<String>();
        for (Entry<String, PlayerInfo> entry : cachedPlayerName.entrySet()) {
            if (entry.getKey().startsWith(startWith)) {
                ret.add(entry.getValue().getName());
            }
        }
        return ret;
    }

}