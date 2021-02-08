// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class URLParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 4214386658417445307L;

    private URL value;

    public URLParameter() {
    }

    public URLParameter(URL value) {
        this.value = value;
    }

    public URL getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (URL)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setURL(index, value);
    }

    public String toString() {
        return "URL: " + value;
    }
}
