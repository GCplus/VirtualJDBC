// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet;

import java.net.URLConnection;


/**
 * The HttpRequestModifier lets an external entity partly change the Http-Request
 * that is made by VJDBC in Servlet-Mode. To prevent abuse actually only one method
 * is delegated to the URLConnection.
 * HttpRequestModifier使外部实体可以部分更改由VJDBC在Servlet模式下进行的Http-Request。
 * 为了防止滥用，实际上只有一种方法委托给URLConnection。
 * @author Mike
 *
 */
final class RequestModifierJdk implements RequestModifier {
    private final URLConnection urlConnection;
    
    /**
     * Package visibility, doesn't make sense for other packages.
     * 包可见性，对于其他包没有意义。
     * @param urlConnection Wrapped URLConnection
     */
    RequestModifierJdk(URLConnection urlConnection) {
        this.urlConnection = urlConnection;
    }
    
    /**
     * (non-Javadoc)
     * 添加请求头的键值对
     * @see de.simplicit.vjdbc.servlet.RequestModifier#addRequestProperty(java.lang.String, java.lang.String)
     */
    public void addRequestHeader(String key, String value) {
        this.urlConnection.addRequestProperty(key, value);
    }
}
