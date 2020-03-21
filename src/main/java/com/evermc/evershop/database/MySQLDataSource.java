package com.evermc.evershop.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.configuration.ConfigurationSection;

public class MySQLDataSource extends SQLDataSource {

    private HikariConfig hConfig;

    public MySQLDataSource(ConfigurationSection config){
        this.config = config;
        this.prefix = config.getString("prefix");
        createDataSource();
    }

    public MySQLDataSource createDataSource() {
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
        this.ds = new HikariDataSource(hConfig);
        return this;
    }
}