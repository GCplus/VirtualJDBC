// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.Map;

public class SerialStruct implements Struct, Externalizable {
    private static final long serialVersionUID = 3256444694312792625L;

    private String sqlTypeName;
    private Object[] attributes;

    public SerialStruct() {
    }

    public SerialStruct(String typeName, Object[] attributes) {
        this.sqlTypeName = typeName;
        this.attributes = attributes;
    }

    public SerialStruct(Struct struct) throws SQLException {
        this.sqlTypeName = struct.getSQLTypeName();
        this.attributes = struct.getAttributes();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sqlTypeName);
        out.writeObject(attributes);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        sqlTypeName = (String)in.readObject();
        attributes = (Object[])in.readObject();
    }

    public String getSQLTypeName() {
        return sqlTypeName;
    }

    public Object[] getAttributes() {
        return attributes;
    }

    public Object[] getAttributes(Map map) {
        throw new UnsupportedOperationException("getAttributes(Map)");
    }
}
