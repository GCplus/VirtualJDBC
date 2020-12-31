// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.rmi.KeepAliveTimerTask;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.UIDEx;

import java.sql.SQLException;
import java.util.Properties;
import java.util.Timer;

/**
 * The DecoratedCommandSink makes it easier to handle the CommandSink. It contains a number
 * of different utility methods which wrap parameters, unwrap results and so on. Additionally
 * it supports a Listener which is called before and after execution of the command.
 * DecoratedCommandSink使处理CommandSink更加容易。
 * 它包含许多不同的方法，这些方法可以包装参数，拆开结果集等。
 * 另外，它还支持在执行命令之前和之后调用的Listener。
 */
public class DecoratedCommandSink {
    private final UIDEx connectionUid;
    private final CommandSink targetSink;
    private CommandSinkListener listener = new NullCommandSinkListener();
    private final CallingContextFactory callingContextFactory;
    private Timer timer;

    public DecoratedCommandSink(UIDEx connuid, CommandSink sink, CallingContextFactory ctxFactory) {
        this(connuid, sink, ctxFactory, 10000L);
    }

    public DecoratedCommandSink(UIDEx connuid, CommandSink sink, CallingContextFactory ctxFactory, long pingPeriod) {
        this.connectionUid = connuid;
        this.targetSink = sink;
        this.callingContextFactory = ctxFactory;

        if (pingPeriod > 0) {
            this.timer = new Timer(true);

            // Schedule the keep alive timer task
            KeepAliveTimerTask task = new KeepAliveTimerTask(this);
            timer.scheduleAtFixedRate(task, pingPeriod, pingPeriod);
        }
    }

    public CommandSink getTargetSink()
    {
        return targetSink;
    }

    public void close() {
        // Stop the keep-alive timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        // Close down the sink
        targetSink.close();
    }

    public void setListener(CommandSinkListener listener) {
        if(listener != null) {
            this.listener = listener;
        } else {
            this.listener = new NullCommandSinkListener();
        }
    }

    public UIDEx connect(String url, Properties props, Properties clientInfo, CallingContext ctx) throws SQLException {
        return targetSink.connect(url, props, clientInfo, ctx);
    }

    public Object process(UIDEx reg, Command cmd) throws SQLException {
        return process(reg, cmd, false);
    }

    public Object process(UIDEx reg, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = callingContextFactory.create();
            }
            listener.preExecution(cmd);
            return targetSink.process(connectionUid != null ? connectionUid.getUID() : null,
                                       reg != null ? reg.getUID() : null, cmd, ctx);
        } finally {
            listener.postExecution(cmd);
        }
    }

    public int processWithIntResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithIntResult(uid, cmd, false);
    }

    public int processWithIntResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = callingContextFactory.create();
            }
            listener.preExecution(cmd);
            return Integer.parseInt(String.valueOf(targetSink.process(connectionUid.getUID(), uid.getUID(), cmd, ctx)));
        } finally {
            listener.postExecution(cmd);
        }
    }

    public boolean processWithBooleanResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithBooleanResult(uid, cmd, false);
    }

    public boolean processWithBooleanResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = callingContextFactory.create();
            }
            listener.preExecution(cmd);
            return Boolean.parseBoolean(String.valueOf(targetSink.process(connectionUid.getUID(), uid.getUID(), cmd, ctx)));
        } finally {
            listener.postExecution(cmd);
        }
    }

    public byte processWithByteResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithByteResult(uid, cmd, false);
    }

    public byte processWithByteResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = callingContextFactory.create();
            }
            listener.preExecution(cmd);
            return Byte.parseByte(String.valueOf(targetSink.process(connectionUid.getUID(),uid.getUID(),cmd,ctx)));
        } finally {
            listener.postExecution(cmd);
        }
    }

    public short processWithShortResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithShortResult(uid, cmd, false);
    }

    public short processWithShortResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = callingContextFactory.create();
            }
            listener.preExecution(cmd);
            Short b = (Short)targetSink.process(connectionUid.getUID(), uid.getUID(), cmd, ctx);
            return b;
        } finally {
            listener.postExecution(cmd);
        }
    }

    public long processWithLongResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithLongResult(uid, cmd, false);
    }

    public long processWithLongResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = callingContextFactory.create();
            }
            listener.preExecution(cmd);
            return Long.parseLong(String.valueOf(targetSink.process(connectionUid.getUID(),uid.getUID(),cmd,ctx)));
        } finally {
            listener.postExecution(cmd);
        }
    }

    public float processWithFloatResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithFloatResult(uid, cmd, false);
    }

    public float processWithFloatResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = callingContextFactory.create();
            }
            listener.preExecution(cmd);
            return Float.parseFloat(String.valueOf(targetSink.process(connectionUid.getUID(),uid.getUID(),cmd,ctx)));
        } finally {
            listener.postExecution(cmd);
        }
    }

    public double processWithDoubleResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithDoubleResult(uid, cmd, false);
    }

    public double processWithDoubleResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = callingContextFactory.create();
            }
            listener.preExecution(cmd);
            return Double.parseDouble(String.valueOf(targetSink.process(connectionUid.getUID(),uid.getUID(),cmd,ctx)));
        } finally {
            listener.postExecution(cmd);
        }
    }
}
