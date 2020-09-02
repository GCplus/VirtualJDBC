// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import de.simplicit.vjdbc.serial.SerialRowId;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.RowId;
import java.sql.SQLException;

public class RowIdParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 8647675527971168478L;

    private SerialRowId value;

    public RowIdParameter() {
    }

    public RowIdParameter(RowId value) {
        this.value = new SerialRowId(value);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (SerialRowId)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setRowId(index, value);
    }

    public String toString() {
        return "RowId: " + value;
    }
}
