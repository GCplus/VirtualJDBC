// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.*;

public class TableCache extends TimerTask {
    private static final Log _logger = LogFactory.getLog(TableCache.class);

    private static final Map<Integer, String> _sqlTypeMappingForHSql = new HashMap<Integer, String>();

    private final Connection _vjdbcConnection;
    private final Connection _hsqlConnection;
    private final DatabaseMetaData _dbMetaData;
    private final Statement _vjdbcStatement;
    private final Statement _hsqlStatement;
    private final Map<String, CacheEntry> _tableEntries = new HashMap<String, CacheEntry>();
    private final Timer _cacheTimer = new Timer(true);
    private final SimpleStatementParser _statementParser = new SimpleStatementParser();

    // Mappings for generation of the HSQL-Create-Table-Statements, some SQL
    // types won't be cached
    static {
        _sqlTypeMappingForHSql.put(Types.BIGINT, "BIGINT");
        _sqlTypeMappingForHSql.put(Types.BIT, "BIT");
        _sqlTypeMappingForHSql.put(Types.CHAR, "CHAR");
        _sqlTypeMappingForHSql.put(Types.DATE, "DATE");
        _sqlTypeMappingForHSql.put(Types.DECIMAL, "DECIMAL");
        _sqlTypeMappingForHSql.put(Types.DOUBLE, "DOUBLE");
        _sqlTypeMappingForHSql.put(Types.FLOAT, "FLOAT");
        _sqlTypeMappingForHSql.put(Types.INTEGER, "INTEGER");
        _sqlTypeMappingForHSql.put(Types.NUMERIC, "NUMERIC");
        _sqlTypeMappingForHSql.put(Types.SMALLINT, "SMALLINT");
        _sqlTypeMappingForHSql.put(Types.TIMESTAMP, "TIMESTAMP");
        _sqlTypeMappingForHSql.put(Types.TINYINT, "TINYINT");
        _sqlTypeMappingForHSql.put(Types.VARCHAR, "VARCHAR");
    }

    // Internal management structure for the SQL-Statements of a table
    private static class CacheEntry {
        boolean _isFilled = false;
        long _lastTimeRefreshed = System.currentTimeMillis();
        String _name;
        int _refreshInterval;
        String _create;
        String _insert;
        String _select;
        String _delete;
        String _drop;

        CacheEntry(String name, int refreshInterval, String create, String insert, String select) {
            _name = name;
            _refreshInterval = refreshInterval;
            _create = create;
            _insert = insert;
            _delete = "DELETE FROM " + name;
            _select = select;
            _drop = "DROP " + name;
        }
    }

    public TableCache(Connection conn, String cachedTables) throws SQLException {
        _vjdbcConnection = conn;
        _dbMetaData = _vjdbcConnection.getMetaData();
        // Get a connection to a In-Memory-Database
        _hsqlConnection = DriverManager.getConnection("jdbc:hsqldb:.", "sa", "");
        // Statement for gathering of cached data
        _vjdbcStatement = _vjdbcConnection.createStatement();
        // Statement for selecting the existing cache
        _hsqlStatement = _hsqlConnection.createStatement();
        // Set up a timer to schedule cache refreshing at a fixed rate
        _cacheTimer.scheduleAtFixedRate(this, 10000, 10000);
        // Parse the table string
        _logger.info("Caching of following tables:");
        StringTokenizer tok = new StringTokenizer(cachedTables, ",");
        while(tok.hasMoreTokens()) {
            createCacheEntry(tok.nextToken());
        }
    }

    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        // Get the tables of the SQL-Statement
        Set<String> tables = _statementParser.getTablesOfSelectStatement(sql);
        // Caching is only possible when the returned list has tables
        boolean cachingPossible = tables.size() > 0;

        if(cachingPossible) {
            // Check if all tables can be cached
            if(_tableEntries.keySet().containsAll(tables)) {
                // Now iterate through all table names and check if they are allowed to
                // be cached. Caching of a statement is not possible if there is one
                // table which isn't in the list of cached tables.
                for (String tableName : tables) {
                    CacheEntry ce = _tableEntries.get(tableName);

                    if (ce != null) {
                        if (!ce._isFilled) {
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
        	_logger.debug("Returning prepared statement from HSQL for query " +sql);
            return _hsqlConnection.prepareStatement(sql);
        } else {
            return null;
        }
    }

    private void refreshCacheEntry(CacheEntry cacheEntry) throws SQLException {
        // Now read the complete table via the VJDBC-Connection
        PreparedStatement hsqlPreparedStatement = null;
        ResultSet vjdbcResultSet = null;

        try {
            // Prepare the INSERT-Statement
            hsqlPreparedStatement = _hsqlConnection.prepareStatement(cacheEntry._insert);
            // Now get the Table content
            vjdbcResultSet = _vjdbcStatement.executeQuery(cacheEntry._select);
            // Read the meta data, this might throw an exception so previously
            // cached data won't be destroyed
            ResultSetMetaData rsMetaData = vjdbcResultSet.getMetaData();
            // Here we delete all rows in the cache
            _hsqlStatement.executeUpdate(cacheEntry._delete);
            // And fill the HSQL-Destination with it
            while(vjdbcResultSet.next()) {
                for(int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                    hsqlPreparedStatement.setObject(i, vjdbcResultSet.getObject(i));
                }
                hsqlPreparedStatement.execute();
            }

            // Commit the whole changes
            _hsqlConnection.commit();
            // Reset the refresh timer
            cacheEntry._lastTimeRefreshed = System.currentTimeMillis();
            cacheEntry._isFilled = true;
        } catch(SQLException e) {
            // Remove the entry when an exception occurs
            _logger.warn("Error while refreshing table " + cacheEntry._name + ", dropping it");
            _hsqlStatement.executeUpdate(cacheEntry._drop);
            cacheEntry._isFilled = false;
            throw e;
        } finally {
            if(vjdbcResultSet != null) {
                try {
                    vjdbcResultSet.close();
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
            _logger.info("... " + table + " with refreshing interval " + refreshInterval);
        } else {
            table = tableConfig;
            refreshInterval = 0;
            _logger.info("... " + table + ", no refreshing");
        }

        // Get the column metadata of the correspondig table
        ResultSet rs = _dbMetaData.getColumns(null, null, table.toUpperCase(), "%");
        // Create different StringBuffers for the future SQL-Statements
        StringBuilder sbCreate = new StringBuilder("CREATE TABLE " + table + " (");
        StringBuilder sbInsert = new StringBuilder("INSERT INTO " + table + " (");
        StringBuilder sbInsert2 = new StringBuilder(" VALUES (");
        StringBuilder sbSelect = new StringBuilder("SELECT ");

        // Analyze all columns
        while(rs.next()) {
            String columnName = rs.getString("COLUMN_NAME");
            int origDataType = rs.getInt("DATA_TYPE");
            String dataType = _sqlTypeMappingForHSql.get(origDataType);

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
        _hsqlStatement.executeQuery(create);
        // If we got here the creation was successful and the new cache entry can be created
        _tableEntries.put(table.toLowerCase(), new CacheEntry(table, refreshInterval, create, insert, select));
    }

    public void run() {
        // Iterate through all table entries
        for (CacheEntry tableEntry : _tableEntries.values()) {
            // Refreshing necessary ?
            if (tableEntry._refreshInterval > 0) {
                // Now measure if the cache should be refreshed
                if ((System.currentTimeMillis() - tableEntry._lastTimeRefreshed) > tableEntry._refreshInterval) {
                    try {
                        _logger.debug("Refreshing cache for table " + tableEntry._name);
                        refreshCacheEntry(tableEntry);
                        _logger.debug("... successfully refreshed");
                    } catch (SQLException e) {
                        _logger.warn("... failed", e);
                    }
                }
            }
        }
    }
}
