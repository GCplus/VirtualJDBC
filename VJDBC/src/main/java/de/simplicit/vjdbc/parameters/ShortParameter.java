// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ShortParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 5384886497454301576L;

    private short value;

    public ShortParameter() {
    }

    public ShortParameter(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readShort();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setShort(index, value);
    }

    public String toString() {
        return "short: " + value;
    }
}
