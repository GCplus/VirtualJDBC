// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.StreamSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.NClob;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class CallableStatementSetNClobCommand implements Command {
    static final long serialVersionUID = 4264932633701227941L;

    private int index;
    private String parameterName;
    private NClob clob;

    public CallableStatementSetNClobCommand() {
    }

    public CallableStatementSetNClobCommand(int index, NClob clob) throws IOException {
        this.index = index;
        this.clob = clob;
    }

    public CallableStatementSetNClobCommand(String paramName, NClob clob) throws IOException {
        this.parameterName = paramName;
        this.clob = clob;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(index);
        out.writeUTF(parameterName);
        out.writeObject(clob);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        index = in.readInt();
        parameterName = in.readUTF();
        clob = (NClob)in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        CallableStatement cstmt = (CallableStatement)target;
        if(parameterName != null) {
            cstmt.setNClob(parameterName, clob);
        } else {
            cstmt.setNClob(index, clob);
        }

        return null;
    }

    public String toString() {
        return "CallableStatementSetNClobCommand";
    }
}
