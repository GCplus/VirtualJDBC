// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet.jakarta;

import org.apache.commons.httpclient.methods.PostMethod;

import de.simplicit.vjdbc.servlet.RequestModifier;

/**
 * The RequestModifierHttpClient lets an external entity partly change the Http-Request
 * that is made by VJDBC in Servlet-Mode. To prevent abuse actually only one method
 * is delegated to the URLConnection.
 * 通过RequestModifierHttpClient，外部实体可以部分更改由VJDBC在Servlet模式下创建的Http-Request。
 * 为了防止滥用，实际上只有一种方法委托给URLConnection。
 * @author Mike
 *
 */
final class RequestModifierJakartaHttpClient implements RequestModifier {
    private final PostMethod postMethod; //引入Apache HttpClient组件的请求类型

    /**
     * Package visibility, doesn't make sense for other packages.
     * 包可见性，对于其他包没有意义。
     * @param {urlConnection} Wrapped URLConnection
     */
    RequestModifierJakartaHttpClient(PostMethod postMethod) {
        this.postMethod = postMethod;
    }

    /**
     * (non-Javadoc)
     * 添加请求头的键值对
     * @see de.simplicit.vjdbc.servlet.RequestModifier#addRequestProperty(java.lang.String, java.lang.String)
     */
    public void addRequestHeader(String key, String value) {
        postMethod.addRequestHeader(key, value);
    }
}
