package com.evermc.evershop.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import com.evermc.evershop.util.LogUtil;

public abstract class SQLDataSource{
    
    protected ConfigurationSection config;
    protected String prefix;
    protected HikariDataSource ds;

    abstract SQLDataSource createDataSource();

    public Connection getConnection() {
        try {
            if (ds != null)
                return ds.getConnection();
        } catch (SQLException e) {
            LogUtil.log(Level.INFO, "Could not retreive a connection");
            return null;
        }
        return null;
    }

    public String getPrefix(){
        return this.prefix;
    }

    public void exec(String[] query){
        Connection conn = null;
        Statement st = null;
        try {
            conn = getConnection();
            if (conn == null){
                LogUtil.log(Level.WARNING, "Failed to getConnection");
                return;
            }
            st = conn.createStatement();

            for (String str : query){
                st.executeUpdate(str);
            }

        } catch (final SQLException e) {
            LogUtil.log(Level.INFO, "Database connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (st != null)
                try {
                    st.close();
                } catch (final SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (final SQLException ignored) {
            }
        }
    }

    public void exec(String query){
        Connection conn = null;
        Statement st = null;
        try {
            conn = getConnection();
            if (conn == null){
                LogUtil.log(Level.WARNING, "Failed to getConnection");
                return;
            }
            st = conn.createStatement();

            st.executeUpdate(query);

        } catch (final SQLException e) {
            LogUtil.log(Level.INFO, "Database connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (st != null)
                try {
                    st.close();
                } catch (final SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (final SQLException ignored) {
            }
        }
    }

    public List<Object[]> query (String query, int col){
        List<Object[]> ret = new ArrayList<Object[]>();
        
        Connection conn = null;
        PreparedStatement s = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            if (conn == null){
                LogUtil.log(Level.WARNING, "Failed to getConnection");
                return null;
            }

            s = conn.prepareStatement(query);
            rs = s.executeQuery();

            while (rs.next()) {
                
                Object[] cur = new Object[col];

                for (int i = 0; i < col; i ++){
                    cur[i] = rs.getObject(i + 1);
                }

                ret.add(cur);
            }

        } catch (final SQLException e) {
            LogUtil.log(Level.INFO, "Database connection error: " + e.getMessage());
            e.printStackTrace();

        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (final SQLException ignored) {
                }
            if (s != null)
                try {
                    s.close();
                } catch (final SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (final SQLException ignored) {
            }
        }
        return ret;
    }

    public Object[] queryFirst (String query, int col){
        Object[] ret = new Object[col];
        
        Connection conn = null;
        PreparedStatement s = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            if (conn == null){
                LogUtil.log(Level.WARNING, "Failed to getConnection");
                return null;
            }

            s = conn.prepareStatement(query);
            rs = s.executeQuery();

            if (rs.next()) {
                for (int i = 0; i < col; i ++){
                    ret[i] = rs.getObject(i + 1);
                }
            }else{
                return null;
            }

        } catch (final SQLException e) {
            LogUtil.log(Level.INFO, "Database connection error: " + e.getMessage());
            e.printStackTrace();

        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (final SQLException ignored) {
                }
            if (s != null)
                try {
                    s.close();
                } catch (final SQLException ignored) {
                }
            if (conn != null)
                try {
                    conn.close();
                } catch (final SQLException ignored) {
            }
        }
        return ret;
    }
}