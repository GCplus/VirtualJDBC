// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NamedQueryConfiguration {
    private static final Log logger = LogFactory.getLog(NamedQueryConfiguration.class);
    private final Map queryMap = new HashMap();

    public Map getQueryMap() {
        return queryMap;
    }

    public void addEntry(String id, String sql) {
        queryMap.put(id, sql);
    }

    public String getSqlForId(String id) throws SQLException {
        String result = (String)queryMap.get(id);
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
