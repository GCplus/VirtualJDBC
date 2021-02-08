// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.StreamSerializer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class CallableStatementSetCharacterStreamCommand implements Command {
    static final long serialVersionUID = 8952810867158345906L;

    private int index;
    private int length;
    private String parameterName;
    private char[] charArray;

    public CallableStatementSetCharacterStreamCommand() {
    }

    public CallableStatementSetCharacterStreamCommand(int index, Reader reader) throws IOException {
        this.index = index;
        this.charArray = StreamSerializer.toCharArray(reader);
        this.length = charArray.length;
    }

    public CallableStatementSetCharacterStreamCommand(String paramName, Reader reader) throws IOException {
        this.parameterName = paramName;
        this.charArray = StreamSerializer.toCharArray(reader);
        this.length = charArray.length;
    }

    public CallableStatementSetCharacterStreamCommand(int index, Reader reader, int len) throws IOException {
        this.index = index;
        this.length = len;
        this.charArray = StreamSerializer.toCharArray(reader, len);
    }

    public CallableStatementSetCharacterStreamCommand(String paramName, Reader reader, int len) throws IOException {
        this.parameterName = paramName;
        this.length = len;
        this.charArray = StreamSerializer.toCharArray(reader, len);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(index);
        out.writeInt(length);
        out.writeObject(parameterName);
        out.writeObject(charArray);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        index = in.readInt();
        length = in.readInt();
        parameterName = (String)in.readObject();
        charArray = (char[])in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        CallableStatement cstmt = (CallableStatement)target;
        Reader reader = StreamSerializer.toReader(charArray);
        if(parameterName != null) {
            cstmt.setCharacterStream(parameterName, reader, length);
        } else {
            cstmt.setCharacterStream(index, reader, length);
        }

        return null;
    }

    public String toString() {
        return "CallableStatementSetCharacterStreamCommand";
    }
}
