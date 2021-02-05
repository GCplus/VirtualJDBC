// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StringParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -8131525145406357230L;

    private String value;

    public StringParameter() {
    }

    public StringParameter(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (String)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setString(index, value);
    }

    public String toString() {
        return "String: " + value;
    }
}
