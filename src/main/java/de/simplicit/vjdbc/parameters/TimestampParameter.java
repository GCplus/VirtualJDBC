// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public class TimestampParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -3786979212713144035L;

    private Timestamp value;
    private Calendar calendar;
    
    public TimestampParameter() {
    }

    public TimestampParameter(Timestamp value, Calendar cal) {
        this.value = value;
        this.calendar = cal;
    }
    
    public Timestamp getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (Timestamp)in.readObject();
        calendar = (Calendar)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
        out.writeObject(calendar);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        if(calendar == null) {
            pstmt.setTimestamp(index, value);
        } else {
            pstmt.setTimestamp(index, value, calendar);
        }
    }

    public String toString() {
        return "Timestamp: " + value;
    }
}
