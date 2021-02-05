// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;

public class TimeParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -3833958578075965947L;

    private Time value;
    private Calendar calendar;

    public TimeParameter() {
    }

    public TimeParameter(Time value, Calendar cal) {
        this.value = value;
        this.calendar = cal;
    }

    public Time getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (Time)in.readObject();
        calendar = (Calendar)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
        out.writeObject(calendar);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        if(calendar == null) {
            pstmt.setTime(index, value);
        } else {
            pstmt.setTime(index, value, calendar);
        }
    }

    public String toString() {
        return "Time: " + value;
    }
}
