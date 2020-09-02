// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementExecuteCommand implements Command {
    private static final long serialVersionUID = 3760844562717291058L;

    private String sql;

    public StatementExecuteCommand() {
    }

    public StatementExecuteCommand(String sql) {
        this.sql = sql;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(sql);
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        sql = in.readUTF();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        return ((Statement) target).execute(ctx.resolveOrCheckQuery(sql));
    }

    public String toString() {
        return "StatementExecuteCommand: " + sql;
    }
}
