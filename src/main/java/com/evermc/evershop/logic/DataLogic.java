package com.evermc.evershop.logic;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.database.MySQLDataSource;
import com.evermc.evershop.database.SQLDataSource;
import com.evermc.evershop.util.LogUtil;
import com.evermc.evershop.util.SerializableLocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class DataLogic{
    
    private SQLDataSource SQL;
    private static EverShop plugin;
    private static Map <UUID, Integer> worldList;

    static{
        worldList = null;
        plugin = null;
    }

    public DataLogic(EverShop _plugin){

        plugin = _plugin;
        String sqltype = plugin.getConfig().getString("evershop.database.datasource");
        if ("mysql".equals(sqltype)){
            SQL = new MySQLDataSource(plugin.getConfig().getConfigurationSection("evershop.database.mysql"));
            setupDB();
            LogUtil.log(Level.INFO, "Connected to MySQL dataSource");
        } else {
            LogUtil.log(Level.SEVERE, "Unrecognized data source");
        }

        initWorld();
    }

    public SQLDataSource getSQL(){
        return this.SQL;
    }
    
    public void setupDB(){

        String[] query = {

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "shop` (" +
              "id int(10) NOT NULL AUTO_INCREMENT," +
              "epoch int(10) NOT NULL," +
              "action_id int(10) NOT NULL," +
              "player_id int(10) NOT NULL," +
              "world_id int(10) NOT NULL," +
              "x int(11) NOT NULL," +
              "y int(11) NOT NULL," +
              "z int(11) NOT NULL," +
              "price int(11) NOT NULL," +
              "targets blob NOT NULL," +
              "items blob NOT NULL," +
              "perm varchar(1024) NOT NULL," +
              "PRIMARY KEY (id)," +
              "UNIQUE KEY world_id (world_id,x,y,z)," +
              "KEY player_id (player_id)" +
            ") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "player` (" +
              "id int(11) NOT NULL AUTO_INCREMENT," +
              "name varchar(16) NOT NULL," +
              "uuid varchar(36) NOT NULL," +
              "advanced tinyint(1) NOT NULL DEFAULT '0'," +
              "PRIMARY KEY (id)," +
              "KEY name (name)," +
              "UNIQUE KEY uuid (uuid)" +
            ") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "world` (" +
              "id int(11) NOT NULL AUTO_INCREMENT," +
              "uuid varchar(36) NOT NULL," +
              "PRIMARY KEY (id)," +
              "UNIQUE KEY uuid (uuid)" +
            ") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "target` (" +
              "id int(10) UNSIGNED NOT NULL AUTO_INCREMENT," +
              "world_id int(10) UNSIGNED NOT NULL," +
              "x int(11) NOT NULL," +
              "y int(11) NOT NULL," +
              "z int(11) NOT NULL," +
              "shops varchar(1024) NOT NULL," +
              "PRIMARY KEY (id)," +
              "UNIQUE KEY world_id (world_id,x,y,z)" +
            ") ENGINE=MyISAM DEFAULT CHARSET=utf8;",

            "CREATE TABLE IF NOT EXISTS `" + SQL.getPrefix() + "transaction` (" +
              "id int(10) UNSIGNED NOT NULL AUTO_INCREMENT," +
              "shop_id int(10) UNSIGNED NOT NULL," +
              "player_id int(10) UNSIGNED NOT NULL," +
              "time int(10) UNSIGNED NOT NULL," +
              "count int(10) UNSIGNED NOT NULL," +
              "PRIMARY KEY (id)," +
              "UNIQUE KEY `uni` (shop_id,player_id,time)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
        };
        
        SQL.exec(query);
    }

    public static int getWorldId(World w){
        UUID uid = w.getUID();
        if (worldList.containsKey(uid)){
            return worldList.get(uid);
        } else {
            LogUtil.log(Level.SEVERE, "Reload worlds.");
            plugin.getDataLogic().initWorld();
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

    public void initWorld(){
        
        worldList = new HashMap<UUID, Integer>();

        String query = "INSERT IGNORE INTO `" + SQL.getPrefix() + "world` (uuid) VALUES ";
        
        if (Bukkit.getWorlds() == null){
            LogUtil.log(Level.SEVERE, "Cannot get worlds.");
            return;
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
            LogUtil.log(Level.SEVERE, "Error in initWorld.");
            return;
        }
        
        for (Object[] o : result){
            worldList.put(UUID.fromString((String)o[1]), (Integer)o[0]);
        }

        LogUtil.log(Level.INFO, "Load " + worldList.size() + " worlds.");

    }

    public void removeShop(final Location loc){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            // TODO - remove the corresponding record in chests? Since shopid is auto incremental, it will nor reuse id, then remove is not necessary. 
            String query = "DELETE FROM `" + SQL.getPrefix() + "shop` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
             + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "'";
            SQL.exec(query);
        });
    }

    public void saveShop(final ShopInfo shop, final Runnable afterSave, final Runnable failSave){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            String query = "REPLACE INTO `" + SQL.getPrefix() + "shop` VALUES (null, '" + shop.epoch + "', '"
             + shop.action_id + "', '" + shop.player_id + "', '" + shop.world_id + "', '" + shop.x + "', '"
             + shop.y + "', '" + shop.z + "', '" + shop.price + "', ?, ?, '')";
            byte[] targets = toBlob(shop.targets);
            byte[] items = toBlob(shop.items);
            if (targets.length >= 65535 || items.length >= 65535){
                Bukkit.getScheduler().runTask(plugin, failSave);
                return;
            }
            int ret = SQL.insertBlob(query, targets, items);
            if (ret <= 0){
                Bukkit.getScheduler().runTask(plugin, failSave);
                return;
            }
            for (SerializableLocation sloc : shop.targets){
                query = "INSERT INTO `" + SQL.getPrefix() + "target` VALUES (null, '" + sloc.world + "', '" 
                + sloc.x + "', '" + sloc.y + "', '" + sloc.z + "', '" + ret + "') ON DUPLICATE KEY UPDATE `shops` = CONCAT(`shops`, '," + ret + "')";
                // TODO - if insert failed, revert all of the changes?
                SQL.insert(query);
            }
            Bukkit.getScheduler().runTask(plugin, afterSave);
        });
    }

    public ShopInfo[] getBlockInfo(Location loc){
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

    public ShopInfo getShopInfo(Location loc){
        String query = "SELECT * FROM `" + SQL.getPrefix() + "shop` WHERE `world_id` = '" + DataLogic.getWorldId(loc.getWorld())
        + "' AND `x` = '" + loc.getBlockX() + "' AND `y` = '" + loc.getBlockY() + "' AND `z` = '" + loc.getBlockZ() + "'";
        List<Object[]> ret = SQL.query(query, 12);
        if (ret.size() == 0){
            return null;
        }
        Object[] k = ret.get(0);
        return new ShopInfo((int)k[0], (int)k[1], (int)k[2], (int)k[3], (int)k[4], (int)k[5]
        , (int)k[6], (int)k[7], (int)k[8], (byte[])k[9], (byte[])k[10], (String)k[11]);
    }

    public ShopInfo getShopInfo(int shopid){
        String query = "SELECT * FROM `" + SQL.getPrefix() + "shop` WHERE id = '" + shopid + "'";
        List<Object[]> ret = SQL.query(query, 12);
        if (ret.size() == 0){
            return null;
        }
        Object[] k = ret.get(0);
        return new ShopInfo((int)k[0], (int)k[1], (int)k[2], (int)k[3], (int)k[4], (int)k[5]
        , (int)k[6], (int)k[7], (int)k[8], (byte[])k[9], (byte[])k[10], (String)k[11]);
    }

    public static byte[] toBlob(Object object){
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BukkitObjectOutputStream outputStream = new BukkitObjectOutputStream(out);
            outputStream.writeObject(object);
            byte [] bytes = out.toByteArray();
            outputStream.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null ;
        }
    }
}