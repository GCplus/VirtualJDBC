// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for closing stream securely.
 * 用于安全关闭流的工具类
 * @author Mike
 *
 */
public final class StreamCloser {
    private StreamCloser() {
    }
    
    /**
     * Closes an InputStream.
     * 关闭InputStream
     * @param is InputStream to close 需要关闭的InputStream
     */
    public static void close(InputStream is) {
        if(is != null) {
            try {
                is.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Closes an OutputStream
     * 关闭OutputStream
     * @param os OutputStream to close 需要关闭的OutputStream
     */
    public static void close(OutputStream os) {
        if(os != null) {
            try {
                os.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
