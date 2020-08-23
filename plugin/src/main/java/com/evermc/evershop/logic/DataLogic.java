package com.evermc.evershop.logic;

import java.util.ArrayList;
import java.util.Arrays;
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
              "rev int(10) NOT NULL," +
              "PRIMARY KEY (id)," +
              "UNIQUE KEY world_id (world_id,x,y,z,rev)," +
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
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "slot_transaction` (" +
              "id int(10) NOT NULL AUTO_INCREMENT," +
              "shop_id int(10) NOT NULL," +
              "player_id int(10) NOT NULL," +
              "time int(10) NOT NULL," +
              "count int(10) NOT NULL," +
              "itemkey varchar(64) NOT NULL," +
              "amount int(10) NOT NULL," +
              "PRIMARY KEY (id)" +
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
              "rev INTEGER NOT NULL," +
              "UNIQUE (world_id,x,y,z,rev)" +
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
            ");",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "slot_transaction` (" +
              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
              "shop_id INTEGER NOT NULL," +
              "player_id INTEGER NOT NULL," +
              "time INTEGER NOT NULL," +
              "count INTEGER NOT NULL," +
              "itemkey varchar(64) NOT NULL," +
              "amount INTEGER NOT NULL" +
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
        String query = "SELECT id,world_id,x,y,z FROM `" + SQL.getPrefix() + "shop` WHERE rev = '0'";

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
            if (loc == null) {
                // TODO - load shops when load world
                warn("Shop #" + shopid + " is at a not loaded world.");
                continue;
            }
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
            String query = "SELECT * FROM `" + SQL.getPrefix() + "shop` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
            + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "' AND `rev` = '0'";
            Object[] ret = SQL.queryFirst(query, 13);
            if (ret == null) {
                return;
            }
            if (SQL instanceof MySQLDataSource) {
                query = "UPDATE `" + SQL.getPrefix() + "shop` SET `rev` = `rev` + 1 WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
                + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "' ORDER BY rev DESC";
                SQL.exec(query);
            } else {
                query = "UPDATE `" + SQL.getPrefix() + "shop` SET `rev` = - (`rev` + 1) WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
                + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "' AND `rev` >= 0";
                SQL.exec(query);
                query = "UPDATE `" + SQL.getPrefix() + "shop` SET `rev` = - `rev` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
                + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "' AND `rev` < 0";
                SQL.exec(query);
            }
        });
    }

    public static void saveShop(final ShopInfo shop, final Consumer<Integer> afterSave, final Runnable failSave){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            String query = "REPLACE INTO `" + SQL.getPrefix() + "shop` VALUES (" + (shop.getId() == 0?"null":("'"+shop.getId()+"'")) 
                + ", '" + shop.getEpoch() + "', '" + shop.getAction() + "', '" + shop.getOwnerId() + "', '" + shop.getWorldID() 
                + "', '" + shop.getX() + "', '" + shop.getY() + "', '" + shop.getZ() + "', '" + shop.getPrice()+ "', ?, ?, '" 
                + shop.getExtra()+ "', '0')";
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
            if (shop.getId() == 0) {
                for (SerializableLocation sloc : shop.getTargetAll()){
                    query = "INSERT INTO `" + SQL.getPrefix() + "target` VALUES (null, '" + sloc.world + "', '" 
                    + sloc.x + "', '" + sloc.y + "', '" + sloc.z + "', '" + ret + "') " + SQL.ON_DUPLICATE("world_id,x,y,z") + "`shops` = " + SQL.CONCAT("`shops`", "'," + ret + "'");
                    SQL.insert(query);
                }
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                afterSave.accept(ret);
            });
        });
    }

    public static boolean changeOwner(int shopid, int newowner) {
        String query = "UPDATE `" + SQL.getPrefix() + "shop` SET `player_id` = '" + newowner + "' WHERE `id` = '" + shopid + "'";
        int ret = DataLogic.getSQL().exec(query);
        if (ret != 1) {
            severe("Error in updating shop " + shopid + "; retval = " + ret);
            return false;
        }
        return true;
    }

    public static ShopInfo[] getBlockInfo(Location loc){
        String query = "SELECT shops FROM `" + SQL.getPrefix() + "target` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
        + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "'";
        List<Object[]> ret = SQL.query(query, 1);
        if (ret.size() == 0){
            return null;
        }
        String shopstr = (String)ret.get(0)[0];
        if (shopstr == null || shopstr.length() == 0){
            return null;
        }
        Set<ShopInfo> retval = new HashSet<ShopInfo>();
        List<String> shops = Arrays.asList(shopstr.split(","));
        Set<String> notremove = new HashSet<String>();
        for (String str : shops){
            int shop = 0;
            try {
                shop = Integer.parseInt(str);
            } catch(Exception e){}
            if (shop == 0) {
                continue;
            }
            ShopInfo t = getShopInfo(shop);
            if (t != null){
                retval.add(t);
                notremove.add(str);
            }
        }
        if (notremove.size() > 0) {
            String new_shopstr = String.join(",", notremove);
            query = "UPDATE `" + SQL.getPrefix() + "target` SET `shops` = '" + new_shopstr + "' WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
            + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "'";
            SQL.exec(query);
        } else {
            query = "DELETE FROM `" + SQL.getPrefix() + "target` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
            + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "'";
            SQL.exec(query);
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
        if (shopstr == null || shopstr.length() == 0){
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
        + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "' AND rev = '0'";
        List<Object[]> ret = SQL.query(query, 13);
        if (ret.size() == 0){
            return null;
        }
        Object[] k = ret.get(0);
        return ShopInfo.decode(k);
    }

    public static ShopInfo getShopInfo(int shopid){
        String query = "SELECT * FROM `" + SQL.getPrefix() + "shop` WHERE id = '" + shopid + "' AND rev = '0'";
        Object[] ret = SQL.queryFirst(query, 13);
        if (ret == null){
            return null;
        }
        return ShopInfo.decode(ret);
    }

    public static int getShopOwner(Location loc){
        String query = "SELECT player_id FROM `" + SQL.getPrefix() + "shop` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
        + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "' AND rev = '0'";
        Object[] ret = SQL.queryFirst(query, 1);
        if (ret == null){
            return 0;
        }
        int r = SQL.getInt(ret[0]);
        if (r == 0)severe("getShopOwner(" + loc + ")");
        return r;
    }

    public static int getShopListLength(Player player){
        PlayerInfo pi = PlayerLogic.getPlayerInfo(player);
        return getShopListLength(pi);
    }

    public static int getShopListLength(PlayerInfo pi){
        String query = "SELECT count(*) FROM `" + SQL.getPrefix() + "shop` WHERE player_id = '" + pi.getId() + "'";
        Object[] ret = SQL.queryFirst(query, 1);
        return SQL.getInt(ret[0]);
    }

    // only basic infomation will be returned!
    public static ShopInfo[] getShopList(int player_id, int page){
        String query = "SELECT id,0,action_id,0,world_id,x,y,z,0,null,null,null,rev FROM `" + SQL.getPrefix() + 
                "shop` WHERE player_id = '" + player_id + "' ORDER BY `epoch` DESC LIMIT 10 OFFSET " + page*10;

        List<Object[]> ret = SQL.query(query, 13);
        if (ret.size() == 0){
            return null;
        }
        ArrayList<ShopInfo> result = new ArrayList<ShopInfo>();
        for (Object[] k : ret){
            result.add(ShopInfo.decode(k));
        }
        return result.toArray(new ShopInfo[result.size()]);
    }
    public static ShopInfo[] getShopList(int player_id){
        String query = "SELECT id,0,action_id,0,world_id,x,y,z,0,null,null,null,rev FROM `" + SQL.getPrefix() + 
                "shop` WHERE player_id = '" + player_id + "' ORDER BY `epoch` DESC";

        List<Object[]> ret = SQL.query(query, 13);
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

    public static void recordTransaction(int shopid, int playerid, String key, int amount){
        int time = (int)(System.currentTimeMillis()/1000/60); //create 1 record every minute
        String query = "INSERT INTO `" + SQL.getPrefix() + "slot_transaction` VALUES (null, '" + shopid + "', '" 
        + playerid + "', '" + time + "', '1', '" + key + "', '" + amount + "') ";
        SQL.insert(query);
    }

    public static class TransactionLog {
        public int player_id;
        public int time;
        public int count;
        public String itemkey;
        public int amount;
    }
    public static TransactionLog[] getTransaction(int shopid, int page, boolean isSlotShop){
        if (isSlotShop) {
            return getSlotTransaction(shopid, page - 1);
        } else {
            return getNormalTransaction(shopid, page - 1);
        }
    }
    
    public static TransactionLog[] getNormalTransaction(int shopid, int page){
        String query = "SELECT player_id, time, count FROM `" + SQL.getPrefix() + 
                "transaction` WHERE shop_id = '" + shopid + "' ORDER BY `time` DESC LIMIT 10 OFFSET " + page*10;

        List<Object[]> ret = SQL.query(query, 3);
        if (ret.size() == 0){
            return null;
        }
        TransactionLog[] retval = new TransactionLog[ret.size()];
        for (int i = 0; i < ret.size(); i ++){
            Object[] k = ret.get(i);
            TransactionLog t = new TransactionLog();
            t.player_id = SQL.getInt(k[0]);
            t.time = SQL.getInt(k[1]);
            t.count = SQL.getInt(k[2]);
            retval[i] = t;
        }
        return retval;
    }
    public static TransactionLog[] getSlotTransaction(int shopid, int page){
        String query = "SELECT player_id, time, count, itemkey, amount FROM `" + SQL.getPrefix() + 
                "slot_transaction` WHERE shop_id = '" + shopid + "' ORDER BY `time` DESC LIMIT 10 OFFSET " + page*10;

        List<Object[]> ret = SQL.query(query, 5);
        if (ret.size() == 0){
            return null;
        }
        TransactionLog[] retval = new TransactionLog[ret.size()];
        for (int i = 0; i < ret.size(); i ++){
            Object[] k = ret.get(i);
            TransactionLog t = new TransactionLog();
            t.player_id = SQL.getInt(k[0]);
            t.time = SQL.getInt(k[1]);
            t.count = SQL.getInt(k[2]);
            t.itemkey = (String)k[3];
            t.amount = SQL.getInt(k[4]);
            retval[i] = t;
        }
        return retval;
    }
    public static int getTransactionCount(int shopid, boolean isSlotShop){
        String query;
        if (isSlotShop) {
            query = "SELECT count(*) FROM `" + SQL.getPrefix() + "slot_transaction` WHERE shop_id = '" + shopid + "'";
        } else {
            query = "SELECT count(*) FROM `" + SQL.getPrefix() + "transaction` WHERE shop_id = '" + shopid + "'";
        }
        Object[] ret = SQL.queryFirst(query, 1);
        if (ret == null) {
            return 0;
        }
        return SQL.getInt(ret[0]);
    }
}