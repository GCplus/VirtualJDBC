// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.zip.Deflater;

public class SerializableTransport implements Externalizable {
    static final long serialVersionUID = -5634734498572640609L;

    private boolean isCompressed;
    private Object transportee;
    private transient Object original;

    public SerializableTransport() {
    }
    
    public SerializableTransport(Object transportee, int compressionMode, long minimumSize) {
        deflate(transportee, compressionMode, minimumSize);
    }

    public SerializableTransport(Object transportee) {
        this(transportee, Deflater.BEST_SPEED, 2000);
    }

    public Object getTransportee() throws IOException, ClassNotFoundException {
        if(original == null) {
            if(isCompressed) {
                inflate();
            } else {
                original = transportee;
            }
        }

        return original;
    }

    private void deflate(Object crs, int compressionMode, long minimumSize) {
        if(compressionMode != Deflater.NO_COMPRESSION) {
            try {
                byte[] serializedObject = serializeObject(crs);
                if(serializedObject.length >= minimumSize) {
                    transportee = Zipper.zip(serializedObject, compressionMode);
                    isCompressed = true;
                } else {
                    transportee = crs;
                    isCompressed = false;
                }
            } catch(IOException e) {
                transportee = crs;
                isCompressed = false;
            }
        } else {
            transportee = crs;
            isCompressed = false;
        }
    }

    private void inflate() throws IOException, ClassNotFoundException {
        byte[] unzipped = Zipper.unzip((byte[])transportee);
        original = deserializeObject(unzipped);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        isCompressed = in.readBoolean();
        transportee = in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(isCompressed);
        out.writeObject(transportee);
    }

    private static byte[] serializeObject(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        return baos.toByteArray();
    }

    private static Object deserializeObject(byte[] b) throws ClassNotFoundException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }
}
