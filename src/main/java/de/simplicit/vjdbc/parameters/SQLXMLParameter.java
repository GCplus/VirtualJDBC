// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import de.simplicit.vjdbc.serial.SerialSQLXML;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLXML;
import java.sql.SQLException;

public class SQLXMLParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 8647675527971168478L;

    private SerialSQLXML value;

    public SQLXMLParameter() {
    }

    public SQLXMLParameter(SQLXML value) throws SQLException {
        this.value = new SerialSQLXML(value);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (SerialSQLXML)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setSQLXML(index, value);
    }

    public String toString() {
        return "SQLXML: " + value.getString();
    }
}
