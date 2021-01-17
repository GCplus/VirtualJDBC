// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import de.simplicit.vjdbc.util.JavaVersionInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.*;
import java.util.ArrayList;

/**
 * A RowPacket contains the data of a part (or a whole) JDBC-ResultSet.
 * RowPacket包含部分（或整个）JDBC-ResultSet的数据。
 */
public class RowPacket implements Externalizable {
    private static final Log logger = LogFactory.getLog(RowPacket.class);
    static final long serialVersionUID = 6366194574502000718L;

    private static final int ORACLE_ROW_ID = -8; // oracle数据库默认row ID
    private static final int DEFAULT_ARRAY_SIZE = 100; // oracle数据库默认行限制


    private int rowCount = 0;
    private boolean forwardOnly = false;
    private boolean lastPart = false;

    // Transient attributes
    // 临时属性
    private transient FlattenedColumnValues[] flattenedColumnsValues = null;
    private transient ArrayList<Object[]> rows = null;
    private transient int[] columnTypes = null;
    private transient int offset = 0;
    private transient int maxRows = 0;

    public RowPacket() {
    }

    public RowPacket(int packetsize, boolean forwardOnly) {
        this.maxRows = packetsize;
        this.forwardOnly = forwardOnly;
    }

    /**
     * 序列化
     * @param out ObjectOutput
     * @throws IOException IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(forwardOnly);
        out.writeBoolean(lastPart);
        out.writeInt(rowCount);
        if(rowCount > 0) {
            out.writeObject(flattenedColumnsValues);
        }
    }

    /**
     * 反序列化
     * @param in ObjectInput
     * @throws IOException IOException
     * @throws ClassNotFoundException ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        forwardOnly = in.readBoolean();
        lastPart = in.readBoolean();
        rowCount = in.readInt();
        if(rowCount > 0) {
            FlattenedColumnValues[] flattenedColumns = (FlattenedColumnValues[]) in.readObject();
            rows = new ArrayList<>(rowCount);
            for(int i = 0; i < rowCount; i++) {
                Object[] row = new Object[flattenedColumns.length];
                for(int j = 0; j < flattenedColumns.length; j++) {
                    row[j] = flattenedColumns[j].getValue(i);
                }
                rows.add(row);
            }
        }
        else {
            rows = new ArrayList<>();
        }
    }

    /**
     * 获取对应行号的数据
     * @param index 行号
     * @return Object[]
     * @throws SQLException SQLException
     */
    public Object[] get(int index) throws SQLException {
        int adjustedIndex = index - offset;

        if(adjustedIndex < 0) {
            throw new SQLException("Index " + index + " is below the possible index");
        } else if(adjustedIndex >= rows.size()) {
            throw new SQLException("Index " + index + " is above the possible index");
        } else {
            return rows.get(adjustedIndex);
        }
    }

    /**
     * 查询大小
     * @return int
     */
    public int size() {
        return offset + rowCount;
    }

    /**
     * 是否为最后一部分
     * @return boolean
     */
    public boolean isLastPart() {
        return lastPart;
    }

    public boolean populate(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        int columnCount = metaData.getColumnCount();
        rowCount = 0;

        while (rs.next()) {
            if(rowCount == 0) {
                prepareFlattenedColumns(metaData, columnCount);
            }

            for(int i = 1; i <= columnCount; i++) {
                boolean foundMatch = true;

                int internalIndex = i - 1;

                switch (columnTypes[internalIndex]) {
                case Types.NULL:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, null);
                    break;

                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, rs.getString(i));
                    break;

                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.LONGNVARCHAR:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, rs.getNString(i));
                    break;

                case Types.NUMERIC:
                case Types.DECIMAL:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, rs.getBigDecimal(i));
                    break;

                case Types.BIT:
                    flattenedColumnsValues[internalIndex].setBoolean(rowCount, rs.getBoolean(i));
                    break;

                case Types.TINYINT:
                    flattenedColumnsValues[internalIndex].setByte(rowCount, rs.getByte(i));
                    break;

                case Types.SMALLINT:
                    flattenedColumnsValues[internalIndex].setShort(rowCount, rs.getShort(i));
                    break;

                case Types.INTEGER:
                    flattenedColumnsValues[internalIndex].setInt(rowCount, rs.getInt(i));
                    break;

                case Types.BIGINT:
                    flattenedColumnsValues[internalIndex].setLong(rowCount, rs.getLong(i));
                    break;

                case Types.REAL:
                    flattenedColumnsValues[internalIndex].setFloat(rowCount, rs.getFloat(i));
                    break;

                case Types.FLOAT:
                case Types.DOUBLE:
                    flattenedColumnsValues[internalIndex].setDouble(rowCount, rs.getDouble(i));
                    break;

                case Types.DATE:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, rs.getDate(i));
                    break;

                case Types.TIME:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, rs.getTime(i));
                    break;

                case Types.TIMESTAMP:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, rs.getTimestamp(i));
                    break;

                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, rs.getBytes(i));
                    break;

                case Types.JAVA_OBJECT:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, new SerialJavaObject(rs.getObject(i)));
                    break;

                case Types.CLOB:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, new SerialClob(rs.getClob(i)));
                    break;

                case Types.NCLOB:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, new SerialNClob(rs.getNClob(i)));
                    break;

                case Types.BLOB:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, new SerialBlob(rs.getBlob(i)));
                    break;

                case Types.ARRAY:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, new SerialArray(rs.getArray(i)));
                    break;

                case Types.STRUCT:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, new SerialStruct((Struct) rs.getObject(i)));
                    break;

                case ORACLE_ROW_ID:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, new SerialRowId(rs.getRowId(i)));
                    break;

                    // what oracle does instead of SQLXML in their 1.6 driver,
                    // don't ask me why, commented out so we don't need
                    // an oracle driver to compile this class
                    //oracle在其1.6驱动程序中代替SQLXML做什么，不要问我为什么，注释掉了，所以我们不需要oracle驱动程序来编译此类
                    //case 2007:
                    //flattenedColumnsValues[internalIndex].setObject(rowCount, new XMLType(((OracleResultSet)rs).getOPAQUE(i)));
                case Types.SQLXML:
                    flattenedColumnsValues[internalIndex].setObject(rowCount, new SerialSQLXML(rs.getSQLXML(i)));
                    break;

                default:
                    if(JavaVersionInfo.use16Api) {
                        if(columnTypes[internalIndex] == Types.BOOLEAN) {
                            flattenedColumnsValues[internalIndex].setBoolean(rowCount, rs.getBoolean(i));
                        }
                        else {
                            foundMatch = false;
                        }
                    } else {
                        foundMatch = false;
                    }
                    break;
                }

                if(foundMatch) {
                    if(rs.wasNull()) {
                        flattenedColumnsValues[internalIndex].setIsNull(rowCount);
                    }
                } else {
                    throw new SQLException("Unsupported JDBC-Type: " + columnTypes[internalIndex]);
                }
            }

            rowCount++;

            if(maxRows > 0 && rowCount == maxRows) {
                break;
            }
        }

        lastPart = maxRows == 0 || rowCount < maxRows;

        return lastPart;
    }

    private void prepareFlattenedColumns(ResultSetMetaData metaData, int columnCount) throws SQLException {
        columnTypes = new int[columnCount];
        flattenedColumnsValues = new FlattenedColumnValues[columnCount];

        for(int i = 1; i <= columnCount; i++) {
            int columnType = columnTypes[i - 1] = metaData.getColumnType(i);

            if(logger.isDebugEnabled()) {
                logger.debug("Column-Type " + i + ": " + metaData.getColumnType(i));
            }

            Class componentType;

            switch (columnType) {
            case Types.BIT:
                componentType = Boolean.TYPE;
                break;

            case Types.TINYINT:
                componentType = Byte.TYPE;
                break;

            case Types.SMALLINT:
                componentType = Short.TYPE;
                break;

            case Types.INTEGER:
                componentType = Integer.TYPE;
                break;

            case Types.BIGINT:
                componentType = Long.TYPE;
                break;

            case Types.REAL:
                componentType = Float.TYPE;
                break;

            case Types.FLOAT:
            case Types.DOUBLE:
                componentType = Double.TYPE;
                break;

            default:
                if(JavaVersionInfo.use16Api) {
                    if(columnType == Types.BOOLEAN) {
                        componentType = Boolean.TYPE;
                    } else {
                        componentType = Object.class;
                    }
                } else {
                    componentType = Object.class;
                }
                break;
            }

            flattenedColumnsValues[i - 1] = new FlattenedColumnValues(componentType, maxRows == 0 ? DEFAULT_ARRAY_SIZE : maxRows);
        }
    }

    public void merge(RowPacket rsp) {
        if(forwardOnly) {
            offset += rowCount;
            rowCount = rsp.rowCount;
            rows = rsp.rows;
        } else {
            rows.addAll(rsp.rows);
            rowCount = rows.size();
        }
    }
}
