// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.SerialRef;
import de.simplicit.vjdbc.serial.SerializableTransport;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.CallableStatement;
import java.sql.Ref;
import java.sql.SQLException;

public class CallableStatementGetRefCommand implements Command {
    static final long serialVersionUID = 6253579473434177231L;

    private int index;
    private String parameterName;

    public CallableStatementGetRefCommand() {
    }

    public CallableStatementGetRefCommand(int index) {
        this.index = index;
        this.parameterName = null;
    }

    public CallableStatementGetRefCommand(String paramName) {
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
        Ref result;
        if(parameterName != null) {
            result = cstmt.getRef(parameterName);
        } else {
            result = cstmt.getRef(index);
        }
        return new SerializableTransport(new SerialRef(result), ctx.getCompressionMode(), ctx.getCompressionThreshold());
    }

    public String toString() {
        return "CallableStatementGetRefCommand";
    }
}
