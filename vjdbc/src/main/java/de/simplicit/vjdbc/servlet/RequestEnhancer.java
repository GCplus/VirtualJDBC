// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet;

/**
 * The RequestEnhancer interface must be implemented by classes that want to enhance
 * the Http-Requests that VJDBC sends to the VJDBC-Servlet. This way it's possible to
 * send connection specific data like authentication cookies.
 * RequestEnhancer接口必须由想要增强VJDBC发送到VJDBC-Servlet的Http请求的类实现。 这样，可以发送特定于连接的数据，例如身份验证Cookie。
 * @author Mike
 */
public interface RequestEnhancer {
    /**
     * Called before the initial connect request of VJDBC is sent.
     * 在发送VJDBC的初始连接请求之前调用。
     * @param requestModifier 修改后的请求头
     */
    void enhanceConnectRequest(RequestModifier requestModifier);

    /**
     * Called before each processing request of VJDBC.
     * 在VJDBC的每个请求处理之前调用。
     * @param requestModifier 修改后的请求头
     */
    void enhanceProcessRequest(RequestModifier requestModifier);
}
