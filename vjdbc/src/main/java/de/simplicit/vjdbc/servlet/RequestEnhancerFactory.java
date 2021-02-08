// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet;

/**
 * Interface that must be implemented by the classes that register themselves with the
 * VJdbcProperties.SERVLET_REQUEST_ENHANCER_FACTORY.
 * <p>必须由向VJdbcProperties.SERVLET_REQUEST_ENHANCER_FACTORY注册的类所实现的接口
 * @author Mike
 */
public interface RequestEnhancerFactory {
    /**
     * Factory method to create a RequestEnhancer object
     * <p>创建RequestEnhancer对象的工厂方法
     * @return Created RequestEnhancer <p>创建RequestEnhancer
     */
    RequestEnhancer create();
}
