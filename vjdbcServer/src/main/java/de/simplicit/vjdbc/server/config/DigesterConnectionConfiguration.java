// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

public class DigesterConnectionConfiguration extends ConnectionConfiguration {
    public void setTraceCommandCount(String traceCommandCount) {
        this.traceCommandCount = ConfigurationUtil.getBooleanFromString(traceCommandCount);
    }

    public void setTraceOrphanedObjects(String traceOrphandedObjects) {
        traceOrphanedObjects = ConfigurationUtil.getBooleanFromString(traceOrphandedObjects);
    }

    public void setConnectionPooling(String connectionPooling) {
        this.connectionPooling = ConfigurationUtil.getBooleanFromString(connectionPooling);
    }
}
