// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.server.command.CommandProcessor;
import de.simplicit.vjdbc.server.config.ConfigurationException;
import de.simplicit.vjdbc.server.config.ConnectionConfiguration;
import de.simplicit.vjdbc.server.config.VJdbcConfiguration;
import de.simplicit.vjdbc.servlet.ServletCommandSinkIdentifier;
import de.simplicit.vjdbc.util.SQLExceptionHelper;
import de.simplicit.vjdbc.util.StreamCloser;
import javax.servlet.ServletContext;

public class ServletCommandSink extends HttpServlet {
    private static final String INIT_PARAMETER_CONFIG_RESOURCE = "config-resource";
    private static final String INIT_PARAMETER_CONFIG_VARIABLES = "config-variables";
    private static final String DEFAULT_CONFIG_RESOURCE = "/WEB-INF/vjdbc-config.xml";
    private static final long serialVersionUID = 3257570624301249846L;
    private static final Log logger = LogFactory.getLog(ServletCommandSink.class);

    private CommandProcessor processor;

    public ServletCommandSink() {
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        String configResource = servletConfig.getInitParameter(INIT_PARAMETER_CONFIG_RESOURCE);

        // Use default location when nothing is configured
        // 什么都没有配置时使用默认配置
        if(configResource == null) {
            configResource = DEFAULT_CONFIG_RESOURCE;
        }

        ServletContext ctx = servletConfig.getServletContext();

        logger.info("Trying to get config resource " + configResource + "...");
        InputStream configResourceInputStream = ctx.getResourceAsStream(configResource);
        if(null == configResourceInputStream) {
            try {
                configResourceInputStream =
                    new FileInputStream(ctx.getRealPath(configResource));
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        }

        if(configResourceInputStream == null) {
            String msg = "VJDBC-Configuration " + configResource + " not found !";
            logger.error(msg);
            throw new ServletException(msg);
        }

        // Are config variables specifiec ?
        // 是否指定了配置变量？
        String configVariables = servletConfig.getInitParameter(INIT_PARAMETER_CONFIG_VARIABLES);
        Properties configVariablesProps = null;

        if(configVariables != null) {
            logger.info("... using variables specified in " + configVariables);

            InputStream configVariablesInputStream = null;

            try {
                configVariablesInputStream = ctx.getResourceAsStream(configVariables);
                if(null == configVariablesInputStream) {
                    configVariablesInputStream =
                        new FileInputStream(ctx.getRealPath(configVariables));
                }

                configVariablesProps = new Properties();
                configVariablesProps.load(configVariablesInputStream);
            } catch (IOException e) {
                String msg = "Reading of configuration variables failed";
                logger.error(msg, e);
                throw new ServletException(msg, e);
            } finally {
                if(configVariablesInputStream != null) {
                    try {
                        configVariablesInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        try {
            logger.info("Initialize VJDBC-Configuration");
            VJdbcConfiguration.init(configResourceInputStream, configVariablesProps);
            processor = CommandProcessor.getInstance();
        } catch (ConfigurationException e) {
            logger.error("Initialization failed", e);
            throw new ServletException("VJDBC-Initialization failed", e);
        } finally {
                StreamCloser.close(configResourceInputStream);
        }
    }

    public void destroy() {
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        handleRequest(httpServletRequest, httpServletResponse);
    }

    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        handleRequest(httpServletRequest, httpServletResponse);
    }

    private void handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try {
            // Get the method to execute
            // 获取执行方法
            String method = httpServletRequest.getHeader(ServletCommandSinkIdentifier.METHOD_IDENTIFIER);

            if(method != null) {
                ois = new ObjectInputStream(httpServletRequest.getInputStream());
                // And initialize the output
                // 并初始化输出
                OutputStream os = httpServletResponse.getOutputStream();
                oos = new ObjectOutputStream(os);
                Object objectToReturn = null;

                try {
                    // Some command to process ?
                    // 一些命令要处理？
                    if(method.equals(ServletCommandSinkIdentifier.PROCESS_COMMAND)) {
                        // Read parameter objects
                        // 读取参数对象
                        Long connuid = (Long) ois.readObject();
                        Long uid = (Long) ois.readObject();
                        Command cmd = (Command) ois.readObject();
                        CallingContext ctx = (CallingContext) ois.readObject();
                        // Delegate execution to the CommandProcessor
                        // 将执行委托给CommandProcessor
                        objectToReturn = processor.process(connuid, uid, cmd, ctx);
                    } else if(method.equals(ServletCommandSinkIdentifier.CONNECT_COMMAND)) {
                        String url = ois.readUTF();
                        Properties props = (Properties) ois.readObject();
                        Properties clientInfo = (Properties) ois.readObject();
                        CallingContext ctx = (CallingContext) ois.readObject();

                        ConnectionConfiguration connectionConfiguration = VJdbcConfiguration.singleton().getConnection(url);

                        if(connectionConfiguration != null) {
                            Connection conn = connectionConfiguration.create(props);
                            objectToReturn = processor.registerConnection(conn, connectionConfiguration, clientInfo, ctx);
                        } else {
                            objectToReturn = new SQLException("VJDBC-Connection " + url + " not found");
                        }
                    }
                } catch (Throwable t) {
                    // Wrap any exception so that it can be transported back to
                    // the client
                    // 包装任何异常，以便可以将其传输回客户端
                    objectToReturn = SQLExceptionHelper.wrap(t);
                }

                // Write the result in the response buffer
                // 将结果写入响应缓冲区
                oos.writeObject(objectToReturn);
                oos.flush();

                httpServletResponse.flushBuffer();
            } else {
                // No VJDBC-Method ? Then we redirect the stupid browser user to
                // some information page :-)
                // 没有VJDBC方法？ 然后，我们将愚蠢的浏览器用户重定向到一些信息页面：-)
                httpServletResponse.sendRedirect("index.html");
            }
        } catch (Exception e) {
            logger.error("Unexpected Exception", e);
            throw new ServletException(e);
        } finally {
            StreamCloser.close(ois);
            StreamCloser.close(oos);
        }
    }
}
