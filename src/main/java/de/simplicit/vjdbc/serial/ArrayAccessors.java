//VJDBC - Virtual JDBC
//Written by Michael Link
//Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * 数组访问类
 */
class ArrayAccessors {
    private static final Map<Class, ArrayAccess> arrayAccessors = new HashMap<Class, ArrayAccess>();
    private static final ArrayAccess objectArrayFiller = new ArrayAccessObject();

    //加载数组类型
    static {
        arrayAccessors.put(Boolean.TYPE, new ArrayAccessBoolean());
        arrayAccessors.put(Byte.TYPE, new ArrayAccessByte());
        arrayAccessors.put(Character.TYPE, new ArrayAccessCharacter());
        arrayAccessors.put(Short.TYPE, new ArrayAccessShort());
        arrayAccessors.put(Integer.TYPE, new ArrayAccessInteger());
        arrayAccessors.put(Long.TYPE, new ArrayAccessLong());
        arrayAccessors.put(Float.TYPE, new ArrayAccessFloat());
        arrayAccessors.put(Double.TYPE, new ArrayAccessDouble());
    }

    //获取数组基础类型
    static ArrayAccess getArrayAccessorForPrimitiveType(Class primitiveType) {
        return arrayAccessors.get(primitiveType);
    }

    //获取数组的内容（object类型）
    static ArrayAccess getObjectArrayAccessor() {
        return objectArrayFiller;
    }

    /**
     * 访问数组中的某个布尔型变量值Boolean
     */
    private static class ArrayAccessBoolean implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getBoolean(array, index);
            }
        }
    }

    /**
     * 访问数组中的某个比特型变量值Byte
     */
    private static class ArrayAccessByte implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getByte(array, index);
            }
        }
    }

    /**
     * 访问数组中的某个字符型变量值Char
     */
    private static class ArrayAccessCharacter implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getChar(array, index);
            }
        }
    }

    /**
     * 访问数组中的某个短整型型变量值Short
     */
    private static class ArrayAccessShort implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getShort(array, index);
            }
        }
    }

    /**
     * 访问数组中的某个整数型变量值(int)
     */
    private static class ArrayAccessInteger implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getInt(array, index);
            }
        }
    }

    /**
     * 访问数组中的某个long型变量值
     */
    private static class ArrayAccessLong implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getLong(array, index);
            }
        }
    }

    /**
     * 访问数组中的某个Float型变量值
     */
    private static class ArrayAccessFloat implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getFloat(array, index);
            }
        }
    }

    /**
     * 访问数组中的某个double型变量值
     */
    private static class ArrayAccessDouble implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getDouble(array, index);
            }
        }
    }

    /**
     * 访问数组中的某个object型变量值
     */
    private static class ArrayAccessObject implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            return Array.get(array, index);
        }
    }
}