// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

import de.simplicit.vjdbc.command.*;
import de.simplicit.vjdbc.serial.SerializableTransport;
import de.simplicit.vjdbc.serial.StreamingResultSet;
import de.simplicit.vjdbc.serial.UIDEx;
import de.simplicit.vjdbc.util.SQLExceptionHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VirtualStatement继承了VirtualBase基类并引入了Statement的静态sql运行支持
 */
public class VirtualStatement extends VirtualBase implements Statement {
    protected Connection connection;
    protected List batchCollector = new ArrayList();
    protected int maxRows = -1;
    protected int queryTimeout = -1;
    protected StreamingResultSet currentResultSet;
    protected int resultSetType;
    protected boolean isClosed = false;
    protected boolean isCloseOnCompletion = false;

    public VirtualStatement(UIDEx reg, Connection connection, DecoratedCommandSink theSink, int resultSetType) {
        super(reg, theSink);
        // Remember the connection
        // 记住连接
        this.connection = connection;
        // Remember ResultSetType
        // 记住ResultSetType
        this.resultSetType = resultSetType;
        // Get the optional additional information which was delivered from the server
        // upon creation of the Statement object.
        // 获取创建Statement对象时从服务器提供的可选附加信息。
        if (reg.getValue1() != Integer.MIN_VALUE) {
            this.queryTimeout = reg.getValue1();
        }
        if (reg.getValue2() != Integer.MIN_VALUE) {
            this.maxRows = reg.getValue2();
        }
        // We no longer need the additional values for information, so reset
        // them so they are no longer serialized
        // 我们不再需要其他值来获取信息，因此请重置它们，以便不再进行序列化
        reg.resetValues();
    }

    protected void finalize() throws Throwable {
        if (!isClosed) {
            close();
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            SerializableTransport st = (SerializableTransport) sink.process(objectUid, new StatementQueryCommand(sql,
                    resultSetType), true);
            StreamingResultSet srs = (StreamingResultSet) st.getTransportee();
            srs.setStatement(this);
            srs.setCommandSink(sink);
            currentResultSet = srs;
            return srs;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public int executeUpdate(String sql) throws SQLException {
        return sink.processWithIntResult(objectUid, new StatementUpdateCommand(sql));
    }

    public void close() throws SQLException {
        sink.process(objectUid, new DestroyCommand(objectUid, JdbcInterfaceType.STATEMENT));
        isClosed = true;
    }

    public int getMaxFieldSize() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getMaxFieldSize"));
    }

    public void setMaxFieldSize(int max) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setMaxFieldSize",
                new Object[] {max}, ParameterTypeCombinations.INT));
    }

    public int getMaxRows() throws SQLException {
        if (maxRows < 0) {
            maxRows = sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                    "getMaxRows"));
        }

        return maxRows;
    }

    public void setMaxRows(int max) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setMaxRows",
                new Object[] {max}, ParameterTypeCombinations.INT));
        maxRows = max;
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setEscapeProcessing",
                new Object[] { enable ? Boolean.TRUE : Boolean.FALSE }, ParameterTypeCombinations.BOL));
    }

    public int getQueryTimeout() throws SQLException {
        if (queryTimeout < 0) {
            queryTimeout = sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                    "getQueryTimeout"));
        }

        return queryTimeout;
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setQueryTimeout",
                new Object[] {seconds}, ParameterTypeCombinations.INT));
        queryTimeout = seconds;
    }

    public void cancel() throws SQLException {
        sink.process(objectUid, new StatementCancelCommand());
    }

    public SQLWarning getWarnings() throws SQLException {
        return (SQLWarning) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "getWarnings"));
    }

    public void clearWarnings() throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "clearWarnings"));
    }

    public void setCursorName(String name) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setCursorName",
                new Object[] { name }, ParameterTypeCombinations.STR));
    }

    public boolean execute(String sql) throws SQLException {
        // Reset the current ResultSet before executing this command
        // 执行此命令之前，请重置当前的ResultSet
        currentResultSet = null;

        return sink.processWithBooleanResult(objectUid, new StatementExecuteCommand(sql));
    }

    public ResultSet getResultSet() throws SQLException {
        if (currentResultSet == null) {
            try {
                SerializableTransport st = (SerializableTransport) sink.process(objectUid,
                        new StatementGetResultSetCommand(), true);
                currentResultSet = (StreamingResultSet) st.getTransportee();
                currentResultSet.setStatement(this);
                currentResultSet.setCommandSink(sink);
            } catch (Exception e) {
                throw SQLExceptionHelper.wrap(e);
            }
        }

        return currentResultSet;
    }

    public int getUpdateCount() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool
                .getReflectiveCommand(JdbcInterfaceType.STATEMENT, "getUpdateCount"));
    }

    public boolean getMoreResults() throws SQLException {
        try {
            return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                    "getMoreResults"));
        } finally {
            currentResultSet = null;
        }
    }

    public void setFetchDirection(int direction) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setFetchDirection",
                new Object[] {direction}, ParameterTypeCombinations.INT));
    }

    public int getFetchDirection() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getFetchDirection"));
    }

    public void setFetchSize(int rows) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setFetchSize",
                new Object[] {rows}, ParameterTypeCombinations.INT));
    }

    public int getFetchSize() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "getFetchSize"));
    }

    public int getResultSetConcurrency() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getResultSetConcurrency"));
    }

    public int getResultSetType() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getResultSetType"));
    }

    public void addBatch(String sql) throws SQLException {
        batchCollector.add(sql);
    }

    public void clearBatch() {
        batchCollector.clear();
    }

    public int[] executeBatch() throws SQLException {
        String[] sql = (String[]) batchCollector.toArray(new String[batchCollector.size()]);
        int[] result = (int[]) sink.process(objectUid, new StatementExecuteBatchCommand(sql));
        batchCollector.clear();
        return result;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean getMoreResults(int current) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getMoreResults", new Object[] {current}, ParameterTypeCombinations.INT));
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        try {
            SerializableTransport st = (SerializableTransport) sink.process(objectUid,
                    new StatementGetGeneratedKeysCommand(), true);
            StreamingResultSet srs = (StreamingResultSet) st.getTransportee();
            srs.setStatement(this);
            srs.setCommandSink(sink);
            return srs;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return sink.processWithIntResult(objectUid, new StatementUpdateExtendedCommand(sql, autoGeneratedKeys));
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return sink.processWithIntResult(objectUid, new StatementUpdateExtendedCommand(sql, columnIndexes));
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return sink.processWithIntResult(objectUid, new StatementUpdateExtendedCommand(sql, columnNames));
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return sink.processWithBooleanResult(objectUid, new StatementExecuteExtendedCommand(sql, autoGeneratedKeys));
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return sink.processWithBooleanResult(objectUid, new StatementExecuteExtendedCommand(sql, columnIndexes));
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return sink.processWithBooleanResult(objectUid, new StatementExecuteExtendedCommand(sql, columnNames));
    }

    public int getResultSetHoldability() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "getResultSetHoldability"));
    }

    /* start JDBC4 support */
    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    public void setPoolable(boolean poolable) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT, "setPoolable",
                new Object[]{poolable ? Boolean.TRUE : Boolean.FALSE},
                ParameterTypeCombinations.BOL));
    }

    public boolean isPoolable() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.STATEMENT,
                "isPoolable"));
    }

    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(VirtualStatement.class);
    }

    public <T> T unwrap(Class<T> iface) {
        return (T)this;
    }
    /* end JDBC4 support */

    /* start JDK7 support */
    public void closeOnCompletion() {
        isCloseOnCompletion = true;
    }

    public boolean isCloseOnCompletion() {
        return isCloseOnCompletion;
    }
    /* end JDK7 support */
}
