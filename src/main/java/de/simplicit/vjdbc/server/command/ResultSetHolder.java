//VJDBC - Virtual JDBC
//Written by Michael Link
//Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.command;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.simplicit.vjdbc.serial.RowPacket;
import de.simplicit.vjdbc.serial.SerializableTransport;
import de.simplicit.vjdbc.server.config.ConnectionConfiguration;

/**
 * The ResultSetHolder is responsible to hold a reference to an open ResultSet.
 * It reads succeeding RowPackets in a Worker-Thread to immediately return a
 * result when nextRowPacket is called.
 * <p>ResultSetHolder负责保存对打开的ResultSet的引用。
 * 它读取工作线程中的后续RowPackets，以在调用nextRowPacket时立即返回结果。
 */
public class ResultSetHolder {
    private static final Log logger = LogFactory.getLog(ResultSetHolder.class);

    private final Object lock = new Object();
    private boolean readerThreadIsRunning = false;

    private ResultSet resultSet;
    private SerializableTransport currentSerializedRowPacket;
    private final ConnectionConfiguration connectionConfiguration;
    private boolean lastPartReached;
    private SQLException lastOccurredException = null;

    ResultSetHolder(ResultSet resultSet, ConnectionConfiguration config, boolean lastPartReached) throws SQLException {
        this.resultSet = resultSet;
        this.connectionConfiguration = config;
        this.lastPartReached = lastPartReached;
        if(!this.lastPartReached) {
            synchronized(lock) {
                readNextRowPacket();
            }
        }
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        synchronized (lock) {
            return resultSet.getMetaData();
        }
    }

    public void close() throws SQLException {
        synchronized (lock) {
            resultSet.close();
            resultSet = null;
        }
    }

    public SerializableTransport nextRowPacket() throws SQLException {
        synchronized (lock) {
            // If the reader thread is still running we must wait
            // for the lock to be released by the reader
            // 如果读取线程仍在运行，则必须等待读取释放锁定
            while(readerThreadIsRunning) {
                try {
                    // Wait for the reader thread to finish
                    // 等待读取线程完成
                    lock.wait();
                } catch (InterruptedException e) {
                    String msg = "Reader thread interrupted unexpectedly";
                    // Some unexpected exception occured, we must leave the loop here as the
                    // termination flag might not be reset to false.
                    // 发生了一些意外的异常，我们必须在此处离开循环，因为终止标志可能不会重置为false。
                    logger.error(msg, e);
                    lastOccurredException = new SQLException(msg);
                    break;
                }
            }

            // If any exception occured in the worker thread it will
            // be delivered to the client as a normal SQL exception
            // 如果工作线程中发生任何异常，它将作为常规SQL异常传递给客户端。
            if(lastOccurredException != null) {
                throw lastOccurredException;
            }

            // Remember current row packet as the result
            // 记住当前行包作为结果
            SerializableTransport result = currentSerializedRowPacket;
            // Start next reader thread
            // 启动下一个读取线程
            readNextRowPacket();
            // Return the result
            // 返回结果
            return result;
        }
    }

    private void readNextRowPacket() throws SQLException {
        if(resultSet != null && !lastPartReached) {
            // Start the thread
            // 启动线程
            try {
                connectionConfiguration.execute(new Runnable() {
                    public void run() {
                        // Aquire lock immediately
                        // 立即获得锁
                        synchronized (lock) {
                            try {
                                // When the ResultSet is null here, the client closed the ResultSet concurrently right
                                // after the upper check "_resultSet != null".
                                // 当此处的ResultSet为null时，客户端在上面的检查"resultSet != null"之后同时关闭ResultSet
                                if(resultSet != null) {
                                    RowPacket rowPacket = new RowPacket(connectionConfiguration.getRowPacketSize(), false);
                                    // Populate the new RowPacket using the ResultSet
                                    // 使用ResultSet填充新的RowPacket
                                    lastPartReached = rowPacket.populate(resultSet);
                                    currentSerializedRowPacket = new SerializableTransport(rowPacket, connectionConfiguration.getCompressionModeAsInt(),
                                            connectionConfiguration.getCompressionThreshold());
                                }
                            } catch (SQLException e) {
                                // Just remember the exception, it will be thrown at
                                // the next call to nextRowPacket
                                // 只要记住这个异常，它将在下一次调用nextRowPacket时被抛出
                                lastOccurredException = e;
                            } finally {
                                readerThreadIsRunning = false;
                                // Notify possibly waiting subsequent Readers
                                // 通知可能等待的后续读取器
                                lock.notify();
                            }
                        }
                    }
                });

                // Set the flag that the reader thread is considered to be running.
                // 设置读取线程被认为正在运行的标志。
                readerThreadIsRunning = true;
            } catch (InterruptedException e) {
                String msg = "Reader thread interrupted unexpectedly";
                logger.error(msg, e);
                throw new SQLException(msg);
            }
        } else {
            currentSerializedRowPacket = null;
        }
    }
}
