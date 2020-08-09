// VJDBC - Virtual JDBC
// Written by Hunter Payne
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.io.*;
import java.sql.SQLXML;
import java.sql.SQLException;
import java.util.Map;
import java.lang.reflect.Constructor;

import javax.xml.transform.Source;
import javax.xml.transform.Result;

import org.xml.sax.InputSource;

public class SerialSQLXML implements SQLXML, Externalizable {
    static final long serialVersionUID = 68757548812947189L;

    private final StringBuilder xml;

    public SerialSQLXML() {
        this.xml = new StringBuilder();
    }

    public SerialSQLXML(String xml) {
        this.xml = new StringBuilder(xml);
    }

    public SerialSQLXML(SQLXML sqlxml) throws SQLException {
        this.xml = new StringBuilder(sqlxml.getString());
        sqlxml.free();
    }

    public void free() {
        xml.delete(0, xml.length());
    }

    public InputStream getBinaryStream() {
        return new ByteArrayInputStream(xml.toString().getBytes());
    }

    public Reader getCharacterStream() {
        return new StringReader(xml.toString());
    }

    public <T extends Source> T getSource(Class<T> sourceClass) {
        try {
            Constructor<T> constructor =
                sourceClass.getConstructor(InputSource.class);
            return constructor.newInstance(new InputSource(getCharacterStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getString() {
        return xml.toString();
    }

    public OutputStream setBinaryStream() {
        throw new UnsupportedOperationException("SQLXML.setBinaryStream() not supported, use setString(String value) instead");
    }

    public Writer setCharacterStream() {
        throw new UnsupportedOperationException("SQLXML.setCharacterStream() not supported, use setString(String value) instead");
    }

    public <T extends Result> T setResult(Class<T> resultClass) {
        throw new UnsupportedOperationException("SQLXML.setResult() not supported");
    }

    public void setString(String value) {
        xml.delete(0, xml.length());
        xml.append(value);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(xml.toString());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        xml.append(in.readUTF());
    }
}
