// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ByteParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -6844809323174032034L;

    private byte value;
    
    public ByteParameter() {
    }

    public ByteParameter(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readByte();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeByte(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setByte(index, value);
    }

    public String toString() {
        return "byte: " + value;
    }
}
