// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SerialResultSetMetaData implements ResultSetMetaData, Externalizable {
    static final long serialVersionUID = 9034215340975782405L;

    private int columnCount;

    private String[] catalogName;
    private String[] schemaName;
    private String[] tableName;
    private String[] columnClassName;
    private String[] columnLabel;
    private String[] columnName;
    private String[] columnTypeName;

    private Integer[] columnType;
    private Integer[] columnDisplaySize;
    private Integer[] precision;
    private Integer[] scale;
    private Integer[] nullable;

    private Boolean[] autoIncrement;
    private Boolean[] caseSensitive;
    private Boolean[] currency;
    private Boolean[] readOnly;
    private Boolean[] searchable;
    private Boolean[] signed;
    private Boolean[] writable;
    private Boolean[] definitivelyWritable;

    public SerialResultSetMetaData() {
    }

    public SerialResultSetMetaData(ResultSetMetaData rsmd) throws SQLException {
        this.columnCount = rsmd.getColumnCount();

        allocateArrays();
        fillArrays(rsmd);
    }

    public String[] readStringArr(ObjectInput in) throws IOException
    {
        int numElems = in.readShort();
        if (numElems != -1) {
            String[] ret = new String[numElems];
            for (int i = 0; i < numElems; i++) {
                byte notNull = in.readByte();
                if (1 == notNull) {
                    ret[i] = in.readUTF();
                } else {
                    ret[i] = null;
                }
            }
            return ret;
        }
        return null;
    }

    public void writeStringArr(String[] arr, ObjectOutput out)
        throws IOException
    {
        if (arr != null) {
            out.writeShort(arr.length);
            for (String s : arr) {
                if (s != null) {
                    out.writeByte(1);
                    out.writeUTF(s);
                } else {
                    out.writeByte(0);
                }
            }
        } else {
            out.writeShort(-1);
        }
    }

    public Integer[] readIntArr(ObjectInput in) throws IOException
    {
        int numElems = in.readShort();
        if (numElems != -1) {
            Integer[] ret = new Integer[numElems];
            for (int i = 0; i < numElems; i++) {
                ret[i] = in.readInt();
            }
            return ret;
        }
        return null;
    }

    public void writeIntArr(Integer[] arr, ObjectOutput out)
        throws IOException
    {
        if (arr != null) {
            out.writeShort(arr.length);
            for (Integer integer : arr) {
                out.writeInt(integer);
            }
        } else {
            out.writeShort(-1);
        }
    }

    public Boolean[] readBooleanArr(ObjectInput in) throws IOException
    {
        int numElems = in.readShort();
        if (numElems != -1) {
            Boolean[] ret = new Boolean[numElems];
            for (int i = 0; i < numElems; i++) {
                ret[i] = in.readBoolean();
            }
            return ret;
        }
        return null;
    }

    public void writeBooleanArr(Boolean[] arr, ObjectOutput out)
        throws IOException
    {
        if (arr != null) {
            out.writeShort(arr.length);
            for (Boolean aBoolean : arr) {
                out.writeBoolean(aBoolean);
            }
        } else {
            out.writeShort(-1);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        columnCount = in.readInt();

        catalogName = readStringArr(in);
        schemaName = readStringArr(in);
        tableName = readStringArr(in);
        columnClassName = readStringArr(in);
        columnLabel = readStringArr(in);
        columnName = readStringArr(in);
        columnTypeName = readStringArr(in);

        columnType = readIntArr(in);
        columnDisplaySize = readIntArr(in);
        precision = readIntArr(in);
        scale = readIntArr(in);
        nullable = readIntArr(in);

        autoIncrement = readBooleanArr(in);
        caseSensitive = readBooleanArr(in);
        currency = readBooleanArr(in);
        readOnly = readBooleanArr(in);
        searchable = readBooleanArr(in);
        signed = readBooleanArr(in);
        writable = readBooleanArr(in);
        definitivelyWritable = readBooleanArr(in);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(columnCount);

        writeStringArr(catalogName, out);
        writeStringArr(schemaName, out);
        writeStringArr(tableName, out);
        writeStringArr(columnClassName, out);
        writeStringArr(columnLabel, out);
        writeStringArr(columnName, out);
        writeStringArr(columnTypeName, out);

        writeIntArr(columnType, out);
        writeIntArr(columnDisplaySize, out);
        writeIntArr(precision, out);
        writeIntArr(scale, out);
        writeIntArr(nullable, out);

        writeBooleanArr(autoIncrement, out);
        writeBooleanArr(caseSensitive, out);
        writeBooleanArr(currency, out);
        writeBooleanArr(readOnly, out);
        writeBooleanArr(searchable, out);
        writeBooleanArr(signed, out);
        writeBooleanArr(writable, out);
        writeBooleanArr(definitivelyWritable, out);
    }

    private void allocateArrays() {
        catalogName = new String[columnCount];
        schemaName = new String[columnCount];
        tableName = new String[columnCount];
        columnClassName = new String[columnCount];
        columnLabel = new String[columnCount];
        columnName = new String[columnCount];
        columnTypeName = new String[columnCount];

        columnDisplaySize = new Integer[columnCount];
        columnType = new Integer[columnCount];
        precision = new Integer[columnCount];
        scale = new Integer[columnCount];
        nullable = new Integer[columnCount];

        autoIncrement = new Boolean[columnCount];
        caseSensitive = new Boolean[columnCount];
        currency = new Boolean[columnCount];
        readOnly = new Boolean[columnCount];
        searchable = new Boolean[columnCount];
        signed = new Boolean[columnCount];
        writable = new Boolean[columnCount];
        definitivelyWritable = new Boolean[columnCount];
    }

    private void fillArrays(ResultSetMetaData rsmd) {
        for(int i = 0; i < columnCount; i++) {
            int col = i + 1;

            try {
                catalogName[i] = rsmd.getCatalogName(col);
            } catch(Exception e) {
                catalogName[i] = null;
            }

            try {
                schemaName[i] = rsmd.getSchemaName(col);
            } catch(Exception e1) {
                schemaName[i] = null;
            }

            try {
                tableName[i] = rsmd.getTableName(col);
            } catch(Exception e2) {
                tableName[i] = null;
            }

            try {
                columnLabel[i] = rsmd.getColumnLabel(col);
            } catch(Exception e3) {
                columnLabel[i] = null;
            }

            try {
                columnName[i] = rsmd.getColumnName(col);
            } catch(Exception e4) {
                columnName[i] = null;
            }

            try {
                columnClassName[i] = rsmd.getColumnClassName(col);
            } catch(Exception e5) {
                columnClassName[i] = null;
            }

            try {
                columnTypeName[i] = rsmd.getColumnTypeName(col);
            } catch(Exception e6) {
                columnTypeName[i] = null;
            }

            try {
                columnDisplaySize[i] = rsmd.getColumnDisplaySize(col);
            } catch(Exception e7) {
                columnDisplaySize[i] = null;
            }

            try {
                columnType[i] = rsmd.getColumnType(col);
            } catch(Exception e8) {
                columnType[i] = null;
            }

            try {
                precision[i] = rsmd.getPrecision(col);
            } catch(Exception e9) {
                precision[i] = null;
            }

            try {
                scale[i] = rsmd.getScale(col);
            } catch(Exception e10) {
                scale[i] = null;
            }

            try {
                nullable[i] = rsmd.isNullable(col);
            } catch(Exception e11) {
                nullable[i] = null;
            }

            try {
                autoIncrement[i] = rsmd.isAutoIncrement(col) ? Boolean.TRUE : Boolean.FALSE;
            } catch(Exception e12) {
                autoIncrement[i] = null;
            }

            try {
                caseSensitive[i] = rsmd.isCaseSensitive(col) ? Boolean.TRUE : Boolean.FALSE;
            } catch(Exception e13) {
                caseSensitive[i] = null;
            }

            try {
                currency[i] = rsmd.isCurrency(col) ? Boolean.TRUE : Boolean.FALSE;
            } catch(Exception e14) {
                currency[i] = null;
            }

            try {
                readOnly[i] = rsmd.isReadOnly(col) ? Boolean.TRUE : Boolean.FALSE;
            } catch(Exception e15) {
                readOnly[i] = null;
            }

            try {
                searchable[i] = rsmd.isSearchable(col) ? Boolean.TRUE : Boolean.FALSE;
            } catch(Exception e16) {
                searchable[i] = null;
            }

            try {
                signed[i] = rsmd.isSigned(col) ? Boolean.TRUE : Boolean.FALSE;
            } catch(Exception e17) {
                signed[i] = null;
            }

            try {
                writable[i] = rsmd.isWritable(col) ? Boolean.TRUE : Boolean.FALSE;
            } catch(Exception e18) {
                writable[i] = null;
            }

            try {
                definitivelyWritable[i] = rsmd.isDefinitelyWritable(col) ? Boolean.TRUE : Boolean.FALSE;
            } catch(Exception e18) {
                definitivelyWritable[i] = null;
            }
        }
    }

    public int getColumnCount() {
        return columnCount;
    }

    public boolean isAutoIncrement(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(autoIncrement[column - 1]);
        return autoIncrement[column - 1];
    }

    public boolean isCaseSensitive(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(caseSensitive[column - 1]);
        return caseSensitive[column - 1];
    }

    public boolean isSearchable(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(searchable[column - 1]);
        return searchable[column - 1];
    }

    public boolean isCurrency(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(currency[column - 1]);
        return currency[column - 1];
    }

    public int isNullable(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(nullable[column - 1]);
        return nullable[column - 1];
    }

    public boolean isSigned(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(signed[column - 1]);
        return signed[column - 1];
    }

    public int getColumnDisplaySize(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(columnDisplaySize[column - 1]);
        return columnDisplaySize[column - 1];
    }

    public String getColumnLabel(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(columnLabel[column - 1]);
        return columnLabel[column - 1];
    }

    public String getColumnName(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(columnName[column - 1]);
        return columnName[column - 1];
    }

    public String getSchemaName(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(schemaName[column - 1]);
        return schemaName[column - 1];
    }

    public int getPrecision(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(precision[column - 1]);
        return precision[column - 1];
    }

    public int getScale(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(scale[column - 1]);
        return scale[column - 1];
    }

    public String getTableName(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(tableName[column - 1]);
        return tableName[column - 1];
    }

    public String getCatalogName(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(catalogName[column - 1]);
        return catalogName[column - 1];
    }

    public int getColumnType(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(columnType[column - 1]);
        return columnType[column - 1];
    }

    public String getColumnTypeName(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(columnTypeName[column - 1]);
        return columnTypeName[column - 1];
    }

    public boolean isReadOnly(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(readOnly[column - 1]);
        return readOnly[column - 1];
    }

    public boolean isWritable(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(writable[column - 1]);
        return writable[column - 1];
    }

    public boolean isDefinitelyWritable(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(definitivelyWritable[column - 1]);
        return definitivelyWritable[column - 1];
    }

    public String getColumnClassName(int column) throws SQLException {
        checkColumnIndex(column);
        throwIfNull(columnClassName[column - 1]);
        return columnClassName[column - 1];
    }

    public void setColumnCount(int columnCount) throws SQLException {
        if (columnCount < 0) {
            throw new SQLException("invalid number of columns " + columnCount);
        }
        this.columnCount = columnCount;
        allocateArrays();
    }

    public void setAutoIncrement(int columnIndex, boolean property)
        throws SQLException {
        checkColumnIndex(columnIndex);
        autoIncrement[columnIndex - 1] = property;
    }

    public void setCaseSensitive(int columnIndex, boolean property)
        throws SQLException {
        checkColumnIndex(columnIndex);
        caseSensitive[columnIndex - 1] = property;
    }

    public void setCatalogName(int columnIndex, String catalogName)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.catalogName[columnIndex - 1] = catalogName;
    }

    public void setColumnDisplaySize(int columnIndex, int size)
        throws SQLException {
        checkColumnIndex(columnIndex);
        columnDisplaySize[columnIndex - 1] = size;
    }

    public void setColumnLabel(int columnIndex, String label)
        throws SQLException {
        checkColumnIndex(columnIndex);
        columnLabel[columnIndex - 1] = label;
    }

    public void setColumnName(int columnIndex, String columnName)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.columnName[columnIndex - 1] = columnName;
    }

    public void setColumnType(int columnIndex, int SQLType)
        throws SQLException {
        checkColumnIndex(columnIndex);
        columnType[columnIndex - 1] = SQLType;
    }

    public void setColumnTypeName(int columnIndex, String typeName)
        throws SQLException {
        checkColumnIndex(columnIndex);
        columnTypeName[columnIndex - 1] = typeName;
    }

    public void setCurrency(int columnIndex, boolean property)
        throws SQLException {
        checkColumnIndex(columnIndex);
        currency[columnIndex - 1] = property;
    }

    public void setNullable(int columnIndex, int property)
        throws SQLException {
        checkColumnIndex(columnIndex);
        nullable[columnIndex - 1] = property;
    }

    public void setPrecision(int columnIndex, int precision)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.precision[columnIndex - 1] = precision;
    }

    public void setScale(int columnIndex, int scale)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.scale[columnIndex - 1] = scale;
    }

    public void setSchemaName(int columnIndex, String schemaName)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.schemaName[columnIndex - 1] = schemaName;
    }

    public void setSearchable(int columnIndex, boolean property)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.searchable[columnIndex - 1] = property;
    }

    public void setSigned(int columnIndex, boolean property)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.signed[columnIndex - 1] = property;
    }

    public void setTableName(int columnIndex, String tableName)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.tableName[columnIndex - 1] = tableName;
    }

    public void setReadOnly(int columnIndex, boolean readOnly)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.readOnly[columnIndex - 1] = readOnly;
    }

    public void setWritable(int columnIndex, boolean writable)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.writable[columnIndex - 1] = writable;
    }

    public void setDefinitelyWritable(int columnIndex, boolean writable)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.definitivelyWritable[columnIndex - 1] = writable;
    }

    public void setColumnClassName(int columnIndex, String columnClassName)
        throws SQLException {
        checkColumnIndex(columnIndex);
        this.columnClassName[columnIndex - 1] = columnClassName;
    }

    private void throwIfNull(Object obj) throws SQLException {
        if(obj == null) {
            throw new SQLException("Method not supported");
        }
    }

    private void checkColumnIndex(int columnIndex) throws SQLException {
        if (columnIndex < 1 || columnIndex > columnCount) {
            throw new SQLException("invalid column index " + columnIndex);
        }
    }

    /* start JDBC4 support */
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(SerialResultSetMetaData.class);
    }

    public <T> T unwrap(Class<T> iface) {
        return (T)this;
    }
    /* end JDBC4 support */
}

