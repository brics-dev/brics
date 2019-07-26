
package gov.nih.tbi.repository.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Dao class used to generate tables and sequences based on information from data structures to store submitted data.
 * 
 * @author dhollo
 * 
 */
public interface DataStoreDao
{

    /**
     * Creates a table based on provided tableName and Map of column names to column types. This should be used with
     * createSequence().
     * 
     * @param tableName
     *            - the name of the table to create
     * @param columns
     *            - map with column names as the keys and SQL to generate the column type as the value
     * @throws SQLException
     */
    public void createTable(String tableName, Map<String, String> columns) throws SQLException;

    /**
     * Creates a sequence based on the provided tableName. This should be used with createTable().
     * 
     * @param tableName
     * @throws SQLException
     */
    public void createSequence(String tableName) throws SQLException;

    /**
     * Execute batch insert queries
     * 
     * @param batchQueries
     * @throws SQLException
     */
    public void executeBatchQueries(List<String> batchQueries) throws SQLException;

    /**
     * Wrapper method to run a CRUD statement
     *
     * @param sql
     * @return
     * @throws SQLException
     */

    int executeUpdate(String sql) throws SQLException;

    /**
     * Provides access to the jdbcTemplate for testing purposes.
     * 
     * @return the jdbcTemplate for the repositoryConnection
     */
    public JdbcTemplate getJdbcTemplate();
}
