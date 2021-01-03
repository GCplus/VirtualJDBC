// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import de.simplicit.vjdbc.command.*;
import de.simplicit.vjdbc.util.JavaVersionInfo;
import de.simplicit.vjdbc.util.SQLExceptionHelper;
import de.simplicit.vjdbc.VirtualStatement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * 引用了ResultSet和Externalizable接口
 *
 * Serializable接口
 *    |-优点：内建支持
 *    |
 *    |-优点：易于实现
 *    |
 *    |-缺点：占用空间过大
 *    |
 *    |-缺点：由于额外的开销导致速度变比较慢
 *
 * Externalizable接口
 *    |- 优点：开销较少（程序员决定存储什么）
 *    |
 *    |——优点：可能的速度提升
 *    |
 *    |-缺点：虚拟机不提供任何帮助，也就是说所有的工作都落到了开发人员的肩上。
 */
public class StreamingResultSet implements ResultSet, Externalizable {
    static final long serialVersionUID = 8291019975153433161L;

    private static final Log logger = LogFactory.getLog(StreamingResultSet.class);

    private int[] columnTypes;
    private String[] columnNames;
    private String[] columnLabels;
    private RowPacket rows;
    private int rowPacketSize;
    private boolean forwardOnly;
    private String charset;
    private boolean lastPartReached = true;
    private UIDEx remainingResultSet = null;
    private SerialResultSetMetaData metaData = null;

    private transient DecoratedCommandSink commandSink = null;
    private transient int cursor = -1;
    private transient int lastReadColumn = 0;
    private transient Object[] actualRow;
    private transient int fetchDirection;
    private transient boolean prefetchMetaData;
    private transient Statement statement;

    protected void finalize() throws Throwable {
        super.finalize();
        if(remainingResultSet != null) {
            close();
        }
    }

    public StreamingResultSet() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(columnTypes);
        out.writeObject(columnNames);
        out.writeObject(columnLabels);
        out.writeObject(rows);
        out.writeInt(rowPacketSize);
        out.writeBoolean(forwardOnly);
        out.writeUTF(charset);
        out.writeBoolean(lastPartReached);
        out.writeObject(remainingResultSet);
        out.writeObject(metaData);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        columnTypes = (int[])in.readObject();
        columnNames = (String[])in.readObject();
        columnLabels = (String[])in.readObject();
        rows = (RowPacket)in.readObject();
        rowPacketSize = in.readInt();
        forwardOnly = in.readBoolean();
        charset = in.readUTF();
        lastPartReached = in.readBoolean();
        remainingResultSet = (UIDEx)in.readObject();
        metaData = (SerialResultSetMetaData)in.readObject();

        cursor = -1;
    }

    public StreamingResultSet(int rowPacketSize, boolean forwardOnly, boolean prefetchMetaData, String charset) {
        this.rowPacketSize = rowPacketSize;
        this.forwardOnly = forwardOnly;
        this.prefetchMetaData = prefetchMetaData;
        this.charset = charset;
    }

    public void setStatement(Statement stmt) {
        statement = stmt;
    }

    public void setCommandSink(DecoratedCommandSink sink) {
        commandSink = sink;
    }

    public void setRemainingResultSetUID(UIDEx reg) {
        remainingResultSet = reg;
    }

    public boolean populate(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Fetch the meta data immediately if required. Succeeding getMetaData() calls
        // on the ResultSet won't require an additional remote call
        if(prefetchMetaData) {
            logger.debug("Fetching MetaData of ResultSet");
            metaData = new SerialResultSetMetaData(metaData);
        }

        int columnCount = metaData.getColumnCount();
        columnTypes = new int[columnCount];
        columnNames = new String[columnCount];
        columnLabels = new String[columnCount];

        for(int i = 1; i <= columnCount; i++) {
            columnTypes[i-1] = metaData.getColumnType(i);
            columnNames[i-1] = metaData.getColumnName(i).toLowerCase();
            columnLabels[i-1] = metaData.getColumnLabel(i).toLowerCase();
        }

        // Create first ResultSet-Part
        rows = new RowPacket(rowPacketSize, forwardOnly);
        // Populate it
        rows.populate(rs);

        lastPartReached = rows.isLastPart();

        return lastPartReached;
    }

    public boolean next() throws SQLException {
        boolean result = false;

        if(++cursor < rows.size()) {
            actualRow = rows.get(cursor);
            result = true;
        } else {
            if(!lastPartReached) {
                try {
                    SerializableTransport st = (SerializableTransport)commandSink.process(remainingResultSet, new NextRowPacketCommand());
                    RowPacket rsp = (RowPacket)st.getTransportee();

                    if(rsp.isLastPart()) {
                        lastPartReached = true;
                    }

                    if(rsp.size() > 0) {
                        rows.merge(rsp);
                        actualRow = rows.get(cursor);
                        result = true;
                    }
                } catch(Exception e) {
                    throw SQLExceptionHelper.wrap(e);
                }
            }
        }

        return result;
    }

    public void close() throws SQLException {
        cursor = -1;
        if(remainingResultSet != null) {
            // The server-side created StreamingResultSet is garbage-collected after it was send over the wire. Thus
            // we have to check here if it is such a server object because in this case we don't have to try the remote
            // call which indeed causes a NPE.
            if(commandSink != null) {
                commandSink.process(remainingResultSet, new DestroyCommand(remainingResultSet, JdbcInterfaceType.RESULTSETHOLDER));
            }
            remainingResultSet = null;
        }
        if (((VirtualStatement)statement).isCloseOnCompletion()) {
            statement.close();
        }
    }

    public boolean wasNull() {
        return actualRow[lastReadColumn] == null;
    }

    public String getString(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return actualRow[columnIndex].toString();
        } else {
            return null;
        }
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            Object value = actualRow[columnIndex];

            switch(columnTypes[columnIndex]) {
                case Types.BIT:
                    // Boolean
                    return (Boolean) value;
                case Types.TINYINT:
                    // Byte
                    return (Byte) value != 0;
                case Types.SMALLINT:
                    // Short
                    return (Short) value != 0;
                case Types.INTEGER:
                    // Integer
                    return (Integer) value != 0;
                case Types.BIGINT:
                    // Long
                    return (Long) value != 0;
                case Types.REAL:
                    // Float
                    return (Float) value != 0.0f;
                case Types.FLOAT:
                case Types.DOUBLE:
                    // Double
                    return (Double) value != 0.0f;
                case Types.NUMERIC:
                case Types.DECIMAL:
                    // BigDecimal
                    return ((BigDecimal)value).intValue() != 0;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    // String
                    try {
                        return Integer.parseInt((String)value) != 0;
                    } catch (NumberFormatException e) {
                        throw new SQLException("Can't convert String value '" + value + "' to boolean, must be an integer");
                    }
                default:
                    if(JavaVersionInfo.use16Api) {
                        if(columnTypes[columnIndex] == Types.BOOLEAN) {
                            // Boolean
                            return (Boolean) value;
                        }
                    }
                    break;
            }

            throw new SQLException("Can't convert type to boolean: " + value.getClass());
        }

        return false;
    }

    public byte getByte(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            Object value = actualRow[columnIndex];

            switch(columnTypes[columnIndex]) {
                case Types.BIT:
                    // Boolean
                    return (Boolean) value ? (byte)1 : (byte)0;
                case Types.TINYINT:
                    // Byte
                    return (Byte) value;
                case Types.SMALLINT:
                    // Short
                    return ((Short)value).byteValue();
                case Types.INTEGER:
                    // Integer
                    return ((Integer)value).byteValue();
                case Types.BIGINT:
                    // Long
                    return ((Long)value).byteValue();
                case Types.REAL:
                    // Float
                    return ((Float)value).byteValue();
                case Types.FLOAT:
                case Types.DOUBLE:
                    // Double
                    return ((Double)value).byteValue();
                case Types.NUMERIC:
                case Types.DECIMAL:
                    // BigDecimal
                    return ((BigDecimal)value).byteValue();
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    // String
                    try {
                        return Byte.parseByte((String)value);
                    } catch (NumberFormatException e) {
                        throw new SQLException("Can't convert String value '" + value + "' to byte");
                    }
                default:
                    if(JavaVersionInfo.use16Api) {
                        if(columnTypes[columnIndex] == Types.BOOLEAN) {
                            // Boolean
                            return (Boolean) value ? (byte)1 : (byte)0;
                        }
                    }
                    break;
            }

            throw new SQLException("Can't convert type to byte: " + value.getClass());
        }

        return 0;
    }

    public short getShort(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            Object value = actualRow[columnIndex];

            switch(columnTypes[columnIndex]) {
                case Types.BIT:
                    // Boolean
                    return (Boolean) value ? (short)1 : (short)0;
                case Types.TINYINT:
                    // Byte
                    return ((Byte)value).shortValue();
                case Types.SMALLINT:
                    // Short
                    return (Short) value;
                case Types.INTEGER:
                    // Integer
                    return ((Integer)value).shortValue();
                case Types.BIGINT:
                    // Long
                    return ((Long)value).shortValue();
                case Types.REAL:
                    // Float
                    return ((Float)value).shortValue();
                case Types.FLOAT:
                case Types.DOUBLE:
                    // Double
                    return ((Double)value).shortValue();
                case Types.NUMERIC:
                case Types.DECIMAL:
                    // BigDecimal
                    return ((BigDecimal)value).shortValue();
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    // String
                    try {
                        return Short.parseShort((String)value);
                    } catch (NumberFormatException e) {
                        throw new SQLException("Can't convert String value '" + value + "' to short");
                    }
                default:
                    if(JavaVersionInfo.use16Api) {
                        if(columnTypes[columnIndex] == Types.BOOLEAN) {
                            // Boolean
                            return (Boolean) value ? (short)1 : (short)0;
                        }
                    }
                    break;
            }

            throw new SQLException("Can't convert type to short: " + value.getClass());
        }

        return 0;
    }

    public int getInt(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            Object value = actualRow[columnIndex];

            switch(columnTypes[columnIndex]) {
                case Types.BIT:
                    // Boolean
                    return (Boolean) value ? 1 : 0;
                case Types.TINYINT:
                    // Byte
                    return ((Byte)value).intValue();
                case Types.SMALLINT:
                    // Short
                    return ((Short)value).intValue();
                case Types.INTEGER:
                    // Integer
                    return (Integer) value;
                case Types.BIGINT:
                    // Long
                    return ((Long)value).intValue();
                case Types.REAL:
                    // Float
                    return ((Float)value).intValue();
                case Types.FLOAT:
                case Types.DOUBLE:
                    // Double
                    return ((Double)value).intValue();
                case Types.NUMERIC:
                case Types.DECIMAL:
                    // BigDecimal
                    return ((BigDecimal)value).intValue();
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    // String
                    try {
                        return Integer.parseInt((String)value);
                    } catch (NumberFormatException e) {
                        throw new SQLException("Can't convert String value '" + value + "' to integer");
                    }
                default:
                    if(JavaVersionInfo.use16Api) {
                        if(columnTypes[columnIndex] == Types.BOOLEAN) {
                            // Boolean
                            return (Boolean) value ? 1 : 0;
                        }
                    }
                    break;
            }

            throw new SQLException("Can't convert type to integer: " + value.getClass());
        }

        return 0;
    }

    public long getLong(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            Object value = actualRow[columnIndex];

            switch(columnTypes[columnIndex]) {
                case Types.BIT:
                    // Boolean
                    return (Boolean) value ? 1 : 0;
                case Types.TINYINT:
                    // Byte
                    return ((Byte)value).longValue();
                case Types.SMALLINT:
                    // Short
                    return ((Short)value).longValue();
                case Types.INTEGER:
                    // Integer
                    return ((Integer)value).longValue();
                case Types.BIGINT:
                    // Long
                    return (Long) value;
                case Types.REAL:
                    // Float
                    return ((Float)value).longValue();
                case Types.FLOAT:
                case Types.DOUBLE:
                    // Double
                    return ((Double)value).longValue();
                case Types.NUMERIC:
                case Types.DECIMAL:
                    // BigDecimal
                    return ((BigDecimal)value).longValue();
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    // String
                    try {
                        return Long.parseLong((String)value);
                    } catch (NumberFormatException e) {
                        throw new SQLException("Can't convert String value '" + value + "' to long");
                    }
                default:
                    if(JavaVersionInfo.use16Api) {
                        if(columnTypes[columnIndex] == Types.BOOLEAN) {
                            // Boolean
                            return (Boolean) value ? 1 : 0;
                        }
                    }
                    break;
            }

            throw new SQLException("Can't convert type to long: " + value.getClass());
        }

        return 0;
    }

    public float getFloat(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            Object value = actualRow[columnIndex];

            switch(columnTypes[columnIndex]) {
                case Types.BIT:
                    // Boolean
                    return (Boolean) value ? 1.0f : 0.0f;
                case Types.TINYINT:
                    // Byte
                    return ((Byte)value).floatValue();
                case Types.SMALLINT:
                    // Short
                    return ((Short)value).floatValue();
                case Types.INTEGER:
                    // Integer
                    return ((Integer)value).floatValue();
                case Types.BIGINT:
                    // Long
                    return ((Long)value).floatValue();
                case Types.REAL:
                    // Float
                    return (Float) value;
                case Types.FLOAT:
                case Types.DOUBLE:
                    // Double
                    return ((Double)value).floatValue();
                case Types.NUMERIC:
                case Types.DECIMAL:
                    // BigDecimal
                    return ((BigDecimal)value).floatValue();
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    // String
                    try {
                        return Float.parseFloat((String)value);
                    } catch (NumberFormatException e) {
                        throw new SQLException("Can't convert String value '" + value + "' to float");
                    }
                default:
                    if(JavaVersionInfo.use16Api) {
                        if(columnTypes[columnIndex] == Types.BOOLEAN) {
                            // Boolean
                            return (Boolean) value ? 1 : 0;
                        }
                    }
                    break;
            }

            throw new SQLException("Can't convert type to float: " + value.getClass());
        }

        return 0.0f;
    }

    public double getDouble(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            Object value = actualRow[columnIndex];

            switch(columnTypes[columnIndex]) {
                case Types.BIT:
                    // Boolean
                    return (Boolean) value ? 1.0 : 0.0;
                case Types.TINYINT:
                    // Byte
                    return ((Byte)value).doubleValue();
                case Types.SMALLINT:
                    // Short
                    return ((Short)value).doubleValue();
                case Types.INTEGER:
                    // Integer
                    return ((Integer)value).doubleValue();
                case Types.BIGINT:
                    // Long
                    return ((Long)value).doubleValue();
                case Types.REAL:
                    // Float
                    return ((Float)value).doubleValue();
                case Types.FLOAT:
                case Types.DOUBLE:
                    // Double
                    return (Double) value;
                case Types.NUMERIC:
                case Types.DECIMAL:
                    // BigDecimal
                    return ((BigDecimal)value).doubleValue();
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    // String
                    try {
                        return Double.parseDouble((String)value);
                    } catch (NumberFormatException e) {
                        throw new SQLException("Can't convert String value '" + value + "' to double");
                    }
                default:
                    if(JavaVersionInfo.use16Api) {
                        if(columnTypes[columnIndex] == Types.BOOLEAN) {
                            // Boolean
                            return (Boolean) value ? 1 : 0;
                        }
                    }
                    break;
            }

            throw new SQLException("Can't convert type to double: " + value.getClass());
        }

        return 0.0;
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return internalGetBigDecimal(actualRow[columnIndex], columnTypes[columnIndex], scale);
        } else {
            return null;
        }
    }

    public byte[] getBytes(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return (byte[])actualRow[columnIndex];
        } else {
            return null;
        }
    }

    public Date getDate(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            switch(columnTypes[columnIndex]) {
                case Types.DATE:
                    return (Date)actualRow[columnIndex];
                case Types.TIME:
                    return getCleanDate((((Time)actualRow[columnIndex]).getTime()));
                case Types.TIMESTAMP:
                    return getCleanDate(((Timestamp)actualRow[columnIndex]).getTime());
            }

            throw new SQLException("Can't convert type to Date: " + actualRow[columnIndex].getClass());
        } else {
            return null;
        }
    }

    public Time getTime(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            switch(columnTypes[columnIndex]) {
                case Types.TIME:
                    return (Time)actualRow[columnIndex];
                case Types.DATE:
                    Date date = ((Date)actualRow[columnIndex]);
                    return getCleanTime(date.getTime());
                case Types.TIMESTAMP:
                    Timestamp timestamp = ((Timestamp)actualRow[columnIndex]);
                    return getCleanTime(timestamp.getTime());
            }

            throw new SQLException("Can't convert type to Time: " + actualRow[columnIndex].getClass());
        } else {
            return null;
        }
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            switch(columnTypes[columnIndex]) {
                case Types.TIME:
                    return new Timestamp(((Time)actualRow[columnIndex]).getTime());
                case Types.DATE:
                    return new Timestamp(((Date)actualRow[columnIndex]).getTime());
                case Types.TIMESTAMP:
                    return ((Timestamp)actualRow[columnIndex]);
            }

            throw new SQLException("Can't convert type to Timestamp: " + actualRow[columnIndex].getClass());
        } else {
            return null;
        }
    }

    public InputStream getAsciiStream(int columnIndex) {
        throw new UnsupportedOperationException("getAsciiStream");
    }

    public InputStream getUnicodeStream(int columnIndex) {
        throw new UnsupportedOperationException("getUnicodeStream");
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            Object obj = actualRow[columnIndex];

            byte[] bytes;

            if(obj instanceof byte[]) {
                bytes = (byte[])obj;
            } else if(obj instanceof String) {
                try {
                    bytes = ((String)obj).getBytes(charset);
                } catch(UnsupportedEncodingException e) {
                    throw SQLExceptionHelper.wrap(e);
                }
            } else {
                String msg = "StreamingResultSet.getBinaryStream(): Can't convert object of type '" + obj.getClass() + "' to InputStream";
                throw new SQLException(msg);
            }

            return new ByteArrayInputStream(bytes);
        } else {
            return null;
        }
    }

    public String getString(String columnName) throws SQLException {
        return getString(getIndexForName(columnName));
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return getBoolean(getIndexForName(columnName));
    }

    public byte getByte(String columnName) throws SQLException {
        return getByte(getIndexForName(columnName));
    }

    public short getShort(String columnName) throws SQLException {
        return getShort(getIndexForName(columnName));
    }

    public int getInt(String columnName) throws SQLException {
        return getInt(getIndexForName(columnName));
    }

    public long getLong(String columnName) throws SQLException {
        return getLong(getIndexForName(columnName));
    }

    public float getFloat(String columnName) throws SQLException {
        return getFloat(getIndexForName(columnName));
    }

    public double getDouble(String columnName) throws SQLException {
        return getDouble(getIndexForName(columnName));
    }

    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return getBigDecimal(getIndexForName(columnName), scale);
    }

    public byte[] getBytes(String columnName) throws SQLException {
        return getBytes(getIndexForName(columnName));
    }

    public Date getDate(String columnName) throws SQLException {
        return getDate(getIndexForName(columnName));
    }

    public Time getTime(String columnName) throws SQLException {
        return getTime(getIndexForName(columnName));
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        return getTimestamp(getIndexForName(columnName));
    }

    public InputStream getAsciiStream(String columnName) {
        throw new UnsupportedOperationException("getAsciiStream");
    }

    public InputStream getUnicodeStream(String columnName) {
        throw new UnsupportedOperationException("getUnicodeStream");
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        return getBinaryStream(getIndexForName(columnName));
    }

    public SQLWarning getWarnings() throws SQLException {
        if(cursor < 0) {
            throw new SQLException("ResultSet already closed");
        } else {
            return null;
        }
    }

    public void clearWarnings() {
    }

    public String getCursorName() {
        throw new UnsupportedOperationException("getCursorName");
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        if(metaData == null) {
            SerializableTransport st = (SerializableTransport)commandSink.process(remainingResultSet, new ResultSetGetMetaDataCommand());
            if(st != null) {
                try {
                    metaData = (SerialResultSetMetaData)st.getTransportee();
                } catch(Exception e) {
                    throw new SQLException("Can't get ResultSetMetaData, reason: " + e.toString());
                }
            } else {
                throw new SQLException("Can't get ResultSetMetaData");
            }
        }

        return metaData;
    }

    public Object getObject(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return actualRow[columnIndex];
        }
        else {
            return null;
        }
    }

    public Object getObject(String columnName) throws SQLException {
        return getObject(getIndexForName(columnName));
    }

    public int findColumn(String columnName) throws SQLException {
        return getIndexForName(columnName);
    }

    public Reader getCharacterStream(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return new StringReader((String)actualRow[columnIndex]);
        }
        else {
            return null;
        }
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        return getCharacterStream(getIndexForName(columnName));
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return internalGetBigDecimal(actualRow[columnIndex], columnTypes[columnIndex], -1);
        }
        else {
            return null;
        }
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return getBigDecimal(getIndexForName(columnName));
    }

    private BigDecimal internalGetBigDecimal(Object value, int columnType, int scale) throws SQLException {
        BigDecimal result = null;

        if(value != null) {
            switch(columnType) {
                case Types.BIT:
                    // Boolean
                    result = BigDecimal.valueOf(Boolean.parseBoolean(String.valueOf(value)) ? 1.0 : 0.0);
                    break;
                case Types.TINYINT:
                    // Byte
                    result = BigDecimal.valueOf(Byte.valueOf(String.valueOf(value)).doubleValue());
                    break;
                case Types.SMALLINT:
                    // Short
                    result = BigDecimal.valueOf(Short.valueOf(String.valueOf(value)).doubleValue());
                    break;
                case Types.INTEGER:
                    // Integer
                    result = BigDecimal.valueOf(Integer.valueOf(String.valueOf(value)).doubleValue());
                    break;
                case Types.BIGINT:
                    // Long
                    result = BigDecimal.valueOf(Long.valueOf(String.valueOf(value)).doubleValue());
                    break;
                case Types.REAL:
                    // Float
                    result = BigDecimal.valueOf(Float.valueOf(String.valueOf(value)).doubleValue());
                    break;
                case Types.FLOAT:
                case Types.DOUBLE:
                    // Double
                    result = BigDecimal.valueOf(Double.parseDouble(String.valueOf(value)));
                    break;
                case Types.NUMERIC:
                case Types.DECIMAL:
                    // BigDecimal
                    result = (BigDecimal)value;
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    // String
                    try {
                        result = BigDecimal.valueOf(Double.parseDouble(String.valueOf(value)));
                    } catch (NumberFormatException e) {
                        throw new SQLException("Can't convert String value '" + value + "' to double");
                    }
                default:
                    if(JavaVersionInfo.use16Api) {
                        if(columnType == Types.BOOLEAN) {
                            // Boolean
                            result = BigDecimal.valueOf(Boolean.parseBoolean(String.valueOf(value)) ? 1.0 : 0.0);
                        }
                    }
                    break;
            }

            // Set scale if necessary
            if(result != null) {
                if(scale >= 0) {
                    result = result.setScale(scale);
                }
            }
            else {
                throw new SQLException("Can't convert type to BigDecimal: " + value.getClass());
            }
        }

        return result;
    }

    public boolean isBeforeFirst() {
        return cursor < 0;
    }

    public boolean isAfterLast() {
        return rows.isLastPart() && (cursor == rows.size());
    }

    public boolean isFirst() {
        return cursor == 0;
    }

    public boolean isLast() {
        return rows.isLastPart() && (cursor == (rows.size() - 1));
    }

    public void beforeFirst() {
        cursor = -1;
        actualRow = null;
    }

    public void afterLast() {
        // Request all remaining Row-Packets
//        while(requestNextRowPacket()) ;
        cursor = rows.size();
        actualRow = null;
    }

    public boolean first() {
        try {
            cursor = 0;
            actualRow = rows.get(cursor);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean last() {
        try {
            // Request all remaining Row-Packets
//            while(requestNextRowPacket()) ;
            cursor = rows.size() - 1;
            actualRow = rows.get(cursor);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public int getRow() {
        return cursor + 1;
    }

    public boolean absolute(int row) throws SQLException {
        return setCursor(row - 1);
    }

    public boolean relative(int step) throws SQLException {
        return setCursor(cursor + step);
    }

    public boolean previous() throws SQLException {
        if(forwardOnly) {
            throw new SQLException("previous() not possible on Forward-Only-ResultSet");
        } else {
            if(cursor > 0) {
                actualRow = rows.get(--cursor);
                return true;
            } else {
                return false;
            }
        }
    }

    public void setFetchDirection(int direction) {
        fetchDirection = direction;
    }

    public int getFetchDirection() {
        return fetchDirection;
    }

    public void setFetchSize(int rows) {
    }

    public int getFetchSize() {
        return 0;
    }

    public int getType() {
        return forwardOnly ? ResultSet.TYPE_FORWARD_ONLY : ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    public int getConcurrency() {
        return ResultSet.CONCUR_READ_ONLY;
    }

    public boolean rowUpdated() {
        return false;
    }

    public boolean rowInserted() {
        return false;
    }

    public boolean rowDeleted() {
        return false;
    }

    public void updateNull(int columnIndex) {
        throw new UnsupportedOperationException("updateNull");
    }

    public void updateBoolean(int columnIndex, boolean x) {
        throw new UnsupportedOperationException("updateBoolean");
    }

    public void updateByte(int columnIndex, byte x) {
        throw new UnsupportedOperationException("updateByte");
    }

    public void updateShort(int columnIndex, short x) {
        throw new UnsupportedOperationException("updateShort");
    }

    public void updateInt(int columnIndex, int x) {
        throw new UnsupportedOperationException("updateInt");
    }

    public void updateLong(int columnIndex, long x) {
        throw new UnsupportedOperationException("updateLong");
    }

    public void updateFloat(int columnIndex, float x) {
        throw new UnsupportedOperationException("updateFloat");
    }

    public void updateDouble(int columnIndex, double x) {
        throw new UnsupportedOperationException("updateDouble");
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) {
        throw new UnsupportedOperationException("updateBigDecimal");
    }

    public void updateString(int columnIndex, String x) {
        throw new UnsupportedOperationException("updateString");
    }

    public void updateBytes(int columnIndex, byte[] x) {
        throw new UnsupportedOperationException("updateBytes");
    }

    public void updateDate(int columnIndex, Date x) {
        throw new UnsupportedOperationException("updateDate");
    }

    public void updateTime(int columnIndex, Time x) {
        throw new UnsupportedOperationException("updateTime");
    }

    public void updateTimestamp(int columnIndex, Timestamp x) {
        throw new UnsupportedOperationException("updateTimestamp");
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) {
        throw new UnsupportedOperationException("updateAsciiStream");
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) {
        throw new UnsupportedOperationException("updateBinaryStream");
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) {
        throw new UnsupportedOperationException("updateCharacterStream");
    }

    public void updateObject(int columnIndex, Object x, int scale) {
        throw new UnsupportedOperationException("updateObject");
    }

    public void updateObject(int columnIndex, Object x) {
        throw new UnsupportedOperationException("updateObject");
    }

    public void updateNull(String columnName) {
        throw new UnsupportedOperationException("updateNull");
    }

    public void updateBoolean(String columnName, boolean x) {
        throw new UnsupportedOperationException("updateBoolean");
    }

    public void updateByte(String columnName, byte x) {
        throw new UnsupportedOperationException("updateByte");
    }

    public void updateShort(String columnName, short x) {
        throw new UnsupportedOperationException("updateShort");
    }

    public void updateInt(String columnName, int x) {
        throw new UnsupportedOperationException("updateInt");
    }

    public void updateLong(String columnName, long x) {
        throw new UnsupportedOperationException("updateLong");
    }

    public void updateFloat(String columnName, float x) {
        throw new UnsupportedOperationException("updateFloat");
    }

    public void updateDouble(String columnName, double x) {
        throw new UnsupportedOperationException("updateDouble");
    }

    public void updateBigDecimal(String columnName, BigDecimal x) {
        throw new UnsupportedOperationException("updateBigDecimal");
    }

    public void updateString(String columnName, String x) {
        throw new UnsupportedOperationException("updateString");
    }

    public void updateBytes(String columnName, byte[] x) {
        throw new UnsupportedOperationException("updateBytes");
    }

    public void updateDate(String columnName, Date x) {
        throw new UnsupportedOperationException("updateDate");
    }

    public void updateTime(String columnName, Time x) {
        throw new UnsupportedOperationException("updateTime");
    }

    public void updateTimestamp(String columnName, Timestamp x) {
        throw new UnsupportedOperationException("updateTimestamp");
    }

    public void updateAsciiStream(String columnName, InputStream x, int length) {
        throw new UnsupportedOperationException("updateAsciiStream");
    }

    public void updateBinaryStream(String columnName, InputStream x, int length) {
        throw new UnsupportedOperationException("updateBinaryStream");
    }

    public void updateCharacterStream(String columnName, Reader reader, int length) {
        throw new UnsupportedOperationException("updateCharacterStream");
    }

    public void updateObject(String columnName, Object x, int scale) {
        throw new UnsupportedOperationException("updateObject");
    }

    public void updateObject(String columnName, Object x) {
        throw new UnsupportedOperationException("updateObject");
    }

    public void insertRow() {
        throw new UnsupportedOperationException("insertRow");
    }

    public void updateRow() {
        throw new UnsupportedOperationException("updateRow");
    }

    public void deleteRow() {
        throw new UnsupportedOperationException("deleteRow");
    }

    public void refreshRow() {
        throw new UnsupportedOperationException("refreshRow");
    }

    public void cancelRowUpdates() {
        throw new UnsupportedOperationException("cancelRowUpdates");
    }

    public void moveToInsertRow() {
        throw new UnsupportedOperationException("moveToInsertRow");
    }

    public void moveToCurrentRow() {
        throw new UnsupportedOperationException("moveToCurrentRow");
    }

    public Statement getStatement() {
        return statement;
    }

    public Object getObject(int i, Map map) {
        throw new UnsupportedOperationException("getObject");
    }

    public Ref getRef(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return (Ref)actualRow[columnIndex];
        }
        else {
            return null;
        }
    }

    public Blob getBlob(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return (Blob)actualRow[columnIndex];
        }
        else {
            return null;
        }
    }

    public Clob getClob(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return (Clob)actualRow[columnIndex];
        }
        else {
            return null;
        }
    }

    public Array getArray(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return (Array)actualRow[columnIndex];
        }
        else {
            return null;
        }
    }

    public Object getObject(String colName, Map map) {
        throw new UnsupportedOperationException("getObject");
    }

    public <T> T getObject(String columnName, Class<T> clazz) {
        throw new UnsupportedOperationException("getObject(String, Class)");
    }

    public <T> T getObject(int columnIndex, Class<T> clazz) {
        throw new UnsupportedOperationException("getObject(int, Class)");
    }

    public Ref getRef(String colName) throws SQLException {
        return getRef(getIndexForName(colName));
    }

    public Blob getBlob(String colName) throws SQLException {
        return getBlob(getIndexForName(colName));
    }

    public Clob getClob(String colName) throws SQLException {
        return getClob(getIndexForName(colName));
    }

    public Array getArray(String colName) throws SQLException {
        return getArray(getIndexForName(colName));
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            cal.setTime(getDate(columnIndex));
            return (Date)cal.getTime();
        }
        else {
            return null;
        }
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return getDate(getIndexForName(columnName), cal);
    }

    public Time getTime(int columnIndex, Calendar cal) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            Time time = (Time)actualRow[columnIndex];
            cal.setTime(time);
            return (Time)cal.getTime();
        }
        else {
            return null;
        }
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return getTime(getIndexForName(columnName), cal);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        Timestamp timestamp = getTimestamp(columnIndex);
        if(timestamp != null) {
            cal.setTime(timestamp);
            return (Timestamp)cal.getTime();
        }
        else {
            return null;
        }
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return getTimestamp(getIndexForName(columnName), cal);
    }

    public URL getURL(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return (URL)actualRow[columnIndex];
        }
        else {
            return null;
        }
    }

    public URL getURL(String columnName) throws SQLException {
        return getURL(getIndexForName(columnName));
    }

    public void updateRef(int columnIndex, Ref x) {
        throw new UnsupportedOperationException("updateRef");
    }

    public void updateRef(String columnName, Ref x) {
        throw new UnsupportedOperationException("updateRef");
    }

    public void updateBlob(int columnIndex, Blob x) {
        throw new UnsupportedOperationException("updateBlob");
    }

    public void updateBlob(String columnName, Blob x) {
        throw new UnsupportedOperationException("updateBlob");
    }

    public void updateClob(int columnIndex, Clob x) {
        throw new UnsupportedOperationException("updateClob");
    }

    public void updateClob(String columnName, Clob x) {
        throw new UnsupportedOperationException("updateClob");
    }

    public void updateArray(int columnIndex, Array x) {
        throw new UnsupportedOperationException("updateArray");
    }

    public void updateArray(String columnName, Array x) {
        throw new UnsupportedOperationException("updateArray");
    }

    private int getIndexForName(String name) throws SQLException {
        int result = -1;
        String nameLowercase = name.toLowerCase();
        // first search in the columns names (hit is very likely)
        for(int i = 0; i < columnNames.length; ++i) {
            if(columnNames[i].equals(nameLowercase)) {
                result = i;
                break;
            }
        }
        // not found ? then search in the labels
        if(result < 0) {
                for(int i = 0; i < columnLabels.length; ++i) {
                    if(columnLabels[i].equals(nameLowercase)) {
                        result = i;
                        break;
                    }
                }
        }
        if(result < 0) {
            throw new SQLException("Unknown column " + name);
        }
        else {
            lastReadColumn = result;
        }

        return result + 1;
    }

    private boolean preGetCheckNull(int index) {
        lastReadColumn = index;
        boolean wasNull = actualRow[lastReadColumn] == null;
        return !wasNull;
    }

    private boolean requestNextRowPacket() throws SQLException {
        if(!lastPartReached) {
            try {
                SerializableTransport st = (SerializableTransport)commandSink.process(remainingResultSet, new NextRowPacketCommand());
                RowPacket rsp = (RowPacket)st.getTransportee();
                if(rsp.isLastPart()) {
                    lastPartReached = true;
                }
                if(rsp.size() > 0) {
                    rows.merge(rsp);
                    return true;
                } else {
                    return false;
                }
            } catch(Exception e) {
                throw SQLExceptionHelper.wrap(e);
            }
        } else {
            return false;
        }
    }

    private boolean setCursor(int row) throws SQLException {
        if(row >= 0) {
            if(row < rows.size()) {
                cursor = row;
                actualRow = rows.get(cursor);
                return true;
            } else {
                // If new row is not in the range of the actually available
                // rows then try to load the next row packets successively
                while(requestNextRowPacket()) {
                    if(row < rows.size()) {
                        cursor = row;
                        actualRow = rows.get(cursor);
                        return true;
                    }
                }
                return false;
            }
        } else {
            return false;
        }
    }

    private Date getCleanDate(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    private Time getCleanTime(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.MILLISECOND, 0);
        return new Time(cal.getTimeInMillis());
    }

    /* start JDBC4 support */
    public RowId getRowId(int parameterIndex) throws SQLException {
        return (RowId)commandSink.process(remainingResultSet, CommandPool.getReflectiveCommand(JdbcInterfaceType.RESULTSETHOLDER, "getRowId",
                new Object[]{parameterIndex},
                ParameterTypeCombinations.INT));
    }

    public RowId getRowId(String parameterName) throws SQLException {
        return (RowId)commandSink.process(remainingResultSet, CommandPool.getReflectiveCommand(JdbcInterfaceType.RESULTSETHOLDER, "getRowId",
                new Object[]{parameterName},
                ParameterTypeCombinations.STR));
    }

    public void setRowId(String parameterName, RowId x) {
        throw new UnsupportedOperationException("setRowId");
    }

    public void updateRowId(int columnIndex, RowId x) {
        throw new UnsupportedOperationException("updateRowId");
    }

    public void updateRowId(String columnLabel, RowId x) {
        throw new UnsupportedOperationException("updateRowId");
    }

    public int getHoldability() throws SQLException {
        return commandSink.processWithIntResult(remainingResultSet, CommandPool.getReflectiveCommand(JdbcInterfaceType.RESULTSETHOLDER, "getHoldability"));
    }

    public boolean isClosed() {
        return (cursor < 0);
    }

    public void updateNString(int columnIndex, String nString) {
        throw new UnsupportedOperationException("updateNString");
    }

    public void updateNString(String columnLabel, String nString) {
        throw new UnsupportedOperationException("updateNString");
    }

    public void updateNClob(int columnIndex, NClob nClob) {
        throw new UnsupportedOperationException("updateNClob");
    }

    public void updateNClob(String columnLabel, NClob nClob) {
        throw new UnsupportedOperationException("updateNClob");
    }

    public NClob getNClob(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return (NClob)actualRow[columnIndex];
        }
        else {
            return null;
        }
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        return getNClob(getIndexForName(columnLabel));
    }

    public SQLXML getSQLXML(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return (SQLXML)actualRow[columnIndex];
        }
        else {
            return null;
        }
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return getSQLXML(getIndexForName(columnLabel));
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject) {
        throw new UnsupportedOperationException("updateSQLXML");
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject) {
        throw new UnsupportedOperationException("updateSQLXML");
    }

    public String getNString(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return actualRow[columnIndex].toString();
        } else {
            return null;
        }
    }

    public String getNString(String columnLabel) throws SQLException {
        return getNString(getIndexForName(columnLabel));
    }

    public Reader getNCharacterStream(int columnIndex) {
        columnIndex--;
        if(preGetCheckNull(columnIndex)) {
            return new StringReader((String)actualRow[columnIndex]);
        }
        else {
            return null;
        }
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return getNCharacterStream(getIndexForName(columnLabel));
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length) {
        throw new UnsupportedOperationException("updateNCharacterStream");
    }

    public void updateNCharacterStream(String columnLabel, Reader x, long length) {
        throw new UnsupportedOperationException("updateNCharacterStream");
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length) {
        throw new UnsupportedOperationException("updateAsciiStream");
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length) {
        throw new UnsupportedOperationException("updateBinaryStream");
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length) {
        throw new UnsupportedOperationException("updateCharacterStream");
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length) {
        throw new UnsupportedOperationException("updateAsciiStream");
    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length) {
        throw new UnsupportedOperationException("updateBinaryStream");
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) {
        throw new UnsupportedOperationException("updateCharacterStream");
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length) {
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) {
        throw new UnsupportedOperationException("updateBlob");
    }

    public void updateClob(int columnIndex, Reader reader, long length) {
        throw new UnsupportedOperationException("updateClob");
    }

    public void updateClob(String columnLabel, Reader reader, long length) {
        throw new UnsupportedOperationException("updateClob");
    }

    public void updateNClob(int columnIndex, Reader reader, long length) {
        throw new UnsupportedOperationException("updateNClob");
    }

    public void updateNClob(String columnLabel, Reader reader, long length) {
        throw new UnsupportedOperationException("updateNClob");
    }

    public void updateNCharacterStream(int columnIndex, Reader reader) {
        throw new UnsupportedOperationException("updateNCharacterStream");
    }

    public void updateNCharacterStream(String columnLabel, Reader reader) {
        throw new UnsupportedOperationException("updateNCharacterStream");
    }

    public void updateAsciiStream(int columnIndex, InputStream x) {
        throw new UnsupportedOperationException("updateAsciiStream");
    }

    public void updateBinaryStream(int columnIndex, InputStream x)  {
        throw new UnsupportedOperationException("updateBinaryStream");
    }

    public void updateCharacterStream(int columnIndex, Reader reader) {
        throw new UnsupportedOperationException("updateCharacterStream");
    }

    public void updateCharacterStream(String columnLabel, Reader reader) {
        throw new UnsupportedOperationException("updateCharacterStream");
    }

    public void updateAsciiStream(String columnLabel, InputStream x) {
        throw new UnsupportedOperationException("updateAsciiStream");
    }

    public void updateBinaryStream(String columnLabel, InputStream x) {
        throw new UnsupportedOperationException("updateBinaryStream");
    }

    public void updateBlob(int columnIndex, InputStream inputStream) {
        throw new UnsupportedOperationException("updateBlob");
    }

    public void updateBlob(String columnLabel, InputStream inputStream) {
        throw new UnsupportedOperationException("updateBlob");
    }

    public void updateClob(int columnIndex, Reader reader) {
        throw new UnsupportedOperationException("updateClob");
    }

    public void updateClob(String columnLabel, Reader reader) {
        throw new UnsupportedOperationException("updateClob");
    }

    public void updateNClob(int columnIndex, Reader reader) {
        throw new UnsupportedOperationException("updateNClob");
    }

    public void updateNClob(String columnLabel, Reader reader) {
        throw new UnsupportedOperationException("updateNClob");
    }

    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(StreamingResultSet.class);
    }

    public <T> T unwrap(Class<T> iface) {
        return (T)this;
    }
    /* end JDBC4 support */
}
