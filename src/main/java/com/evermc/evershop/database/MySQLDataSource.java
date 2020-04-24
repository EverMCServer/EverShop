package com.evermc.evershop.database;

import java.util.logging.Level;

import com.evermc.evershop.util.LogUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.configuration.ConfigurationSection;

public class MySQLDataSource extends SQLDataSource {

    private HikariConfig hConfig;

    public MySQLDataSource(ConfigurationSection config){
        this.config = config;
        this.prefix = config.getString("prefix");
        String jdbc = "jdbc:mysql://" + this.config.getString("hostname") + ":"
                    + this.config.getInt("port") + "/" + this.config.getString("db_name")
                    + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true";
        hConfig = new HikariConfig();
        hConfig.setJdbcUrl(jdbc);
        hConfig.setUsername(this.config.getString("username"));
        hConfig.setPassword(this.config.getString("password"));
        hConfig.addDataSourceProperty("cachePrepStmts", "true");
        hConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hConfig.addDataSourceProperty("useLocalSessionState", "true");
        hConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hConfig.addDataSourceProperty("maintainTimeStats", "true");
        try{
            this.ds = new HikariDataSource(hConfig);
        }catch(Exception e){
            LogUtil.log(Level.SEVERE, "Fail to connect database:");
            e.printStackTrace();
        }
    }

    public String INSERT_IGNORE(){
        return "INSERT IGNORE ";
    }

    public String ON_DUPLICATE(String col){
        return "ON DUPLICATE KEY UPDATE ";
    }

    public String CONCAT(String s1, String s2){
        return "CONCAT(" + s1 + ", " + s2 + ")";
    }
    
    public int getInt(Object k){
        return (int)(long)k;
    }
}