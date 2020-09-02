// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConnectionPoolConfiguration {
    private static final Log logger = LogFactory.getLog(ConnectionPoolConfiguration.class);

    protected int maxActive = 8;
    protected int maxIdle = 8;
    protected int minIdle = 0;
    protected long maxWait = -1;
    protected int timeBetweenEvictionRunsMillis = -1;
    protected int minEvictableIdleTimeMillis = 1000 * 60 * 30;

    public ConnectionPoolConfiguration() {
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    void log() {
        logger.info("  ConnectionPool-Configuration");
        logger.info("    Max. active connections .............. " + maxActive);
        logger.info("    Max. number of idle connections ...... " + maxIdle);
        logger.info("    Min. number of idle connections ...... " + minIdle);
        logger.info("    Max. waiting time for connections .... " + ConfigurationUtil.getStringFromMillis(maxWait));
        logger.info("    Time between eviction runs ........... " + ConfigurationUtil.getStringFromMillis(timeBetweenEvictionRunsMillis));
        logger.info("    Min. idle time before eviction ....... " + ConfigurationUtil.getStringFromMillis(minEvictableIdleTimeMillis));
    }
}
