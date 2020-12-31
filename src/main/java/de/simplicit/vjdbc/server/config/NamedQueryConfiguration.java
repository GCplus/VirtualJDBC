// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 命名查询配置
 */
public class NamedQueryConfiguration {
    private static final Log logger = LogFactory.getLog(NamedQueryConfiguration.class);
    private final Map queryMap = new HashMap();

    /**
     * 获取查询语句集
     * @return 存储的查询语句集(Map类型)
     */
    public Map getQueryMap() {
        return queryMap;
    }

    /**
     * 添加一个查询实体
     * @param id 查询语句编码
     * @param sql 查询用的sql语句
     */
    public void addEntry(String id, String sql) {
        queryMap.put(id, sql);
    }

    /**
     * 根据id查询sql语句
     * @param id 查询语句编码
     * @return sql语句(String类型)
     * @throws SQLException 抛出sql异常
     */
    public String getSqlForId(String id) throws SQLException {
        String result = String.valueOf(queryMap.get(id));
        if(result != null) {
            return result;
        }
        else {
            String msg = "Named-Query for key '" + id + "' not found";
            logger.error(msg);
            throw new SQLException(msg);
        }
    }

    void log() {
        logger.info("  Named Query-Configuration:");

        for (Object o : queryMap.keySet()) {
            String id = (String) o;
            logger.info("    [" + id + "] = [" + queryMap.get(id) + "]");
        }
    }
}
