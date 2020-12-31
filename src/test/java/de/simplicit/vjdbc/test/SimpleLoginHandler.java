// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.test;

import de.simplicit.vjdbc.VJdbcException;
import de.simplicit.vjdbc.server.LoginHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 简单的登录处理
 */
public class SimpleLoginHandler implements LoginHandler {
    private Properties properties = new Properties();

    /**
     * 从"de/simplicit/vjdbc/test/user.properties"处以流的形式读取配置文件
     * @throws IOException 读取失败抛出IO异常
     */
    public SimpleLoginHandler() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("de/simplicit/vjdbc/test/user.properties");
        properties.load(is);
    }

    /**
     * 从环境变量里面读取用户对应的密码
     * (bug猜想) 如果user或者password是"",如何判断
     * @param user 用户名
     * @param password 密码
     * @throws VJdbcException 抛出用户信息异常信息
     */
    public void checkLogin(String user, String password) throws VJdbcException {
        if (user != null) {
            String pw = properties.getProperty(user);

            if (pw != null) {
                if (!pw.equals(password)) {
                    throw new VJdbcException("Password for user " + user + " is wrong");
                }
            } else {
                throw new VJdbcException("Unknown user " + user);
            }
        } else {
            throw new VJdbcException("User is null");
        }
    }
}
