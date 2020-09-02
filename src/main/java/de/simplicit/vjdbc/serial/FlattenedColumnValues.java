//VJDBC - Virtual JDBC
//Written by Michael Link
//Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;

class FlattenedColumnValues implements Externalizable {
    private static final long serialVersionUID = 3691039872299578672L;
    
    private Object arrayOfValues;
    private boolean[] nullFlags;
    
    private transient ArrayAccess arrayAccessor;
    
    /**
     * Default constructor needed for Serialisation.
     *
     */
    public FlattenedColumnValues() {
    }
    
    FlattenedColumnValues(Class clazz, int size) {
        // Any of these types ? boolean, byte, char, short, int, long, float, and double
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
        
    void ensureCapacity(int minCapacity) {
        // This algorithm is actually copied from the ArrayList implementation. Seems to
        // be a good strategy to grow a statically sized array.
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
