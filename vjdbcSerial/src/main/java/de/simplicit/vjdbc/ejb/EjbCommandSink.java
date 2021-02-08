// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.ejb;

import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.UIDEx;

import javax.ejb.EJBObject;
import javax.ejb.Local;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Properties;

@Local
public interface EjbCommandSink extends EJBObject {

    public UIDEx connect(String url, Properties props, Properties clientInfo,
                         CallingContext ctx)
            throws SQLException, RemoteException;

    public Object process(Long connuid, Long uid, Command cmd,
                          CallingContext ctx)
            throws SQLException, RemoteException;

    public void close() throws RemoteException;
}
