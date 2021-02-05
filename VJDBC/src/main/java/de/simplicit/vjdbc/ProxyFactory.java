// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

import java.sql.SQLException;

/**
 * A factory for turning proxy network objects back into their full JDBC
 * form on the client.
 *
 * 一个工厂，用于在客户端上将代理网络对象转换回其完整的JDBC形式。
 *
 */
public interface ProxyFactory {

    public Object makeJdbcObject(Object proxy) throws SQLException;
}
