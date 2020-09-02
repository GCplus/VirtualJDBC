//VJDBC - Virtual JDBC
//Written by Michael Link
//Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

class ArrayAccessors {
    private static final Map<Class, ArrayAccess> arrayAccessors = new HashMap<Class, ArrayAccess>();
    private static final ArrayAccess objectArrayFiller = new ArrayAccessObject();

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

    static ArrayAccess getArrayAccessorForPrimitiveType(Class primitiveType) {
        return arrayAccessors.get(primitiveType);
    }

    static ArrayAccess getObjectArrayAccessor() {
        return objectArrayFiller;
    }

    private static class ArrayAccessBoolean implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getBoolean(array, index);
            }
        }
    }

    private static class ArrayAccessByte implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getByte(array, index);
            }
        }
    }

    private static class ArrayAccessCharacter implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getChar(array, index);
            }
        }
    }

    private static class ArrayAccessShort implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getShort(array, index);
            }
        }
    }

    private static class ArrayAccessInteger implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getInt(array, index);
            }
        }
    }

    private static class ArrayAccessLong implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getLong(array, index);
            }
        }
    }

    private static class ArrayAccessFloat implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getFloat(array, index);
            }
        }
    }

    private static class ArrayAccessDouble implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            if(nullFlags[index]) {
                return null;
            } else {
                return Array.getDouble(array, index);
            }
        }
    }

    private static class ArrayAccessObject implements ArrayAccess {
        public Object getValue(Object array, int index, boolean[] nullFlags) {
            return Array.get(array, index);
        }
    }
}