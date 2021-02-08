// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class holds configuration information for the OCCT.
 * <p>此类包含OCCT的配置信息
 */
public class OcctConfiguration {
    private static final Log logger = LogFactory.getLog(OcctConfiguration.class);

    private long checkingPeriod = 30000;
    private long timeout = 120000;

    public OcctConfiguration() {
    }

    public long getCheckingPeriodInMillis() {
        return checkingPeriod;
    }

    public void setCheckingPeriodInMillis(long checkingPeriod) {
        if(checkingPeriod != 0 && checkingPeriod <= 1000) {
            logger.error("Checking-Period must be greater than 1 second");
        }
        else {
            this.checkingPeriod = checkingPeriod;
        }
    }

    public long getTimeoutInMillis() {
        return timeout;
    }

    public void setTimeoutInMillis(long timeout) {
        if(timeout > 0 && timeout <= 1000) {
            logger.error("Timeout must be greater than 1 second " + timeout);
        }
        else {
            this.timeout = timeout;
        }
    }

    void log() {
        if(checkingPeriod > 0) {
            logger.info("OrphanedConnectionCollectorTask-Configuration (OCCT)");
            logger.info("  Checking-Period........... " + ConfigurationUtil.getStringFromMillis(checkingPeriod));
            logger.info("  Timeout................... " + ConfigurationUtil.getStringFromMillis(timeout));
        }
        else {
            logger.info("OrphanedConnectionCollectorTask-Configuration (OCCT): off");
        }
    }
}
