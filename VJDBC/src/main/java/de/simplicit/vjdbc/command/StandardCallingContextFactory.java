// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.CallingContext;

/**
 * This class produces standard Calling-Contexts which contain the callstack of the
 * executing command.
 * <p>此类产生标准的Calling-Context，其中包含执行命令的调用堆栈。
 */
public class StandardCallingContextFactory implements CallingContextFactory {
    public CallingContext create() {
        return new CallingContext();
    }
}
