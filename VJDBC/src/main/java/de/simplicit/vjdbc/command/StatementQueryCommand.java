// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementQueryCommand implements Command, ResultSetProducerCommand {
    static final long serialVersionUID = -8463588846424302034L;

    private int resultSetType;
    private String sql;

    public StatementQueryCommand() {
    }

    public StatementQueryCommand(String sql, int resultSetType) {
        this.sql = sql;
        this.resultSetType = resultSetType;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(resultSetType);
        out.writeUTF(sql);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        resultSetType = in.readInt();
        sql = in.readUTF();
    }

    public int getResultSetType() {
        return resultSetType;
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        return ((Statement) target).executeQuery(ctx.resolveOrCheckQuery(sql));
    }

    public String toString() {
        return "StatementQueryCommand: " + sql;
    }
}
