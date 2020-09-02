// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

import de.simplicit.vjdbc.command.*;
import de.simplicit.vjdbc.serial.*;
import de.simplicit.vjdbc.util.SQLExceptionHelper;

import java.io.InputStream;
import java.io.Reader;
import java.io.CharArrayReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class VirtualCallableStatement extends VirtualPreparedStatement implements CallableStatement {
    VirtualCallableStatement(UIDEx reg, Connection connection, String sql, DecoratedCommandSink sink, int resultSetType) {
        super(reg, connection, sql, sink, resultSetType);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    public void registerOutParameter(int parameterIndex, int sqlType)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT,
                "registerOutParameter",
                new Object[]{parameterIndex, sqlType},
                ParameterTypeCombinations.INTINT));
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT,
                "registerOutParameter",
                new Object[]{parameterIndex, sqlType, scale},//依靠jvm底层自动拆装箱
                ParameterTypeCombinations.INTINTINT));
    }

    public boolean wasNull() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "wasNull"));
    }

    public String getString(int parameterIndex) throws SQLException {
        return (String)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getString",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getBoolean",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public byte getByte(int parameterIndex) throws SQLException {
        return sink.processWithByteResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getByte",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public short getShort(int parameterIndex) throws SQLException {
        return sink.processWithShortResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getShort",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public int getInt(int parameterIndex) throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getInt",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public long getLong(int parameterIndex) throws SQLException {
        return sink.processWithLongResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getLong",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public float getFloat(int parameterIndex) throws SQLException {
        return sink.processWithFloatResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getFloat",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public double getDouble(int parameterIndex) throws SQLException {
        return sink.processWithDoubleResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getDouble",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public BigDecimal getBigDecimal(int parameterIndex, int scale)
            throws SQLException {
        return (BigDecimal)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getBigDecimal",
                new Object[]{parameterIndex, scale},
                ParameterTypeCombinations.INTINT));
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        return (byte[])sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getBytes",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public Date getDate(int parameterIndex) throws SQLException {
        return (Date)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getDate",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public Time getTime(int parameterIndex) throws SQLException {
        return (Time)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getTime",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public Timestamp getTimestamp(int parameterIndex)
            throws SQLException {
        return (Timestamp)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getTimestamp",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public Object getObject(int parameterIndex) throws SQLException {
        try {
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, new CallableStatementGetObjectCommand(parameterIndex));
            Object transportee = st.getTransportee();
            checkTransporteeForStreamingResultSet(transportee);
            return transportee;
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return (BigDecimal)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getBigDecimal",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public Object getObject(int i, Map map) throws SQLException {
        try {
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, new CallableStatementGetObjectCommand(i, map));
            Object transportee = st.getTransportee();
            checkTransporteeForStreamingResultSet(transportee);
            return transportee;
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }

    }

    public Ref getRef(int i) throws SQLException {
        try {
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, new CallableStatementGetRefCommand(i));
            return (Ref)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }

    }

    public Blob getBlob(int i) throws SQLException {
        try {
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, new CallableStatementGetBlobCommand(i));
            return (Blob)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Clob getClob(int i) throws SQLException {
        try {
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, new CallableStatementGetClobCommand(i));
            return (Clob)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Array getArray(int i) throws SQLException {
        try {
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, new CallableStatementGetArrayCommand(i));
            return (Array)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Date getDate(int parameterIndex, Calendar cal)
            throws SQLException {
        return (Date)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getDate",
                new Object[]{parameterIndex, cal},
                ParameterTypeCombinations.INTCAL));
    }

    public Time getTime(int parameterIndex, Calendar cal)
            throws SQLException {
        return (Time)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getTime",
                new Object[]{parameterIndex, cal},
                ParameterTypeCombinations.INTCAL));
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal)
            throws SQLException {
        return (Timestamp)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getTimestamp",
                new Object[]{parameterIndex, cal},
                ParameterTypeCombinations.INTCAL));
    }

    public void registerOutParameter(int paramIndex, int sqlType, String typeName)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "registerOutParameter",
                new Object[]{paramIndex, sqlType, typeName},
                ParameterTypeCombinations.INTINTSTR));
    }

    public void registerOutParameter(String parameterName, int sqlType)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "registerOutParameter",
                new Object[]{parameterName, sqlType},
                ParameterTypeCombinations.STRINT));
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "registerOutParameter",
                new Object[]{parameterName, sqlType, scale},
                ParameterTypeCombinations.STRINTINT));
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "registerOutParameter",
                new Object[]{parameterName, sqlType, typeName},
                ParameterTypeCombinations.STRINTSTR));
    }

    public URL getURL(int parameterIndex) throws SQLException {
        return (URL)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getURL",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public void setURL(String parameterName, URL val) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setURL",
                new Object[]{parameterName, val},
                ParameterTypeCombinations.STRURL));
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setNull",
                new Object[]{parameterName, sqlType},
                ParameterTypeCombinations.STRINT));
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setBoolean",
                new Object[]{parameterName, x ? Boolean.TRUE : Boolean.FALSE},
                ParameterTypeCombinations.STRBOL));
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setByte",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRBYT));
    }

    public void setShort(String parameterName, short x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setShort",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRSHT));
    }

    public void setInt(String parameterName, int x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setInt",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRINT));
    }

    public void setLong(String parameterName, long x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setLong",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRLNG));
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setFloat",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRFLT));
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setDouble",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRDBL));
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setBigDecimal",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRBID));
    }

    public void setString(String parameterName, String x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setString",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRSTR));
    }

    public void setBytes(String parameterName, byte[] x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setBytes",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRBYTA));
    }

    public void setDate(String parameterName, Date x)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setDate",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRDAT));
    }

    public void setTime(String parameterName, Time x)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setTime",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRTIM));
    }

    public void setTimestamp(String parameterName, Timestamp x)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setTimestamp",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRTMS));
    }

    public void setAsciiStream(String parameterName, InputStream x, int length)
            throws SQLException {
        try {
            sink.process(objectUid, new CallableStatementSetAsciiStreamCommand(parameterName, x, length));
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setBinaryStream(String parameterName, InputStream x,
                                int length) throws SQLException {
        try {
            sink.process(objectUid, new CallableStatementSetBinaryStreamCommand(parameterName, x, length));
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale)
            throws SQLException {
        CallableStatementSetObjectCommand cmd = new CallableStatementSetObjectCommand(parameterName, targetSqlType, scale);
        cmd.setObject(x);
        sink.process(objectUid, cmd);
    }

    public void setObject(String parameterName, Object x, int targetSqlType)
            throws SQLException {
        CallableStatementSetObjectCommand cmd = new CallableStatementSetObjectCommand(parameterName,
                targetSqlType,
                null);
        cmd.setObject(x);
        sink.process(objectUid, cmd);
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        CallableStatementSetObjectCommand cmd = new CallableStatementSetObjectCommand(parameterName,
                null,
                null);
        cmd.setObject(x);
        sink.process(objectUid, cmd);
    }

    public void setCharacterStream(String parameterName,
                                   Reader reader,
                                   int length) throws SQLException {
        try {
            CallableStatementSetCharacterStreamCommand cmd = new CallableStatementSetCharacterStreamCommand(parameterName, reader, length);
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setDate(String parameterName, Date x, Calendar cal)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setDate",
                new Object[]{parameterName, x, cal},
                ParameterTypeCombinations.STRDATCAL));
    }

    public void setTime(String parameterName, Time x, Calendar cal)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setTime",
                new Object[]{parameterName, x, cal},
                ParameterTypeCombinations.STRTIMCAL));
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setTimestamp",
                new Object[]{parameterName, x, cal},
                ParameterTypeCombinations.STRTMSCAL));
    }

    public void setNull(String parameterName, int sqlType, String typeName)
            throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setNull",
                new Object[]{parameterName, sqlType, typeName},
                ParameterTypeCombinations.STRINTSTR));
    }

    public String getString(String parameterName) throws SQLException {
        return (String)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getString",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getBoolean",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public byte getByte(String parameterName) throws SQLException {
        return sink.processWithByteResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getByte",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public short getShort(String parameterName) throws SQLException {
        return sink.processWithShortResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getShort",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public int getInt(String parameterName) throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getInt",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public long getLong(String parameterName) throws SQLException {
        return sink.processWithLongResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getLong",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public float getFloat(String parameterName) throws SQLException {
        return sink.processWithFloatResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getFloat",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public double getDouble(String parameterName) throws SQLException {
        return sink.processWithDoubleResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getDouble",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        return (byte[])sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getBytes",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public Date getDate(String parameterName) throws SQLException {
        return (Date)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getDate",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public Time getTime(String parameterName) throws SQLException {
        return (Time)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getTime",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return (Timestamp)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getTimestamp",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public Object getObject(String parameterName) throws SQLException {
        try {
            CallableStatementGetObjectCommand cmd = new CallableStatementGetObjectCommand(parameterName);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            Object transportee = st.getTransportee();
            checkTransporteeForStreamingResultSet(transportee);
            return transportee;
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return (BigDecimal)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getBigDecimal",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public Object getObject(String parameterName, Map map) throws SQLException {
        try {
            CallableStatementGetObjectCommand cmd = new CallableStatementGetObjectCommand(parameterName, map);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            Object transportee = st.getTransportee();
            checkTransporteeForStreamingResultSet(transportee);
            return transportee;
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Ref getRef(String parameterName) throws SQLException {
        try {
            CallableStatementGetRefCommand cmd = new CallableStatementGetRefCommand(parameterName);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return (SerialRef)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Blob getBlob(String parameterName) throws SQLException {
        try {
            CallableStatementGetBlobCommand cmd = new CallableStatementGetBlobCommand(parameterName);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return (SerialBlob)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Clob getClob(String parameterName) throws SQLException {
        try {
            CallableStatementGetClobCommand cmd = new CallableStatementGetClobCommand(parameterName);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return (SerialClob)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Array getArray(String parameterName) throws SQLException {
        try {
            CallableStatementGetArrayCommand cmd = new CallableStatementGetArrayCommand(parameterName);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return (SerialArray)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Date getDate(String parameterName, Calendar cal)
            throws SQLException {
        return (Date)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getDate",
                new Object[]{parameterName, cal},
                ParameterTypeCombinations.STRCAL));
    }

    public Time getTime(String parameterName, Calendar cal)
            throws SQLException {
        return (Time)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getTime",
                new Object[]{parameterName, cal},
                ParameterTypeCombinations.STRCAL));
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal)
            throws SQLException {
        return (Timestamp)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getTimestamp",
                new Object[]{parameterName, cal},
                ParameterTypeCombinations.STRCAL));
    }

    public URL getURL(String parameterName) throws SQLException {
        return (URL)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getURL",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    private void checkTransporteeForStreamingResultSet(Object transportee) {
        // The transportee might be a StreamingResultSet (i.e. Oracle can return database cursors). Thus
        // we must check the transportee and set some references correspondingly when it is a ResultSet.
        if(transportee instanceof StreamingResultSet) {
            StreamingResultSet srs = (StreamingResultSet)transportee;
            srs.setStatement(this);
            srs.setCommandSink(sink);
        }
    }

    /* start JDBC4 support */
    public RowId getRowId(int parameterIndex) throws SQLException {
        return (RowId)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getRowId",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public RowId getRowId(String parameterName) throws SQLException {
        return (RowId)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getRowId",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {
        try {
            CallableStatementSetRowIdCommand cmd =
                new CallableStatementSetRowIdCommand(parameterName, new SerialRowId(x));
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setNString(String parameterName, String x) throws SQLException {
        sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "setNString",
                new Object[]{parameterName, x},
                ParameterTypeCombinations.STRSTR));
    }

    public void setNCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        try {
            CallableStatementSetNCharacterStreamCommand cmd = new CallableStatementSetNCharacterStreamCommand(parameterName, reader, (int)length);
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setNClob(String parameterName, NClob value) throws SQLException {
        try {
            CallableStatementSetNClobCommand cmd = new CallableStatementSetNClobCommand(parameterName, value);
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        try {
            CallableStatementSetClobCommand cmd =
                new CallableStatementSetClobCommand(parameterName, new SerialClob(reader, length));
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        try {
            CallableStatementSetBlobCommand cmd =
                new CallableStatementSetBlobCommand(parameterName, new SerialBlob(inputStream, length));
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        try {
            CallableStatementSetNClobCommand cmd =
                new CallableStatementSetNClobCommand(parameterName, new SerialNClob(reader, length));
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        try {
            CallableStatementGetNClobCommand cmd = new CallableStatementGetNClobCommand(parameterIndex);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return (SerialNClob)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public NClob getNClob(String parameterName) throws SQLException {
        try {
            CallableStatementGetNClobCommand cmd = new CallableStatementGetNClobCommand(parameterName);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return (SerialNClob)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        try {
            CallableStatementSetSQLXMLCommand cmd =
                new CallableStatementSetSQLXMLCommand(parameterName, new SerialSQLXML(xmlObject));
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        try {
            CallableStatementGetSQLXMLCommand cmd = new CallableStatementGetSQLXMLCommand(parameterIndex);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return (SerialSQLXML)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        try {
            CallableStatementGetSQLXMLCommand cmd = new CallableStatementGetSQLXMLCommand(parameterName);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return (SerialSQLXML)st.getTransportee();
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public String getNString(int parameterIndex) throws SQLException {
        return (String)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getNString",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public String getNString(String parameterName) throws SQLException {
        return (String)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CALLABLESTATEMENT, "getNString",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        try {
            CallableStatementGetNCharacterStreamCommand cmd = new CallableStatementGetNCharacterStreamCommand(parameterIndex);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return new CharArrayReader((char[])st.getTransportee());
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        try {
            CallableStatementGetNCharacterStreamCommand cmd = new CallableStatementGetNCharacterStreamCommand(parameterName);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return new CharArrayReader((char[])st.getTransportee());
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        try {
            CallableStatementGetCharacterStreamCommand cmd = new CallableStatementGetCharacterStreamCommand(parameterIndex);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return new CharArrayReader((char[])st.getTransportee());
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        try {
            CallableStatementGetCharacterStreamCommand cmd = new CallableStatementGetCharacterStreamCommand(parameterName);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            return new CharArrayReader((char[])st.getTransportee());
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setClob(String parameterName, Clob clob) throws SQLException {
        try {
            CallableStatementSetClobCommand cmd =
                new CallableStatementSetClobCommand(parameterName, clob);
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setBlob(String parameterName, Blob blob) throws SQLException {
        try {
            CallableStatementSetBlobCommand cmd =
                new CallableStatementSetBlobCommand(parameterName, blob);
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        try {
            sink.process(objectUid, new CallableStatementSetAsciiStreamCommand(parameterName, x, (int)length));
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        try {
            sink.process(objectUid, new CallableStatementSetBinaryStreamCommand(parameterName, x, (int)length));
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        try {
            CallableStatementSetCharacterStreamCommand cmd = new CallableStatementSetCharacterStreamCommand(parameterName, reader, (int)length);
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        try {
            sink.process(objectUid, new CallableStatementSetAsciiStreamCommand(parameterName, x));
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        try {
            sink.process(objectUid, new CallableStatementSetBinaryStreamCommand(parameterName, x));
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        try {
            CallableStatementSetCharacterStreamCommand cmd = new CallableStatementSetCharacterStreamCommand(parameterName, reader);
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setNCharacterStream(String parameterName, Reader reader) throws SQLException {
        try {
            CallableStatementSetNCharacterStreamCommand cmd = new CallableStatementSetNCharacterStreamCommand(parameterName, reader);
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setClob(String parameterName, Reader reader) throws SQLException {
        try {
            CallableStatementSetClobCommand cmd =
                new CallableStatementSetClobCommand(parameterName, new SerialClob(reader));
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        try {
            CallableStatementSetBlobCommand cmd =
                new CallableStatementSetBlobCommand(parameterName, new SerialBlob(inputStream));
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public void setNClob(String parameterName, Reader reader) throws SQLException {
        try {
            CallableStatementSetNClobCommand cmd =
                new CallableStatementSetNClobCommand(parameterName, new SerialNClob(reader));
            sink.process(objectUid, cmd);
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    /* end JDBC4 support */

    /* start JDK7 support */
    public <T> T getObject(int parameterIndex, Class<T> clazz)
        throws SQLException {
        try {
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, new CallableStatementGetObjectCommand(parameterIndex, clazz));
            Object transportee = st.getTransportee();
            checkTransporteeForStreamingResultSet(transportee);
            return (T)transportee;
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public <T> T getObject(String parameterName, Class<T> clazz)
        throws SQLException {
        try {
            CallableStatementGetObjectCommand cmd = new CallableStatementGetObjectCommand(parameterName, clazz);
            SerializableTransport st = (SerializableTransport)sink.process(objectUid, cmd);
            Object transportee = st.getTransportee();
            checkTransporteeForStreamingResultSet(transportee);
            return (T)transportee;
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }
    /* end JDK7 support */
}
