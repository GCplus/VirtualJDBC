// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet;

/**
 * Interface for manipulating the request headers of VJDBC-Http-Requests.
 * 用于操纵VJDBC Http Requests的请求标头的接口
 * @author Mike
 *
 */
public interface RequestModifier {
    /**
     * Adds a Request-Property with the same semantics like URLConnection.addRequestProperty
     * 添加具有与URLConnection.addRequestProperty相同语义的Request-Property
     * @param key Key of the Request-Property 请求属性的键
     * @param value Value of the Request-Property 请求属性的值
     */
    public abstract void addRequestHeader(String key, String value);
}