// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BooleanParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 1915488329736405680L;

    private boolean value;
    
    public BooleanParameter() {
    }

    public BooleanParameter(boolean value) {
        this.value = value;
    }
    
    public boolean getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readBoolean();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setBoolean(index, value);
    }

    public String toString() {
        return "boolean: " + (value ? "true" : "false");
    }
}
