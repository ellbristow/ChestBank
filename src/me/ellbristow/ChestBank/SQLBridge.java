package me.ellbristow.ChestBank;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import org.bukkit.plugin.Plugin;

public class SQLBridge {

    private static Plugin plugin;
    private Connection conn;
    private File sqlFile;
    private Statement statement;
    private PreparedStatement pstatement;
    private boolean isPrepared = false;
    private HashMap<Integer, HashMap<String, Object>> rows = new HashMap<Integer, HashMap<String, Object>>();
    private int numRows = 0;
    
    public SQLBridge () {
        plugin = ChestBank.plugin;
        sqlFile = new File(plugin.getDataFolder() + File.separator + plugin.getName() + ".db");
    }
    
    public synchronized Connection getConnection() {
        if (conn == null) {
            return open();
        }
        return conn;
    }
    
    public synchronized Connection open() {    	
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + sqlFile.getAbsolutePath());
            return conn;
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
            plugin.getPluginLoader().disablePlugin(plugin);
            e.printStackTrace();
        }
        return null;
    }
    
    public synchronized void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                plugin.getLogger().severe(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public boolean checkTable(String tableName) {
        DatabaseMetaData dbm;

        try {
            dbm = getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            if (tables.next()) {
            	tables.close();
                return true;
            }
            else {
            	tables.close();
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean checkColumn(String tableName, String columnName) {
        DatabaseMetaData dbm;

        try {
            dbm = getConnection().getMetaData();
            ResultSet tables = dbm.getColumns(null, null, tableName, columnName);
            if (tables.next()) {
            	tables.close();
                return true;
            }
            else {
            	tables.close();
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean createTable(String tableName, String[] columns, String[] dims) {
        isPrepared = false;
        try {
            statement = getConnection().createStatement();
            String query = "CREATE TABLE " + tableName + "(";
            for (int i = 0; i < columns.length; i++) {
                if (i!=0) {
                    query += ",";
                }
                query += columns[i] + " " + dims[i];
            }
            query += ")";
            statement.execute(query);
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
        } 
        return true;
    }
    
    public void delete(String query) {
        isPrepared = false;
        try {
            statement = getConnection().createStatement();
            statement.executeUpdate(query);
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void insert(String afterInsert, String tableName, String[] fields, List<String[]> valueSet) {
        try {
            isPrepared = true;
            String insert = "INSERT" + afterInsert + " INTO " + tableName + " (";
            
            String fieldList = "";
            for (String field : fields) {
                if (!fieldList.equals(""))
                    fieldList += ", ";
                fieldList += field;
            }
            insert += fieldList + ")";
            
            String valueText = "";
            int c = 0;
            for (String[] values: valueSet) {
                if (!valueText.equals(""))
                    valueText += " UNION SELECT ";
                else 
                    valueText += " SELECT ";
                String valueList = "";
                for (String value: values) {
                    if (!valueList.equals("")) 
                        valueList += ", ";
                    valueList += "?";
                }
                valueText += valueList;
                c++;
            }
            insert += valueText;
            
            pstatement = getConnection().prepareStatement(insert);

            c = 0;
            for (String[] values : valueSet) {
                for (int j = 0; j < values.length; j++) {
                    pstatement.setString(c+1, values[j]);
                    c++;
                }
            }
            
            pstatement.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void insert(String afterInsert, String tableName, String[] fields, String[] values) {
        try {
            isPrepared = true;
            String insert = "INSERT" + afterInsert + " INTO " + tableName + "(";
            
            String fieldList = "";
            for (String field : fields) {
                if (!fieldList.equals(""))
                    fieldList += ", ";
                fieldList += field;
            }
            insert += fieldList + ") VALUES (";
            
            String valueList = "";
            for (String value: values) {
                if (!valueList.equals("")) 
                    valueList += ", ";
                valueList += "?";
            }
            insert += valueList + ")";
            
            pstatement = getConnection().prepareStatement(insert);
            
            for (int i = 0; i < values.length; i++) {
                pstatement.setString(i+1, values[i]);
            }
            
            pstatement.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void insert(String query) {
        isPrepared = false;
        try {
            statement = getConnection().createStatement();
            statement.executeUpdate(query);
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public ResultSet query(String query) {
        isPrepared = false;
        ResultSet results = null;

        try {
            statement = getConnection().createStatement();
            results = statement.executeQuery(query);
            return results;
        } catch (Exception e) {
            if (!e.getMessage().contains("not return ResultSet") || (e.getMessage().contains("not return ResultSet") && query.startsWith("SELECT"))) {
                plugin.getLogger().severe(e.getMessage());
                e.printStackTrace();
            }
            if (results != null) {
                try {
                    results.close();
                    statement.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (results != null) {
            try {
                results.close();
                statement.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    public HashMap<Integer, HashMap<String, Object>> select(String fields, String tableName, String where, String group, String order) {
        if ("".equals(fields) || fields == null) {
            fields = "*";
        }
        String query = "SELECT " + fields + " FROM " + tableName;
        if (!"".equals(where) && where != null) {
            query += " WHERE " + where;
        }
        if (!"".equals(group) && group != null) {
            query += " GROUP BY " + group;
        }
        if (!"".equals(order) && order != null) {
            query += " ORDER BY " + order;
        }

        ResultSet results = null;
        try {
            rows.clear();
            numRows = 0;
            results = query(query);
                        
            if (results != null) {
                int columns = results.getMetaData().getColumnCount();
                String columnNames = "";
                for (int i = 1; i <= columns; i++) {
                    if (!"".equals(columnNames)) {
                        columnNames += ",";
                    }
                    columnNames += results.getMetaData().getColumnName(i);
                }
                String[] columnArray = columnNames.split(",");
                numRows = 0;
                while (results.next()) {
                    HashMap<String, Object> thisColumn = new HashMap<String, Object>();
                    for (String columnName : columnArray) {
                        thisColumn.put(columnName, results.getObject(columnName));
                    }
                    rows.put(numRows, thisColumn);
                    numRows++;
                }
                results.close();
                statement.close();
                return rows;
            } else {
                return null;
            }
        } catch (Exception e) { 
            plugin.getLogger().severe(e.getMessage());
            e.printStackTrace();
            if (results != null) {
                try {
                    results.close();
                    statement.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }
}
