// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询拦截器配置
 */
public class QueryFilterConfiguration {
    private static final Log logger = LogFactory.getLog(QueryFilterConfiguration.class);
    private final List filters = new ArrayList();
    private final Perl5Matcher matcher = new Perl5Matcher();

    private static final PatternCompiler sPatternCompiler = new Perl5Compiler();

    private static class Filter {

        boolean isDenyFilter;
        String regExp;
        Pattern pattern;
        boolean containsType;

        Filter(boolean isDenyFilter, String regExp, Pattern pattern, boolean containsType) {
            this.isDenyFilter = isDenyFilter;
            this.regExp = regExp;
            this.pattern = pattern;
            this.containsType = containsType;
        }
    }

    public void addDenyEntry(String regexp, String type) throws ConfigurationException {
        addEntry(true, regexp, type);
    }

    public void addAllowEntry(String regexp, String type) throws ConfigurationException {
        addEntry(false, regexp, type);
    }

    private void addEntry(boolean isDenyFilter, String regexp, String type) throws ConfigurationException {
        try {
            Pattern pattern = sPatternCompiler.compile(regexp, Perl5Compiler.CASE_INSENSITIVE_MASK);
            filters.add(new Filter(isDenyFilter, regexp, pattern, type != null && type.equals("contains")));
        } catch (MalformedPatternException e) {
            throw new ConfigurationException("Malformed RegEx-Pattern", e);
        }
    }

    public void checkAgainstFilters(String sql) throws SQLException {
        if(!filters.isEmpty()) {
            for (Object o : filters) {
                Filter filter = (Filter) o;
                boolean matched = filter.containsType ? matcher.contains(sql, filter.pattern) : matcher.matches(sql,
                        filter.pattern);

                if (matched) {
                    if (filter.isDenyFilter) {
                        String msg = "SQL [" + sql + "] is denied due to Deny-Filter [" + filter.regExp + "]";
                        logger.warn(msg);
                        throw new SQLException(msg);
                    } else {
                        if (logger.isDebugEnabled()) {
                            String msg = "SQL [" + sql + "] is allowed due to Allow-Filter [" + filter.regExp + "]";
                            logger.debug(msg);
                        }
                        return;
                    }
                }
            }
            
            String msg = "SQL [" + sql + "] didn't match any query filter and won't be executed";
            logger.error(msg);
            throw new SQLException(msg);
        }
    }

    void log() {
        logger.info("  Query Filter-Configuration:");

        for (Object o : filters) {
            Filter filter = (Filter) o;
            if (filter.isDenyFilter) {
                logger.info("    Deny  : [" + filter.regExp + "]");
            } else {
                logger.info("    Allow : [" + filter.regExp + "]");
            }
        }
    }
}
