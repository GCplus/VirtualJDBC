// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SerialJavaObject implements Externalizable {
    private static final long serialVersionUID = 4050198631045215540L;
    
    private Object javaObject;

    public SerialJavaObject(Object javaObject) {
        this.javaObject = javaObject;
    }

    public SerialJavaObject() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(javaObject);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        javaObject = in.readObject();
    }
}
