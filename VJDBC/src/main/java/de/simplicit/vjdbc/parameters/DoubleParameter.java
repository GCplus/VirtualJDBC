// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DoubleParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -8304299062026994797L;

    private double value;

    public DoubleParameter() {
    }

    public DoubleParameter(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readDouble();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setDouble(index, value);
    }

    public String toString() {
        return "double: " + value;
    }
}
