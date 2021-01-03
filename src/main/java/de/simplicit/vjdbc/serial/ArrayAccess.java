//VJDBC - Virtual JDBC
//Written by Michael Link
//Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.serial;

/**
 * 获取数组内的对应编号的数值
 */
interface ArrayAccess {
    Object getValue(Object array, int index, boolean[] nullFlags);
}
