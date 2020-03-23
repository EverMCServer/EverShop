package com.evermc.evershop.logic;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import com.evermc.evershop.EverShop;
import com.evermc.evershop.database.MySQLDataSource;
import com.evermc.evershop.database.SQLDataSource;
import com.evermc.evershop.util.LogUtil;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class DataLogic{
    
    private EverShop plugin;
    private SQLDataSource SQL;
    private Map <UUID, Integer> worldList;

    public DataLogic(EverShop plugin){

        this.plugin = plugin;
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
              "id int(10) UNSIGNED NOT NULL AUTO_INCREMENT," +
              "epoch int(10) UNSIGNED NOT NULL," +
              "action_id int(10) UNSIGNED NOT NULL," +
              "player_id int(10) UNSIGNED NOT NULL," +
              "world_id int(10) UNSIGNED NOT NULL," +
              "x int(11) NOT NULL," +
              "y int(11) NOT NULL," +
              "z int(11) NOT NULL," +
              "price int(11) NOT NULL," +
              "targets blob NOT NULL," +
              "items blob NOT NULL," +
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

    public int getWorldId(World w){
        UUID uid = w.getUID();
        if (worldList.containsKey(uid)){
            return worldList.get(uid);
        } else {
            LogUtil.log(Level.SEVERE, "Reload worlds.");
            initWorld();
            return worldList.get(uid);
        }
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

    public void saveShop(final ShopInfo shop, final Runnable afterSave, final Runnable failSave){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            String query = "REPLACE INTO `" + SQL.getPrefix() + "shop` VALUES (null, '" + shop.epoch + "', '"
             + shop.action_id + "', '" + shop.player_id + "', '" + shop.world_id + "', '" + shop.x + "', '"
             + shop.y + "', '" + shop.z + "', '" + shop.price + "', ?, ?)";
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
            Bukkit.getScheduler().runTask(plugin, afterSave);
        });
    }

    public static byte[] toBlob(Serializable object){
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