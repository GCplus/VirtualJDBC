// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPrepareCallCommand implements Command {
    private static final long serialVersionUID = 3258125843279655728L;

    private String sql;
    private Integer resultSetType;
    private Integer resultSetConcurrency;
    private Integer resultSetHoldability;

    public ConnectionPrepareCallCommand() {
    }

    public ConnectionPrepareCallCommand(String sql) {
        this.sql = sql;
    }

    public ConnectionPrepareCallCommand(String sql, int resultSetType, int resultSetConcurrency) {
        this.sql = sql;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
    }

    public ConnectionPrepareCallCommand(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
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
        // 解决并检查查询
        String sql = ctx.resolveOrCheckQuery(this.sql);
        // Switch to the correct call
        // 切换到正确的调用
        if(resultSetType != null && resultSetConcurrency != null) {
            if(resultSetHoldability != null) {
                return ((Connection) target).prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            }
            else {
                return ((Connection) target).prepareCall(sql, resultSetType, resultSetConcurrency);
            }
        }
        else {
            return ((Connection) target).prepareCall(sql);
        }
    }

    public String toString() {
        return "ConnectionPrepareStatementCommand";
    }
}