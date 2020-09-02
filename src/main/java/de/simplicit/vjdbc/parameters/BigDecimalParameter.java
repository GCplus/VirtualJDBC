// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BigDecimalParameter implements PreparedStatementParameter {
    static final long serialVersionUID = -8577950851500487084L;

    private BigDecimal value;

    public BigDecimalParameter() {
    }
    
    public BigDecimalParameter(BigDecimal value) {
        this.value = value;
    }
    
    public BigDecimal getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        value = (BigDecimal)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }    

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        pstmt.setBigDecimal(index, value);
    }

    public String toString() {
        return "BigDecimal: " + value.toString();
    }
}
