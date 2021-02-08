// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import de.simplicit.vjdbc.serial.SerialArray;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArrayParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 82417815012404533L;

    private SerialArray value;

    public ArrayParameter() {
    }

    public ArrayParameter(Array value) throws SQLException {
        this.value = new SerialArray(value);
    }

    public SerialArray getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (SerialArray)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setArray(index, value);
    }

    public String toString() {
        return "Array: " + value;
    }
}