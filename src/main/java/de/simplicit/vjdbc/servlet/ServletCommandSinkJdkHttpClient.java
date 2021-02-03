// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.Properties;

import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.UIDEx;
import de.simplicit.vjdbc.util.SQLExceptionHelper;
import de.simplicit.vjdbc.util.StreamCloser;

public class ServletCommandSinkJdkHttpClient extends AbstractServletCommandSinkClient {
    public ServletCommandSinkJdkHttpClient(String url, RequestEnhancer requestEnhancer) throws SQLException {
        super(url, requestEnhancer);
    }

    public UIDEx connect(String database, Properties props, Properties clientInfo, CallingContext ctx) throws SQLException {
        HttpURLConnection conn = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            // Open connection and adjust the Input/Output
            // 打开连接并调整输入/输出
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setAllowUserInteraction(false); // system may not ask the user 系统可能不询问用户
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-type", "binary/x-java-serialized" );
            conn.setRequestProperty(ServletCommandSinkIdentifier.METHOD_IDENTIFIER,
                                    ServletCommandSinkIdentifier.CONNECT_COMMAND);
            // Finally let the optional Request-Enhancer set request properties
            // 最后，让可选的Request-Enhancer设置请求属性
            if(requestEnhancer != null) {
                requestEnhancer.enhanceConnectRequest(new RequestModifierJdk(conn));
            }
            // Write the parameter objects to the ObjectOutputStream
            // 将参数对象写入ObjectOutputStream
            oos = new ObjectOutputStream(conn.getOutputStream());
            oos.writeUTF(database);
            oos.writeObject(props);
            oos.writeObject(clientInfo);
            oos.writeObject(ctx);
            oos.flush();
            // Connect ...
            // 连接 ...
            conn.connect();
            // Read the result object from the InputStream
            // 从InputStream读取结果对象
            ois = new ObjectInputStream(conn.getInputStream());
            Object result = ois.readObject();
            // This might be a SQLException which must be rethrown
            // 这可能是必须重新抛出的SQLException
            if(result instanceof SQLException) {
                throw (SQLException)result;
            }
            else {
                return (UIDEx)result;
            }
        } catch(SQLException e) {
            throw e;
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        } finally {
            // Cleanup resources
            // 清理资源
            StreamCloser.close(ois);
            StreamCloser.close(oos);

            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    public Object process(Long connuid, Long uid, Command cmd, CallingContext ctx) throws SQLException {
        HttpURLConnection conn = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty(ServletCommandSinkIdentifier.METHOD_IDENTIFIER, ServletCommandSinkIdentifier.PROCESS_COMMAND);
            // Finally let the optional Request-Enhancer set request properties
            // 最后，让可选的Request-Enhancer设置请求属性
            if(requestEnhancer != null) {
                requestEnhancer.enhanceProcessRequest(new RequestModifierJdk(conn));
            }
            conn.connect();

            oos = new ObjectOutputStream(conn.getOutputStream());
            oos.writeObject(connuid);
            oos.writeObject(uid);
            oos.writeObject(cmd);
            oos.writeObject(ctx);
            oos.flush();

            ois = new ObjectInputStream(conn.getInputStream());
            Object result = ois.readObject();
            if(result instanceof SQLException) {
                throw (SQLException)result;
            }
            else {
                return result;
            }
        } catch(SQLException e) {
            throw e;
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        } finally {
            // Cleanup resources
            // 清理资源
            StreamCloser.close(ois);
            StreamCloser.close(oos);

            if(conn != null) {
                conn.disconnect();
            }
        }
    }
}
