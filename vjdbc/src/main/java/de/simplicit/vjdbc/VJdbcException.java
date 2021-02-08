// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

/**
 * 自定义异常处理
 */
public class VJdbcException extends Exception {
    static final long serialVersionUID = -7552211448253764663L;

    public VJdbcException(String msg) {
        super(msg);
    }

    public VJdbcException(String msg, Exception ex) {
        super(msg, ex);
    }
}
