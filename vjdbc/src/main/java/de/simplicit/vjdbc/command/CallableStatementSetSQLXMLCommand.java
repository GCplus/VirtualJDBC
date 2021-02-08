// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLXML;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class CallableStatementSetSQLXMLCommand implements Command {
    static final long serialVersionUID = 7396654168665073844L;

    private int index;
    private String parameterName;
    private SQLXML sqlxml;

    public CallableStatementSetSQLXMLCommand() {
    }

    public CallableStatementSetSQLXMLCommand(int index, SQLXML sqlxml) {
        this.index = index;
        this.sqlxml = sqlxml;
    }

    public CallableStatementSetSQLXMLCommand(String paramName, SQLXML sqlxml) {
        this.parameterName = paramName;
        this.sqlxml = sqlxml;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(index);
        out.writeUTF(parameterName);
        out.writeObject(sqlxml);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        index = in.readInt();
        parameterName = in.readUTF();
        sqlxml = (SQLXML)in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        CallableStatement cstmt = (CallableStatement)target;
        if(parameterName != null) {
            cstmt.setSQLXML(parameterName, sqlxml);
        } else {
            cstmt.setSQLXML(index, sqlxml);
        }

        return null;
    }

    public String toString() {
        return "CallableStatementSetSQLXMLCommand";
    }
}