// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RmiConfiguration {
    private static final Log logger = LogFactory.getLog(RmiConfiguration.class);

    protected String objectName = "VJdbc";
    protected int registryPort = 2000;
    protected int remotingPort = 0;
    protected boolean createRegistry = true;
    protected boolean useSSL = false;
    protected String rmiClientSocketFactory = null;
    protected String rmiServerSocketFactory = null;

    public RmiConfiguration() {
    }

    public RmiConfiguration(String objectName) {
        this.objectName = objectName;
    }

    public RmiConfiguration(String objectName, int port) {
        this.objectName = objectName;
        this.registryPort = port;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    // Support method for old configuration format
    // 旧配置格式的支持方法
    public int getPort() {
        return registryPort;
    }

    // Support method for old configuration format
    // 旧配置格式的支持方法
    public void setPort(int port) {
        registryPort = port;
    }

    public int getRegistryPort() {
        return registryPort;
    }

    public void setRegistryPort(int registryPort) {
        this.registryPort = registryPort;
    }

    public int getRemotingPort() {
        return remotingPort;
    }

    public void setRemotingPort(int listenerPort) {
        remotingPort = listenerPort;
    }

    public boolean isCreateRegistry() {
        return createRegistry;
    }

    public void setCreateRegistry(boolean createRegistry) {
        this.createRegistry = createRegistry;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public String getRmiClientSocketFactory() {
        return rmiClientSocketFactory;
    }

    public void setRmiClientSocketFactory(String rmiClientSocketFactory) {
        this.rmiClientSocketFactory = rmiClientSocketFactory;
    }

    public String getRmiServerSocketFactory() {
        return rmiServerSocketFactory;
    }

    public void setRmiServerSocketFactory(String rmiServerSocketFactory) {
        this.rmiServerSocketFactory = rmiServerSocketFactory;
    }

    void log() {
        logger.info("RMI-Configuration");
        logger.info("  ObjectName ............... " + objectName);
        logger.info("  Registry-Port ............ " + registryPort);
        if(remotingPort > 0) {
            logger.info("  Remoting-Port ............ " + remotingPort);
        }
        logger.info("  Create Registry .......... " + createRegistry);
        logger.info("  Use SSL .................. " + useSSL);
        if(rmiClientSocketFactory != null) {
            logger.info("  Socket-Factory (client) .. " + rmiClientSocketFactory);
        }
        if(rmiServerSocketFactory != null) {
            logger.info("  Socket-Factory (server) .. " + rmiServerSocketFactory);
        }
    }
}
