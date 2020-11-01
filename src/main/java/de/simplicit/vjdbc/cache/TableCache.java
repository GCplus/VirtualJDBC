// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.*;

public class TableCache extends TimerTask {
    private static final Log logger = LogFactory.getLog(TableCache.class);

    private static final Map<Integer, String> sqlTypeMappingForHSql = new HashMap<Integer, String>();

    private final Connection vJdbcConnection;
    private final Connection hsqlConnection;
    private final DatabaseMetaData dbMetaData;
    private final Statement vJdbcStatement;
    private final Statement hsqlStatement;
    private final Map<String, CacheEntry> tableEntries = new HashMap<String, CacheEntry>();
    private final Timer cacheTimer = new Timer(true);
    private final SimpleStatementParser statementParser = new SimpleStatementParser();

    // Mappings for generation of the HSQL-Create-Table-Statements, some SQL
    // types won't be cached
    // 用于生成HSQL-Create-Table-Statement的映射，某些SQL类型不会被缓存
    /**
     * 这里static对应的是sql里面create语句的字段类型
     */
    static {
        sqlTypeMappingForHSql.put(Types.BIGINT, "BIGINT");
        sqlTypeMappingForHSql.put(Types.BIT, "BIT");
        sqlTypeMappingForHSql.put(Types.CHAR, "CHAR");
        sqlTypeMappingForHSql.put(Types.DATE, "DATE");
        sqlTypeMappingForHSql.put(Types.DECIMAL, "DECIMAL");
        sqlTypeMappingForHSql.put(Types.DOUBLE, "DOUBLE");
        sqlTypeMappingForHSql.put(Types.FLOAT, "FLOAT");
        sqlTypeMappingForHSql.put(Types.INTEGER, "INTEGER");
        sqlTypeMappingForHSql.put(Types.NUMERIC, "NUMERIC");
        sqlTypeMappingForHSql.put(Types.SMALLINT, "SMALLINT");
        sqlTypeMappingForHSql.put(Types.TIMESTAMP, "TIMESTAMP");
        sqlTypeMappingForHSql.put(Types.TINYINT, "TINYINT");
        sqlTypeMappingForHSql.put(Types.VARCHAR, "VARCHAR");
    }

    // Internal management structure for the SQL-Statements of a table
    // 表的SQL语句的内部管理结构
    private static class CacheEntry {
        boolean isFilled = false;
        long lastTimeRefreshed = System.currentTimeMillis();
        String name;
        int refreshInterval;
        String create;
        String insert;
        String select;
        String delete;
        String drop;

        CacheEntry(String name, int refreshInterval, String create, String insert, String select) {
            this.name = name;
            this.refreshInterval = refreshInterval;
            this.create = create;
            this.insert = insert;
            this.delete = "DELETE FROM " + name;
            this.select = select;
            this.drop = "DROP " + name;
        }
    }

    /**
     *
     * @param conn
     * @param cachedTables
     * @throws SQLException
     */
    public TableCache(Connection conn, String cachedTables) throws SQLException {
        this.vJdbcConnection = conn;
        this.dbMetaData = vJdbcConnection.getMetaData();
        // Get a connection to a In-Memory-Database
        // 获取到内存中数据库的连接
        this.hsqlConnection = DriverManager.getConnection("jdbc:hsqldb:.", "sa", "");
        // Statement for gathering of cached data
        // 用于收集缓存数据的声明
        this.vJdbcStatement = this.vJdbcConnection.createStatement();
        // Statement for selecting the existing cache
        // 选择现有缓存的声明
        this.hsqlStatement = this.hsqlConnection.createStatement();
        // Set up a timer to schedule cache refreshing at a fixed rate
        // 设置计时器以固定速率进行缓存刷新
        this.cacheTimer.scheduleAtFixedRate(this, 10000, 10000);
        // Parse the table string
        // 解析数据表的字符串
        logger.info("Caching of following tables:");
        StringTokenizer tok = new StringTokenizer(cachedTables, ",");
        while(tok.hasMoreTokens()) {
            createCacheEntry(tok.nextToken());
        }
    }

    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        // Get the tables of the SQL-Statement
        // 获取SQL语句中的表
        Set<String> tables = statementParser.getTablesOfSelectStatement(sql);
        // Caching is only possible when the returned list has tables
        // 仅当返回的列表包含表时才可以进行缓存
        boolean cachingPossible = tables.size() > 0;

        if(cachingPossible) {
            // Check if all tables can be cached
            if(tableEntries.keySet().containsAll(tables)) {
                // Now iterate through all table names and check if they are allowed to
                // be cached. Caching of a statement is not possible if there is one
                // table which isn't in the list of cached tables.
                for (String tableName : tables) {
                    CacheEntry ce = tableEntries.get(tableName);

                    if (ce != null) {
                        if (!ce.isFilled) {
                            try {
                                refreshCacheEntry(ce);
                            } catch (SQLException e) {
                                cachingPossible = false;
                            }
                        }
                    } else {
                        cachingPossible = false;
                    }
                }
            } else {
                cachingPossible = false;
            }
        }

        if(cachingPossible) {
        	logger.debug("Returning prepared statement from HSQL for query " +sql);
            return hsqlConnection.prepareStatement(sql);
        } else {
            return null;
        }
    }

    private void refreshCacheEntry(CacheEntry cacheEntry) throws SQLException {
        // Now read the complete table via the VJDBC-Connection
        PreparedStatement hsqlPreparedStatement = null;
        ResultSet vJdbcResultSet = null;

        try {
            // Prepare the INSERT-Statement
            hsqlPreparedStatement = hsqlConnection.prepareStatement(cacheEntry.insert);
            // Now get the Table content
            vJdbcResultSet = vJdbcStatement.executeQuery(cacheEntry.select);
            // Read the meta data, this might throw an exception so previously
            // cached data won't be destroyed
            ResultSetMetaData rsMetaData = vJdbcResultSet.getMetaData();
            // Here we delete all rows in the cache
            hsqlStatement.executeUpdate(cacheEntry.delete);
            // And fill the HSQL-Destination with it
            while(vJdbcResultSet.next()) {
                for(int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                    hsqlPreparedStatement.setObject(i, vJdbcResultSet.getObject(i));
                }
                hsqlPreparedStatement.execute();
            }

            // Commit the whole changes
            hsqlConnection.commit();
            // Reset the refresh timer
            cacheEntry.lastTimeRefreshed = System.currentTimeMillis();
            cacheEntry.isFilled = true;
        } catch(SQLException e) {
            // Remove the entry when an exception occurs
            logger.warn("Error while refreshing table " + cacheEntry.name + ", dropping it");
            hsqlStatement.executeUpdate(cacheEntry.drop);
            cacheEntry.isFilled = false;
            throw e;
        } finally {
            if(vJdbcResultSet != null) {
                try {
                    vJdbcResultSet.close();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
            if(hsqlPreparedStatement != null) {
                try {
                    hsqlPreparedStatement.close();
                } catch(SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createCacheEntry(String tableConfig) throws SQLException {
        int colonPos = tableConfig.indexOf(':');

        String table;
        int refreshInterval;
        if(colonPos > 0) {
            table = tableConfig.substring(0, colonPos);
            refreshInterval = Integer.parseInt(tableConfig.substring(colonPos + 1));
            logger.info("... " + table + " with refreshing interval " + refreshInterval);
        } else {
            table = tableConfig;
            refreshInterval = 0;
            logger.info("... " + table + ", no refreshing");
        }

        // Get the column metadata of the correspondig table
        ResultSet rs = dbMetaData.getColumns(null, null, table.toUpperCase(), "%");
        // Create different StringBuffers for the future SQL-Statements
        StringBuilder sbCreate = new StringBuilder("CREATE TABLE " + table + " (");
        StringBuilder sbInsert = new StringBuilder("INSERT INTO " + table + " (");
        StringBuilder sbInsert2 = new StringBuilder(" VALUES (");
        StringBuilder sbSelect = new StringBuilder("SELECT ");

        // Analyze all columns
        while(rs.next()) {
            String columnName = rs.getString("COLUMN_NAME");
            int origDataType = rs.getInt("DATA_TYPE");
            String dataType = sqlTypeMappingForHSql.get(origDataType);

            // There might be an unknown data type
            if(dataType != null) {
                int columnSize = rs.getInt("COLUMN_SIZE");
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");

                sbCreate.append(columnName).append(" ").append(dataType).append("(").append(columnSize).append(",").append(decimalDigits).append("), ");
                sbInsert.append(columnName).append(", ");
                sbInsert2.append("?, ");
                sbSelect.append("t.").append(columnName).append(", ");
            } else {
                throw new SQLException("Data-Type " + origDataType + " of column " + columnName + " of table " + table + " is not supported !");
            }
        }

        // Adjust and terminate all the StringBuffers
        sbCreate.setLength(sbCreate.length() - 2);
        sbCreate.append(")");

        sbInsert.setLength(sbInsert.length() - 2);
        sbInsert.append(")");
        sbInsert2.setLength(sbInsert2.length() - 2);
        sbInsert2.append(")");
        sbInsert.append(sbInsert2);

        sbSelect.setLength(sbSelect.length() - 2);
        sbSelect.append(" FROM ").append(table).append(" t");

        // Now get all the SQL-Strings
        String create = sbCreate.toString();
        String insert = sbInsert.toString();
        String select = sbSelect.toString();
        // Execute the creation query
        hsqlStatement.executeQuery(create);
        // If we got here the creation was successful and the new cache entry can be created
        tableEntries.put(table.toLowerCase(), new CacheEntry(table, refreshInterval, create, insert, select));
    }

    public void run() {
        // Iterate through all table entries
        for (CacheEntry tableEntry : tableEntries.values()) {
            // Refreshing necessary ?
            if (tableEntry.refreshInterval > 0) {
                // Now measure if the cache should be refreshed
                if ((System.currentTimeMillis() - tableEntry.lastTimeRefreshed) > tableEntry.refreshInterval) {
                    try {
                        logger.debug("Refreshing cache for table " + tableEntry.name);
                        refreshCacheEntry(tableEntry);
                        logger.debug("... successfully refreshed");
                    } catch (SQLException e) {
                        logger.warn("... failed", e);
                    }
                }
            }
        }
    }
}
