// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.rmi;

import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.command.CommandSinkListener;
import de.simplicit.vjdbc.command.DecoratedCommandSink;
import de.simplicit.vjdbc.command.PingCommand;

import java.sql.SQLException;
import java.util.TimerTask;

/**
 * This timer task will periodically notify the server with a dummy command, just to
 * keep the connection alive. This will prevent the RMI-Object to be garbage-collected when
 * there aren't any RMI-Calls for a specific time (lease value).
 */
public class KeepAliveTimerTask extends TimerTask implements CommandSinkListener {
    private static Command dummyCommand = new PingCommand();
    private DecoratedCommandSink sink;
    private boolean ignoreNextPing = false;

    public KeepAliveTimerTask(DecoratedCommandSink sink) {
        this.sink = sink;
        this.sink.setListener(this);
    }

    public void preExecution(Command cmd) {
        // Next ping can be ignored when there are commands processed
        // to the sink
        ignoreNextPing = true;
    }

    public void postExecution(Command cmd) {
    }

    public void run() {
        try {
            if(ignoreNextPing) {
                ignoreNextPing = false;
            } else {
                sink.process(null, dummyCommand);
            }
        } catch(SQLException e) {
            // Ignore it, sink is already closed
        }
    }
}
