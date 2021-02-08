// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import de.simplicit.vjdbc.VJdbcException;
import de.simplicit.vjdbc.VJdbcProperties;
import de.simplicit.vjdbc.server.DataSourceProvider;
import de.simplicit.vjdbc.server.LoginHandler;
import de.simplicit.vjdbc.server.concurrent.Executor;
import de.simplicit.vjdbc.server.concurrent.PooledExecutor;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.zip.Deflater;

public class ConnectionConfiguration implements Executor {
    private static final Log logger = LogFactory.getLog(ConnectionConfiguration.class);
    private static final String DBCP_ID = "jdbc:apache:commons:dbcp:";

    // Basic properties
    // 基础配置信息
    protected String id;
    protected String driver;
    protected String url;
    protected String dataSourceProvider;
    protected String user;
    protected String password;
    // Trace properties
    // 跟踪属性
    protected boolean traceCommandCount = false;
    protected boolean traceOrphanedObjects = false;
    // Row-Packet size defines the number of rows that is
    // transported in one packet
    // 行数据包大小定义一个数据包中传输的行数
    protected int rowPacketSize = 200;
    // Encoding for strings
    // 字符串编码
    protected String charset = "UTF-8";//原值为ISO-8859-1
    // Compression
    // 压缩
    protected int compressionMode = Deflater.BEST_SPEED;
    protected long compressionThreshold = 1000;
    // Connection pooling
    // 连接池
    protected boolean connectionPooling = true;
    protected ConnectionPoolConfiguration connectionPoolConfiguration = null;
    // Fetch the metadata of a resultset immediately after constructing
    // 构造后立即获取结果集的元数据
    protected boolean prefetchResultSetMetaData = false;
    // Custom login handler
    // 自定义登录处理
    protected String loginHandler;
    private LoginHandler loginHandlerInstance = null;
    // Named queries
    // 命名查询
    protected NamedQueryConfiguration namedQueries;
    // Query filters
    // 查询过滤器
    protected QueryFilterConfiguration queryFilters;

    // Connection pooling support
    // 连接池支持
    private boolean driverInitialized = false;
    private Boolean connectionPoolInitialized = Boolean.FALSE;
    private GenericObjectPool connectionPool = null;
    // Thread pooling support
    // 线程池支持
    private final int maxThreadPoolSize = 8;
    private final PooledExecutor pooledExecutor = new PooledExecutor(maxThreadPoolSize);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDataSourceProvider() {
        return dataSourceProvider;
    }

    public void setDataSourceProvider(String dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isTraceCommandCount() {
        return traceCommandCount;
    }

    public void setTraceCommandCount(boolean traceCommandCount) {
        this.traceCommandCount = traceCommandCount;
    }

    public boolean isTraceOrphanedObjects() {
        return traceOrphanedObjects;
    }

    public void setTraceOrphanedObjects(boolean traceOrphanedObjects) {
        this.traceOrphanedObjects = traceOrphanedObjects;
    }

    public int getRowPacketSize() {
        return rowPacketSize;
    }

    public void setRowPacketSize(int rowPacketSize) {
        this.rowPacketSize = rowPacketSize;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getCompressionModeAsInt() {
        return compressionMode;
    }

    public void setCompressionModeAsInt(int compressionMode) throws ConfigurationException {
        switch (compressionMode) {
            case Deflater.BEST_SPEED:
            case Deflater.BEST_COMPRESSION:
            case Deflater.NO_COMPRESSION:
                this.compressionMode = compressionMode;
            default:
                throw new ConfigurationException("Unknown compression mode");
        }
    }

    public String getCompressionMode() {
        switch (compressionMode) {
            case Deflater.BEST_SPEED:
                return "bestspeed";
            case Deflater.BEST_COMPRESSION:
                return "bestcompression";
            case Deflater.NO_COMPRESSION:
                return "none";
            default:
                throw new RuntimeException("Unknown compression mode");
        }
    }

    public void setCompressionMode(String compressionMode) throws ConfigurationException {
        if (compressionMode.equalsIgnoreCase("bestspeed")) {
            this.compressionMode = Deflater.BEST_SPEED;
        } else if (compressionMode.equalsIgnoreCase("bestcompression")) {
            this.compressionMode = Deflater.BEST_COMPRESSION;
        } else if (compressionMode.equalsIgnoreCase("none")) {
            this.compressionMode = Deflater.NO_COMPRESSION;
        } else {
            throw new ConfigurationException("Unknown compression mode '" + compressionMode
                    + "', use either bestspeed, bestcompression or none");
        }
    }

    public long getCompressionThreshold() {
        return compressionThreshold;
    }

    public void setCompressionThreshold(long compressionThreshold) throws ConfigurationException {
        if (this.compressionThreshold < 0) {
            throw new ConfigurationException("Compression threshold must be >= 0");
        }
        this.compressionThreshold = compressionThreshold;
    }

    public boolean useConnectionPooling() {
        return connectionPooling;
    }

    public void setConnectionPooling(boolean connectionPooling) {
        this.connectionPooling = connectionPooling;
    }

    public ConnectionPoolConfiguration getConnectionPoolConfiguration() {
        return connectionPoolConfiguration;
    }

    public void setConnectionPoolConfiguration(ConnectionPoolConfiguration connectionPoolConfiguration) {
        this.connectionPoolConfiguration = connectionPoolConfiguration;
        connectionPooling = true;
    }

    public boolean isPrefetchResultSetMetaData() {
        return prefetchResultSetMetaData;
    }

    public void setPrefetchResultSetMetaData(boolean fetchResultSetMetaData) {
        this.prefetchResultSetMetaData = fetchResultSetMetaData;
    }

    public String getLoginHandler() {
        return loginHandler;
    }

    public void setLoginHandler(String loginHandler) {
        this.loginHandler = loginHandler;
    }

    public NamedQueryConfiguration getNamedQueries() {
        return namedQueries;
    }

    public void setNamedQueries(NamedQueryConfiguration namedQueries) {
        this.namedQueries = namedQueries;
    }

    public QueryFilterConfiguration getQueryFilters() {
        return queryFilters;
    }

    public void setQueryFilters(QueryFilterConfiguration queryFilters) {
        this.queryFilters = queryFilters;
    }

    void validate() throws ConfigurationException {
        if (url == null && (dataSourceProvider == null)) {
            String msg = "Connection-Entry " + id + ": neither URL nor DataSourceProvider is provided";
            logger.error(msg);
            throw new ConfigurationException(msg);
        }

        // When connection pooling is used, the user/password combination must be
        // provided in the configuration as otherwise user-accounts are mixed up
        // 使用连接池时，必须在配置中提供用户/密码组合，否则用户帐户将被混淆
        if (dataSourceProvider == null) {
            if (connectionPooling && user == null) {
                String msg = "Connection-Entry " + id + ": connection pooling can only be used when a dedicated user is specified for the connection";
                logger.error(msg);
                throw new ConfigurationException(msg);
            }
        }
    }

    void log() {
        String usedPassword = "provided by client";
        if (password != null) {
            char[] hiddenPassword = new char[password.length()];
            for (int i = 0; i < password.length(); i++) {
                hiddenPassword[i] = '*';
            }
            usedPassword = new String(hiddenPassword);
        }

        logger.info("Connection-Configuration '" + id + "'");

        // We must differentiate between the DataSource-API and the older
        // DriverManager-API. When the DataSource-Provider is provided, the
        // driver and URL configurations will be ignored
        // 我们必须区分DataSource-API和较旧的DriverManager-API
        // 提供DataSource-Provider时，驱动程序和URL配置将被忽略
        if (dataSourceProvider != null) {
            logger.info("  DataSource-Provider ........ " + dataSourceProvider);
        } else {
            if (driver != null) {
                logger.info("  Driver ..................... " + driver);
            }
            logger.info("  URL ........................ " + url);
        }
        logger.info("  User ....................... " + ((user != null) ? user : "provided by client"));
        logger.info("  Password ................... " + usedPassword);
        logger.info("  Row-Packetsize ............. " + rowPacketSize);
        logger.info("  Charset .................... " + charset);
        logger.info("  Compression ................ " + getCompressionMode());
        logger.info("  Compression-Thrs ........... " + compressionThreshold + " bytes");
        logger.info("  Connection-Pool ............ " + (connectionPooling ? "on" : "off"));
        logger.info("  Pre-Fetch ResultSetMetaData  " + (prefetchResultSetMetaData ? "on" : "off"));
        logger.info("  Login-Handler .............. " + (loginHandler != null ? loginHandler : "none"));
        logger.info("  Trace Command-Counts ....... " + traceCommandCount);
        logger.info("  Trace Orphaned-Objects ..... " + traceOrphanedObjects);

        if (connectionPoolConfiguration != null) {
            connectionPoolConfiguration.log();
        }

        if (namedQueries != null) {
            namedQueries.log();
        }

        if (queryFilters != null) {
            queryFilters.log();
        }
    }

    public Connection create(Properties props) throws SQLException, VJdbcException {
        checkLogin(props);

        if (dataSourceProvider != null) {
            return createConnectionViaDataSource();
        } else {
            return createConnectionViaDriverManager(props);
        }
    }

    protected Connection createConnectionViaDataSource() throws SQLException {
        Connection result;

        logger.debug("Creating DataSourceFactory from class " + dataSourceProvider);

        try {
            Class clsDataSourceProvider = Class.forName(dataSourceProvider);
            DataSourceProvider dataSourceProvider = (DataSourceProvider) clsDataSourceProvider.newInstance();
            logger.debug("DataSourceProvider created");
            DataSource dataSource = dataSourceProvider.getDataSource();
            logger.debug("Retrieving connection from DataSource");
            if (user != null) {
                result = dataSource.getConnection(user, password);
            } else {
                result = dataSource.getConnection();
            }
            logger.debug("... Connection successfully retrieved");
        } catch (ClassNotFoundException e) {
            String msg = "DataSourceProvider-Class " + dataSourceProvider + " not found";
            logger.error(msg, e);
            throw new SQLException(msg);
        } catch (InstantiationException e) {
            String msg = "Failed to create DataSourceProvider";
            logger.error(msg, e);
            throw new SQLException(msg);
        } catch (IllegalAccessException e) {
            String msg = "Can't access DataSourceProvider";
            logger.error(msg, e);
            throw new SQLException(msg);
        }

        return result;
    }

    protected Connection createConnectionViaDriverManager(Properties props) throws SQLException {
        // Try to load the driver
        // 尝试加载驱动程序
        if (!driverInitialized && driver != null) {
            try {
                logger.debug("Loading driver " + driver);
                Class.forName(driver).newInstance();
                logger.debug("... successful");
            } catch (Exception e) {
                String msg = "Loading of driver " + driver + " failed";
                logger.error(msg, e);
                throw new SQLException(msg);
            }
            driverInitialized = true;
        }

        // When database login is provided use them for the login instead of the
        // ones provided by the client
        // 提供数据库登录名后，请使用它们代替客户端提供的登录名
        if (user != null) {
            logger.debug("Using " + user + " for database-login");
            props.put("user", user);
            if (password != null) {
                props.put("password", password);
            } else {
                logger.warn("No password was provided for database-login " + user);
            }
        }

        String jdbcurl = url;

        if (jdbcurl.length() > 0) {
            logger.debug("JDBC-Connection-String: " + jdbcurl);
        } else {
            String msg = "No JDBC-Connection-String available";
            logger.error(msg);
            throw new SQLException(msg);
        }

        // Connection pooling with DBCP
        // 连接DBCP连接池
        if (connectionPooling && connectionPoolInitialized != null) {
            String dbcpId = DBCP_ID + id;

            if (connectionPool != null) {
                jdbcurl = dbcpId;
            } else {
                try {
                    // Try to load the DBCP-Driver
                    // 尝试加载DBCP驱动程序
                    Class.forName("org.apache.commons.dbcp.PoolingDriver");
                    // Populate configuration object
                    // 填充配置对象
                    if (connectionPoolConfiguration != null) {
                        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
                        poolConfig.maxActive = connectionPoolConfiguration.getMaxActive();
                        poolConfig.maxIdle = connectionPoolConfiguration.getMaxIdle();
                        poolConfig.maxWait = connectionPoolConfiguration.getMaxWait();
                        poolConfig.minIdle = connectionPoolConfiguration.getMinIdle();
                        poolConfig.minEvictableIdleTimeMillis = connectionPoolConfiguration.getMinEvictableIdleTimeMillis();
                        poolConfig.timeBetweenEvictionRunsMillis = connectionPoolConfiguration.getTimeBetweenEvictionRunsMillis();
                        connectionPool = new LoggingGenericObjectPool(id, poolConfig);
                    } else {
                        connectionPool = new LoggingGenericObjectPool(id);
                    }

                    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(jdbcurl, props);
                    new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
                    PoolingDriver driver = (PoolingDriver) DriverManager.getDriver(DBCP_ID);
                    // Register pool with connection id
                    // 用连接ID注册池
                    driver.registerPool(id, connectionPool);
                    connectionPoolInitialized = Boolean.TRUE;
                    jdbcurl = dbcpId;
                    logger.debug("Connection-Pooling successfully initialized for connection " + id);
                } catch (ClassNotFoundException e) {
                    connectionPool = null;
                    connectionPoolInitialized = null;
                    logger.error("Jakarta-DBCP-Driver not found, switching it off for connection " + id, e);
                }
            }
        }

        return DriverManager.getConnection(jdbcurl, props);
    }

    protected void checkLogin(Properties props) throws VJdbcException {
        if (loginHandler != null) {
            logger.debug("Trying to login ...");

            if (loginHandlerInstance == null) {
                try {
                    Class loginHandlerClazz = Class.forName(loginHandler);
                    loginHandlerInstance = (LoginHandler) loginHandlerClazz.newInstance();
                } catch (ClassNotFoundException e) {
                    String msg = "Login-Handler class not found";
                    logger.error(msg, e);
                    throw new VJdbcException(msg, e);
                } catch (InstantiationException | IllegalAccessException e) {
                    String msg = "Error creating instance of Login-Handler class";
                    logger.error(msg, e);
                    throw new VJdbcException(msg, e);
                }
            }

            String loginUser = props.getProperty(VJdbcProperties.LOGIN_USER);
            String loginPassword = props.getProperty(VJdbcProperties.LOGIN_PASSWORD);

            if (loginUser == null) {
                logger.warn("Property vjdbc.login.user is not set, " + "the login-handler might not be satisfied");
            }

            if (loginPassword == null) {
                logger.warn("Property vjdbc.login.password is not set, " + "the login-handler might not be satisfied");
            }

            loginHandlerInstance.checkLogin(loginUser, loginPassword);

            logger.debug("... successful");
        }
    }

    public void execute(Runnable command) throws InterruptedException {
        pooledExecutor.execute(command);
    }
}
