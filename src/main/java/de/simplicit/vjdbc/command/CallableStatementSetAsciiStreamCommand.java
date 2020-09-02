// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.StreamSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class CallableStatementSetAsciiStreamCommand implements Command {
    static final long serialVersionUID = -6772875360380241530L;

    private int index;
    private int length;
    private String parameterName;
    private byte[] byteArray;

    public CallableStatementSetAsciiStreamCommand() {
    }

    public CallableStatementSetAsciiStreamCommand(int index, InputStream is) throws IOException {
        this.index = index;
        this.byteArray = StreamSerializer.toByteArray(is);
        this.length = byteArray.length;
    }

    public CallableStatementSetAsciiStreamCommand(String paramName, InputStream is) throws IOException {
        this.parameterName = paramName;
        this.byteArray = StreamSerializer.toByteArray(is);
        this.length = byteArray.length;
    }

    public CallableStatementSetAsciiStreamCommand(int index, InputStream is, int len) throws IOException {
        this.index = index;
        this.length = len;
        this.byteArray = StreamSerializer.toByteArray(is);
    }

    public CallableStatementSetAsciiStreamCommand(String paramName, InputStream is, int len) throws IOException {
        this.parameterName = paramName;
        this.length = len;
        this.byteArray = StreamSerializer.toByteArray(is);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(index);
        out.writeInt(length);
        out.writeObject(parameterName);
        out.writeObject(byteArray);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        index = in.readInt();
        length = in.readInt();
        parameterName = (String)in.readObject();
        byteArray = (byte[])in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        CallableStatement cstmt = (CallableStatement)target;
        InputStream is = StreamSerializer.toInputStream(byteArray);
        if(parameterName != null) {
            cstmt.setAsciiStream(parameterName, is, length);
        } else {
            cstmt.setAsciiStream(index, is, length);
        }

        return null;
    }

    public String toString() {
        return "CallableStatementSetAsciiStreamCommand";
    }
}
