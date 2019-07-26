package gov.nih.nichd.ctdb.common;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Base Business Manager class for all CTDB's Business Managers. This class
 * provides wrapper methods for transactions.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
@Component
public class CtdbManager {
	public static final boolean AUTOCOMMIT_FALSE = false;
	public static final boolean AUTOCOMMIT_TRUE = true;
	private static int NEGATIVE_COUNTER = Integer.MIN_VALUE;
	private static Logger logger = Logger.getLogger(CtdbManager.class);

	//@Autowired
	private static DataSource dataSource;

	/**
	 * Default constructor
	 */
	public CtdbManager() {}

	public static DataSource getDataSource() {
		return dataSource;
	}

	public static void setDataSource(DataSource dataSource) {
		CtdbManager.dataSource = dataSource;
	}

	/**
	 * getConnection allows a manager to get a database connection from the
	 * ConnectionFactory.
	 *
	 * @return Database Connection
	 * @throws CtdbException
	 *             Thrown if an error occurs trying to create the connection
	 */
	protected static synchronized Connection getConnection() throws CtdbException {
		try {
			return dataSource.getConnection();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get connection: " + e.getMessage(), e);
		}
	}

	protected static synchronized Connection getConnection(boolean autoCommit) throws CtdbException {
		try {
			Connection c = dataSource.getConnection();
			c.setAutoCommit(autoCommit);
			
			return c;
		}
		catch (SQLException sqle) {
			throw new CtdbException("Unable to set Connection autoCommit : " + sqle.getMessage(), sqle);
		}
	}

	/**
	 * Generates a unique negative number to be used in new domain objects.
	 * 
	 * @return A unique negative number used for identifying new domain objects
	 *         that have not been persisted to the database.
	 */
	public static synchronized int getNegativeUniqueId() {
		int negId = NEGATIVE_COUNTER;

		// Check if the negative counter needs to be reset
		if (negId >= 0) {
			negId = Integer.MIN_VALUE;
			NEGATIVE_COUNTER = Integer.MIN_VALUE;
		} else {
			NEGATIVE_COUNTER++;
		}

		return negId;
	}

	/**
	 * Wrapper method used to close a database connection.
	 *
	 * @param conn
	 *            Database Connection
	 * @throws CtdbException
	 *             If the connection cannot be closed.
	 */
	protected final void close(Connection conn) throws CtdbException {
		try {
			if (conn != null) {
				conn.close();
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to close the connection", e);
		}
	}

	/**
	 * Wrapper method used to rollback a transaction on a database connection.
	 *
	 * @param conn
	 *            - Database Connection
	 */
	protected final void rollback(Connection conn) {
		try {
			if (conn != null) {
				conn.rollback();
			}
		}
		catch (SQLException e) {
			logger.warn("Unable to rollback transaction.", e);
		}
	}

	/**
	 * Wrapper method used to commit a transaction on a database connection.
	 * 
	 * @param conn
	 *            - Database Connection
	 * @throws CtdbException
	 *             When the connection cannot be committed.
	 */
	protected final void commit(Connection conn) throws CtdbException {
		try {
			if (conn != null) {
				conn.commit();
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException("Unable to commit transaction.", sqle);
		}
	}
}