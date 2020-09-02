// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ByteArrayParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -850921577178865335L;

    private byte[] value;
    
    public ByteArrayParameter() {
    }

    public ByteArrayParameter(byte[] value) {
        this.value = value;
    }
    
    public byte[] getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (byte[])in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setBytes(index, value);
    }

    public String toString() {
        return "byte[]: " + value.length + " bytes";
    }
}
