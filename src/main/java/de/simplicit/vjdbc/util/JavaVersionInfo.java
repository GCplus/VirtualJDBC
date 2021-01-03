// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.util;

/**
 * Helper class which provides information about the used Java version
 * 提供有关使用的Java版本信息的Helper类
 */
public class JavaVersionInfo {
    public static final boolean use16Api = System.getProperty("java.specification.version").compareTo("1.6") >= 0;
}
