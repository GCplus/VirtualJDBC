// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.parameters.PreparedStatementParameter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementQueryCommand implements Command, ResultSetProducerCommand {
    static final long serialVersionUID = -7028150330288724130L;

    protected PreparedStatementParameter[] params;
    protected int resultSetType;

    public PreparedStatementQueryCommand() {
    }

    public PreparedStatementQueryCommand(PreparedStatementParameter[] params, int resultSetType) {
        this.params = params;
        this.resultSetType = resultSetType;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(resultSetType);
        out.writeObject(params);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        resultSetType = in.readInt();
        params = (PreparedStatementParameter[])in.readObject();
    }

    public int getResultSetType() {
        return resultSetType;
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement)target;
        for(int i = 0; i < params.length; i++) {
            params[i].setParameter(pstmt, i + 1);
        }
        return pstmt.executeQuery();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PreparedStatementQueryCommand");
        if(params != null && params.length > 0) {
            sb.append(" with parameters\n");
            for(int i = 0, n = params.length; i < n; i++) {
                sb.append("\t[").append(i + 1).append("] = ").append(params[i]);
                if(i < n - 1) {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
}
