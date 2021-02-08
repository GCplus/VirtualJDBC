// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.SerialArray;
import de.simplicit.vjdbc.serial.SerializableTransport;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class CallableStatementGetArrayCommand implements Command {
    static final long serialVersionUID = 4247967467888689853L;

    private int index;
    private String parameterName;

    // No-Arg constructor for deserialization
    // 用于反序列化的No-Arg构造函数
    public CallableStatementGetArrayCommand() {
    }

    public CallableStatementGetArrayCommand(int index) {
        this.index = index;
    }

    public CallableStatementGetArrayCommand(String paramName) {
        this.parameterName = paramName;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(index);
        out.writeObject(parameterName);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        index = in.readInt();
        parameterName = (String)in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        CallableStatement cstmt = (CallableStatement)target;
        Array result;
        if(parameterName != null) {
            result = cstmt.getArray(parameterName);
        } else {
            result = cstmt.getArray(index);
        }
        return new SerializableTransport(new SerialArray(result), ctx.getCompressionMode(), ctx.getCompressionThreshold());
    }

    @Override
    public String toString() {
        return "CallableStatementGetArrayCommand";
    }
}