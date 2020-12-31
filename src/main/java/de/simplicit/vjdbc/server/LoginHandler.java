// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server;

import de.simplicit.vjdbc.VJdbcException;

/**
 * 登录处理接口
 * @param user 用户名
 * @param password 密码
 */
public interface LoginHandler {
    void checkLogin(String user, String password) throws VJdbcException;
}
