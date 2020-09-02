// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ObjectParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -9065375715201787003L;

    private Object value;
    private Integer targetSqlType;
    private Integer scale;
    
    public ObjectParameter() {
    }

    public ObjectParameter(Object value, Integer targetSqlType, Integer scale) {
        this.value = value;
        this.targetSqlType = targetSqlType;
        this.scale = scale;
    }
    
    public Object getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readObject();
        targetSqlType = (Integer)in.readObject();
        scale = (Integer)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
        out.writeObject(targetSqlType);
        out.writeObject(scale);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        if(scale == null) {
            if(targetSqlType == null) {
                pstmt.setObject(index, value);
            } else {
                pstmt.setObject(index, value, targetSqlType);
            }
        } else {
            pstmt.setObject(index, value, targetSqlType, scale);
        }
    }

    public String toString() {
        return "Object: " + value;
    }
}
