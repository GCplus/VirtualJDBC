// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NullParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 2061806736191837513L;

    private int sqlType;
    private String typeName;
    
    public NullParameter() {
    }

    public NullParameter(int sqltype, String typename) {
        this.sqlType = sqltype;
        this.typeName = typename;
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        sqlType = in.readInt();
        typeName = (String)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(sqlType);
        out.writeObject(typeName);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        if(typeName == null) {
            pstmt.setNull(index, sqlType);
        } else {
            pstmt.setNull(index, sqlType, typeName);
        }
    }

    public String toString() {
        return "Null, SQL-Type: " + sqlType;
    }
}
