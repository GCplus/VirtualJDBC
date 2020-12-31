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
 * 此计时器任务将使用虚拟命令定期通知服务器，以保持连接有效。
 * 这将防止在特定时间（约定时间）内没有任何RMI调用时对RMI对象进行垃圾收集。
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
