package gov.nih.nichd.ctdb.ebinder.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nih.nichd.ctdb.common.BinderNotFoundException;
import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.ebinder.domain.Ebinder;

/**
 * The E-Binder DAO class that handles all database requests to and from the "binder" table.
 * 
 * @author Originally created by IntelliJ IDEA.  Completely re-wrote by DCB
 */
public class BinderDao extends CtdbDao
{
	/**
	 * The default constructor for this DAO object.
	 */
	private BinderDao()
	{
		super();
	}
	
	/**
	 * Creates new default DAO with NO database connection configured.
	 * 
	 * @return	The default DAO object.
	 */
	public static synchronized BinderDao getInstance()
	{
		return new BinderDao();
	}
	
	/**
	 * Creates a new DAO configured with a connection to the database.
	 * 
	 * @param conn - A connection to the database
	 * @return	A new DAO object that is assigned to a database connection.
	 */
	public static synchronized BinderDao getInstance(Connection conn)
	{
		BinderDao dao = new BinderDao();
		dao.setConnection(conn);
		
		return dao;
	}
	
	/**
	 * Creates a new E-Binder entry in the "binder" table.
	 * 
	 * @param b - The new Ebinder object
	 * @throws CtdbException	If a database error occurs while creating the binder.
	 */
	public void addEbinder(Ebinder b) throws CtdbException
	{
		PreparedStatement stmt = null;
		
		String sql = "insert into binder(binderid, studyid, name, xbindertypeid, jsontree) values (default, ?, ?, ?, ?) ";
		
		try
		{
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, b.getStudyId());
			stmt.setString(2, b.getName());
			stmt.setLong(3, b.getType().getId());
			stmt.setString(4, b.getJsonTree());
			stmt.executeUpdate();
			
			// Set the binder ID
			b.setId(getInsertId(conn, "binder_seq"));
		}
		catch ( SQLException sqle )
		{
			throw new CtdbException("Could not add the binder", sqle);
		}
		finally
		{
			close(stmt);
		}
	}
	
	/**
	 * Updates an E-Binder entry that is in the "binder" table.
	 * 
	 * @param b - The Ebinder object with the new data.
	 * @throws CtdbException	If a database error occurs while updating the binder.
	 */
	public void updateEbinder(Ebinder b) throws CtdbException
	{
		PreparedStatement stmt = null;
		
		String sql = "update binder set studyid = ?, name = ?, xbindertypeid = ?, jsontree = ? where binderid = ? ";
		
		try
		{
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, b.getStudyId());
			stmt.setString(2, b.getName());
			stmt.setLong(3, b.getType().getId());
			stmt.setString(4, b.getJsonTree());
			stmt.setLong(5, b.getId());
			stmt.executeUpdate();
		}
		catch ( SQLException sqle )
		{
			throw new CtdbException("Could not update the binder with ID " + Long.toString(b.getId()), sqle);
		}
		finally
		{
			close(stmt);
		}
	}
	
	/**
	 * Retrieves an E-Binder from the database
	 * 
	 * @param studyId - The ID of a study or protocol
	 * @param typeId - The ID of the E-Binder type from the "xbindertype" table
	 * @return	A Ebinder object representing the requested binder
	 * @throws CtdbException	When a database error occurred during the query.
	 * @throws BinderNotFoundException	When a binder cannot be found in the database.
	 */
	public Ebinder getEbinder(long studyId, long typeId) throws CtdbException, BinderNotFoundException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Ebinder binder = null;
		
		String sql = "select * from binder where studyid = ? and xbindertypeid = ? ";
		
		try
		{
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, studyId);
			stmt.setLong(2, typeId);
			rs = stmt.executeQuery();
			
			// Create the Ebinder object
			if ( rs.next() )
			{
				binder = rsToBinder(rs);
			}
			else
			{
				throw new BinderNotFoundException("Could not find the E-Binder in the database.");
			}
		}
		catch ( SQLException sqle )
		{
			throw new CtdbException("Could not retrieve the E-Binder with studyId == " + Long.toString(studyId) + 
					" and type == " + Long.toString(typeId) + " : " + sqle.getLocalizedMessage(), sqle);
		}
		finally
		{
			close(rs);
			close(stmt);
		}
		
		return binder;
	}
	
	/**
	 * Creates an Ebinder object from the data in a result set.
	 * 
	 * @param rs - The result set from a select statement.
	 * @return	An Ebinder object constructed from the result set.
	 * @throws SQLException	If any database errors occur while building the Ebinder object.
	 */
	private Ebinder rsToBinder(ResultSet rs) throws SQLException
	{
		Ebinder b = new Ebinder();
		b.setId(rs.getInt("binderid"));
		b.setName(rs.getString("name"));
		b.setStudyId(rs.getLong("studyid"));
		b.setType(new CtdbLookup(rs.getInt("xbindertypeid")));
		b.setJsonTree(rs.getString("jsontree"));
		
		return b;
	}
}
