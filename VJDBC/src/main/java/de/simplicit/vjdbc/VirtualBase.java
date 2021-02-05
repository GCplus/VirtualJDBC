// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

import de.simplicit.vjdbc.command.DecoratedCommandSink;
import de.simplicit.vjdbc.serial.UIDEx;

/**
 * Base class for all virtual JDBC-Classes. All VJDBC-Objects have a unique id and a
 * reference to a command sink which can process commands generated by the VJDBC object.
 *
 * 所有虚拟JDBC类的基类。 所有VJDBC对象都有唯一的ID和对命令接收器的引用，该命令接收器可以处理由VJDBC对象生成的命令。
 *
 */
public abstract class VirtualBase {
    protected UIDEx objectUid;
    protected DecoratedCommandSink sink;

    protected VirtualBase(UIDEx objectuid, DecoratedCommandSink sink) {
        this.objectUid = objectuid;
        this.sink = sink;
    }

    protected void finalize() throws Throwable {
        objectUid = null;
        sink = null;
    }

    /**
     * Get a registration ID
     *
     * 获取一个注册ID
     * @return objectUid
     */
    public UIDEx getObjectUID() {
        return objectUid;
    }
}
