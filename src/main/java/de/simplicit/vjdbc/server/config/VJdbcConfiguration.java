// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.substitution.MultiVariableExpander;
import org.apache.commons.digester.substitution.VariableSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * Root configuration class. Can be initialized with different input objects
 * or be built up programmatically.
 * <p>根配置类,可以使用不同的输入对象进行初始化或以编程方式进行构建。
 */
public class VJdbcConfiguration {
    private static final Log logger = LogFactory.getLog(VJdbcConfiguration.class);
    private static VJdbcConfiguration singleton;

    private OcctConfiguration occtConfiguration = new OcctConfiguration();
    private RmiConfiguration rmiConfiguration;
    private final List connections = new ArrayList();
    private static boolean useStreamingResultSet = true;

    /**
     * overrides the use of the StreamingResultSet to allow other types of
     * result set network transport.
     * <p>覆盖对StreamingResultSet的使用，以允许其他类型的结果集网络传输。
     *
     * Don't call this method unless you are defining your own network
     * transport which has it own mechanism for transporting result sets
     * <p>除非您定义自己的网络传输，该方法具有自己的传输结果集的机制，否则请勿调用此方法
     */
    public static void setUseCustomResultSetHandling() {
        useStreamingResultSet = false;
    }

    public static boolean getUseCustomResultSetHandling() {
        return useStreamingResultSet;
    }


    /**
     * Initialization with pre-built configuration object.
     * <p>使用预先构建的配置对象进行初始化
     * @param customConfig 配置
     */
    public static void init(VJdbcConfiguration customConfig) {
        if(singleton != null) {
            logger.warn("VJdbcConfiguration already initialized, init-Call is ignored");
        } else {
            singleton = customConfig;
        }
    }

    /**
     * Initialization with resource.
     * <p>使用资源初始化
     * @param configResource Resource to be loaded by the ClassLoader <p>由ClassLoader加载的资源
     * @throws ConfigurationException 配置异常
     */
    public static void init(String configResource) throws ConfigurationException {
        init(configResource, null);
    }

    /**
     * Initialization with resource.
     * <p>使用资源初始化
     * @param configResource Resource to be loaded by the ClassLoader <p>由ClassLoader加载的资源
     * @throws ConfigurationException 配置异常
     */
    public static void init(String configResource, Properties configVariables) throws ConfigurationException {
        if(singleton != null) {
            logger.warn("VJdbcConfiguration already initialized, init-Call is ignored");
        } else {
            try {
                singleton = new VJdbcConfiguration(configResource, configVariables);
                if(logger.isInfoEnabled()) {
                    singleton.log();
                }
            } catch(Exception e) {
                String msg = "VJdbc-Configuration failed";
                logger.error(msg, e);
                throw new ConfigurationException(msg, e);
            }
        }
    }

    /**
     * Initialization with pre-opened InputStream.
     * <p>使用预先打开的InputStream进行初始化
     * @param configResourceInputStream InputStream
     * @throws ConfigurationException 配置异常
     */
    public static void init(InputStream configResourceInputStream, Properties configVariables) throws ConfigurationException {
        if(singleton != null) {
            logger.warn("VJdbcConfiguration already initialized, init-Call is ignored");
        } else {
            try {
                singleton = new VJdbcConfiguration(configResourceInputStream, configVariables);
                if(logger.isInfoEnabled()) {
                    singleton.log();
                }
            } catch(Exception e) {
                String msg = "VJdbc-Configuration failed";
                logger.error(msg, e);
                throw new ConfigurationException(msg, e);
            }
        }
    }

    /**
     * Accessor method to the configuration singleton.
     * <p>配置单例的访问器方法
     * @return Configuration object <p>将配置封装成的对象
     * @throws RuntimeException Thrown when accessing without being initialized
     * previously <p>在未先初始化的状态下访问时抛出
     */
    public static VJdbcConfiguration singleton() {
        if(singleton == null) {
            throw new RuntimeException("VJdbc-Configuration is not initialized !");
        }
        return singleton;
    }

    /**
     * Constructor. Can be used for programmatical building the Configuration object.
     * <p>构造函数。可以用于以编程方式构建Configuration对象
     */
    public VJdbcConfiguration() {
    }

    public OcctConfiguration getOcctConfiguration() {
        return occtConfiguration;
    }

    public void setOcctConfiguration(OcctConfiguration occtConfiguration) {
        this.occtConfiguration = occtConfiguration;
    }

    /**
     * Returns the RMI-Configuration.
     * <p>返回RMI配置
     * @return RmiConfiguration object or null <p>RMI配置对象或者null
     */
    public RmiConfiguration getRmiConfiguration() {
        return rmiConfiguration;
    }

    /**
     * Sets the RMI-Configuration object.
     * <p>设置RMI配置对象
     * @param rmiConfiguration RmiConfiguration object to be used. <p>要使用的RmiConfiguration对象
     */
    public void setRmiConfiguration(RmiConfiguration rmiConfiguration) {
        this.rmiConfiguration = rmiConfiguration;
    }

    /**
     * Returns a ConnectionConfiguration for a specific identifier.
     * <p>返回特定标识符的ConnectionConfiguration
     * @param name Identifier of the ConnectionConfiguration <p>ConnectionConfiguration的标识符
     * @return ConnectionConfiguration or null <p>连接配置或者null
     */
    public ConnectionConfiguration getConnection(String name) {
        for (Object connection : connections) {
            ConnectionConfiguration connectionConfiguration = (ConnectionConfiguration) connection;
            if (connectionConfiguration.getId().equals(name)) {
                return connectionConfiguration;
            }
        }
        return null;
    }

    /**
     * Adds a ConnectionConfiguration.
     * <p>添加连接配置
     * @param connectionConfiguration 连接配置
     * @throws ConfigurationException Thrown when the connection identifier already exists <p>当连接标识符已经存在时抛出
     */
    public void addConnection(ConnectionConfiguration connectionConfiguration) throws ConfigurationException {
        if(getConnection(connectionConfiguration.getId()) == null) {
            connections.add(connectionConfiguration);
        } else {
            String msg = "Connection configuration for " + connectionConfiguration.getId() + " already exists";
            logger.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    private VJdbcConfiguration(InputStream configResource, Properties vars) throws IOException, SAXException, ConfigurationException {
        Digester digester = createDigester(vars);
        digester.parse(configResource);
        validateConnections();
    }

    private VJdbcConfiguration(String configResource, Properties vars) throws IOException, SAXException, ConfigurationException {
        Digester digester = createDigester(vars);
        digester.parse(configResource);
        validateConnections();
    }

    private Digester createDigester(Properties vars) {
        Digester digester = createDigester();

        if(vars != null) {
            MultiVariableExpander expander = new MultiVariableExpander();
            expander.addSource("$", vars);
            digester.setSubstitutor(new VariableSubstitutor(expander));
        }

        return digester;
    }

    private void validateConnections() throws ConfigurationException {
        // Call the validation method of the configuration
        for (Object connection : connections) {
            ConnectionConfiguration connectionConfiguration = (ConnectionConfiguration) connection;
            connectionConfiguration.validate();
        }
    }

    private Digester createDigester() {
        Digester digester = new Digester();

        digester.push(this);

        digester.addObjectCreate("vjdbc-configuration/occt", DigesterOcctConfiguration.class);
        digester.addSetProperties("vjdbc-configuration/occt");
        digester.addSetNext("vjdbc-configuration/occt",
                "setOcctConfiguration",
                OcctConfiguration.class.getName());

        digester.addObjectCreate("vjdbc-configuration/rmi", DigesterRmiConfiguration.class);
        digester.addSetProperties("vjdbc-configuration/rmi");
        digester.addSetNext("vjdbc-configuration/rmi",
                "setRmiConfiguration",
                RmiConfiguration.class.getName());

        digester.addObjectCreate("vjdbc-configuration/connection", DigesterConnectionConfiguration.class);
        digester.addSetProperties("vjdbc-configuration/connection");
        digester.addSetNext("vjdbc-configuration/connection",
                "addConnection",
                ConnectionConfiguration.class.getName());

        digester.addObjectCreate("vjdbc-configuration/connection/connection-pool", ConnectionPoolConfiguration.class);
        digester.addSetProperties("vjdbc-configuration/connection/connection-pool");
        digester.addSetNext("vjdbc-configuration/connection/connection-pool",
                "setConnectionPoolConfiguration",
                ConnectionPoolConfiguration.class.getName());

        // Named-Queries
        digester.addObjectCreate("vjdbc-configuration/connection/named-queries", NamedQueryConfiguration.class);
        digester.addCallMethod("vjdbc-configuration/connection/named-queries/entry", "addEntry", 2);
        digester.addCallParam("vjdbc-configuration/connection/named-queries/entry", 0, "id");
        digester.addCallParam("vjdbc-configuration/connection/named-queries/entry", 1);
        digester.addSetNext("vjdbc-configuration/connection/named-queries",
                "setNamedQueries",
                NamedQueryConfiguration.class.getName());

        // Query-Filters
        digester.addObjectCreate("vjdbc-configuration/connection/query-filters", QueryFilterConfiguration.class);
        digester.addCallMethod("vjdbc-configuration/connection/query-filters/deny", "addDenyEntry", 2);
        digester.addCallParam("vjdbc-configuration/connection/query-filters/deny", 0);
        digester.addCallParam("vjdbc-configuration/connection/query-filters/deny", 1, "type");
        digester.addCallMethod("vjdbc-configuration/connection/query-filters/allow", "addAllowEntry", 2);
        digester.addCallParam("vjdbc-configuration/connection/query-filters/allow", 0);
        digester.addCallParam("vjdbc-configuration/connection/query-filters/allow", 1, "type");
        digester.addSetNext("vjdbc-configuration/connection/query-filters",
                "setQueryFilters",
                QueryFilterConfiguration.class.getName());

        return digester;
    }

    private void log() {
        if(rmiConfiguration != null) {
            rmiConfiguration.log();
        }
        occtConfiguration.log();
        for (Object connection : connections) {
            ConnectionConfiguration connectionConfiguration = (ConnectionConfiguration) connection;
            connectionConfiguration.log();
        }
    }
}
