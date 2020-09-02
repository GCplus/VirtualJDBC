// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.SerializableTransport;
import de.simplicit.vjdbc.VirtualCallableStatement;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class CallableStatementGetObjectCommand implements Command {
    static final long serialVersionUID = 7045834396073252820L;

    private int index;
    private String parameterName;
    private Map map;
    private Class clazz;

    public CallableStatementGetObjectCommand() {
    }

    public CallableStatementGetObjectCommand(int index) {
        this.index = index;
        this.map = null;
        this.clazz = null;
    }

    public CallableStatementGetObjectCommand(int index, Class clazz) {
        this.index = index;
        this.map = null;
        this.clazz = clazz;
    }

    public CallableStatementGetObjectCommand(int index, Map map) {
        this.index = index;
        this.map = map;
        this.clazz = null;
    }

    public CallableStatementGetObjectCommand(String paramName) {
        this.parameterName = paramName;
        this.map = null;
        this.clazz = null;
    }

    public CallableStatementGetObjectCommand(String paramName, Class clazz) {
        this.parameterName = paramName;
        this.map = null;
        this.clazz = clazz;
    }

    public CallableStatementGetObjectCommand(String paramName, Map map) {
        this.parameterName = paramName;
        this.map = map;
        this.clazz = null;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(index);
        out.writeObject(parameterName);
        out.writeObject(map);

        out.writeBoolean(clazz != null);
        if (clazz != null) out.writeUTF(clazz.getName());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        index = in.readInt();
        parameterName = (String)in.readObject();
        map = (Map)in.readObject();
        if (in.readBoolean())
            clazz = Class.forName(in.readUTF());
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        CallableStatement cstmt = (CallableStatement)target;
        Object result;

        if(parameterName != null) {
            if(map != null) {
                result = cstmt.getObject(parameterName, map);
            } else if (clazz != null) {
                result =
                    cstmt.getObject(parameterName, clazz);
            } else {
                result = cstmt.getObject(parameterName);
            }
        } else {
            if(map != null) {
                result = cstmt.getObject(index, map);
            } else if (clazz != null) {
                result = cstmt.getObject(index, clazz);
            } else {
                result = cstmt.getObject(index);
            }
        }

        // ResultSets are handled by the caller
        if(result instanceof ResultSet) {
            return result;
        }

        // Any other type must be Serializable to be transported
        if(result == null || result instanceof Serializable) {
            return new SerializableTransport(result, ctx.getCompressionMode(), ctx.getCompressionThreshold());
        }

        throw new SQLException("Object of type " + result.getClass().getName() + " is not serializable");
    }

    public String toString() {
        return "CallableStatementGetObjectCommand";
    }
}
