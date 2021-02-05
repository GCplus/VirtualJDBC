// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class CallableStatementSetBlobCommand implements Command {
    static final long serialVersionUID = 4264932633701227941L;

    private int index;
    private String parameterName;
    private Blob clob;

    public CallableStatementSetBlobCommand() {
    }

    public CallableStatementSetBlobCommand(int index, Blob clob) throws IOException {
        this.index = index;
        this.clob = clob;
    }

    public CallableStatementSetBlobCommand(String paramName, Blob clob) throws IOException {
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
        clob = (Blob)in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        CallableStatement cstmt = (CallableStatement)target;
        if(parameterName != null) {
            cstmt.setBlob(parameterName, clob);
        } else {
            cstmt.setBlob(index, clob);
        }

        return null;
    }

    public String toString() {
        return "CallableStatementSetBlobCommand";
    }
}
