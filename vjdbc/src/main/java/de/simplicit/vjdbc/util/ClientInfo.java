// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.util.Properties;
import java.util.StringTokenizer;

public class ClientInfo {
    private static final Log logger = LogFactory.getLog(ClientInfo.class);
    private static Properties properties = null;

    public static Properties getProperties(String propertiesToTransfer) {
        if(properties == null) {
            // Initialize the properties with the first access
            // 首次访问时初始化属性
            properties = new Properties();

            try {
                // Deliver local host information
                // 提供本地主机信息
                InetAddress iadr = InetAddress.getLocalHost();
                properties.put("vjdbc-client.address", iadr.getHostAddress());
                properties.put("vjdbc-client.name", iadr.getHostName());

                // Split the passed string into pieces and put all system properties
                // into the Properties object
                // 将传递的字符串拆分为多个部分，并将所有系统属性放入Properties对象
                if(propertiesToTransfer != null) {
                    // Use StringTokenizer here, split-Method is only available in JDK 1.4
                    // 在这里使用StringTokenizer，split-Method仅在JDK 1.4中可用
                    StringTokenizer tok = new StringTokenizer(propertiesToTransfer, ";");
                    while(tok.hasMoreTokens()) {
                        String key = tok.nextToken();
                        String value = System.getProperty(key);
                        if(value != null) {
                            properties.put(key, value);
                        }
                    }
                }
            } catch (Exception e) {
                logger.info("Access-Exception, System-Properties can't be delivered to the server");
                e.printStackTrace();
            }
        }

        return properties;
    }
}