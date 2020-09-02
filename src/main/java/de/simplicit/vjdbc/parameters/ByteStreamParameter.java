// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.parameters;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ByteStreamParameter implements PreparedStatementParameter {
    static final long serialVersionUID = 8868161011164192986L;

    public static final int TYPE_ASCII = 1;
    public static final int TYPE_UNICODE = 2;
    public static final int TYPE_BINARY = 3;

    private int type;
    private byte[] value;
    private long length;

    public ByteStreamParameter() {
    }

    public ByteStreamParameter(int type, InputStream x, long length) throws SQLException {
        this.type = type;
        this.length = length;

        BufferedInputStream s = new BufferedInputStream(x);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int)(length >= 0 ? length : 1024));
            byte[] buf = new byte[1024];
            int br;
            while((br = s.read(buf)) >= 0) {
                if(br > 0) {
                    bos.write(buf, 0, br);
                }
            }
            value = bos.toByteArray();
            // Adjust length to the amount of read bytes if the user provided
            // -1 as the length parameter
            if(length < 0) {
                this.length = value.length;
            }
        } catch(IOException e) {
            throw new SQLException("InputStream conversion to byte-array failed");
        } finally {
            try {
                s.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getValue() {
        return value;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type = in.readInt();
        value = (byte[])in.readObject();
        length = in.readLong();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(type);
        out.writeObject(value);
        out.writeLong(length);
    }

    public void setParameter(PreparedStatement pstmt, int index) throws SQLException {
        ByteArrayInputStream bais = new ByteArrayInputStream(value);

        switch(type) {
            case TYPE_ASCII:
                pstmt.setAsciiStream(index, bais, length);
                break;

            case TYPE_UNICODE:
                // its ok to downcast here as there is no setUnicodeStream()
                // variant with a long length value
                pstmt.setUnicodeStream(index, bais, (int)length);
                break;

            case TYPE_BINARY:
                pstmt.setBinaryStream(index, bais, length);
                break;
        }
    }

    public String toString() {
        return "ByteStream: " + length + " bytes";
    }
}
