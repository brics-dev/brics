
package gov.nih.tbi.repository.dao.jdbc;

import gov.nih.tbi.repository.dao.DataStoreDao;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC Dao implementation for creating tables and sequences. This class uses JDBC rather than hibernate because the
 * tables being created are defined by data structures and their data elements.
 * 
 * @author dhollo
 * 
 */
@Repository
public class DataStoreDaoImpl implements DataStoreDao {

	static Logger logger = Logger.getLogger(DataStoreDaoImpl.class);

	private static final String PKEY_SUFFIX = "_pkey";
	private static final String FKEY_SUFFIX = "_fkey";
	private static final int SEQUENCE_MIN = 1;
	private static final long SEQUENCE_MAX = 999999999999999999L;
	private static final int SEQUENCE_INCREMENT = 1;
	private static final int SEQUENCE_START = 201;
	private static final String REPEATABLE_GROUP_ID = "id";
	private static final String REPEATABLE_GROUP_JOIN = "SUBMISSION_RECORD_JOIN";
	private static final String REPEATABLE_GROUP_JOIN_ID = "id";
	private static final String REPEATABLE_GROUP_JOIN_COL = REPEATABLE_GROUP_JOIN + "_" + REPEATABLE_GROUP_JOIN_ID;

	private static final String REPEATABLE_GROUP_CONSTANT_COLS =
			REPEATABLE_GROUP_ID + " integer NOT NULL, " + REPEATABLE_GROUP_JOIN_COL + " integer, ";
	private static final String REPEATABLE_GROUP_FK =
			" FOREIGN KEY (" + REPEATABLE_GROUP_JOIN_COL + ") REFERENCES " + REPEATABLE_GROUP_JOIN + " ("
					+ REPEATABLE_GROUP_JOIN_ID + ") MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION";
	private static final String CREATE_SEQUENCE = "CREATE SEQUENCE \"%1$s\"" + // table name
			" minvalue " + SEQUENCE_MIN + " maxvalue " + SEQUENCE_MAX + " increment by " + SEQUENCE_INCREMENT
			+ " start with " + SEQUENCE_START + ";";
	private static final String CREATE_TABLE = "CREATE TABLE %1$s ( " + // table name
			REPEATABLE_GROUP_CONSTANT_COLS + "%2$s" + // data element columns
			"CONSTRAINT %1$s" + PKEY_SUFFIX + " PRIMARY KEY (" + REPEATABLE_GROUP_ID + ")" + // table name
			", CONSTRAINT %1$s" + FKEY_SUFFIX + REPEATABLE_GROUP_FK + // table name
			") WITH ( OIDS=FALSE );";
	private static final String DROP_TABLES = "DROP TABLE IF EXISTS %1$s ";
	private static final String DROP_SEQUENCES = "DROP SEQUENCE IF EXISTS %1$s ";

	private JdbcTemplate jdbcTemplate;

	/**
	 * Initializes the jdbcTemplate with the DataSource information for repositoryConnection.
	 * 
	 * @param repositoryConnection
	 */
	@Autowired
	public void setRepositoryConnection(DataSource repositoryConnection) {

		this.jdbcTemplate = new JdbcTemplate(repositoryConnection);
	}

	/**
	 * @inheritDoc
	 */
	public void executeBatchQueries(List<String> batchQueries) throws SQLException {

		if (!batchQueries.isEmpty()) {
			String[] insertQueries = batchQueries.toArray(new String[batchQueries.size()]);
			jdbcTemplate.batchUpdate(insertQueries);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void createTable(String tableName, Map<String, String> columns) throws SQLException {

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> column : columns.entrySet()) {
			String columnName = column.getKey();
			String columnType = column.getValue();
			// The double quotes were added to handle cases where SQL reserved keywords
			// are part of the FS names
			sb.append("\"").append(columnName).append("\" ").append(columnType).append(", ");
		}
		String dataElementCols = sb.toString();

		Formatter formatter = new Formatter();
		formatter.format(CREATE_TABLE, tableName, dataElementCols);
		executeSql(formatter.toString());
		formatter.close();
	}

	/**
	 * @inheritDoc
	 */
	public void createSequence(String tableName) throws SQLException {

		Formatter formatter = new Formatter();
		formatter.format(CREATE_SEQUENCE, tableName);
		executeSql(formatter.toString());
		formatter.close();
	}

	/**
	 * Helper function to log sql if logging is set to debug and catch/throw errors if they occur.
	 * 
	 * @param sql - The sql to execute
	 * @throws SQLException
	 */
	private void executeSql(String sql) throws SQLException {

		logger.debug("This is the sql string being passed to get executed " + sql);
		try {
			jdbcTemplate.execute(sql);
		} catch (DataAccessException e) {
			throw new SQLException("Error running SQL: \n" + sql, e);
		}
	}

	/**
	 * Wrapper method to run a CRUD statement
	 *
	 * @param sql
	 * @return
	 * @throws SQLException
	 */

	public int executeUpdate(String sql) throws SQLException {

		int result = 0;

		logger.debug("Executing update SQL: " + sql);

		try {
			result = jdbcTemplate.update(sql);
		} catch (DataAccessException e) {
			throw new SQLException("Error running SQL: \n" + sql, e);
		}

		return result;
	}

	/**
	 * @inheritDoc
	 */
	public JdbcTemplate getJdbcTemplate() {

		return this.jdbcTemplate;
	}

	@Override
	public void dropTables(String tableNames) throws SQLException {
		
		Formatter formatter = new Formatter();
		formatter.format(DROP_TABLES, tableNames);
		executeSql(formatter.toString());
		formatter.close();
		
	}

	@Override
	public void dropSequences(String sequences) throws SQLException {
		
		Formatter formatter = new Formatter();
		formatter.format(DROP_SEQUENCES, sequences);
		executeSql(formatter.toString());
		formatter.close();
	}

}
