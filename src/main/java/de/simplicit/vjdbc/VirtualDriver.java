// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

import java.rmi.Naming;
import java.rmi.server.RMISocketFactory;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.simplicit.vjdbc.command.CallingContextFactory;
import de.simplicit.vjdbc.command.CommandSink;
import de.simplicit.vjdbc.command.DecoratedCommandSink;
import de.simplicit.vjdbc.command.NullCallingContextFactory;
import de.simplicit.vjdbc.command.StandardCallingContextFactory;
import de.simplicit.vjdbc.ejb.EjbCommandSinkProxy;
import de.simplicit.vjdbc.rmi.CommandSinkRmi;
import de.simplicit.vjdbc.rmi.CommandSinkRmiProxy;
import de.simplicit.vjdbc.rmi.ConnectionBrokerRmi;
import de.simplicit.vjdbc.rmi.SecureSocketFactory;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.UIDEx;
import de.simplicit.vjdbc.servlet.RequestEnhancer;
import de.simplicit.vjdbc.servlet.RequestEnhancerFactory;
import de.simplicit.vjdbc.servlet.ServletCommandSinkJdkHttpClient;
import de.simplicit.vjdbc.servlet.jakarta.ServletCommandSinkJakartaHttpClient;
import de.simplicit.vjdbc.util.ClientInfo;
import de.simplicit.vjdbc.util.SQLExceptionHelper;

public final class VirtualDriver implements Driver {
    private static final Log logger = LogFactory.getLog(VirtualDriver.class);

    private static final String VJDBC_IDENTIFIER = "jdbc:vjdbc:";
    private static final String EJB_IDENTIFIER = "ejb:";
    private static final String RMI_IDENTIFIER = "rmi:";
    private static final String SERVLET_IDENTIFIER = "servlet:";
    private static SecureSocketFactory sslSocketFactory;
    private static boolean cacheEnabled = false;

    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 7;

    static {
        try {
            DriverManager.registerDriver(new VirtualDriver());
            logger.info("Virtual JDBC-Driver successfully registered");
            try {
                Class.forName("org.hsqldb.jdbcDriver").newInstance();
                logger.info("HSQL-Driver loaded, caching activated");
                cacheEnabled = true;
            } catch (ClassNotFoundException e) {
                logger.info("Couldn't load HSQL-Driver, caching deactivated");
                cacheEnabled = false;
            } catch (Exception e) {
                logger.error("Unexpected exception occured on loading the HSQL-Driver");
                cacheEnabled = false;
            }
        } catch (Exception e) {
            logger.fatal("Couldn't register Virtual-JDBC-Driver !", e);
            throw new RuntimeException("Couldn't register Virtual-JDBC-Driver !", e);
        }
    }

    public VirtualDriver() {
    }

    public Connection connect(String urlstr, Properties props) throws SQLException {
        Connection result = null;

        if (acceptsURL(urlstr)) {
            String realUrl = urlstr.substring(VJDBC_IDENTIFIER.length());

            logger.info("VJdbc-URL: " + realUrl);

            try {
                CommandSink sink;//创建一个空白的CommandSink对象

                String[] urlparts;

                // EJB-Connection
                if (realUrl.startsWith(EJB_IDENTIFIER)) {
                    urlparts = split(realUrl.substring(EJB_IDENTIFIER.length()));
                    logger.info("VJdbc in EJB-Mode, using object " + urlparts[0]);
                    sink = createEjbCommandSink(urlparts[0]);
                    // RMI-Connection
                } else if (realUrl.startsWith(RMI_IDENTIFIER)) {
                    urlparts = split(realUrl.substring(RMI_IDENTIFIER.length()));
                    logger.info("VJdbc in RMI-Mode, using object " + urlparts[0]);
                    // Examine SSL property
                    boolean useSSL;
                    String propSSL = props.getProperty(VJdbcProperties.RMI_SSL);
                    useSSL = (propSSL != null && propSSL.equalsIgnoreCase("true"));
                    if (useSSL) {
                        logger.info("Using Secure Socket Layer (SSL)");
                    }
                    sink = createRmiCommandSink(urlparts[0], useSSL);
                    // Servlet-Connection
                } else if (realUrl.startsWith(SERVLET_IDENTIFIER)) {
                    urlparts = split(realUrl.substring(SERVLET_IDENTIFIER.length()));
                    logger.info("VJdbc in Servlet-Mode, using URL " + urlparts[0]);
                    sink = createServletCommandSink(urlparts[0], props);
                } else {
                    throw new SQLException("Unknown protocol identifier " + realUrl);
                }

                if (urlparts[1].length() > 0) {
                    logger.info("Connecting to datasource " + urlparts[1]);
                } else {
                    logger.info("Using default datasource");
                }

                // Connect with the sink
                UIDEx reg = sink.connect(
                        urlparts[1],
                        props,
                        ClientInfo.getProperties(props.getProperty(VJdbcProperties.CLIENTINFO_PROPERTIES)),
                        new CallingContext());

                CallingContextFactory ctxFactory;
                // The value 1 signals that every remote call shall provide a calling context. This should only
                // be used for debugging purposes as sending of these objects is quite expensive.
                if (reg.getValue1() == 1) {
                    ctxFactory = new StandardCallingContextFactory();
                } else {
                    ctxFactory = new NullCallingContextFactory();
                }
                // Decorate the sink
                DecoratedCommandSink decosink = new DecoratedCommandSink(reg, sink, ctxFactory);
                // return the new connection
                result = new VirtualConnection(reg, decosink, props, cacheEnabled);
            } catch (Exception e) {
                logger.error(e);
                throw SQLExceptionHelper.wrap(e);
            }
        }

        return result;
    }

    public boolean acceptsURL(String url) {
        return url.startsWith(VJDBC_IDENTIFIER);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        return MAJOR_VERSION;
    }

    public int getMinorVersion() {
        return MINOR_VERSION;
    }

    public boolean jdbcCompliant() {
        return true;
    }

    private CommandSink createRmiCommandSink(String rminame, boolean useSSL) throws Exception {
        if (useSSL) {
            if (sslSocketFactory == null) {
                sslSocketFactory = new SecureSocketFactory();
                RMISocketFactory.setSocketFactory(sslSocketFactory);
            }
        }
        ConnectionBrokerRmi broker = (ConnectionBrokerRmi) Naming.lookup(rminame);
        CommandSinkRmi rmiSink = broker.createCommandSink();
        return new CommandSinkRmiProxy(rmiSink);
    }

    private CommandSink createEjbCommandSink(String ejbname) throws Exception {
        Context ctx = new InitialContext();
        logger.info("Lookup " + ejbname);
        Object ref = ctx.lookup(ejbname);
        logger.info("remote bean " + ref.getClass().getName());
        return (EjbCommandSinkProxy) ref;
    }

    private CommandSink createServletCommandSink(String url, Properties props) throws Exception {
        RequestEnhancer requestEnhancer = null;

        String requestEnhancerFactoryClassName = props.getProperty(VJdbcProperties.SERVLET_REQUEST_ENHANCER_FACTORY);

        if (requestEnhancerFactoryClassName != null) {
            logger.debug("Found RequestEnhancerFactory class: " + requestEnhancerFactoryClassName);
            Class requestEnhancerFactoryClass = Class.forName(requestEnhancerFactoryClassName);
            RequestEnhancerFactory requestEnhancerFactory = (RequestEnhancerFactory) requestEnhancerFactoryClass.newInstance();
            logger.debug("RequestEnhancerFactory successfully created");
            requestEnhancer = requestEnhancerFactory.create();
        }

        // Decide here if we should use Jakarta-HTTP-Client
        String useJakartaHttpClient = props.getProperty(VJdbcProperties.SERVLET_USE_JAKARTA_HTTP_CLIENT);
        if (useJakartaHttpClient != null && useJakartaHttpClient.equals("true")) {
            return new ServletCommandSinkJakartaHttpClient(url, requestEnhancer);
        } else {
            return new ServletCommandSinkJdkHttpClient(url, requestEnhancer);
        }
    }

    // Helper method (can't use the 1.4-Method because support for 1.3 is desired)
    private String[] split(String url) {
        char[] splitChars = {',', ';', '#', '$'};

        for (char splitChar : splitChars) {
            int charindex = url.indexOf(splitChar);

            if (charindex >= 0) {
                return new String[]{url.substring(0, charindex), url.substring(charindex + 1)};
            }
        }

        return new String[]{url, ""};
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger");
    }
}
