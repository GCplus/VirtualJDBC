// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.SerializableTransport;
import de.simplicit.vjdbc.serial.StreamSerializer;

import java.io.*;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class CallableStatementGetNCharacterStreamCommand implements Command {
    static final long serialVersionUID = -8218845136435435097L;

    private int index;
    private String parameterName;

    public CallableStatementGetNCharacterStreamCommand() {
    }

    public CallableStatementGetNCharacterStreamCommand(int index) {
        this.index = index;
    }

    public CallableStatementGetNCharacterStreamCommand(String paramName) {
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
        Reader result;
        if(parameterName != null) {
            result = cstmt.getNCharacterStream(parameterName);
        } else {
            result = cstmt.getNCharacterStream(index);
        }
        try {
            // read reader and return as a char[]
            return new SerializableTransport(StreamSerializer.toCharArray(result), ctx.getCompressionMode(), ctx.getCompressionThreshold());
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    public String toString() {
        return "CallableStatementGetNCharacterStreamCommand";
    }
}
