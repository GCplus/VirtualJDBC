// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import de.simplicit.vjdbc.serial.SerialRef;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.SQLException;

public class RefParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 8647675527971168478L;

    private SerialRef value;

    public RefParameter() {
    }

    public RefParameter(Ref value) throws SQLException {
        this.value = new SerialRef(value);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (SerialRef)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setRef(index, value);
    }

    public String toString() {
        return "Ref: " + value;
    }
}
