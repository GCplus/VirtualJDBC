//VJDBC - Virtual JDBC
//Written by Michael Link
//Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * This class inherits from the GenericObjectPool and provides a little bit
 * of logging when eviction happens.
 * <p>这个类继承自GenericObjectPool，并在回收发生时提供了一些日志记录。
 * @author Mike
 */
public class LoggingGenericObjectPool extends GenericObjectPool {
    private static final Log logger = LogFactory.getLog(LoggingGenericObjectPool.class);
    
    private final String idOfConnection;

    public LoggingGenericObjectPool(String nameOfConnection) {
        super(null);
        idOfConnection = nameOfConnection;
    }
    
    public LoggingGenericObjectPool(String nameOfConnection, GenericObjectPool.Config config) {
        super(null, config);
        idOfConnection = nameOfConnection;
    }
        
    public synchronized void evict() throws Exception {
        super.evict();
        if(logger.isDebugEnabled()) {
            logger.debug("DBCP-Evictor: number of idle connections in '" + idOfConnection + "' = " + getNumIdle());
        }
    }
}
