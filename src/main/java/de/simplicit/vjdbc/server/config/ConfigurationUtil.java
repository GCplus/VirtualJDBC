// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

/**
 * 配置类
 */
class ConfigurationUtil {
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;

    /**
     * 从string类型转换为布尔类型
     * @param value 传入string类型的值,只接受true或者on,其他值均为false
     * @return 布尔类型的真或者假
     */
    static boolean getBooleanFromString(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on");
    }

    /**
     * 从string类型转换为毫秒
     * @param value 传入的时间
     * @return 返回毫秒(Long类型)
     */
    static long getMillisFromString(String value) {
        long parseLong = Long.parseLong(value.substring(0, value.length() - 1));
        if(value.endsWith("s")) {
            return parseLong * MILLIS_PER_SECOND;
        }
        else if(value.endsWith("m")) {
            return parseLong * MILLIS_PER_MINUTE;
        }
        else {
            return Long.parseLong(value);
        }
    }

    /**
     * 从Long类型的毫秒转换为字符类型的时间值
     * @param value 传入毫秒(Long类型)
     * @return 分钟(min),秒(sec)或毫秒(ms)
     */
    static String getStringFromMillis(long value) {
        if( (value % MILLIS_PER_MINUTE) == 0) {
            return "" + (value / MILLIS_PER_MINUTE) + " min";
        }
        else if( (value % MILLIS_PER_SECOND) == 0) {
            return "" + (value / MILLIS_PER_SECOND) + " sec";
        }
        else {
            return "" + value + " ms";
        }
    }
}
