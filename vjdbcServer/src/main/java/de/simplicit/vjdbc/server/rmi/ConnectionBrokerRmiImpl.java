// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.rmi;

import de.simplicit.vjdbc.rmi.CommandSinkRmi;
import de.simplicit.vjdbc.rmi.ConnectionBrokerRmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Root object for RMI communication.
 * <p>RMI通信的根对象。
 */
public class ConnectionBrokerRmiImpl extends UnicastRemoteObject implements ConnectionBrokerRmi {
    private static final long serialVersionUID = 3257290235934029618L;
    private int remotingPort = 0;

    public ConnectionBrokerRmiImpl(int remotingPort) throws RemoteException {
        super(remotingPort);
        this.remotingPort = remotingPort;
    }

    public CommandSinkRmi createCommandSink() throws RemoteException {
        return new CommandSinkRmiImpl(remotingPort);
    }
}
