// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet.jakarta;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;

import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.UIDEx;
import de.simplicit.vjdbc.servlet.AbstractServletCommandSinkClient;
import de.simplicit.vjdbc.servlet.RequestEnhancer;
import de.simplicit.vjdbc.servlet.ServletCommandSinkIdentifier;
import de.simplicit.vjdbc.util.SQLExceptionHelper;
import de.simplicit.vjdbc.util.StreamCloser;

/**
 * ServletCommandSinkClient implementation which uses Jakarta-HttpClient to communicate with the
 * web server.
 * ServletCommandSinkClient实现，该实现使用Jakarta-HttpClient与Web服务器通信。
 * @author Mike
 */
public class ServletCommandSinkJakartaHttpClient extends AbstractServletCommandSinkClient {
    private final String urlExternalForm;
    private final HttpClient httpClient;
    private final MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager;

    /**
     * 构造一个http客户端
     * @param url 传入网址
     * @param requestEnhancer http请求头
     */
    public ServletCommandSinkJakartaHttpClient(String url, RequestEnhancer requestEnhancer) {
        super(url, requestEnhancer);
        this.urlExternalForm = super.url.toExternalForm();
        this.multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
        this.httpClient = new HttpClient(multiThreadedHttpConnectionManager);

        httpClient.getParams().setBooleanParameter("http.connection.stalecheck", false);//关闭http client的旧链接复用检查
    }

    public void close() {
        super.close();
        multiThreadedHttpConnectionManager.shutdown();
    }

    /**
     * 构建数据库连接的上下文信息
     * @param database  数据库
     * @param props 参数
     * @param clientInfo 客户端信息
     * @param ctx 上下文
     * @return 将sql result 强制转换成UIDEx类型并返回
     * @throws SQLException 抛出sql异常
     */
    public UIDEx connect(String database, Properties props, Properties clientInfo, CallingContext ctx) throws SQLException {
        PostMethod post = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            // Open connection and adjust the Input/Output
            // 打开连接并调整输入/输出
            post = new PostMethod(urlExternalForm);
            post.setDoAuthentication(false);
            post.setFollowRedirects(false);
            post.setRequestHeader("Content-type", "binary/x-java-serialized");
            post.setRequestHeader(ServletCommandSinkIdentifier.METHOD_IDENTIFIER, ServletCommandSinkIdentifier.CONNECT_COMMAND);
            // Finally let the optional Request-Enhancer set request headers
            // 最后，让可选的Request-Enhancer设置请求标头
            if(requestEnhancer != null) {
                requestEnhancer.enhanceConnectRequest(new RequestModifierJakartaHttpClient(post));
            }
            // Write the parameter objects using a ConnectRequestEntity
            // 使用ConnectRequestEntity编写参数对象
            post.setRequestEntity(new ConnectRequestEntity(database, props, clientInfo, ctx));

            // Call ...
            // 连接 ...
            httpClient.executeMethod(post);

            // Check the HTTP status.
            // 检查HTTP状态
            if(post.getStatusCode() != HttpStatus.SC_OK) {
                throw SQLExceptionHelper.wrap(new HttpClientError(post.getStatusLine().toString()));
            } else {
                // Read the result object from the InputStream
                // 从InputStream读取结果对象
                ois = new ObjectInputStream(new BufferedInputStream(post.getResponseBodyAsStream()));
                Object result = ois.readObject();
                // This might be a SQLException which must be rethrown
                // 这可能是必须重新抛出的SQLException
                if(result instanceof SQLException) {
                    throw (SQLException) result;
                } else {
                    return (UIDEx) result;
                }
            }

        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        } finally {
            // Cleanup resources
            // 清理资源
            StreamCloser.close(oos);
            StreamCloser.close(ois);

            if(post != null) {
                post.releaseConnection();
            }
        }
    }

    public Object process(Long connuid, Long uid, Command cmd, CallingContext ctx) throws SQLException {
        PostMethod post = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            post = new PostMethod(urlExternalForm);
            post.setDoAuthentication(false);
            post.setFollowRedirects(false);
            post.setContentChunked(false);
            post.setRequestHeader(ServletCommandSinkIdentifier.METHOD_IDENTIFIER, ServletCommandSinkIdentifier.PROCESS_COMMAND);
            // Finally let the optional Request-Enhancer set request properties
            // 最后，让可选的Request-Enhancer设置请求属性
            if(requestEnhancer != null) {
                requestEnhancer.enhanceProcessRequest(new RequestModifierJakartaHttpClient(post));
            }
            // Write the parameter objects using a ProcessRequestEntity
            // 使用ProcessRequestEntity编写参数对象
            post.setRequestEntity(new ProcessRequestEntity(connuid, uid, cmd, ctx));

            // Call ...
            // 传输 ...
            httpClient.executeMethod(post);

            if(post.getStatusCode() != HttpStatus.SC_OK) {
                throw SQLExceptionHelper.wrap(new HttpClientError(post.getStatusLine().toString()));
            } else {
                ois = new ObjectInputStream(new BufferedInputStream(post.getResponseBodyAsStream()));
                Object result = ois.readObject();
                if(result instanceof SQLException) {
                    throw (SQLException) result;
                } else {
                    return result;
                }
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        } finally {
            // Cleanup resources
            // 清理资源
            StreamCloser.close(oos);
            StreamCloser.close(ois);

            if(post != null) {
                post.releaseConnection();
            }
        }
    }
}
