// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

public class DateParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 5153278906714835319L;

    private Date value;
    private Calendar calendar;
    
    public DateParameter() {
    }

    public DateParameter(Date value, Calendar cal) {
        this.value = value;
        this.calendar = cal;
    }

    public Date getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (Date)in.readObject();
        calendar = (Calendar)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
        out.writeObject(calendar);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        if(calendar == null) {
            pstmt.setDate(index, value);
        } else {
            pstmt.setDate(index, value, calendar);
        }
    }

    public String toString() {
        return "Date: " + value;
    }
}
