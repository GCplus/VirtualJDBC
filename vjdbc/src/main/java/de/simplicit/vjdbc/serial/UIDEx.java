// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * UIDEx 类型是作者构造的类似于指针的理念,指向result类型数据的id
 */
public class UIDEx implements Externalizable {
    static final long serialVersionUID = 1682984916549281270L;

    private static long sCookie = 1;

    private Long uid = sCookie++;
    private int value1 = Integer.MIN_VALUE;
    private int value2 = Integer.MIN_VALUE;

    /**
     * 以下全是构造方法
     * 抽象一个空的构造方法
     */
    public UIDEx() {
    }

    public UIDEx(int value1) {
        this.value1 = value1;
    }

    public UIDEx(int value1, int value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public UIDEx(Long uid, int value1) {
        this.uid = uid;
        this.value1 = value1;
    }

    public UIDEx(Long uid, int value1, int value2) {
        this.uid = uid;
        this.value1 = value1;
        this.value2 = value2;
    }
    /**
     * 以上全是构造方法
     */

    public Long getUID() {
        return uid;
    }

    public int getValue1() {
        return value1;
    }

    public int getValue2() {
        return value2;
    }

    /**
     * 重置参数,数值1和数值2会变成int类型最小值
     */
    public void resetValues() {
        value1 = Integer.MIN_VALUE;
        value2 = Integer.MIN_VALUE;
    }

    public int hashCode() {
        return uid.hashCode();
    }

    public boolean equals(Object obj) {
        return (obj instanceof UIDEx) && (uid.equals(((UIDEx)obj).uid));
    }

    public String toString() {
        return uid.toString();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(uid);
        out.writeInt(value1);
        out.writeInt(value2);
    }

    public void readExternal(ObjectInput in) throws IOException {
        uid = in.readLong();
        value1 = in.readInt();
        value2 = in.readInt();
    }
}
