// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

public class DigesterRmiConfiguration extends RmiConfiguration {
    public void setCreateRegistry(String createRegistry) {
        this.createRegistry = ConfigurationUtil.getBooleanFromString(createRegistry);
    }
}
