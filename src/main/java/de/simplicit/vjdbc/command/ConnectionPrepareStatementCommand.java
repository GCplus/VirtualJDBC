// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPrepareStatementCommand implements Command {
    private static final long serialVersionUID = 3905239013827949875L;

    private String sql;
    private Integer resultSetType;
    private Integer resultSetConcurrency;
    private Integer resultSetHoldability;

    public ConnectionPrepareStatementCommand() {
    }

    public ConnectionPrepareStatementCommand(String sql) {
        this.sql = sql;
    }

    public ConnectionPrepareStatementCommand(String sql, int resultSetType, int resultSetConcurrency) {
        this.sql = sql;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
    }

    public ConnectionPrepareStatementCommand(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        this.sql = sql;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(sql);
        out.writeObject(resultSetType);
        out.writeObject(resultSetConcurrency);
        out.writeObject(resultSetHoldability);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        sql = in.readUTF();
        resultSetType = (Integer)in.readObject();
        resultSetConcurrency = (Integer)in.readObject();
        resultSetHoldability = (Integer)in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        // Resolve and check the query
        String sql = ctx.resolveOrCheckQuery(this.sql);
        // Now choose the correct call
        if(resultSetType != null && resultSetConcurrency != null) {
            if(resultSetHoldability != null) {
                return ((Connection) target).prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            }
            else {
                return ((Connection) target).prepareStatement(sql, resultSetType, resultSetConcurrency);
            }
        }
        else {
            return ((Connection) target).prepareStatement(sql);
        }
    }

    public String toString() {
        return "ConnectionPrepareStatementCommand";
    }
}
