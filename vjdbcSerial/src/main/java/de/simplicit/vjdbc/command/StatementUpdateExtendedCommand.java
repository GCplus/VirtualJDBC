// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementUpdateExtendedCommand implements Command {
    private static final long serialVersionUID = 3690198762949851445L;

    private String sql;
    private int autoGeneratedKeys;
    private int[] columnIndexes;
    private String[] columnNames;

    public StatementUpdateExtendedCommand() {
    }

    public StatementUpdateExtendedCommand(String sql, int autoGeneratedKeys) {
        this.sql = sql;
        this.autoGeneratedKeys = autoGeneratedKeys;
    }

    public StatementUpdateExtendedCommand(String sql, int[] columnIndexes) {
        this.sql = sql;
        this.columnIndexes = columnIndexes;
    }

    public StatementUpdateExtendedCommand(String sql, String[] columnNames) {
        this.sql = sql;
        this.columnNames = columnNames;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(sql);
        out.writeInt(autoGeneratedKeys);
        out.writeObject(columnIndexes);
        out.writeObject(columnNames);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        sql = in.readUTF();
        autoGeneratedKeys = in.readInt();
        columnIndexes = (int[])in.readObject();
        columnNames = (String[])in.readObject();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        String sql = ctx.resolveOrCheckQuery(this.sql);
        // Now make the descision what call to execute
        // 现在决定执行什么调用
        if(columnIndexes != null) {
            return ((Statement) target).executeUpdate(sql, columnIndexes);
        }
        else if(columnNames != null) {
            return ((Statement) target).executeUpdate(sql, columnNames);
        }
        else {
            return ((Statement) target).executeUpdate(sql, autoGeneratedKeys);
        }
    }

    public String toString() {
        return "StatementUpdateExtendedCommand: " + sql;
    }
}