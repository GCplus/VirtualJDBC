// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.simplicit.vjdbc.util.SQLExceptionHelper;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

public class ReflectiveCommand implements Command, Externalizable {
    static final long serialVersionUID = 1573361368678688726L;

    private static final Log logger = LogFactory.getLog(ReflectiveCommand.class);
    private static final Object[] zeroParameters = new Object[0];

    private int interfaceType;
    private String cmd;
    private Object[] parameters;
    private int parameterTypes;
    private transient Class targetClass;

    public ReflectiveCommand() {
    }

    public ReflectiveCommand(int interfaceType, String cmd) {
        this.interfaceType = interfaceType;
        this.cmd = cmd;
        this.parameters = zeroParameters;
    }

    public ReflectiveCommand(int interfaceType, String cmd, Object[] parms, int parmTypes) {
        this.interfaceType = interfaceType;
        this.cmd = cmd;
        this.parameters = parms;
        this.parameterTypes = parmTypes;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(interfaceType);
        out.writeUTF(cmd);
        out.writeInt(parameters.length);
        for (Object parameter : parameters) {
            out.writeObject(parameter);
        }
        out.writeInt(parameterTypes);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        interfaceType = in.readInt();
        cmd = in.readUTF();
        int len = in.readInt();
        parameters = new Object[len];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = in.readObject();
        }
        parameterTypes = in.readInt();
    }

    public Object execute(Object target, ConnectionContext ctx) throws SQLException {
        try {
            targetClass = JdbcInterfaceType.interfaces[interfaceType];
            Method method = targetClass.getDeclaredMethod(cmd, ParameterTypeCombinations.typeCombinations[parameterTypes]);
            return method.invoke(target, parameters);
        } catch(NoSuchMethodException e) {
            String msg = "No such method '" + cmd + "' on object " + target + " (Target-Class " + targetClass.getName() + ")";
            logger.warn(msg);
            logger.warn(getParameterTypesAsString());
            throw SQLExceptionHelper.wrap(e);
        } catch(SecurityException e) {
            String msg = "Security exception with '" + cmd + "' on object " + target;
            logger.error(msg, e);
            throw SQLExceptionHelper.wrap(e);
        } catch(IllegalAccessException e) {
            String msg = "Illegal access exception with '" + cmd + "' on object " + target;
            logger.error(msg, e);
            throw SQLExceptionHelper.wrap(e);
        } catch(IllegalArgumentException e) {
            String msg = "Illegal argument exception with '" + cmd + "' on object " + target;
            logger.error(msg, e);
            throw SQLExceptionHelper.wrap(e);
        } catch(InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            String msg = "Unexpected invocation target exception: " + targetException.toString();
            logger.warn(msg, targetException);
            throw SQLExceptionHelper.wrap(targetException);
        } catch(Exception e) {
            String msg = "Unexpected exception: " + e.toString();
            logger.error(msg, e);
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public int getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(int interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getCommand() {
        return cmd;
    }

    public void setCommand(String cmd) {
        this.cmd = cmd;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public int getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(int parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ReflectiveCommand '").append(cmd).append("'");
        if(targetClass != null) {
            sb.append(" on object of class ").append(targetClass.getName());
        }
        if(parameters.length > 0) {
            sb.append(" with ").append(parameters.length).append(" parameters\n");
            for(int i = 0, n = parameters.length; i < n; i++) {
                sb.append("\t[").append(i).append("] ");
                if(parameters[i] != null) {
                    String value = parameters[i].toString();
                    if(value.length() > 0) {
                        sb.append(value);
                    } else {
                        sb.append("<empty>");
                    }
                } else {
                    sb.append("<null>");
                }
                if(i < n - 1) {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    private String getParameterTypesAsString() {
        Class[] parameterTypes = ParameterTypeCombinations.typeCombinations[this.parameterTypes];
        StringBuilder buff = new StringBuilder();
        for(int i = 0; i < parameterTypes.length; i++) {
            buff.append("Parameter-Type ").append(i).append(": ").append(parameterTypes[i].getName()).append("\n");
        }
        return buff.toString();
    }
}
