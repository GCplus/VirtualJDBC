// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.command;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.simplicit.vjdbc.Registerable;
import de.simplicit.vjdbc.VJdbcException;
import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.command.DestroyCommand;
import de.simplicit.vjdbc.command.StatementCancelCommand;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.UIDEx;
import de.simplicit.vjdbc.server.config.ConnectionConfiguration;
import de.simplicit.vjdbc.server.config.OcctConfiguration;
import de.simplicit.vjdbc.server.config.VJdbcConfiguration;
import de.simplicit.vjdbc.util.SQLExceptionHelper;

/**
 * The CommandProcessor is a singleton class which dispatches calls from the
 * client to the responsible connection object.
 * <p>CommandProcessor是一个单例类，可将调用从客户端调度到负责的连接对象。
 */
public class CommandProcessor {
    private static final Log logger = LogFactory.getLog(CommandProcessor.class);
    private static CommandProcessor singleton;

    private static boolean closeConnectionsOnKill = true;
    private static long s_connectionId = 1;
    private Timer timer = null;
    private final Map<Long, ConnectionEntry> connectionEntries =
        Collections.synchronizedMap(new HashMap<Long, ConnectionEntry>());
    private final OcctConfiguration occtConfig;

    public static CommandProcessor getInstance() {
        if(singleton == null) {
            singleton = new CommandProcessor();
            installShutdownHook();
        }
        return singleton;
    }

    public static void setDontCloseConnectionsOnKill()
    {
        closeConnectionsOnKill = false;
    }

    private CommandProcessor() {
        occtConfig = VJdbcConfiguration.singleton().getOcctConfiguration();

        if(occtConfig.getCheckingPeriodInMillis() > 0) {
            logger.debug("OCCT starts");

            timer = new Timer(true);
            timer.scheduleAtFixedRate(new OrphanedConnectionCollectorTask(), occtConfig.getCheckingPeriodInMillis(), occtConfig
                    .getCheckingPeriodInMillis());
        } else {
            logger.info("OCCT is turned off");
        }
    }

    private static void installShutdownHook() {
        // Install the shutdown hook
        // 安装关机钩子函数
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                getInstance().destroy();
            }
        }));
    }

    public UIDEx createConnection(String url, Properties props, Properties clientInfo, CallingContext ctx) throws SQLException {
        ConnectionConfiguration connectionConfiguration = VJdbcConfiguration.singleton().getConnection(url);

        if(connectionConfiguration != null) {
            logger.debug("Found connection configuration " + connectionConfiguration.getId());

            Connection conn;
            try {
                conn = connectionConfiguration.create(props);
            } catch (VJdbcException e) {
                throw SQLExceptionHelper.wrap(e);
            }

            logger.debug("Created connection, registering it now ...");

            UIDEx reg = registerConnection(conn, connectionConfiguration, clientInfo, ctx);

            if(logger.isDebugEnabled()) {
                logger.debug("Registered " + conn.getClass().getName() + " with UID " + reg);
            }

            return reg;
        } else {
            throw new SQLException("Can't find connection configuration for " + url);
        }
    }

    public ConnectionEntry getConnectionEntry(long connid) {
        return connectionEntries.get(connid);
    }

    public synchronized UIDEx registerConnection(Connection conn, ConnectionConfiguration config, Properties clientInfo, CallingContext ctx) {
        // To optimize the communication we can tell the client if
        // calling-contexts should be delivered at all
        // 为了优化通信，我们可以告诉客户端是否应该传递调用上下文
        Long connid = s_connectionId++;
        UIDEx reg = new UIDEx(connid, config.isTraceOrphanedObjects() ? 1 : 0);
        connectionEntries.put(connid, new ConnectionEntry(connid, conn, config, clientInfo, ctx));
        return reg;
    }

    public void registerJDBCObject(Long connid, Registerable obj)
    {
        ConnectionEntry entry = getConnectionEntry(connid);
        assert(entry != null);
        entry.addJDBCObject(obj.getReg().getUID(), obj);
    }

    public void unregisterJDBCObject(Long connid, Registerable obj)
    {
        ConnectionEntry entry = getConnectionEntry(connid);
        if (entry != null) {
            entry.removeJDBCObject(obj.getReg().getUID());
        }
    }

    public void destroy() {
        logger.info("Destroying CommandProcessor ...");

        // Stop the timer
        // 停止计数器
        if(timer != null) {
            timer.cancel();
        }

        if (closeConnectionsOnKill) {

            // Copy ConnectionEntries for closing
            // 复制ConnectionEntries以关闭
            ArrayList copyOfConnectionEntries = new ArrayList(connectionEntries.values());
            // and clear the map immediately
            // 马上清理MAP
            connectionEntries.clear();

            for (Object copyOfConnectionEntry : copyOfConnectionEntries) {
                ConnectionEntry connectionEntry = (ConnectionEntry) copyOfConnectionEntry;
                synchronized (connectionEntry) {
                    connectionEntry.close();
                }
            }
        } else {
            connectionEntries.clear();
        }
        singleton = null;

        logger.info("CommandProcessor successfully destroyed");
    }

    public Object process(Long connuid, Long uid, Command cmd, CallingContext ctx) throws SQLException {
        Object result = null;

        if(logger.isDebugEnabled()) {
            logger.debug(cmd);
        }

        if(connuid != null) {
            // Retrieving connection entry for the UID
            // 检索UID的连接条目
            ConnectionEntry connentry = connectionEntries.get(connuid);

            if(connentry != null) {
                try {
                    // StatementCancelCommand can be executed asynchronously to terminate
                    // a running query
                    // StatementCancelCommand可以异步执行，终止正在运行的查询
                    if(cmd instanceof StatementCancelCommand) {
                        connentry.cancelCurrentStatementExecution(
                            connuid, uid, (StatementCancelCommand)cmd);
                    }
                    else {
                        // All other commands must be executed synchronously which is done
                        // by calling the synchronous executeCommand-Method
                        // 所有其他命令都必须通过调用synchronous executeccommand - method来同步执行
                        result = connentry.executeCommand(uid, cmd, ctx);
                    }
                } catch (SQLException e) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("SQLException", e);
                    }
                    // Wrap the SQLException into something that can be safely thrown
                    // 将SQLException包装到可以安全抛出的对象中
                    throw SQLExceptionHelper.wrap(e);
                } catch (Throwable e) {
                    // Serious runtime error occured, wrap it in an SQLException
                    // 发生了严重的运行时错误，将其包装在SQLException中
                    logger.error(e);
                    throw SQLExceptionHelper.wrap(e);
                } finally {
                    // When there are no more JDBC objects left in the connection entry (that
                    // means even the JDBC-Connection is gone) the connection entry will be
                    // immediately destroyed and removed.
                    // 当连接项中没有更多的JDBC对象时(这意味着JDBC连接也消失了)，连接项将立即被销毁和删除。
                    if(!connentry.hasJdbcObjects()) {
                        // As remove can be called asynchronously here, we must check the
                        // return value.
                        // 因为这里可以异步调用remove，所以我们必须检查返回值。
                        if(connectionEntries.remove(connuid) != null) {
                            logger.info("Connection " + connuid + " closed, statistics:");
                            connentry.traceConnectionStatistics();
                        }
                    }
                }
            } else {
                if(cmd instanceof DestroyCommand) {
                    logger.debug("Connection entry already gone, DestroyCommand will be ignored");
                } else {
                    String msg = "Unknown connection entry " + connuid + " for command " + cmd.toString();
                    logger.error(msg);
                    throw new SQLException(msg);
                }
            }
        } else {
            String msg = "Connection id is null";
            logger.fatal(msg);
            throw new SQLException(msg);
        }

        return result;
    }

    /**
     * The orphaned connection collector task periodically checks the existing
     * connection entries for orphaned entries that means connections which
     * weren't used for a specific time and where the client didn't send keep
     * alive pings.
     * <p>孤立连接收集器任务会定期检查现有连接条目中是否存在孤立条目，
     * 这意味着在特定时间内未使用且客户端未发送保持连接ping的连接。
     */
    private class OrphanedConnectionCollectorTask extends TimerTask {
        public void run() {
            try {
                logger.debug("Checking for orphaned connections ...");

                long millis = System.currentTimeMillis();

                for(Iterator<Long> it = connectionEntries.keySet().iterator(); it.hasNext();) {
                    Long key = it.next();
                    ConnectionEntry connentry = connectionEntries.get(key);

                    // Synchronize here so that the process-Method doesn't
                    // access the same entry concurrently
                    // 此处进行同步，以便进程方法不会并发地访问相同的条目
                    synchronized (connentry) {
                        long idleTime = millis - connentry.getLastAccess();

                        if(!connentry.isActive() && (idleTime > occtConfig.getTimeoutInMillis())) {
                            logger.info("Closing orphaned connection " + key + " after being idle for about " + (idleTime / 1000) + "sec");
                            // The close method doesn't throw an exception
                            // close方法不会抛出异常
                            connentry.close();
                            it.remove();
                        }
                    }
                }
            } catch (ConcurrentModificationException e) {
                // This exception might happen when the process-Method has changed the
                // connection-entry map while this iteration is ongoing. We just ignore the
                // exception and let the entry stay in the map until the next run of the
                // OCCT (the last access time isn't modified until the next run).
                // 当进程方法在进行迭代时更改了连接条目映射时，可能会发生此异常。
                // 我们只是忽略该异常，而将条目保留在MAP中，直到下一次OCCT运行为止（上一次访问时间直到下一次运行才被修改）。
                if(logger.isDebugEnabled()) {
                    String msg = "ConcurrentModificationException in OCCT";
                    logger.debug(msg, e);
                }
            } catch (RuntimeException e) {
                // Any other error will be propagated so that the timer task is stopped
                // 任何其他错误都将传播，以使计时器任务停止
                String msg = "Unexpected Runtime-Exception in OCCT";
                logger.fatal(msg, e);
            }
        }
    }
}
