package com.evermc.evershop.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.database.LiteDataSource;
import com.evermc.evershop.database.MySQLDataSource;
import com.evermc.evershop.database.SQLDataSource;
import com.evermc.evershop.structure.PlayerInfo;
import com.evermc.evershop.structure.ShopInfo;
import com.evermc.evershop.util.NBTUtil;
import com.evermc.evershop.util.SerializableLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import static com.evermc.evershop.util.LogUtil.info;
import static com.evermc.evershop.util.LogUtil.warn;
import static com.evermc.evershop.util.LogUtil.severe;

public class DataLogic{
    
    private static SQLDataSource SQL = null;
    private static EverShop plugin = null;
    private static Map <UUID, Integer> worldList = null;

    public static boolean init(EverShop _plugin){

        plugin = _plugin;
        String sqltype = plugin.getConfig().getString("evershop.database.datasource");
        if ("mysql".equals(sqltype)){
            SQL = new MySQLDataSource(plugin.getConfig().getConfigurationSection("evershop.database.mysql"));
            if (SQL == null || !SQL.testConnection()) return false;
            setupDB_MySQL();
            info("Connected to MySQL dataSource");
        } else if ("sqlite".equals(sqltype)){
            SQL = new LiteDataSource(plugin.getConfig().getConfigurationSection("evershop.database.sqlite"));  
            if (SQL == null || !SQL.testConnection()) return false;
            setupDB_SQLite();
            info("Connected to SQLite dataSource");
        } else {
            severe("Unrecognized database type: " + sqltype + ", should be one of the following: mysql, sqlite");
            return false;
        }

        if (!initWorld()) return false;
        if (!initShops()) return false;
        return true;
    }

    public static SQLDataSource getSQL(){
        return SQL;
    }
    
    public static String getPrefix(){
        return SQL.getPrefix();
    }
    
    public static void setupDB_MySQL(){

        String[] query = {

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "shop` (" +
              "id int(10) NOT NULL AUTO_INCREMENT," +
              "epoch int(10) NOT NULL," +
              "action_id int(10) NOT NULL," +
              "player_id int(10) NOT NULL," +
              "world_id int(10) NOT NULL," +
              "x int(10) NOT NULL," +
              "y int(10) NOT NULL," +
              "z int(10) NOT NULL," +
              "price int(10) NOT NULL," +
              "targets blob NOT NULL," +
              "items blob NOT NULL," +
              "extra varchar(1024) NOT NULL," +
              "PRIMARY KEY (id)," +
              "UNIQUE KEY world_id (world_id,x,y,z)," +
              "KEY player_id (player_id)" +
            ") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "player` (" +
              "id int(10) NOT NULL AUTO_INCREMENT," +
              "name varchar(16) NOT NULL," +
              "uuid varchar(36) NOT NULL," +
              "advanced tinyint(1) NOT NULL DEFAULT '0'," +
              "PRIMARY KEY (id)," +
              "KEY name (name)," +
              "UNIQUE KEY uuid (uuid)" +
            ") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "world` (" +
              "id int(10) NOT NULL AUTO_INCREMENT," +
              "uuid varchar(36) NOT NULL," +
              "PRIMARY KEY (id)," +
              "UNIQUE KEY uuid (uuid)" +
            ") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "target` (" +
              "id int(10) NOT NULL AUTO_INCREMENT," +
              "world_id int(10) NOT NULL," +
              "x int(10) NOT NULL," +
              "y int(10) NOT NULL," +
              "z int(10) NOT NULL," +
              "shops varchar(1024) NOT NULL," +
              "PRIMARY KEY (id)," +
              "UNIQUE KEY world_id (world_id,x,y,z)" +
            ") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "transaction` (" +
              "id int(10) NOT NULL AUTO_INCREMENT," +
              "shop_id int(10) NOT NULL," +
              "player_id int(10) NOT NULL," +
              "time int(10) NOT NULL," +
              "count int(10) NOT NULL," +
              "PRIMARY KEY (id)," +
              "UNIQUE KEY `uni` (shop_id,player_id,time)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
        };
        
        SQL.exec(query);
    }

    public static void setupDB_SQLite(){

        String[] query = {

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "shop` (" +
              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
              "epoch INTEGER NOT NULL," +
              "action_id INTEGER NOT NULL," +
              "player_id INTEGER NOT NULL," +
              "world_id INTEGER NOT NULL," +
              "x INTEGER NOT NULL," +
              "y INTEGER NOT NULL," +
              "z INTEGER NOT NULL," +
              "price INTEGER NOT NULL," +
              "targets BLOB NOT NULL," +
              "items BLOB NOT NULL," +
              "extra varchar(1024) NOT NULL," +
              "UNIQUE (world_id,x,y,z)" +
            ");",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "player` (" +
              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
              "name varchar(16) NOT NULL," +
              "uuid varchar(36) NOT NULL," +
              "advanced tinyint(1) NOT NULL DEFAULT '0'," +
              "UNIQUE (uuid)" +
            ");",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "world` (" +
              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
              "uuid varchar(36) NOT NULL," +
              "UNIQUE (uuid)" +
            ");",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "target` (" +
              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
              "world_id INTEGER NOT NULL," +
              "x INTEGER NOT NULL," +
              "y INTEGER NOT NULL," +
              "z INTEGER NOT NULL," +
              "shops varchar(1024) NOT NULL," +
              "UNIQUE (world_id,x,y,z)" +
            ");",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "transaction` (" +
              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
              "shop_id INTEGER NOT NULL," +
              "player_id INTEGER NOT NULL," +
              "time INTEGER NOT NULL," +
              "count INTEGER NOT NULL," +
              "UNIQUE (shop_id,player_id,time)" +
            ");"
        };
        SQL.exec(query);
    }

    public static int getWorldId(World w){
        UUID uid = w.getUID();
        if (worldList.containsKey(uid)){
            return worldList.get(uid);
        } else {
            severe("Reload worlds.");
            DataLogic.initWorld();
            return worldList.get(uid);
        }
    }

    public static UUID getWorldUUID(int id){
        for (UUID uuid : worldList.keySet()){
            if (worldList.get(uuid) == id){
                return uuid;
            }
        }
        return null;
    }

    public static World getWorld(int id){
        UUID uid = getWorldUUID(id);
        if (uid == null) return null;
        return Bukkit.getWorld(uid);
    }

    private static boolean initWorld(){
        
        worldList = new HashMap<UUID, Integer>();

        String query = SQL.INSERT_IGNORE() + "INTO `" + SQL.getPrefix() + "world` (uuid) VALUES ";
        
        if (Bukkit.getWorlds() == null){
            severe("Cannot get worlds.");
            return false;
        }

        for (World w : Bukkit.getWorlds()){
            UUID uw = w.getUID();
            query += "('" + uw + "'),";
        }
        query = query.substring(0, query.length() - 1);
        query = query + ";";

        SQL.exec(query);

        query = "SELECT * FROM `" + SQL.getPrefix() + "world`";

        List<Object[]> result = SQL.query(query, 2);
        
        if (result == null){
            severe("Error in initWorld.");
            return false;
        }
        
        for (Object[] o : result){
            worldList.put(UUID.fromString((String)o[1]), (Integer)o[0]);
        }

        info("Load " + worldList.size() + " worlds.");

        return true;
    }

    private static boolean initShops() {
        String query = "SELECT id,world_id,x,y,z FROM `" + SQL.getPrefix() + "shop`";

        List<Object[]> ret = SQL.query(query, 5);
        if (ret == null) {
            return false;
        }
        if (ret.size() == 0){
            return true;
        }
        for (Object[] k : ret){
            int shopid = SQL.getInt(k[0]);
            Location loc = SerializableLocation.toLocation(SQL.getInt(k[1]), SQL.getInt(k[2]), SQL.getInt(k[3]), SQL.getInt(k[4]));
            if (ShopLogic.isShopSign(loc.getBlock(), false)) {
                Sign sign = (Sign)loc.getBlock().getState();
                sign.setMetadata("shopid", new FixedMetadataValue(EverShop.getInstance(), shopid));
            } else {
                removeShop(loc);
                warn("Shop at " + SerializableLocation.toString(loc) + " is unavailable, remove.");
            }
        }
        return true;
    }

    public static void removeShop(final Location loc){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            String query = "DELETE FROM `" + SQL.getPrefix() + "shop` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
             + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "'";
            SQL.exec(query);
        });
    }

    public static void removeShop(final int shopid){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            String query = "DELETE FROM `" + SQL.getPrefix() + "shop` WHERE `id` = '" + shopid + "'";
            SQL.exec(query);
        });
    }

    public static void saveShop(final ShopInfo shop, final Consumer<Integer> afterSave, final Runnable failSave){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            String query = "REPLACE INTO `" + SQL.getPrefix() + "shop` VALUES (null, '" + shop.getEpoch() + "', '"
             + shop.getAction() + "', '" + shop.getOwnerId() + "', '" + shop.getWorldID() + "', '" + shop.getX() + "', '"
             + shop.getY() + "', '" + shop.getZ() + "', '" + shop.getPrice()+ "', ?, ?, '" + shop.getExtra()+ "')";
            byte[] targets = SerializableLocation.serialize(shop.getTargetOut(), shop.getTargetIn());
            byte[] items = NBTUtil.serialize(shop.getItemOut(), shop.getItemIn());
            if (targets.length >= 65535 || items.length >= 65535){
                Bukkit.getScheduler().runTask(plugin, failSave);
                return;
            }
            final int ret = SQL.insertBlob(query, targets, items);
            if (ret <= 0){
                Bukkit.getScheduler().runTask(plugin, failSave);
                return;
            }
            for (SerializableLocation sloc : shop.getTargetAll()){
                query = "INSERT INTO `" + SQL.getPrefix() + "target` VALUES (null, '" + sloc.world + "', '" 
                + sloc.x + "', '" + sloc.y + "', '" + sloc.z + "', '" + ret + "') " + SQL.ON_DUPLICATE("world_id,x,y,z")+ "`shops` = " + SQL.CONCAT("`shops`", "'," + ret + "'");
                SQL.insert(query);
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                afterSave.accept(ret);
            });
        });
    }

    public static ShopInfo[] getBlockInfo(Location loc){
        String query = "SELECT shops FROM `" + SQL.getPrefix() + "target` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
        + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "'";
        List<Object[]> ret = SQL.query(query, 1);
        if (ret.size() == 0){
            return null;
        }
        String shopstr = (String)ret.get(0)[0];
        if (shopstr == null || shopstr == ""){
            return null;
        }
        Set<ShopInfo> retval = new HashSet<ShopInfo>();
        for (String str : shopstr.split(",")){
            int shop = Integer.parseInt(str);
            ShopInfo t = getShopInfo(shop);
            if (t != null){
                retval.add(t);
            }
        }
        if (retval.size() > 0){
            return retval.toArray(new ShopInfo[retval.size()]);
        } else {
            return null;
        }
    }

    public static int getBlockLinkedCount(Location loc){
        String query = "SELECT shops FROM `" + SQL.getPrefix() + "target` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
        + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "'";
        List<Object[]> ret = SQL.query(query, 1);
        if (ret.size() == 0){
            return 0;
        }
        String shopstr = (String)ret.get(0)[0];
        if (shopstr == null || shopstr == ""){
            return 0;
        }
        int retval = 0;
        for (String str : shopstr.split(",")){
            int shop = Integer.parseInt(str);
            ShopInfo t = getShopInfo(shop);
            if (t != null){
                retval ++;
            }
        }
        return retval;
    }

    public static ShopInfo getShopInfo(Location loc){
        String query = "SELECT * FROM `" + SQL.getPrefix() + "shop` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
        + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "'";
        List<Object[]> ret = SQL.query(query, 12);
        if (ret.size() == 0){
            return null;
        }
        Object[] k = ret.get(0);
        return ShopInfo.decode(k);
    }

    public static int getShopOwner(Location loc){
        String query = "SELECT player_id FROM `" + SQL.getPrefix() + "shop` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
        + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "'";
        Object[] ret = SQL.queryFirst(query, 1);
        if (ret == null){
            return 0;
        }
        int r = SQL.getInt(ret[0]);
        if (r == 0)severe("getShopOwner(" + loc + ")");
        return r;
    }

    public static ShopInfo[] getShopInfo(Location[] locs){
        if (locs == null || locs.length == 0) return null;
        String query = "SELECT * FROM `" + SQL.getPrefix() + "shop` WHERE ";
        for (Location loc:locs){
            query += "(`world_id` = '" + DataLogic.getWorldId(loc.getWorld()) + "' AND `x` = '" + 
                loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "') OR ";
        }
        query = query.substring(0, query.length() - 4);

        List<Object[]> ret = SQL.query(query, 12);
        if (ret.size() == 0){
            return null;
        }
        HashSet<ShopInfo> result = new HashSet<ShopInfo>();
        for (Object[] k : ret){
            result.add(ShopInfo.decode(k));
        }
        return result.toArray(new ShopInfo[result.size()]);
    }

    public static int[] getShopOwner(Location[] locs){
        if (locs == null || locs.length == 0) return null;
        String query = "SELECT player_id FROM `" + SQL.getPrefix() + "shop` WHERE ";
        for (Location loc:locs){
            query += "(`world_id` = '" + DataLogic.getWorldId(loc.getWorld()) + "' AND `x` = '" + 
                loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "') OR ";
        }
        query = query.substring(0, query.length() - 4);

        List<Object[]> ret = SQL.query(query, 1);
        if (ret.size() == 0){
            return null;
        }
        HashSet<Integer> result = new HashSet<Integer>();
        for (Object[] k : ret){
            if (k[0] instanceof Integer) result.add((int)k[0]);
            else if (k[0] instanceof Long) result.add((int)(long)k[0]);
            else severe("getShopOwner(): retval="+k[0]);
        }
        if (result.size() > 0){
            int[] retval = new int[result.size()];
            int i = 0;
            for (Integer in:result){
                retval[i] = in;
                i++;
            }
            return retval;
        }
        return null;
    }

    public static ShopInfo getShopInfo(int shopid){
        String query = "SELECT * FROM `" + SQL.getPrefix() + "shop` WHERE id = '" + shopid + "'";
        List<Object[]> ret = SQL.query(query, 12);
        if (ret.size() == 0){
            return null;
        }
        Object[] k = ret.get(0);
        return ShopInfo.decode(k);
    }

    public static int getShopListLength(Player player){
        PlayerInfo pi = PlayerLogic.getPlayerInfo(player);
        return getShopListLength(pi);
    }

    public static int getShopListLength(PlayerInfo pi){
        String query = "SELECT count(*) FROM `" + SQL.getPrefix() + "shop` WHERE player_id = '" + pi.getId() + "'";
        Object[] ret = SQL.queryFirst(query, 1);
        if (ret[0] instanceof Integer) return (int)ret[0];
        else if (ret[0] instanceof Long) return (int)(long)ret[0];
        severe("getShopListLength(" + pi.getName()+ "): retval="+ret[0]);
        return 0;
    }

    // only basic infomation will be returned!
    public static ShopInfo[] getShopList(int player_id, int page){
        String query = "SELECT id,0,action_id,0,world_id,x,y,z,0,null,null,null FROM `" + SQL.getPrefix() + 
                "shop` WHERE player_id = '" + player_id + "' ORDER BY `epoch` DESC LIMIT 10 OFFSET " + page*10;

        List<Object[]> ret = SQL.query(query, 12);
        if (ret.size() == 0){
            return null;
        }
        ArrayList<ShopInfo> result = new ArrayList<ShopInfo>();
        for (Object[] k : ret){
            result.add(ShopInfo.decode(k));
        }
        return result.toArray(new ShopInfo[result.size()]);
    }

    public static void recordTransaction(int shopid, int playerid){
        int time = (int)(System.currentTimeMillis()/1000/60); //create 1 record every minute
        String query = "INSERT INTO `" + SQL.getPrefix() + "transaction` VALUES (null, '" + shopid + "', '" 
        + playerid + "', '" + time + "', '1') " + SQL.ON_DUPLICATE("shop_id,player_id,time")+ " count = count + 1";
        SQL.insert(query);
    }

    public static int[][] getTransaction(int shopid){
        String query = "SELECT player_id, time, count FROM `" + SQL.getPrefix() + 
                "transaction` WHERE shop_id = '" + shopid + "' ORDER BY `time` DESC LIMIT 10";

        List<Object[]> ret = SQL.query(query, 3);
        if (ret.size() == 0){
            return null;
        }
        int [][] retval = new int[ret.size()][3];
        for (int i = 0; i < ret.size(); i ++){
            Object[] k = ret.get(i);
            retval[i][0] = SQL.getInt(k[0]);
            retval[i][1] = SQL.getInt(k[1]);
            retval[i][2] = SQL.getInt(k[2]);
        }
        return retval;
    }
}