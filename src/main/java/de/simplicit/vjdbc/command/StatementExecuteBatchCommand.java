// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementExecuteBatchCommand implements Command {
    static final long serialVersionUID = -995205757280796006L;

    private String[] _sql;

    public StatementExecuteBatchCommand() {
    }

    public StatementExecuteBatchCommand(String[] sql) {
        _sql = sql;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(_sql);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _sql = (String[])in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        Statement stmt = (Statement)target;
        stmt.clearBatch();
        for (String s : _sql) {
            stmt.addBatch(ctx.resolveOrCheckQuery(s));
        }
        return stmt.executeBatch();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : _sql) {
            sb.append(s);
            sb.append('\n');
        }
        return "StatementExecuteBatchCommand:\n" + sb.toString();
    }
}
