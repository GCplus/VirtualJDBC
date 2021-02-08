// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server;

import de.simplicit.vjdbc.VJdbcException;

/**
 * 登录处理接口
 */
public interface LoginHandler {
    /**
     * 登录
     * @param user 用户名
     * @param password 密码
     * @throws VJdbcException 自定义异常
     */
    void checkLogin(String user, String password) throws VJdbcException;
}
