// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.SerialBlob;
import de.simplicit.vjdbc.serial.SerializableTransport;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class CallableStatementGetBlobCommand implements Command {
    static final long serialVersionUID = -2976001646644624286L;

    private int index;
    private String parameterName;

    public CallableStatementGetBlobCommand() {
    }

    public CallableStatementGetBlobCommand(int index) {
        this.index = index;
    }

    public CallableStatementGetBlobCommand(String paramName) {
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
        Blob result;
        if(parameterName != null) {
            result = cstmt.getBlob(parameterName);
        } else {
            result = cstmt.getBlob(index);
        }
        return new SerializableTransport(new SerialBlob(result), ctx.getCompressionMode(), ctx.getCompressionThreshold());
    }

    public String toString() {
        return "CallableStatementGetBlobCommand";
    }
}
