// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import de.simplicit.vjdbc.serial.SerialClob;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClobParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -8231456859022053216L;

    private SerialClob value;

    public ClobParameter() {
    }
    
    public ClobParameter(Clob value) throws SQLException {
        this.value = new SerialClob(value);
    }
    
    public SerialClob getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (SerialClob)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setClob(index, value);
    }

    public String toString() {
        return "Clob: " + value;
    }
}
