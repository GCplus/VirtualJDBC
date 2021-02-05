// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IntegerParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 7906650418670821329L;

    private int value;

    public IntegerParameter() {
    }

    public IntegerParameter(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setInt(index, value);
    }

    public String toString() {
        return "int: " + value;
    }
}
