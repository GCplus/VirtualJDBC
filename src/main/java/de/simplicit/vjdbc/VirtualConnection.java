// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

import de.simplicit.vjdbc.cache.TableCache;
import de.simplicit.vjdbc.command.*;
import de.simplicit.vjdbc.serial.*;
import de.simplicit.vjdbc.util.ClientInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.Map;
import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;

public class VirtualConnection extends VirtualBase implements Connection {
    private static final Log logger = LogFactory.getLog(VirtualConnection.class);

    private static TableCache s_tableCache;
    private boolean cachingEnabled = false;
    private Boolean isAutoCommit = null;
    private final Properties connectionProperties;
    protected DatabaseMetaData databaseMetaData;
    protected boolean isClosed = false;

    protected ProxyFactory proxyFactory = null;

    public VirtualConnection(UIDEx reg, DecoratedCommandSink sink, Properties props, boolean cachingEnabled) {
        super(reg, sink);
        this.connectionProperties = props;
        this.cachingEnabled = cachingEnabled;
    }

    public void setProxyFactory(ProxyFactory factory) {
        proxyFactory = factory;
    }

    @Override
    protected void finalize() throws Throwable {
        if(!isClosed) {
            close();
        }
    }

    public Statement createStatement() throws SQLException {
        Object result =sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "createStatement"), true);
        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualStatement(reg, this, sink, ResultSet.TYPE_FORWARD_ONLY);
        }
        return (Statement)proxyFactory.makeJdbcObject(result);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement pstmt = null;

        if(cachingEnabled) {
            if(s_tableCache == null) {
                String cachedTables = connectionProperties.getProperty(VJdbcProperties.CACHE_TABLES);

                if(cachedTables != null) {
                    try {
                        s_tableCache = new TableCache(this, cachedTables);
                    } catch(SQLException e) {
                        logger.error("Creation of table cache failed, disable caching", e);
                        cachingEnabled = false;
                    }
                }
            }

            if(s_tableCache != null) {
                pstmt = s_tableCache.getPreparedStatement(sql);
            }
        }

        if(pstmt == null) {
            Object result = sink.process(objectUid, new ConnectionPrepareStatementCommand(sql), true);

            if (result instanceof UIDEx) {
                UIDEx reg = (UIDEx)result;
                pstmt = new VirtualPreparedStatement(reg, this, sql, sink, ResultSet.TYPE_FORWARD_ONLY);
            } else {
                pstmt = (PreparedStatement)proxyFactory.makeJdbcObject(result);
            }
        }

        return pstmt;
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        Object result = sink.process(objectUid, new ConnectionPrepareCallCommand(sql), true);
        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualCallableStatement(reg, this, sql, sink, ResultSet.TYPE_FORWARD_ONLY);
        }
        return (CallableStatement)proxyFactory.makeJdbcObject(result);
    }

    public String nativeSQL(String sql) throws SQLException {
        return (String)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "nativeSQL",
                new Object[]{sql},
                ParameterTypeCombinations.STR));
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setAutoCommit",
                new Object[]{autoCommit ? Boolean.TRUE : Boolean.FALSE},
                ParameterTypeCombinations.BOL));
        // Remember the auto-commit value to prevent unnecessary remote calls
        isAutoCommit = autoCommit;
    }

    public boolean getAutoCommit() throws SQLException {
        if(isAutoCommit == null) {
            boolean autoCommit = sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getAutoCommit"));
            isAutoCommit = autoCommit;
        }
        return isAutoCommit;
    }

    public void commit() throws SQLException {
        sink.processWithBooleanResult(objectUid, new ConnectionCommitCommand());
    }

    public void rollback() throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "rollback"));
    }

    public void close() throws SQLException {
        if(databaseMetaData != null && databaseMetaData instanceof VirtualDatabaseMetaData) {
            UIDEx metadataId = ((VirtualDatabaseMetaData)databaseMetaData).objectUid;
            sink.process(metadataId, new DestroyCommand(metadataId, JdbcInterfaceType.DATABASEMETADATA));
            databaseMetaData = null;
        }
        sink.process(objectUid, new DestroyCommand(objectUid, JdbcInterfaceType.CONNECTION));
        sink.close();
        isClosed = true;
    }

    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        if(databaseMetaData == null) {
            Object result = sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getMetaData"), true);
            if (result instanceof UIDEx) {
                UIDEx reg = (UIDEx)result;
                databaseMetaData = new VirtualDatabaseMetaData(this, reg, sink);
            } else {
                databaseMetaData = (DatabaseMetaData)proxyFactory.makeJdbcObject(result);
            }
        }
        return databaseMetaData;
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setReadOnly",
                new Object[]{readOnly ? Boolean.TRUE : Boolean.FALSE},
                ParameterTypeCombinations.BOL));
    }

    public boolean isReadOnly() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "isReadOnly"));
    }

    public void setCatalog(String catalog) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setCatalog",
                new Object[]{catalog},
                ParameterTypeCombinations.STR));
    }

    public String getCatalog() throws SQLException {
        return (String)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getCatalog"));
    }

    public void setTransactionIsolation(int level) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setTransactionIsolation",
                new Object[]{level},
                ParameterTypeCombinations.INT));
    }

    public int getTransactionIsolation() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getTransactionIsolation"));
    }

    public SQLWarning getWarnings() throws SQLException {
        /*
        if(sink.lastProcessedCommandKindOf(ConnectionCommitCommand.class) && _lastCommitWithoutWarning) {
            _anyWarnings = false;
            return null;
        } else {
            SQLWarning warnings = (SQLWarning)sink.process(objectUid, CommandPool.getReflectiveCommand("getWarnings"));
            // Remember if any warnings were reported
            _anyWarnings = warnings != null;
            return warnings;
        }
        */
        return null;
    }

    public void clearWarnings() throws SQLException {
        // Ignore the call if the previous getWarnings()-Call returned null
        /*
        if(_anyWarnings) {
            sink.process(objectUid, CommandPool.getReflectiveCommand("clearWarnings"));
        }
        */
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        Object result = sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "createStatement",
                new Object[]{resultSetType, resultSetConcurrency},
                ParameterTypeCombinations.INTINT), true);

        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualStatement(reg, this, sink, resultSetType);
        }
        return (Statement)proxyFactory.makeJdbcObject(result);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency)
            throws SQLException {
        Object result = sink.process(objectUid, new ConnectionPrepareStatementCommand(sql, resultSetType, resultSetConcurrency), true);

        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualPreparedStatement(reg, this, sql, sink, resultSetType);
        }
        return (PreparedStatement)proxyFactory.makeJdbcObject(result);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws SQLException {
        Object result = sink.process(objectUid, new ConnectionPrepareCallCommand(sql, resultSetType, resultSetConcurrency), true);

        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualCallableStatement(reg, this, sql, sink, resultSetType);
        }
        return (CallableStatement)proxyFactory.makeJdbcObject(result);
    }

    public Map getTypeMap() throws SQLException {
        return (Map)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getTypeMap"));
    }

    public void setTypeMap(Map map) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setTypeMap",
                new Object[]{map},
                ParameterTypeCombinations.MAP));
    }

    public void setHoldability(int holdability) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setHoldability",
                new Object[]{holdability},
                ParameterTypeCombinations.INT));
    }

    public int getHoldability() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getHoldability"));
    }

    public Savepoint setSavepoint() throws SQLException {
        UIDEx reg = (UIDEx)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setSavepoint"), true);
        return new VirtualSavepoint(reg, sink);
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        UIDEx reg = (UIDEx)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setSavepoint",
                new Object[]{name},
                ParameterTypeCombinations.STR), true);
        return new VirtualSavepoint(reg, sink);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        VirtualSavepoint vsp = (VirtualSavepoint)savepoint;
        sink.process(objectUid, new ConnectionRollbackWithSavepointCommand(vsp.getObjectUID().getUID()));
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        VirtualSavepoint vsp = (VirtualSavepoint)savepoint;
        sink.process(objectUid, new ConnectionReleaseSavepointCommand(vsp.getObjectUID().getUID()));
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException {
        Object result = sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "createStatement",
                new Object[]{resultSetType,
                        resultSetConcurrency,
                        resultSetHoldability},
                ParameterTypeCombinations.INTINTINT), true);
        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualStatement(reg, this, sink, resultSetType);
        }
        return (Statement)proxyFactory.makeJdbcObject(result);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        Object result = sink.process(objectUid, new ConnectionPrepareStatementCommand(sql, resultSetType, resultSetConcurrency, resultSetHoldability), true);

        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualPreparedStatement(reg, this, sql, sink, resultSetType);
        }
        return (PreparedStatement)proxyFactory.makeJdbcObject(result);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        Object result = sink.process(objectUid, new ConnectionPrepareCallCommand(sql, resultSetType, resultSetConcurrency, resultSetHoldability), true);

        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualCallableStatement(reg, this, sql, sink, resultSetType);
        }
        return (CallableStatement)proxyFactory.makeJdbcObject(result);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        Object result = sink.process(objectUid, new ConnectionPrepareStatementExtendedCommand(sql, autoGeneratedKeys), true);

        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualPreparedStatement(reg, this, sql, sink, ResultSet.TYPE_FORWARD_ONLY);
        }
        return (PreparedStatement)proxyFactory.makeJdbcObject(result);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        Object result = sink.process(objectUid, new ConnectionPrepareStatementExtendedCommand(sql, columnIndexes), true);

        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualPreparedStatement(reg, this, sql, sink, ResultSet.TYPE_FORWARD_ONLY);
        }
        return (PreparedStatement)proxyFactory.makeJdbcObject(result);
    }

    public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException {
        Object result = sink.process(objectUid, new ConnectionPrepareStatementExtendedCommand(sql, columnNames), true);

        if (result instanceof UIDEx) {
            UIDEx reg = (UIDEx)result;
            return new VirtualPreparedStatement(reg, this, sql, sink, ResultSet.TYPE_FORWARD_ONLY);
        }
        return (PreparedStatement)proxyFactory.makeJdbcObject(result);
    }

    /* start JDBC4 support */
    public Clob createClob() throws SQLException {
        return new SerialClob();
    }

    public Blob createBlob() throws SQLException {
        return new SerialBlob();
    }

    public NClob createNClob() throws SQLException {
        return new SerialNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return new SerialSQLXML();
    }

    class ValidRunnable implements Runnable {
        public volatile boolean finished = false;
        public void run() {
            try {
                Object[] args = new Object[1];
                args[0] = 0; // doesn't matter for this call
                sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "isValid", args, 2));
                finished = true;
            } catch (SQLException sqle) {
                logger.info(sqle.getMessage(), sqle);
                sqle.printStackTrace();
            }
        }
    }

    public boolean isValid(int timeout) throws SQLException {

        if (timeout <= 0) {
            throw new SQLException("invalid timeout value " + timeout);
        }

        // Schedule the keep alive timer task
        ValidRunnable task = new ValidRunnable();
        Thread t = new Thread(task);
        long end = System.currentTimeMillis() + timeout;
        long diff = timeout;
        t.start();

        while (!task.finished && diff > 0) {
            try {
                Thread.sleep(diff);
            } catch (Exception e) {
                e.printStackTrace();
            }
            diff = end - System.currentTimeMillis();
        }

        return !task.finished;
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        Properties clientProps = ClientInfo.getProperties(null);
        clientProps.put(name, value);
        try {
            sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setClientInfo",
                new Object[]{ name, value },
                ParameterTypeCombinations.STRSTR), true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        Properties clientProps = ClientInfo.getProperties(null);
        for (Object o : properties.keySet()) {
            String key = String.valueOf(o);
            String value = properties.getProperty(key);
            setClientInfo(key, value);
        }
    }

    public String getClientInfo(String name) throws SQLException {

        Properties clientProps = ClientInfo.getProperties(null);
        String value = clientProps.getProperty(name);
        if (value != null) {
            return value;
        }
        String ret = (String)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getClientInfo",
                new Object[]{ name },
                ParameterTypeCombinations.STR), true);
        if (ret != null) {
            clientProps.setProperty(name, ret);
        }
        return ret;
    }

    public Properties getClientInfo() throws SQLException {
        Properties clientProps = ClientInfo.getProperties(null);
        if (clientProps != null && clientProps.size() > 1) {
            return clientProps;
        }
        /**
         * 当前将sink对象的process方法返回的object强制转换成properties对象
         */
        Properties ret = (Properties) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getClientInfo"));
        for (Object o : ret.keySet()) {
            String key = String.valueOf(o);
            String value = ret.getProperty(key);
            Objects.requireNonNull(clientProps).setProperty(key, value);
        }
        return ret;
    }


    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return new SerialArray(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return new SerialStruct(typeName, attributes);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(VirtualConnection.class);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T) this;
    }
    /* end JDBC4 support */

    /* start JDK7 support */
    public void setSchema(String schema) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setSchema", new Object[]{ schema },
            ParameterTypeCombinations.STR), true);
    }

    public String getSchema() throws SQLException {
        return (String)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getSchema",
                new Object[]{ }, 0), true);
    }

    public void abort(Executor executor) throws SQLException {
        Runnable r = new Runnable() {
                public void run() {
                    try {
                        close();
                    } catch (SQLException e) {
                        logger.info(e.getMessage(), e);
                        e.printStackTrace();
                    }
                }
            };
        executor.execute(r);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds)
        throws SQLException {
        // unsupported due to complexities of providing the executor to the
        // engine driver on the other side of the connection
        throw new UnsupportedOperationException("setNetworkTimeout");
    }

    public int getNetworkTimeout() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getNetworkTimeout"));
    }
    /* end JDK7 support */
}
