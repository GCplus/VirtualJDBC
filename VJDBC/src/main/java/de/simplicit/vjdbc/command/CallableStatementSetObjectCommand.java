// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.SerializableTransport;
import de.simplicit.vjdbc.util.SQLExceptionHelper;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class CallableStatementSetObjectCommand implements Command {
    static final long serialVersionUID = -9132697894345849726L;

    private int index;
    private String paramName;
    private Integer targetSqlType;
    private Integer scale;
    private SerializableTransport transport;

    public CallableStatementSetObjectCommand() {
    }

    public CallableStatementSetObjectCommand(int index, Integer targetSqlType, Integer scale) {
        this.index = index;
        this.targetSqlType = targetSqlType;
        this.scale = scale;
        this.transport = null;
    }

    public CallableStatementSetObjectCommand(String paramName, Integer targetSqlType, Integer scale) {
        this.paramName = paramName;
        this.targetSqlType = targetSqlType;
        this.scale = scale;
        this.transport = null;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(index);
        out.writeObject(paramName);
        out.writeObject(targetSqlType);
        out.writeObject(scale);
        out.writeObject(transport);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        index = in.readInt();
        paramName = (String)in.readObject();
        targetSqlType = (Integer)in.readObject();
        scale = (Integer)in.readObject();
        transport = (SerializableTransport)in.readObject();
    }

    public void setObject(Object obj) throws SQLException {
        if(obj instanceof Serializable) {
            transport = new SerializableTransport(obj);
        } else {
            throw new SQLException("Object of type " + obj.getClass().getName() + " is not serializable");
        }
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        CallableStatement cstmt = (CallableStatement)target;

        Object obj;
        try {
            obj = transport.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }

        if(paramName != null) {
            if(targetSqlType != null) {
                if(scale != null) {
                    cstmt.setObject(paramName, obj, targetSqlType, scale);
                } else {
                    cstmt.setObject(paramName, obj, targetSqlType);
                }
            } else {
                cstmt.setObject(paramName, obj);
            }
        } else {
            if(targetSqlType != null) {
                if(scale != null) {
                    cstmt.setObject(index, obj, targetSqlType, scale);
                } else {
                    cstmt.setObject(index, obj, targetSqlType);
                }
            } else {
                cstmt.setObject(index, obj);
            }
        }

        return null;
    }

    public String toString() {
        return "CallableStatementSetObjectCommand";
    }
}
