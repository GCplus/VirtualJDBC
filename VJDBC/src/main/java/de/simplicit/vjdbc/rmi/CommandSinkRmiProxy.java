// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.rmi;

import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.command.CommandSink;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.UIDEx;
import de.simplicit.vjdbc.util.SQLExceptionHelper;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Properties;

public class CommandSinkRmiProxy implements CommandSink {
    private CommandSinkRmi targetSink;

    public CommandSinkRmiProxy(CommandSinkRmi target) {
        this.targetSink = target;
    }

    public UIDEx connect(String url, Properties props, Properties clientInfo, CallingContext ctx) throws SQLException {
        if(targetSink != null) {
            try {
                return targetSink.connect(url, props, clientInfo, ctx);
            } catch (RemoteException e) {
                throw SQLExceptionHelper.wrap(e);
            }
        }
        else {
            throw new SQLException("Connection is already closed");
        }
    }

    public Object process(Long connuid, Long uid, Command cmd, CallingContext ctx) throws SQLException {
        if(targetSink != null) {
            try {
                return targetSink.process(connuid, uid, cmd, ctx);
            } catch (RemoteException e) {
                throw SQLExceptionHelper.wrap(e);
            }
        } else {
            throw new SQLException("Connection is already closed");
        }
    }

    public void close() {
        targetSink = null;
    }
}
