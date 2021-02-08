// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FloatParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -2273786408954216402L;

    private float value;

    public FloatParameter() {
    }

    public FloatParameter(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readFloat();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setFloat(index, value);
    }

    public String toString() {
        return "float: " + value;
    }
}
