// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.ejb;

import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.UIDEx;
import de.simplicit.vjdbc.server.command.CommandProcessor;
import de.simplicit.vjdbc.ejb.*;
import de.simplicit.vjdbc.util.SQLExceptionHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Properties;

@Stateless
@Remote
public class EjbCommandSinkBean implements EjbCommandSink, EjbCommandSinkProxy {

    private static final Log logger = LogFactory.getLog(EjbCommandSinkBean.class);

    private transient CommandProcessor processor;

    public EjbCommandSinkBean() {
        this.processor = CommandProcessor.getInstance();
    }

    public UIDEx connect(String url, Properties props, Properties clientInfo,
                         CallingContext ctx)
        throws SQLException {
        try {
            return processor.createConnection(url, props, clientInfo, ctx);
        } catch (Exception e) {
            logger.error(url, e);
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public Object process(Long connuid, Long uid, Command cmd,
                          CallingContext ctx)
        throws SQLException {

        return processor.process(connuid, uid, cmd, ctx);
    }

    public void close() {
        processor = null;
    }

    @Override
    public EJBHome getEJBHome() {
        return null;
    }

    @Override
    public Object getPrimaryKey() {
        return null;
    }

    @Override
    public void remove() {

    }

    @Override
    public Handle getHandle() {
        return null;
    }

    @Override
    public boolean isIdentical(EJBObject ejbObject) {
        return false;
    }
}
