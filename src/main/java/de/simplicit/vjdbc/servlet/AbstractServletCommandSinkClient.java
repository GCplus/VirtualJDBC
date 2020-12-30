// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import de.simplicit.vjdbc.command.CommandSink;
import de.simplicit.vjdbc.util.SQLExceptionHelper;

/**
 * Abstract base class for clients of VJDBC in Servlet-Mode.
 * @author Mike
 *
 */
public abstract class AbstractServletCommandSinkClient implements CommandSink {
    protected URL url;
    protected RequestEnhancer requestEnhancer;

    public AbstractServletCommandSinkClient(String url, RequestEnhancer requestEnhancer) throws SQLException {
        try {
            this.url = new URL(url);
            this.requestEnhancer = requestEnhancer;
        } catch(IOException e) {
            e.printStackTrace();//出现IO异常时打印堆栈
        }
    }

    public void close() {
        // Nothing to do
        //在关闭时什么也不做
    }
}