// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

import de.simplicit.vjdbc.command.*;
import de.simplicit.vjdbc.serial.SerializableTransport;
import de.simplicit.vjdbc.serial.StreamingResultSet;
import de.simplicit.vjdbc.serial.UIDEx;
import de.simplicit.vjdbc.util.SQLExceptionHelper;

import javax.validation.constraints.NotNull;
import java.sql.*;

public class VirtualDatabaseMetaData extends VirtualBase implements DatabaseMetaData {
    private final Connection connection;

    public VirtualDatabaseMetaData(Connection conn, UIDEx reg, DecoratedCommandSink sink) {
        super(reg, sink);
        this.connection = conn;
    }

    public boolean allProceduresAreCallable() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "allProceduresAreCallable"));
    }

    public boolean allTablesAreSelectable() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "allTablesAreSelectable"));
    }

    public String getURL() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getURL"));
    }

    public String getUserName() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getUserName"));
    }

    public boolean isReadOnly() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool
                .getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "isReadOnly"));
    }

    public boolean nullsAreSortedHigh() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "nullsAreSortedHigh"));
    }

    public boolean nullsAreSortedLow() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "nullsAreSortedLow"));
    }

    public boolean nullsAreSortedAtStart() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "nullsAreSortedAtStart"));
    }

    public boolean nullsAreSortedAtEnd() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "nullsAreSortedAtEnd"));
    }

    public String getDatabaseProductName() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getDatabaseProductName"));
    }

    public String getDatabaseProductVersion() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getDatabaseProductVersion"));
    }

    public String getDriverName() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getDriverName"));
    }

    public String getDriverVersion() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getDriverVersion"));
    }

    public int getDriverMajorVersion() {
        try {
            return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                    "getDriverMajorVersion"));
        } catch (SQLException e) {
            return 1;
        }
    }

    public int getDriverMinorVersion() {
        try {
            return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                    "getDriverMinorVersion"));
        } catch (SQLException e) {
            return 0;
        }
    }

    public boolean usesLocalFiles() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "usesLocalFiles"));
    }

    public boolean usesLocalFilePerTable() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "usesLocalFilePerTable"));
    }

    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsMixedCaseIdentifiers"));
    }

    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "storesUpperCaseIdentifiers"));
    }

    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "storesLowerCaseIdentifiers"));
    }

    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "storesMixedCaseIdentifiers"));
    }

    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsMixedCaseQuotedIdentifiers"));
    }

    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "storesUpperCaseQuotedIdentifiers"));
    }

    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "storesLowerCaseQuotedIdentifiers"));
    }

    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "storesMixedCaseQuotedIdentifiers"));
    }

    public String getIdentifierQuoteString() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getIdentifierQuoteString"));
    }

    public String getSQLKeywords() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getSQLKeywords"));
    }

    public String getNumericFunctions() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getNumericFunctions"));
    }

    public String getStringFunctions() throws SQLException {
        return (String) sink.process(objectUid, CommandPool
                .getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getStringFunctions"));
    }

    public String getSystemFunctions() throws SQLException {
        return (String) sink.process(objectUid, CommandPool
                .getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getSystemFunctions"));
    }

    public String getTimeDateFunctions() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getTimeDateFunctions"));
    }

    public String getSearchStringEscape() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getSearchStringEscape"));
    }

    public String getExtraNameCharacters() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getExtraNameCharacters"));
    }

    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsAlterTableWithAddColumn"));
    }

    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsAlterTableWithDropColumn"));
    }

    public boolean supportsColumnAliasing() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsColumnAliasing"));
    }

    public boolean nullPlusNonNullIsNull() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "nullPlusNonNullIsNull"));
    }

    public boolean supportsConvert() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsConvert"));
    }

    public boolean supportsConvert(int fromType, int toType) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsConvert", new Object[] {fromType, toType},
                ParameterTypeCombinations.INTINT));
    }

    public boolean supportsTableCorrelationNames() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsTableCorrelationNames"));
    }

    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsDifferentTableCorrelationNames"));
    }

    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsExpressionsInOrderBy"));
    }

    public boolean supportsOrderByUnrelated() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsOrderByUnrelated"));
    }

    public boolean supportsGroupBy() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsGroupBy"));
    }

    public boolean supportsGroupByUnrelated() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsGroupByUnrelated"));
    }

    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsGroupByBeyondSelect"));
    }

    public boolean supportsLikeEscapeClause() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsLikeEscapeClause"));
    }

    public boolean supportsMultipleResultSets() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsMultipleResultSets"));
    }

    public boolean supportsMultipleTransactions() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsMultipleTransactions"));
    }

    public boolean supportsNonNullableColumns() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsNonNullableColumns"));
    }

    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsMinimumSQLGrammar"));
    }

    public boolean supportsCoreSQLGrammar() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsCoreSQLGrammar"));
    }

    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsExtendedSQLGrammar"));
    }

    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsANSI92EntryLevelSQL"));
    }

    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsANSI92IntermediateSQL"));
    }

    public boolean supportsANSI92FullSQL() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsANSI92FullSQL"));
    }

    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsIntegrityEnhancementFacility"));
    }

    public boolean supportsOuterJoins() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsOuterJoins"));
    }

    public boolean supportsFullOuterJoins() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsFullOuterJoins"));
    }

    public boolean supportsLimitedOuterJoins() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsLimitedOuterJoins"));
    }

    public String getSchemaTerm() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getSchemaTerm"));
    }

    public String getProcedureTerm() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getProcedureTerm"));
    }

    public String getCatalogTerm() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getCatalogTerm"));
    }

    public boolean isCatalogAtStart() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "isCatalogAtStart"));
    }

    public String getCatalogSeparator() throws SQLException {
        return (String) sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getCatalogSeparator"));
    }

    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSchemasInDataManipulation"));
    }

    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSchemasInProcedureCalls"));
    }

    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSchemasInTableDefinitions"));
    }

    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSchemasInIndexDefinitions"));
    }

    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSchemasInPrivilegeDefinitions"));
    }

    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsCatalogsInDataManipulation"));
    }

    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsCatalogsInProcedureCalls"));
    }

    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsCatalogsInTableDefinitions"));
    }

    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsCatalogsInIndexDefinitions"));
    }

    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsCatalogsInPrivilegeDefinitions"));
    }

    public boolean supportsPositionedDelete() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsPositionedDelete"));
    }

    public boolean supportsPositionedUpdate() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsPositionedUpdate"));
    }

    public boolean supportsSelectForUpdate() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSelectForUpdate"));
    }

    public boolean supportsStoredProcedures() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsStoredProcedures"));
    }

    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSubqueriesInComparisons"));
    }

    public boolean supportsSubqueriesInExists() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSubqueriesInExists"));
    }

    public boolean supportsSubqueriesInIns() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSubqueriesInIns"));
    }

    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSubqueriesInQuantifieds"));
    }

    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsCorrelatedSubqueries"));
    }

    public boolean supportsUnion() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsUnion"));
    }

    public boolean supportsUnionAll() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsUnionAll"));
    }

    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsOpenCursorsAcrossCommit"));
    }

    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsOpenCursorsAcrossRollback"));
    }

    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsOpenStatementsAcrossCommit"));
    }

    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsOpenStatementsAcrossRollback"));
    }

    public int getMaxBinaryLiteralLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxBinaryLiteralLength"));
    }

    public int getMaxCharLiteralLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxCharLiteralLength"));
    }

    public int getMaxColumnNameLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxColumnNameLength"));
    }

    public int getMaxColumnsInGroupBy() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxColumnsInGroupBy"));
    }

    public int getMaxColumnsInIndex() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxColumnsInIndex"));
    }

    public int getMaxColumnsInOrderBy() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxColumnsInOrderBy"));
    }

    public int getMaxColumnsInSelect() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxColumnsInSelect"));
    }

    public int getMaxColumnsInTable() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxColumnsInTable"));
    }

    public int getMaxConnections() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxConnections"));
    }

    public int getMaxCursorNameLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxCursorNameLength"));
    }

    public int getMaxIndexLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxIndexLength"));
    }

    public int getMaxSchemaNameLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxSchemaNameLength"));
    }

    public int getMaxProcedureNameLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxProcedureNameLength"));
    }

    public int getMaxCatalogNameLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxCatalogNameLength"));
    }

    public int getMaxRowSize() throws SQLException {
        return sink
                .processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getMaxRowSize"));
    }

    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "doesMaxRowSizeIncludeBlobs"));
    }

    public int getMaxStatementLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxStatementLength"));
    }

    public int getMaxStatements() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxStatements"));
    }

    public int getMaxTableNameLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxTableNameLength"));
    }

    public int getMaxTablesInSelect() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxTablesInSelect"));
    }

    public int getMaxUserNameLength() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getMaxUserNameLength"));
    }

    public int getDefaultTransactionIsolation() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getDefaultTransactionIsolation"));
    }

    public boolean supportsTransactions() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsTransactions"));
    }

    public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool
                .getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "supportsTransactionIsolationLevel", new Object[] {level}, ParameterTypeCombinations.INT));
    }

    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsDataDefinitionAndDataManipulationTransactions"));
    }

    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsDataManipulationTransactionsOnly"));
    }

    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "dataDefinitionCausesTransactionCommit"));
    }

    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "dataDefinitionIgnoredInTransactions"));
    }

    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
            throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getProcedures", new Object[] { catalog,
                schemaPattern, procedureNamePattern }, ParameterTypeCombinations.STRSTRSTR));
    }

    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern,
            String columnNamePattern) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getProcedureColumns", new Object[] {
                catalog, schemaPattern, procedureNamePattern, columnNamePattern },
                ParameterTypeCombinations.STRSTRSTRSTR));
    }

    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
            throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getTables", new Object[] { catalog,
                schemaPattern, tableNamePattern, types }, ParameterTypeCombinations.STRSTRSTRSTRA));
    }

    public ResultSet getSchemas() throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getSchemas"));
    }

    public ResultSet getCatalogs() throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getCatalogs"));
    }

    public ResultSet getTableTypes() throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getTableTypes"));
    }

    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getColumns", new Object[] { catalog,
                schemaPattern, tableNamePattern, columnNamePattern }, ParameterTypeCombinations.STRSTRSTRSTR));
    }

    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern)
            throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getColumnPrivileges", new Object[] {
                catalog, schema, table, columnNamePattern }, ParameterTypeCombinations.STRSTRSTRSTR));
    }

    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)
            throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getTablePrivileges", new Object[] {
                catalog, schemaPattern, tableNamePattern }, ParameterTypeCombinations.STRSTRSTR));
    }

    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)
            throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getBestRowIdentifier", new Object[] {
                catalog, schema, table, scope, nullable ? Boolean.TRUE : Boolean.FALSE },
                ParameterTypeCombinations.STRSTRSTRINTBOL));
    }

    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getVersionColumns", new Object[] {
                catalog, schema, table }, ParameterTypeCombinations.STRSTRSTR));
    }

    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getPrimaryKeys", new Object[] {
                catalog, schema, table }, ParameterTypeCombinations.STRSTRSTR));
    }

    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getImportedKeys", new Object[] {
                catalog, schema, table }, ParameterTypeCombinations.STRSTRSTR));
    }

    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getExportedKeys", new Object[] {
                catalog, schema, table }, ParameterTypeCombinations.STRSTRSTR));
    }

    public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable,
            String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getCrossReference", new Object[] {
                primaryCatalog, primarySchema, primaryTable, foreignCatalog, foreignSchema, foreignTable },
                ParameterTypeCombinations.STRSTRSTRSTRSTRSTR));
    }

    public ResultSet getTypeInfo() throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getTypeInfo"));
    }

    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)
            throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getIndexInfo", new Object[] { catalog,
                schema, table, unique ? Boolean.TRUE : Boolean.FALSE, approximate ? Boolean.TRUE : Boolean.FALSE },
                ParameterTypeCombinations.STRSTRSTRBOLBOL));
    }

    public boolean supportsResultSetType(int type) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsResultSetType", new Object[] {type}, ParameterTypeCombinations.INT));
    }

    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsResultSetConcurrency", new Object[] {type, concurrency},
                ParameterTypeCombinations.INTINT));
    }

    public boolean ownUpdatesAreVisible(int type) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "ownUpdatesAreVisible", new Object[] {type}, ParameterTypeCombinations.INT));
    }

    public boolean ownDeletesAreVisible(int type) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "ownDeletesAreVisible", new Object[] {type}, ParameterTypeCombinations.INT));
    }

    public boolean ownInsertsAreVisible(int type) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "ownInsertsAreVisible", new Object[] {type}, ParameterTypeCombinations.INT));
    }

    public boolean othersUpdatesAreVisible(int type) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "othersUpdatesAreVisible", new Object[] {type}, ParameterTypeCombinations.INT));
    }

    public boolean othersDeletesAreVisible(int type) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "othersDeletesAreVisible", new Object[] {type}, ParameterTypeCombinations.INT));
    }

    public boolean othersInsertsAreVisible(int type) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "othersInsertsAreVisible", new Object[] {type}, ParameterTypeCombinations.INT));
    }

    public boolean updatesAreDetected(int type) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "updatesAreDetected", new Object[] {type}, ParameterTypeCombinations.INT));
    }

    public boolean deletesAreDetected(int type) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "deletesAreDetected", new Object[] {type}, ParameterTypeCombinations.INT));
    }

    public boolean insertsAreDetected(int type) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "insertsAreDetected", new Object[] {type}, ParameterTypeCombinations.INT));
    }

    public boolean supportsBatchUpdates() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsBatchUpdates"));
    }

    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)
            throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getUDTs", new Object[] { catalog,
                schemaPattern, typeNamePattern, types }, ParameterTypeCombinations.STRSTRSTRINTA));
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean supportsSavepoints() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsSavepoints"));
    }

    public boolean supportsNamedParameters() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsNamedParameters"));
    }

    public boolean supportsMultipleOpenResults() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsMultipleOpenResults"));
    }

    public boolean supportsGetGeneratedKeys() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsGetGeneratedKeys"));
    }

    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getSuperTypes", new Object[] { catalog,
                schemaPattern, typeNamePattern }, ParameterTypeCombinations.STRSTRSTR));
    }

    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getSuperTables", new Object[] {
                catalog, schemaPattern, tableNamePattern }, ParameterTypeCombinations.STRSTRSTR));
    }

    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern,
            String attributeNamePattern) throws SQLException {
        return queryResultSet(CommandPool
                .getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getAttributes", new Object[] { catalog, schemaPattern,
                        typeNamePattern, attributeNamePattern }, ParameterTypeCombinations.STRSTRSTRSTR));
    }

    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsResultSetHoldability", new Object[] {holdability},
                ParameterTypeCombinations.INT));
    }

    public int getResultSetHoldability() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getResultSetHoldability"));
    }

    public int getDatabaseMajorVersion() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getDatabaseMajorVersion"));
    }

    public int getDatabaseMinorVersion() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getDatabaseMinorVersion"));
    }

    public int getJDBCMajorVersion() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getJDBCMajorVersion"));
    }

    public int getJDBCMinorVersion() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getJDBCMinorVersion"));
    }

    public int getSQLStateType() throws SQLException {
        return sink.processWithIntResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "getSQLStateType"));
    }

    public boolean locatorsUpdateCopy() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "locatorsUpdateCopy"));
    }

    public boolean supportsStatementPooling() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsStatementPooling"));
    }

    protected ResultSet queryResultSet(Command cmd) throws SQLException {
        try {
            SerializableTransport st = (SerializableTransport) sink.process(objectUid, cmd, true);
            StreamingResultSet rs = (StreamingResultSet) st.getTransportee();
            rs.setCommandSink(sink);
            return rs;
        } catch (Exception e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    /* start JDBC4 support */
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return RowIdLifetime.valueOf((String)sink.process(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getRowIdLifetime")));
    }

    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getSchemas", new Object[] { catalog,
                schemaPattern }, ParameterTypeCombinations.STRSTR));
    }

    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "supportsStoredFunctionsUsingCallSyntax"));
    }

    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "autoCommitFailureClosesAllResultSets"));
    }

    public ResultSet getClientInfoProperties() throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getClientInfoProperties"));
    }

    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getFunctions", new Object[] { catalog,
                schemaPattern, functionNamePattern }, ParameterTypeCombinations.STRSTR));
    }

    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getFunctionColumns", new Object[] { catalog,
                schemaPattern, functionNamePattern, columnNamePattern },
                ParameterTypeCombinations.STRSTRSTRSTR));
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(VirtualDatabaseMetaData.class);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T) this;
    }
    /* end JDBC4 support */

    /* start JDK7 support */
    public ResultSet getPseudoColumns(
        String catalog,
        String schemaPattern,
        String tableNamePattern,
        String columnNamePattern)
        throws SQLException
    {
        return queryResultSet(CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA, "getPseudoColumns", new Object[] { catalog,
                schemaPattern, tableNamePattern, columnNamePattern },
                ParameterTypeCombinations.STRSTRSTRSTR));
    }

    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return sink.processWithBooleanResult(objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.DATABASEMETADATA,
                "generatedKeyAlwaysReturned"));
    }
    /* end JDK7 support */
}
