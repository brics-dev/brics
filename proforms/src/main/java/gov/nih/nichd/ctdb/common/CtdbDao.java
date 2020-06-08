package gov.nih.nichd.ctdb.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Base DAO object for all CTDB DAO implementations.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CtdbDao {
	private static final Logger logger = Logger.getLogger(CtdbDao.class);
	protected static final String DATA_ENCRYPTION_KEY = "daBL53dxgA+ZXe*IzLtj#fzm=AubGQU!EZcNkQu1l=s6iPFpWqaPbqw80w#uK>5I";
	protected static final String ENCRYPTION_FUNC_OPTS = "cipher-algo=aes256";
	protected static final String STANDARD_ENCRYPTION_FUNCT = "pgp_sym_encrypt(?, '" + CtdbDao.DATA_ENCRYPTION_KEY + "'::text, '" + CtdbDao.ENCRYPTION_FUNC_OPTS + "'::text)";
    protected Connection conn;
    
    protected static final String pdClinicalCoordinatorRole = "PDBP Clinical Coordinator";
    
    /**
     * Wrapper method to close a database ResultSet. Null checks are done, and any errors are logged as warnings.
     *
     * @param rs ResultSet to close
     */
    protected final void close(ResultSet rs) {
    	try {
	        if ( rs != null ) {
	           rs.close();
	        }
    	}
    	catch ( SQLException sqle ) {
    		logger.warn("Couldn't close the result set.", sqle);
    	}
    }

    /**
     * Wrapper method to close a database statement. Null checks are done, and any errors are logged as warnings.
     *
     * @param stmt Statement to close
     */
    protected final void close(Statement stmt) {
    	try {
    		if ( stmt != null ) {
    			stmt.close();
        	}
    	}
    	catch ( SQLException sqle ) {
    		logger.warn("Couldn't close the JDBC statement.", sqle);
    	}
    }

    /**
     * Method to set the Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     */
    protected void setConnection(Connection conn) {
        this.conn = conn;
    }
    
    /**
     *  Gets the next sequence value and increments the sequence by increment number,
     * @param conn
     * @param sequenceName
     * @return
     * @throws SQLException
     */
    protected final int getSetSequenceValues(Connection conn, String sequenceName, int numToIncrement) throws SQLException, CtdbException {
    	Statement stmt = null;
    	try {
	    	int id = getNextSequenceValue(conn, sequenceName);
	    	stmt = conn.createStatement();
	    	stmt.execute("ALTER SEQUENCE " + sequenceName + " INCREMENT BY " + Integer.toString(numToIncrement));
	    	return id;
    	}
    	finally {
    		this.close(stmt);
    	}
    }
    
    /**
     * Gets the current value of the specified database sequence
     * 
     * @param conn - The connection to the database
     * @param sequenceName - The name of the database sequence that the current value will be taken from
     * @return	The current value of the specified sequence
     * @throws SQLException	If an error occurs during the preparation of the SQL statement.
     * @throws CtdbException	If no value can be extracted from the specified sequence.
     */
    protected final int getInsertId(Connection conn, String sequenceName)
    		throws SQLException, CtdbException {
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	try {
    		StringBuffer sql = new StringBuffer("SELECT currval(?) as currval");
	    	stmt = conn.prepareStatement(sql.toString());
	    	stmt.setString(1, sequenceName);
	    	rs = stmt.executeQuery();
	    	if (rs.next()) {
	    		return rs.getInt("currval");
	    	}
	    	else {
	    		throw new CtdbException("Failure obtaining the inserted ID value");
	    	}
    	}
    	finally {
    		this.close(rs);
    		this.close(stmt);
    	}
    }
    
    protected final int getNextSequenceValue(Connection conn, String sequenceName) throws SQLException, CtdbException {
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	try {
    		StringBuffer sql = new StringBuffer("SELECT nextval(?) as nextval");
	    	stmt = conn.prepareStatement(sql.toString());
	    	stmt.setString(1, sequenceName);
	    	rs = stmt.executeQuery();
	    	if (rs.next()) {
	    		return rs.getInt("nextval");
	    	}
	    	else {
	    		throw new CtdbException("Failure obtaining the next sequence ID value");
	    	}
    	}
    	finally {
    		this.close(rs);
    		this.close(stmt);
    	}
    }

	protected boolean hasData (String s) {
		if ( (s != null) && !s.trim().equals("") ) {
			return true;
		}
		
		return false;
	}
	
	protected boolean hasData (int i) {
		if ( (i != Integer.MIN_VALUE) && (i != 0) ) {
			return true;
		}
		
		return false;
	}
	
	protected boolean hasData (Date s) {
		if (s != null ) {
			return true;
		}
		
		return false;
	}
	
	/**
     * Wrapper method used to roll back a transaction on a database connection. Any errors are logged as warnings.
     * 
     */
    protected final void rollback() {
        try {
            conn.rollback();
        }
        catch (SQLException e) {
            logger.warn("Unable to rollback transaction.", e);
        }
    }
    
    /**
     * Gets the decryption function call for PostgreSQL for the given column
     * 
     * @param columnName - The name of a column in the database.
     * @return	The complete call to the PostgreSQL decryption function.
     */
    public static String getDecryptionFunc(String columnName) {
    	return "pgp_sym_decrypt(" + columnName + ", '" + CtdbDao.DATA_ENCRYPTION_KEY + "'::text)";
    	
    }
}