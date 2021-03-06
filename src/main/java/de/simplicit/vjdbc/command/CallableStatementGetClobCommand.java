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

    private int _index;
    private String _parameterName;

    public CallableStatementGetClobCommand() {
    }

    public CallableStatementGetClobCommand(int index) {
        _index = index;
    }

    public CallableStatementGetClobCommand(String paramName) {
        _parameterName = paramName;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(_index);
        out.writeObject(_parameterName);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _index = in.readInt();
        _parameterName = (String)in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        CallableStatement cstmt = (CallableStatement)target;
        Clob result;
        if(_parameterName != null) {
            result = cstmt.getClob(_parameterName);
        } else {
            result = cstmt.getClob(_index);
        }
        return new SerializableTransport(new SerialClob(result), ctx.getCompressionMode(), ctx.getCompressionThreshold());
    }

    public String toString() {
        return "CallableStatementGetClobCommand";
    }
}
