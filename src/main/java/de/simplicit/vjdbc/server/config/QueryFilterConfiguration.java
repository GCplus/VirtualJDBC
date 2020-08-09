// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QueryFilterConfiguration {
    private static final Log _logger = LogFactory.getLog(QueryFilterConfiguration.class);
    private final List _filters = new ArrayList();
    private final Perl5Matcher _matcher = new Perl5Matcher();

    private static final PatternCompiler s_patternCompiler = new Perl5Compiler();

    private static class Filter {
        Filter(boolean isDenyFilter, String regExp, Pattern pattern, boolean containsType) {
            _isDenyFilter = isDenyFilter;
            _regExp = regExp;
            _pattern = pattern;
            _containsType = containsType;
        }

        boolean _isDenyFilter;
        String _regExp;
        Pattern _pattern;
        boolean _containsType;
    }

    public void addDenyEntry(String regexp, String type) throws ConfigurationException {
        addEntry(true, regexp, type);
    }

    public void addAllowEntry(String regexp, String type) throws ConfigurationException {
        addEntry(false, regexp, type);
    }

    private void addEntry(boolean isDenyFilter, String regexp, String type) throws ConfigurationException {
        try {
            Pattern pattern = s_patternCompiler.compile(regexp, Perl5Compiler.CASE_INSENSITIVE_MASK);
            _filters.add(new Filter(isDenyFilter, regexp, pattern, type != null && type.equals("contains")));
        } catch (MalformedPatternException e) {
            throw new ConfigurationException("Malformed RegEx-Pattern", e);
        }
    }

    public void checkAgainstFilters(String sql) throws SQLException {
        if(!_filters.isEmpty()) {
            for (Object o : _filters) {
                Filter filter = (Filter) o;
                boolean matched = filter._containsType ? _matcher.contains(sql, filter._pattern) : _matcher.matches(sql,
                        filter._pattern);

                if (matched) {
                    if (filter._isDenyFilter) {
                        String msg = "SQL [" + sql + "] is denied due to Deny-Filter [" + filter._regExp + "]";
                        _logger.warn(msg);
                        throw new SQLException(msg);
                    } else {
                        if (_logger.isDebugEnabled()) {
                            String msg = "SQL [" + sql + "] is allowed due to Allow-Filter [" + filter._regExp + "]";
                            _logger.debug(msg);
                        }
                        return;
                    }
                }
            }
            
            String msg = "SQL [" + sql + "] didn't match any query filter and won't be executed";
            _logger.error(msg);
            throw new SQLException(msg);
        }
    }

    void log() {
        _logger.info("  Query Filter-Configuration:");

        for (Object o : _filters) {
            Filter filter = (Filter) o;
            if (filter._isDenyFilter) {
                _logger.info("    Deny  : [" + filter._regExp + "]");
            } else {
                _logger.info("    Allow : [" + filter._regExp + "]");
            }
        }
    }
}
