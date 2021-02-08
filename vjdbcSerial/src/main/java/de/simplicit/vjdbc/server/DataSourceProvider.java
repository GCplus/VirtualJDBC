// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * To use the DataSource-API with VJDBC a class must be provided
 * that implements the <code>DataSourceProvider</code> interface.
 * <p>要使用VJDBC的DataSource-API，必须提供一个实现DataSourceProvider接口的类。
 */
public interface DataSourceProvider {
    /**
     * Retrieves a DataSource object from the DataSourceProvider. This
     * will be used to create the JDBC connections.
     * <p>从DataSourceProvider检索DataSource对象。这将用于创建JDBC连接。
     * @return DataSource to be used for creating the connections
     * <p>用于创建连接的数据源
     */
    DataSource getDataSource() throws SQLException;
}
