// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet.jakarta;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.httpclient.methods.RequestEntity;

import de.simplicit.vjdbc.serial.CallingContext;

/**
 * RequestEntity implementation which streams all of the parameters necessary for
 * a connect request.
 * RequestEntity实现，用于流化连接请求所需的所有参数。
 * @author Mike
 */
class ConnectRequestEntity implements RequestEntity {
    private final String database;
    private final Properties props;
    private final Properties clientInfo;
    private final CallingContext ctx;
    
    public ConnectRequestEntity(String database, Properties props, Properties clientInfo, CallingContext ctx) {
        this.database = database;
        this.props = props;
        this.clientInfo = clientInfo;
        this.ctx = ctx;
    }
    
    public long getContentLength() {
        return -1; // Don't know the length in advance 事先不知道长度
    }

    public String getContentType() {
        return "binary/x-java-serialized";
    }

    public boolean isRepeatable() {
        return true;
    }

    public void writeRequest(OutputStream os) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeUTF(database);
        oos.writeObject(props);
        oos.writeObject(clientInfo);
        oos.writeObject(ctx);
        oos.flush();
    }
}
