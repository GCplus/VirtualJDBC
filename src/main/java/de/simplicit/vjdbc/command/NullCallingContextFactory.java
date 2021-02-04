// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.CallingContext;

/**
 * Dummy class which is doesn't create CallingContexts but returns null.
 * <p>虚拟类，它不会创建CallingContexts，但返回null。
 */
public class NullCallingContextFactory implements CallingContextFactory {
    public CallingContext create() {
        return null;
    }
}
