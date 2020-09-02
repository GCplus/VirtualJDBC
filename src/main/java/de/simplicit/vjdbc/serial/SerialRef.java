// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.Ref;
import java.sql.SQLException;
import java.util.Map;

public class SerialRef implements Ref, Externalizable {
    static final long serialVersionUID = -9145419222061515405L;

    private String baseTypeName;
    private Object javaObject;

    public SerialRef(Ref ref) throws SQLException {
        this.baseTypeName = ref.getBaseTypeName();
        this.javaObject = ref.getObject();
    }

    public SerialRef() {
    }

    public String getBaseTypeName() {
        return baseTypeName;
    }

    public Object getObject(Map map) {
        throw new UnsupportedOperationException("Ref.getObject(Map) not supported");
    }

    public Object getObject() {
        return javaObject;
    }

    public void setObject(Object value) {
        throw new UnsupportedOperationException("Ref.setObject(Object) not supported");
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(baseTypeName);
        out.writeObject(javaObject);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        baseTypeName = in.readUTF();
        javaObject = in.readObject();
    }
}
