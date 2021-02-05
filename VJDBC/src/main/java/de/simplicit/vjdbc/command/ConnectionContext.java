// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.sql.SQLException;

/**
 * This interface provides access to connection specific context for all commands
 * executed on the server.
 * 该接口提供对服务器上执行的所有命令的连接特定上下文的访问
 */
public interface ConnectionContext {
    // Accessor methods to all registered JDBC objects
    //所有已注册JDBC对象的访问器方法
    Object getJDBCObject(Long key);
    void addJDBCObject(Long key, Object partner);
    Object removeJDBCObject(Long key);
    // Compression
    // 压缩
    int getCompressionMode();
    long getCompressionThreshold();
    // Row-Packets
    // 行数据包
    int getRowPacketSize();
    String getCharset();
    // Resolve and check query
    // 检查并解决 查询语句
    String resolveOrCheckQuery(String sql) throws SQLException;
    // convenience method to remove all related JdbcObjects from this connection
    // 从此连接中删除所有相关JdbcObjects的便捷方法
    void closeAllRelatedJdbcObjects() throws SQLException;
}
