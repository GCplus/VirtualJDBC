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

    private String _sql;
    private Integer _resultSetType;
    private Integer _resultSetConcurrency;
    private Integer _resultSetHoldability;

    public ConnectionPrepareStatementCommand() {
    }

    public ConnectionPrepareStatementCommand(String sql) {
        _sql = sql;
    }

    public ConnectionPrepareStatementCommand(String sql, int resultSetType, int resultSetConcurrency) {
        _sql = sql;
        _resultSetType = resultSetType;
        _resultSetConcurrency = resultSetConcurrency;
    }

    public ConnectionPrepareStatementCommand(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        _sql = sql;
        _resultSetType = resultSetType;
        _resultSetConcurrency = resultSetConcurrency;
        _resultSetHoldability = resultSetHoldability;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(_sql);
        out.writeObject(_resultSetType);
        out.writeObject(_resultSetConcurrency);
        out.writeObject(_resultSetHoldability);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _sql = in.readUTF();
        _resultSetType = (Integer)in.readObject();
        _resultSetConcurrency = (Integer)in.readObject();
        _resultSetHoldability = (Integer)in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        // Resolve and check the query
        String sql = ctx.resolveOrCheckQuery(_sql);
        // Now choose the correct call
        if(_resultSetType != null && _resultSetConcurrency != null) {
            if(_resultSetHoldability != null) {
                return ((Connection) target).prepareStatement(sql, _resultSetType, _resultSetConcurrency, _resultSetHoldability);
            }
            else {
                return ((Connection) target).prepareStatement(sql, _resultSetType, _resultSetConcurrency);
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
