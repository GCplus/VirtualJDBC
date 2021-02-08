// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet;

/**
 * Common identifiers which are used in the Http-Header to route the requests
 * to the corresponding handler.
 * Http标头中用于将请求路由到相应处理程序的通用标识符。
 * @author Mike
 *
 */
public interface ServletCommandSinkIdentifier {
    String METHOD_IDENTIFIER = "vjdbc-method";
    String CONNECT_COMMAND = "connect";
    String PROCESS_COMMAND = "process";
}
