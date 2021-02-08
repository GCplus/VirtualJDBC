// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

/**
 * Potential pool implementation for creating ReflectiveCommand-Objects. Yet
 * only a dummy implementation.
 * <p>用于创建ReflectiveCommand-Objects的动态池实现。 但是只有一个虚拟的实现
 */
public class CommandPool {
    public static Command getReflectiveCommand(int interfaceType, String cmdstr) {
        return new ReflectiveCommand(interfaceType, cmdstr);
    }

    public static Command getReflectiveCommand(int interfaceType, String cmdstr, Object[] parms, int types) {
        return new ReflectiveCommand(interfaceType, cmdstr, parms, types);
    }
}
