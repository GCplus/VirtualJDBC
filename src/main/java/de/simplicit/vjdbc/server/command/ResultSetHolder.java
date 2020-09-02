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
            while(readerThreadIsRunning) {
                try {
                    // Wait for the reader thread to finish
                    lock.wait();
                } catch (InterruptedException e) {
                    String msg = "Reader thread interrupted unexpectedly";
                    // Some unexpected exception occured, we must leave the loop here as the
                    // termination flag might not be reset to false.
                    logger.error(msg, e);
                    lastOccurredException = new SQLException(msg);
                    break;
                }
            }

            // If any exception occured in the worker thread it will
            // be delivered to the client as a normal SQL exception
            if(lastOccurredException != null) {
                throw lastOccurredException;
            }

            // Remember current row packet as the result
            SerializableTransport result = currentSerializedRowPacket;
            // Start next reader thread
            readNextRowPacket();
            // Return the result
            return result;
        }
    }

    private void readNextRowPacket() throws SQLException {
        if(resultSet != null && !lastPartReached) {
            // Start the thread
            try {
                connectionConfiguration.execute(new Runnable() {
                    public void run() {
                        // Aquire lock immediately
                        synchronized (lock) {
                            try {
                                // When the ResultSet is null here, the client closed the ResultSet concurrently right
                                // after the upper check "_resultSet != null".
                                if(resultSet != null) {
                                    RowPacket rowPacket = new RowPacket(connectionConfiguration.getRowPacketSize(), false);
                                    // Populate the new RowPacket using the ResultSet
                                    lastPartReached = rowPacket.populate(resultSet);
                                    currentSerializedRowPacket = new SerializableTransport(rowPacket, connectionConfiguration.getCompressionModeAsInt(),
                                            connectionConfiguration.getCompressionThreshold());
                                }
                            } catch (SQLException e) {
                                // Just remember the exception, it will be thrown at
                                // the next call to nextRowPacket
                                lastOccurredException = e;
                            } finally {
                                readerThreadIsRunning = false;
                                // Notify possibly waiting subsequent Readers
                                lock.notify();
                            }
                        }
                    }
                });

                // Set the flag that the reader thread is considered to be running.
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
