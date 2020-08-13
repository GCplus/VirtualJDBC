// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

/**
 * An interface for a JDBC object that can be reconstructed by the client from
 * a network proxy.  The client must implement a ProxyFactory that can take
 * the proxied object and turn it back into the proper client side JDBC object.
 *
 * 客户端可以从网络代理重构的JDBC对象的接口。客户端必须实现一个ProxyFactory，该ProxyFactory可以接受代理的对象并将其转换回适当的客户端JDBC对象。
 *
 */
public interface ProxiedObject extends Registerable {

    /**
     * The object to be serialized and transported via the command sink.
     * The returned value must implement Serializable or Externalizable
     *
     * 要通过命令接收器序列化和传输的对象。返回值必须实现Serializable或Externalizable
     *
     */
    public Object getProxy();
}
