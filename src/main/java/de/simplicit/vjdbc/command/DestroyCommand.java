// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.serial.UIDEx;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

public class DestroyCommand implements Command {
    static final long serialVersionUID = 4457392123395584636L;

    private static final Log logger = LogFactory.getLog(DestroyCommand.class);
    private Long uid;
    private int interfaceType;

    public DestroyCommand() {
    }

    public DestroyCommand(UIDEx regentry, int interfaceType) {
        this.uid = regentry.getUID();
        this.interfaceType = interfaceType;
    }

    public DestroyCommand(Long uid, int interfaceType) {
    	this.uid = uid;
    	this.interfaceType = interfaceType;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(uid);
        out.writeInt(interfaceType);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        uid = in.readLong();
        interfaceType = in.readInt();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
    	/*
    	 * if we are trying to close a Connection, we also need to close all the other associated
    	 * JDBC objects, such as ResultSet, Statements, etc.
         * <p> 如果尝试关闭Connection，则还需要关闭所有其他关联的JDBC对象，例如ResultSet，Statements等
         */
    	if(target instanceof Connection) {
    		if(logger.isDebugEnabled()) {
    			logger.debug("******************************************************");
    			logger.debug("Destroy command for Connection found!");
    			logger.debug("destroying and closing all related JDBC objects first.");
    			logger.debug("******************************************************");
    		}
    		ctx.closeAllRelatedJdbcObjects();
    	}
    	// now we are ready to go on and close this connection
    	// 现在我们准备继续并关闭此连接
        Object removed = ctx.removeJDBCObject(uid);

        // Check for identity
        // 检查身份
        if(removed == target) {
            if(logger.isDebugEnabled()) {
                logger.debug("Removed " + target.getClass().getName() + " with UID " + uid);
            }
            try {
                Class targetClass = JdbcInterfaceType.interfaces[interfaceType];
                Method mth = targetClass.getDeclaredMethod("close", new Class[0]);
                mth.invoke(target, (Object[])null);
                if(logger.isDebugEnabled()) {
                    logger.debug("Invoked close() successfully");
                }
            } catch(NoSuchMethodException e) {
                // Object doesn't support close()
                // 对象不支持close()
            	if(logger.isDebugEnabled()) {
            		logger.debug("close() not supported");
            	}
            } catch(Exception e) {
                if(logger.isDebugEnabled()) {
                    logger.debug("Invocation of close() failed", e);
                }
            }
        } else {
            if(logger.isWarnEnabled()) {
                logger.warn("Target object " + target + " wasn't registered with UID " + uid);
            }
        }
        return null;
    }

    public String toString() {
        return "DestroyCommand";
    }
}
