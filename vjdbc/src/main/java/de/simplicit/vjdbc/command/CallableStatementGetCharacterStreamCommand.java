// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.SerializableTransport;
import de.simplicit.vjdbc.serial.StreamSerializer;

import java.io.*;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class CallableStatementGetCharacterStreamCommand implements Command {
    static final long serialVersionUID = 3594832624574651235L;

    private int index;
    private String parameterName;

    public CallableStatementGetCharacterStreamCommand() {
    }

    public CallableStatementGetCharacterStreamCommand(int index) {
        this.index = index;
    }

    public CallableStatementGetCharacterStreamCommand(String paramName) {
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
            result = cstmt.getCharacterStream(parameterName);
        } else {
            result = cstmt.getCharacterStream(index);
        }
        try {
            // read reader and return as a char[]
            // 读取reader并作为char[]返回
            return new SerializableTransport(StreamSerializer.toCharArray(result), ctx.getCompressionMode(), ctx.getCompressionThreshold());
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    public String toString() {
        return "CallableStatementGetCharacterStreamCommand";
    }
}
