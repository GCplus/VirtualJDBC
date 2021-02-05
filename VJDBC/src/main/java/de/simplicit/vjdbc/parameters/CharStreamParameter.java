// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import de.simplicit.vjdbc.util.SQLExceptionHelper;

public class CharStreamParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -3934051486806729706L;

    private char[] value;

    public CharStreamParameter() {
    }

    public CharStreamParameter(Reader x) throws SQLException {
        try {
            value = de.simplicit.vjdbc.serial.StreamSerializer.toCharArray(x);
        } catch(IOException e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public CharStreamParameter(Reader x, long length) throws SQLException {
        try {
            value = de.simplicit.vjdbc.serial.StreamSerializer.toCharArray(x, length);
        } catch(IOException e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public char[] getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (char[])in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setCharacterStream(index, new CharArrayReader(value), value.length);
    }

    public String toString() {
        return "CharStream: " + value.length + " chars";
    }
}
