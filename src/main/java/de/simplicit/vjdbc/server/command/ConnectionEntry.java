// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.command;

import de.simplicit.vjdbc.ProxiedObject;
import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.command.ConnectionContext;
import de.simplicit.vjdbc.command.DestroyCommand;
import de.simplicit.vjdbc.command.JdbcInterfaceType;
import de.simplicit.vjdbc.command.StatementCancelCommand;
import de.simplicit.vjdbc.command.ResultSetProducerCommand;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.SerialResultSetMetaData;
import de.simplicit.vjdbc.serial.SerializableTransport;
import de.simplicit.vjdbc.serial.StreamingResultSet;
import de.simplicit.vjdbc.serial.UIDEx;
import de.simplicit.vjdbc.server.config.ConnectionConfiguration;
import de.simplicit.vjdbc.server.config.VJdbcConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.*;

class ConnectionEntry implements ConnectionContext {
    private static final Log logger = LogFactory.getLog(ConnectionEntry.class);

    // Unique identifier for the ConnectionEntry
    // ConnectionEntry的唯一标识符
    private final Long uid;
    // The real JDBC-Connection
    // 真正的JDBC连接
    private final Connection connection;
    // Configuration information
    // 配置信息
    private final ConnectionConfiguration connectionConfiguration;
    // Properties delivered from the client
    // 客户端交付的属性
    private final Properties clientInfo;
    // Flag that signals the activity of this connection
    // 表示此连接活动的标志
    private boolean active = false;

    // Statistics
    // 统计
    private long lastAccessTimestamp = System.currentTimeMillis();
    private long numberOfProcessedCommands = 0;

    // Map containing all JDBC-Objects which are created by this Connection
    // entry
    // 包含此Connection条目创建的所有JDBC对象的映射
    private final Map<Long, JdbcObjectHolder> jdbcObjects =
        Collections.synchronizedMap(new HashMap<Long, JdbcObjectHolder>());
    // Map for counting commands
    // 映射计数命令
    private final Map<String, Integer> commandCountMap =
        Collections.synchronizedMap(new HashMap<String, Integer>());

    ConnectionEntry(Long connuid, Connection conn, ConnectionConfiguration config, Properties clientInfo, CallingContext ctx) {
        connection = conn;
        connectionConfiguration = config;
        this.clientInfo = clientInfo;
        uid = connuid;
        // Put the connection into the JDBC-Object map
        // 将连接放入JDBC-Object映射
        jdbcObjects.put(connuid, new JdbcObjectHolder(conn, ctx, JdbcInterfaceType.CONNECTION));
    }

    void close() {
        try {
            if(!connection.isClosed()) {
                connection.close();

                if(logger.isDebugEnabled()) {
                    logger.debug("Closed connection " + uid);
                }
            }

            traceConnectionStatistics();
        } catch (SQLException e) {
            logger.error("Exception during closing connection", e);
        }
    }

    public void closeAllRelatedJdbcObjects() throws SQLException {
        Set<Long> keys = null;
        synchronized (jdbcObjects) {
            keys = new HashSet<Long>(jdbcObjects.keySet());
        }
        if (!keys.isEmpty()) {
            for (Long key : keys) {
                JdbcObjectHolder jdbcObject = jdbcObjects.get(key);
                // don't act on the Connection itself - this will be done elsewhere
                // 不要对连接本身采取行动-这将在其他地方完成
                if (jdbcObject.getJdbcInterfaceType() == JdbcInterfaceType.CONNECTION)
                    continue;
                // create a DestroyCommand and act on it
                // 创建一个摧毁命令并执行它
                Command destroy = new DestroyCommand(key, jdbcObject.getJdbcInterfaceType());
                destroy.execute(jdbcObject.getJdbcObject(), this);
            }
        }
    }
    
    boolean hasJdbcObjects() {
        return !jdbcObjects.isEmpty();
    }

    public Properties getClientInfo() {
        return clientInfo;
    }

    public boolean isActive() {
        return active;
    }

    public long getLastAccess() {
        return lastAccessTimestamp;
    }

    public long getNumberOfProcessedCommands() {
        return numberOfProcessedCommands;
    }

    public Object getJDBCObject(Long key) {
        JdbcObjectHolder jdbcObjectHolder = jdbcObjects.get(key);
        if(jdbcObjectHolder != null) {
            return jdbcObjectHolder.getJdbcObject();
        } else {
            return null;
        }
    }

    public void addJDBCObject(Long key, Object partner) {
    	int jdbcInterfaceType = getJdbcInterfaceTypeFromObject(partner);
        jdbcObjects.put(key, new JdbcObjectHolder(partner, null, jdbcInterfaceType));
    }

    public Object removeJDBCObject(Long key) {
        JdbcObjectHolder jdbcObjectHolder = jdbcObjects.remove(key);
        if(jdbcObjectHolder != null) {
            return jdbcObjectHolder.getJdbcObject();
        } else {
            return null;
        }
    }

    public int getCompressionMode() {
        return connectionConfiguration.getCompressionModeAsInt();
    }

    public long getCompressionThreshold() {
        return connectionConfiguration.getCompressionThreshold();
    }

    public int getRowPacketSize() {
        return connectionConfiguration.getRowPacketSize();
    }

    public String getCharset() {
        return connectionConfiguration.getCharset();
    }

    public String resolveOrCheckQuery(String sql) throws SQLException
    {
        if (sql.startsWith("$")) {
            return getNamedQuery(sql.substring(1));
        }
        else {
            checkAgainstQueryFilters(sql);
            return sql;
        }
    }

    public synchronized Object executeCommand(Long uid, Command cmd, CallingContext ctx) throws SQLException {
        try {
            active = true;
            lastAccessTimestamp = System.currentTimeMillis();

            Object result = null;

            // Some target object ?
            // 某个目标对象?
            if(uid != null) {
                // ... get it
                // ... 获取它
                JdbcObjectHolder target = jdbcObjects.get(uid);

                if(target != null) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("Target for UID " + uid + " found");
                    }
                    // Execute the command on the target object
                    // 在目标对象上执行命令
                    result = cmd.execute(target.getJdbcObject(), this);
                    // Check if the result must be remembered on the server side with a UID
                    // 检查是否必须使用UID在服务器端记住结果
                    UIDEx uidResult = ReturnedObjectGuard.checkResult(result);

                    if(uidResult != null) {
                        // put it in the JDBC-Object-Table
                        // 把它放在JDBC-Object-Table中
                    	int jdbcInterfaceType = getJdbcInterfaceTypeFromObject(result);
                        jdbcObjects.put(uidResult.getUID(), new JdbcObjectHolder(result, ctx, jdbcInterfaceType));
                        if(logger.isDebugEnabled()) {
                            logger.debug("Registered " + result.getClass().getName() + " with UID " + uidResult);
                        }
                        if (result instanceof ProxiedObject) {
                            return ((ProxiedObject)result).getProxy();
                        }
                        return uidResult;
                    } else {
                        // When the result is of type ResultSet then handle it specially
                        // 当结果的类型为ResultSet时，请特别处理
                        if(result != null &&
                           VJdbcConfiguration.getUseCustomResultSetHandling()) {
                            if(result instanceof ResultSet) {
                                boolean forwardOnly = false;
                                if(cmd instanceof ResultSetProducerCommand) {
                                    forwardOnly = ((ResultSetProducerCommand) cmd).getResultSetType() == ResultSet.TYPE_FORWARD_ONLY;
                                } else {
                                    logger.debug("Command " + cmd.toString() + " doesn't implement "
                                            + "ResultSetProducer-Interface, assuming ResultSet is scroll insensitive");
                                }
                                result = handleResultSet((ResultSet) result, forwardOnly, ctx);
                            } else if(result instanceof ResultSetMetaData) {
                                result = handleResultSetMetaData((ResultSetMetaData) result);
                            } else {
                                if(logger.isDebugEnabled()) {
                                    logger.debug("... returned " + result);
                                }
                            }
                        }
                    }
                } else {
                    logger.warn("JDBC-Object for UID " + uid + " and command " + cmd + " is null !");
                }
            } else {
                result = cmd.execute(null, this);
            }

            if(connectionConfiguration.isTraceCommandCount()) {
                String cmdString = cmd.toString();
                Integer oldval = commandCountMap.get(cmdString);
                if(oldval == null) {
                    commandCountMap.put(cmdString, 1);
                } else {
                    commandCountMap.put(cmdString, oldval + 1);
                }
            }

            numberOfProcessedCommands++;

            return result;
        } finally {
            active = false;
            lastAccessTimestamp = System.currentTimeMillis();
        }
    }

    public void cancelCurrentStatementExecution(
        Long connuid, Long uid, StatementCancelCommand cmd) {
        // Get the Statement object
        // 获取Statement对象
        JdbcObjectHolder target = jdbcObjects.get(uid);

        if (target != null) {
            try {
                Statement stmt = (Statement)target.getJdbcObject();
                if (stmt != null) {
                    cmd.execute(stmt, this);
                } else {
                    logger.info("no statement with id " + uid + " to cancel");
                }
            } catch (Exception e) {
                logger.info(e.getMessage(), e);
            }
        } else {
            logger.info("no statement with id " + uid + " to cancel");
        }
    }

    public void traceConnectionStatistics() {
        logger.info("  Connection ........... " + connectionConfiguration.getId());
        logger.info("  IP address ........... " + clientInfo.getProperty("vjdbc-client.address", "n.a."));
        logger.info("  Host name ............ " + clientInfo.getProperty("vjdbc-client.name", "n.a."));
        dumpClientInfoProperties();
        logger.info("  Last time of access .. " + new Date(lastAccessTimestamp));
        logger.info("  Processed commands ... " + numberOfProcessedCommands);

        if(jdbcObjects.size() > 0) {
            logger.info("  Remaining objects .... " + jdbcObjects.size());
            for (JdbcObjectHolder jdbcObjectHolder : jdbcObjects.values()) {
                logger.info("  - " + jdbcObjectHolder.getJdbcObject().getClass().getName());
                if (connectionConfiguration.isTraceOrphanedObjects()) {
                    if (jdbcObjectHolder.getCallingContext() != null) {
                        logger.info(jdbcObjectHolder.getCallingContext().getStackTrace());
                    }
                }
            }
        }

        if(!commandCountMap.isEmpty()) {
            logger.info("  Command-Counts:");

            ArrayList entries = new ArrayList(commandCountMap.entrySet());
            Collections.sort(entries, new Comparator() {
                public int compare(Object o1, Object o2) {
                    Map.Entry e1 = (Map.Entry) o1;
                    Map.Entry e2 = (Map.Entry) o2;

                    Integer v1 = (Integer) e1.getValue();
                    Integer v2 = (Integer) e2.getValue();

                    // Descending sort
                    // 降序排列
                    return -v1.compareTo(v2);
                }
            });

            for (Object o : entries) {
                Map.Entry entry = (Map.Entry) o;
                String cmd = (String) entry.getKey();
                Integer count = (Integer) entry.getValue();
                logger.info("     " + count + " : " + cmd);
            }
        }
    }

    private Object handleResultSet(ResultSet result, boolean forwardOnly, CallingContext ctx) throws SQLException {
        // Populate a StreamingResultSet
        // 填充StreamingResultSet
        StreamingResultSet srs = new StreamingResultSet(
                connectionConfiguration.getRowPacketSize(),
                forwardOnly,
                connectionConfiguration.isPrefetchResultSetMetaData(),
                connectionConfiguration.getCharset());
        // Populate it
        // 填充它
        boolean lastPartReached = srs.populate(result);
        // Remember the ResultSet and put the UID in the StreamingResultSet
        // 记住ResultSet并将UID放在StreamingResultSet中
        UIDEx uid = new UIDEx();
        srs.setRemainingResultSetUID(uid);
        jdbcObjects.put(uid.getUID(), new JdbcObjectHolder(new ResultSetHolder(result, connectionConfiguration, lastPartReached), ctx, JdbcInterfaceType.RESULTSETHOLDER));
        if(logger.isDebugEnabled()) {
            logger.debug("Registered ResultSet with UID " + uid.getUID());
        }
        return new SerializableTransport(srs, getCompressionMode(), getCompressionThreshold());
    }

    private Object handleResultSetMetaData(ResultSetMetaData result) throws SQLException {
        return new SerializableTransport(new SerialResultSetMetaData(result), getCompressionMode(), getCompressionThreshold());
    }

    private void dumpClientInfoProperties() {
        if(logger.isInfoEnabled() && !clientInfo.isEmpty()) {
            boolean printedHeader = false;

            for(Enumeration it = clientInfo.keys(); it.hasMoreElements();) {
                String key = (String) it.nextElement();
                if(!key.startsWith("vjdbc")) {
                    if(!printedHeader) {
                        printedHeader = true;
                        logger.info("  Client-Properties ...");
                    }
                    logger.info("    " + key + " => " + clientInfo.getProperty(key));
                }
            }
        }
    }

    private String getNamedQuery(String id) throws SQLException {
        if(connectionConfiguration.getNamedQueries() != null) {
            return connectionConfiguration.getNamedQueries().getSqlForId(id);
        } else {
            String msg = "No named-queries are associated with this connection";
            logger.error(msg);
            throw new SQLException(msg);
        }
    }

    private void checkAgainstQueryFilters(String sql) throws SQLException {
        if(connectionConfiguration.getQueryFilters() != null) {
            connectionConfiguration.getQueryFilters().checkAgainstFilters(sql);
        }
    }
    
    private int getJdbcInterfaceTypeFromObject(Object jdbcObject) {
    	int jdbcInterfaceType = 0;
    	if(jdbcObject == null) {
    		return jdbcInterfaceType;
    	}
    	if(jdbcObject instanceof CallableStatement) {
    		jdbcInterfaceType = JdbcInterfaceType.CALLABLESTATEMENT;
    	} else if(jdbcObject instanceof Connection) {
    		jdbcInterfaceType = JdbcInterfaceType.CONNECTION;
    	} else if(jdbcObject instanceof DatabaseMetaData) {
    		jdbcInterfaceType = JdbcInterfaceType.DATABASEMETADATA;
    	} else if(jdbcObject instanceof PreparedStatement) {
    		jdbcInterfaceType = JdbcInterfaceType.PREPAREDSTATEMENT;
    	} else if(jdbcObject instanceof Savepoint) {
    		jdbcInterfaceType = JdbcInterfaceType.SAVEPOINT;
    	} else if(jdbcObject instanceof Statement) {
    		jdbcInterfaceType = JdbcInterfaceType.STATEMENT;
    	} else if(jdbcObject instanceof ResultSetHolder) {
    		jdbcInterfaceType = JdbcInterfaceType.RESULTSETHOLDER;
    	}
    	return jdbcInterfaceType;
    }
}
