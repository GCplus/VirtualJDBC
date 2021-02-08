//VJDBC - Virtual JDBC
//Written by Michael Link
//Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;

/**
 * 继承外部序列化，对指定数据进行序列化和反序列化
 */
class FlattenedColumnValues implements Externalizable {
    private static final long serialVersionUID = 3691039872299578672L;

    private Object arrayOfValues;

    private boolean[] nullFlags;

    private transient ArrayAccess arrayAccessor;

    /**
     * Default constructor needed for Serialisation.
     * 序列化所需的默认构造函数。
     */
    public FlattenedColumnValues() {
    }

    FlattenedColumnValues(Class clazz, int size) {
        // Any of these types ? boolean, byte, char, short, int, long, float, and double
        // 这些类型中的任何一种？ 布尔值，字节，字符，短整数，整数，长整数，浮点数和双精度
        if(clazz.isPrimitive()) {
            arrayOfValues = Array.newInstance(clazz, size);
            nullFlags = new boolean[size];
            arrayAccessor = ArrayAccessors.getArrayAccessorForPrimitiveType(clazz);
        }
        else {
            arrayOfValues = Array.newInstance(clazz, size);
            nullFlags = null;
            arrayAccessor = ArrayAccessors.getObjectArrayAccessor();
        }
    }

    /**
     * 反序列化
     * @param in ObjectInput
     * @throws IOException IOException
     * @throws ClassNotFoundException ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        arrayOfValues = in.readObject();
        nullFlags = (boolean[])in.readObject();
        Class componentType = arrayOfValues.getClass().getComponentType();
        if(componentType.isPrimitive()) {
            arrayAccessor = ArrayAccessors.getArrayAccessorForPrimitiveType(componentType);
        }
        else {
            arrayAccessor = ArrayAccessors.getObjectArrayAccessor();
        }
    }

    /**
     * 序列化
     * @param out ObjectOutput
     * @throws IOException IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(arrayOfValues);
        out.writeObject(nullFlags);
    }

    void setObject(int index, Object value) {
        ensureCapacity(index + 1);
        Array.set(arrayOfValues, index, value);
    }

    void setBoolean(int index, boolean value) {
        ensureCapacity(index + 1);
        Array.setBoolean(arrayOfValues, index, value);
    }

    void setByte(int index, byte value) {
        ensureCapacity(index + 1);
        Array.setByte(arrayOfValues, index, value);
    }

    void setShort(int index, short value) {
        ensureCapacity(index + 1);
        Array.setShort(arrayOfValues, index, value);
    }

    void setInt(int index, int value) {
        ensureCapacity(index + 1);
        Array.setInt(arrayOfValues, index, value);
    }

    void setLong(int index, long value) {
        ensureCapacity(index + 1);
        Array.setLong(arrayOfValues, index, value);
    }

    void setFloat(int index, float value) {
        ensureCapacity(index + 1);
        Array.setFloat(arrayOfValues, index, value);
    }

    void setDouble(int index, double value) {
        ensureCapacity(index + 1);
        Array.setDouble(arrayOfValues, index, value);
    }

    void setIsNull(int index) {
        ensureCapacity(index + 1);
        if(nullFlags != null) {
            nullFlags[index] = true;
        }
    }

    Object getValue(int index) {
        return arrayAccessor.getValue(arrayOfValues, index, nullFlags);
    }

    // This algorithm is actually copied from the ArrayList implementation. Seems to
    // be a good strategy to grow a statically sized array.
    // 该算法实际上是从ArrayList实现中复制的。 似乎是控制静态数组增长大小的的好策略。
    void ensureCapacity(int minCapacity) {
        int oldCapacity = Array.getLength(arrayOfValues);
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            Object tmpArrayOfValues = arrayOfValues;
            arrayOfValues = Array.newInstance(tmpArrayOfValues.getClass().getComponentType(), newCapacity);
            System.arraycopy(tmpArrayOfValues, 0, arrayOfValues, 0, Array.getLength(tmpArrayOfValues));
            if(nullFlags != null) {
                boolean[] tmpNullFlags = nullFlags;
                nullFlags = new boolean[newCapacity];
                System.arraycopy(tmpNullFlags, 0, nullFlags, 0, tmpNullFlags.length);
            }
        }
    }
}
