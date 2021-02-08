// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.command;

import de.simplicit.vjdbc.serial.CallingContext;

class JdbcObjectHolder {
    private final Object jdbcObject;
    private final CallingContext callingContext;
    private final int jdbcInterfaceType;

    JdbcObjectHolder(Object jdbcObject, CallingContext ctx, int jdbcInterfaceType) {
        this.jdbcObject = jdbcObject;
        this.callingContext = ctx;
        this.jdbcInterfaceType = jdbcInterfaceType;
    }

    CallingContext getCallingContext() {
        return this.callingContext;
    }

    Object getJdbcObject() {
        return this.jdbcObject;
    }

    int getJdbcInterfaceType() {
        return this.jdbcInterfaceType;
    }
}
