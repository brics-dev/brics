package gov.nih.nichd.ctdb.ebinder.manager;

import java.sql.Connection;

import gov.nih.nichd.ctdb.common.BinderNotFoundException;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.ebinder.dao.BinderDao;
import gov.nih.nichd.ctdb.ebinder.domain.Ebinder;

/**
 * The data manager for the E-Binder.
 * 
 * @author Originally created by IntelliJ IDEA.  Completely re-wrote by DCB
 */
public class BinderManager extends CtdbManager
{
	public static final String DEFAULT_TREE = "{\"folders\": [],\"files\" : []}";
	public static final int TYPE_STUDY = 1;
	
	/**
	 * Persists the changes from the Ebinder object to the database.
	 * 
	 * @param binder - The Ebinder object to save to the database
	 * @throws CtdbException	When a database error occurs while saving the binder.
	 */
	public void saveEbinder(Ebinder binder) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			BinderDao bDao = BinderDao.getInstance(conn);
			
			// Check if the binder needs to be created or updated
			if ( binder.getId() > 0 )
			{
				bDao.updateEbinder(binder);
			}
			else
			{
				bDao.addEbinder(binder);
			}
		}
		finally
		{
			close(conn);
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
		Connection conn = null;
		
		try
		{
			conn = getConnection();
			
			return BinderDao.getInstance(conn).getEbinder(studyId, typeId);
		}
		finally
		{
			close(conn);
		}
	}
}
