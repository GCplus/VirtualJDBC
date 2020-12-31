package de.simplicit.vjdbc.servlet.jakarta;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;

import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.serial.CallingContext;

public class ProcessRequestEntity implements RequestEntity {
    private final Long connuid;
    private final Long uid;
    private final Command cmd;
    private final CallingContext ctx;
    
    public ProcessRequestEntity(Long connuid, Long uid, Command cmd, CallingContext ctx) {
        this.connuid = connuid;
        this.uid = uid;
        this.cmd = cmd;
        this.ctx = ctx;
    }
    
    public long getContentLength() {
        return -1; // Don't know length in advance 事先不知道长度
    }

    public String getContentType() {
        return "binary/x-java-serialized";
    }

    public boolean isRepeatable() {
        return true;
    }

    public void writeRequest(OutputStream os) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(connuid);
        oos.writeObject(uid);
        oos.writeObject(cmd);
        oos.writeObject(ctx);
        oos.flush();
    }
}
