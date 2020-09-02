// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.SerialClob;
import de.simplicit.vjdbc.serial.SerializableTransport;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.SQLException;

public class CallableStatementGetClobCommand implements Command {
    static final long serialVersionUID = 8230491873823084213L;

    private int index;
    private String parameterName;

    public CallableStatementGetClobCommand() {
    }

    public CallableStatementGetClobCommand(int index) {
        this.index = index;
    }

    public CallableStatementGetClobCommand(String paramName) {
        this.parameterName = paramName;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.index);
        out.writeObject(this.parameterName);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        index = in.readInt();
        parameterName = (String)in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        CallableStatement cstmt = (CallableStatement)target;
        Clob result;
        if(parameterName != null) {
            result = cstmt.getClob(parameterName);
        } else {
            result = cstmt.getClob(index);
        }
        return new SerializableTransport(new SerialClob(result), ctx.getCompressionMode(), ctx.getCompressionThreshold());
    }

    public String toString() {
        return "CallableStatementGetClobCommand";
    }
}
