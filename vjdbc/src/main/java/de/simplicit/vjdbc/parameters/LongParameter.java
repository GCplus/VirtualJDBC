// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class  LongParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 2047115344356276027L;

    private long value;

    public LongParameter() {
    }

    public LongParameter(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readLong();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setLong(index, value);
    }

    public String toString() {
        return "long: " + value;
    }
}
