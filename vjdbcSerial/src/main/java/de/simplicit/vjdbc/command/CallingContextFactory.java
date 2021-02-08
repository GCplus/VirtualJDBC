// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.CallingContext;

/**
 * A CallingContextFactory creates CallingContext objects.
 * <p>CallingContextFactory创建CallingContext对象
 * @author Mike
 */
public interface CallingContextFactory {
    CallingContext create();
}
