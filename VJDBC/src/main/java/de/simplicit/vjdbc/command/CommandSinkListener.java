// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

/**
 * Interface for objects which are interested what is happening in the command sink.
 * 对象接口, 很关心在command sink中正在发生的事情
 */
public interface CommandSinkListener {
    void preExecution(Command cmd);

    void postExecution(Command cmd);
}
