// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

public class ConnectionReleaseSavepointCommand implements Command {
    static final long serialVersionUID = 6221665321426908025L;

    private Long uidOfSavepoint;

    public ConnectionReleaseSavepointCommand() {
    }

    public ConnectionReleaseSavepointCommand(Long uidOfSavepoint) {
        this.uidOfSavepoint = uidOfSavepoint;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(uidOfSavepoint);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        uidOfSavepoint = in.readLong();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        Savepoint sp = (Savepoint)ctx.getJDBCObject(uidOfSavepoint);
        ((Connection)target).releaseSavepoint(sp);
        return null;
    }

    public String toString() {
        return "ConnectionReleaseSavepointCommand";
    }
}
