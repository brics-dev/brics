package gov.nih.nichd.ctdb.attachments.manager;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.attachments.dao.AttachmentDao;
import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateArchiveObjectException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.ServerFileSystemException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Dec 18, 2006
 * Time: 9:50:33 AM
 * 
 * Edited by CIT
 * User: engj
 */
public class AttachmentManager extends CtdbManager
{
	public static final int FILE_STUDY = 1;
	public static final int FILE_PATIENT = 2;
	public static final int FILE_COLLECTION = 3;
	public static final int FILE_FORM = 4;
	public static final int FILE_STUDY_SHARE_AGREEMENT = 5;
	public static final int FILE_STUDY_EBINDER = 6;
	
	private static final Logger logger = Logger.getLogger(AttachmentManager.class);
	
	/**
	 * Creates a database record for an attachment to an object, and saves the physical file to the 
	 * server's file system.
	 * 
	 * @param a - The attachment to be persisted to the database and server file system
	 * @throws DuplicateObjectException	When a duplicate mapping insertion is attempted
	 * @throws CtdbException	When any other errors occur while persisting the attachment to the database.
	 * @throws ServerFileSystemException	When an error occurs while saving the attachment to the server's file system.
	 */
	public void createAttachment(Attachment a, int protocolId)
			throws DuplicateObjectException, CtdbException, ServerFileSystemException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			AttachmentDao aDao = AttachmentDao.getInstance(conn);
			
			// Persist the attachment object to the database
			aDao.createAttachment(a);
			aDao.createAttachmentOrganization(a, protocolId);

			// Save the attachment's physical file to the server's file system
            if ( (a.getAttachFile() != null && a.getAttachFile().length() > 0) || (a.getAttachFileContent() != null) ) {
				aDao.saveFile(a, a.getAssociatedId(), a.getType().getId());
			}
			
			commit(conn);
		}
		finally
		{
			rollback(conn);
			close(conn);
		}
	}
	
	
	/**
	 * Creates a database record for each attachment in a Java list, and saves the physical files to the 
	 * server's file system.
	 * 
	 * @param attachments
	 * @throws CtdbException 
	 * @deprecated As of release 2.0.3, please use the {@link #createAttachments(List, List)} method instead.
	 */
	public void createAttachments(List<Attachment> attachments, int protocolId) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			AttachmentDao aDao = AttachmentDao.getInstance(conn);
			
			for ( Attachment a : attachments )
			{
				// Persist the attachment object to the database
				aDao.createAttachment(a);
				if (protocolId > 0) {
					aDao.createAttachmentOrganization(a, protocolId);
				}
				commit(conn);
				
				// Save the attachment's physical file to the server's file system
				aDao.saveFile(a, a.getAssociatedId(), a.getType().getId());
			}
		}
		catch ( ServerFileSystemException sfse )
		{
			throw new CtdbException("Could not save an attachment to the server's file system.", sfse);
		}
		finally
		{
			close(conn);
		}
	}
	
	/**
	 * Save any updated Attachment objects in the given Map. Any attachments that have been marked as changed
	 * will be created, updated, or deleted accordingly.
	 * 
	 * @param attachmentMap - A Map object containing attachments that may need their changes saved in the system
	 * @return	A list of attachments whose changes have been saved to the system
	 * @throws CtdbException	When there are any database errors.
	 * @throws ServerFileSystemException	When there are any file system errors.
	 */
	public List<Attachment> saveAttachments(Map<Long, Attachment> attachmentMap, int protocolId)
			throws CtdbException, ServerFileSystemException {
		List<Attachment> completedAttachments = new ArrayList<Attachment>(attachmentMap.size());
		
		for ( Attachment a : attachmentMap.values() ) {
			// Check if the attachment is either new or updated
			if ( a.isUpdated() && !a.isDeleted() ) {
				if ( a.getId() <= 0 ) {
					createAttachment(a, protocolId);
				}
				else {
					updateAttachment(a, protocolId);
				}
				
				completedAttachments.add(a);
			}
			// Process deleted attachment
			else if ( a.isDeleted() ) {
				deleteAttachment(a.getId(), a.getAssociatedId(), a.getType().getId());
				completedAttachments.add(a);
			}
		}
		
		return completedAttachments;
	}
	
	/**
	 * 
	 * @deprecated As of release 2.0.3, please use {@link #saveHashtableAttachments(Hashtable, List)} instead.
	 * @param siteHashMap
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws DuplicateObjectException
	 * @throws CtdbException
	 * @throws DuplicateArchiveObjectException 
	 * @throws ServerFileSystemException
	 */
	public List<Attachment> createUpdateAttachments(Map<String, Attachment> siteHashMap, int protocolId)
			throws ObjectNotFoundException, DuplicateObjectException, CtdbException, DuplicateArchiveObjectException
	{
		List<Attachment> toBeProcessed = new ArrayList<Attachment>();
		List<Attachment> toBeAdded = new ArrayList<Attachment>();
		List<Attachment> toBeEdited = new ArrayList<Attachment>();
		Attachment s = null;
		
		for ( Entry<String, Attachment> me : siteHashMap.entrySet() ) {
			s = me.getValue();
			toBeProcessed.add(s);
			
			if ( (s.getAttachmentActionFlag() != null) && s.getAttachmentActionFlag().equalsIgnoreCase("add_attachment") ) {
				toBeAdded.add(s);
			}
			else if ( (s.getAttachmentActionFlag() != null) && s.getAttachmentActionFlag().equalsIgnoreCase("edit_attachment") ) {
				toBeEdited.add(s);
			}
		}
		
		if ( !toBeAdded.isEmpty() ) {
			createAttachments(toBeAdded, protocolId);
		}
		
		if (!toBeEdited.isEmpty() ) {
			updateAttachments(toBeEdited, protocolId);
		}
		
		return toBeProcessed;
	}
	
	/**
	 * Updates an attachment in the database and file system
	 * 
	 * @param a - The updated attachment
	 * @throws DuplicateObjectException	If the attachment organization mapping is not unique.
	 * @throws CtdbException	If there was any other errors when persisting changes to the database
	 * @throws ServerFileSystemException If there is an error when saving changes to the file system.
	 * @throws DuplicateArchiveObjectException If a duplicate entry was attempted in the "attachmentarchive" table.
	 */
	public void updateAttachment(Attachment a, int protocolId)
			throws DuplicateObjectException, CtdbException, ServerFileSystemException, DuplicateArchiveObjectException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			AttachmentDao dao = AttachmentDao.getInstance(conn);
			
			// Persist changes to the database.
			dao.createAttachmentArchive(a.getId());
			dao.updateAttachment(a);
			if (protocolId > 0) {
				dao.updateAttachmentOrganization(a, protocolId);
			}
			
			// Save changes to the server's file system.
            if ( (a.getAttachFile() != null && a.getAttachFile().length() > 0) || (a.getAttachFileContent() != null) ) {
				dao.deleteFileFromSystem(a.getId(), a.getAssociatedId(), a.getType().getId());
				dao.saveFile(a, a.getAssociatedId(), a.getType().getId());
			}
			
			commit(conn);
		}
		finally
		{
			rollback(conn);
			close(conn);
		}
	}
	
	
	/**
	 * @deprecated As of release 2.0.3, replace with the {@link #updateAttachments(List, List)} method.
	 * @param list
	 * @throws CtdbException
	 * @throws DuplicateArchiveObjectException If a duplicate archive record was tried to be inserted in the "attachmentarchive" table
	 */
	public void updateAttachments(List<Attachment> list, int protocolId)
			throws CtdbException, DuplicateArchiveObjectException {
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			AttachmentDao dao = AttachmentDao.getInstance(conn);
			
			for (Attachment s : list ) {
	            dao.createAttachmentArchive(s.getId());
				dao.updateAttachment(s);
				if (protocolId > 0) {
					dao.updateAttachmentOrganization(s, protocolId);
				}
				dao.deleteFileFromSystem(s.getId(), s.getAssociatedId(), s.getType().getId());
				dao.saveFile(s, s.getAssociatedId(), s.getType().getId());
				commit(conn);
			}
		}
		catch ( ServerFileSystemException sfse )
		{
			rollback(conn);
			throw new CtdbException("Could not save an attachment to the server's file system.", sfse);
		}
		finally {
			rollback(conn);
			close(conn);
		}
	}
    
    /**
     * Deletes an attachment from the database and file system identified by its ID, associated ID, and type ID
     * 
     * @param attachmentId - The ID of the attachment to delete
     * @param associatedId - The ID of the associated object (i.e. study ID, subject ID, etc)
     * @param attachmentTypeId - The type of the object
     * @throws CtdbException	When a database error is thrown while trying to delete the attachment records
     * @throws ServerFileSystemException	When an error is thrown while deleting the attachment from the file system
     */
    public void deleteAttachment(long attachmentId, long associatedId, int attachmentTypeId) throws CtdbException, ServerFileSystemException
    {
    	logger.info("AttachmentManager)->deleteAttachment->attachmentId:\t"+attachmentId+"associatedId:\t"+associatedId+"attachmentTypeId:\t"+attachmentTypeId);
    	Connection conn = null;
    	
    	try
    	{
    		conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
    		AttachmentDao dao = AttachmentDao.getInstance(conn);
    		
    		// Archives the current attachment
    		dao.createAttachmentArchive(attachmentId);
    		
    		// Delete the attachment from the database
    		dao.deleteAttachment(attachmentId);
    		
    		// Delete the attachment from the file system first
    		dao.deleteFileFromSystem(attachmentId, associatedId, attachmentTypeId);
    		
    		commit(conn);
    	}
    	catch ( SecurityException se )
    	{
    		throw new ServerFileSystemException("There was an access violation while accessing attachment " + Long.toString(attachmentId) + 
    				" : " + se.getLocalizedMessage(), se);
    	}
    	finally
    	{
    		rollback(conn);
    		close(conn);
    	}
    }
    
    /**
     * Delete attachments from the database and server's file system that correspond to the list of attachment IDs, the associated object ID, 
     * and the attachment type.  If any database or file system errors occur during the deletion process, the error is logged in the error 
     * message list given to this method, and the process continues to the next available attachment.
     * 
     * @param attIds - The IDs of the attachments to be deleted.
     * @param associatedId - The associated object ID (i.e. study ID, subject ID, etc.)
     * @param attachmentTypeId - The attachment type
     * @param undeletableAttchs - An initially empty list that will be used to store which requested attachments that could not be deleted
     * @return	A list of Attachment objects that were deleted from the database and the server's file system.
     * @throws CtdbException	When there was a unrecoverable database error while trying to delete the requested attachments.
     */
    public List<Attachment> deleteAttachments(List<Long> attIds, long associatedId, int attachmentTypeId, 
    		List<String> undeletableAttchs) throws CtdbException
    {
    	Connection conn = null;
    	List<Attachment> attachList = null;
    	
    	try
    	{
    		conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
    		AttachmentDao aDao = AttachmentDao.getInstance(conn);
    		
    		// Convert the list of IDs with the attachments from the database, then delete them
    		attachList = aDao.getAttachments(attIds, associatedId, attachmentTypeId);
    		attachList = aDao.deleteAttachments(attachList, undeletableAttchs);
    	}
    	finally
    	{
    		rollback(conn);
    		close(conn);
    	}
    	
    	return attachList;
    }
    
	/**
	 * Gets an attachment from the database that corresponds to a specific ID.  The physical file data will not be
	 * included in the returned object.
	 * 
	 * @param attachmentId - The ID of the attachment to be retrieved from the database
	 * @return	The Attachment object that corresponds to the given ID.
	 * @throws CtdbException	If any database errors occurred while retrieving the attachment.
	 * @throws ObjectNotFoundException	When the given attachment ID is not in the database.
	 */
	public Attachment getAttachment (long attachmentId) throws CtdbException, ObjectNotFoundException
	{
		Connection conn = null;
		Attachment a = null;
		
		try
		{
			conn = CtdbManager.getConnection();
			a = AttachmentDao.getInstance(conn).getAttachment(attachmentId);
		}
		finally
		{
			close(conn);
		}
		
		return a;
	}
	
	/**
	 * Gets an attachment based some attachment organization data.
	 * 
	 * @param attachmentId - The ID of the attachment to retrieve from the database
	 * @param assocObjId - The ID of the object associated to the attachment (i.e. study ID, subject ID, etc.)
	 * @param typeId - The type ID of the attachment (see the "xattachmenttypes" table for details)
	 * @return	An Attachment object corresponding to the passed in parameters
	 * @throws ObjectNotFoundException	If no attachment was found matching the parameters
	 * @throws CtdbException	If any other database errors occurred
	 */
	public Attachment getAttachment(long attachmentId, long assocObjId, int typeId) throws ObjectNotFoundException, CtdbException
	{
		Connection conn = null;
		Attachment a = null;
		
		try
		{
			conn = getConnection();
			a = AttachmentDao.getInstance(conn).getAttachment(attachmentId, assocObjId, typeId);
		}
		finally
		{
			close(conn);
		}
		
		return a;
	}
	
	/**
	 * Gets the physical file data from the server's file system that corresponds to a specific attachment.
	 * 
	 * @param file - The Attachment object describing the physical file
	 * @return	A Java File handle to the physical file referenced by the given Attachment object.
	 * @throws ServerFileSystemException	If any errors occurred while retrieving the file from the file system.
	 * @throws ObjectNotFoundException		If the file cannot be found in the file system.
	 */
	public File getFileFromSystem(Attachment file) throws ServerFileSystemException, ObjectNotFoundException
	{
		return AttachmentDao.getInstance().getFileFromSystem(file);
	}
	
    /**
     * Gets all attachments of a certain type associated to whatever associated id is, but without the
     * file data.
     * 
     * @param typeId - The file type
     * @param associatedId - The ID of the associated object (i.e. study ID, subject ID, etc)
     * @return	A list of Attachment objects from the database that corresponds to the specified type and associated ID.
     * @throws CtdbException	If any database errors occurred during the retrieval.
     */
	public List<Attachment> getAttachments(int typeId, long associatedId) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection();
			return AttachmentDao.getInstance(conn).getAttachments(typeId, associatedId);
		}
		finally
		{
			close(conn);
		}
	}
	
	/**
	 * Extracts the attachments out of the given Hashtable and joins it with the attachments from the database.  
	 * Then sorts the resulting list by attachment name.
	 * 
	 * @param attachmentTable - The attachment Hashtable
	 * @param associatedId - The ID of the associated object (i.e. study ID, subject ID, etc.)
	 * @param attachmentType - The attachment type ID
	 * @return	A list of Attachment objects sorted by attachment name
	 * @throws CtdbException 
	 */
	public List<Attachment> getAttachmentListFromHashtable(Hashtable<Long, Attachment> attachmentTable, 
			long associatedId, int attachmentType) throws CtdbException
	{
		List<Attachment> dbAttachmentList = getAttachments(attachmentType, associatedId);
		Hashtable<Long, Attachment> tmpTable = new Hashtable<Long, Attachment>(attachmentTable.size() + dbAttachmentList.size());
		List<Attachment> attachmentList = null;
		
		// Copy the contents of the attachmentTable and db attachment list into the temp table
		for ( Attachment a : dbAttachmentList )
		{
			tmpTable.put(new Long(a.getId()), a);
		}
		
		tmpTable.putAll(attachmentTable);
		
		// Copy the contents of the temp Hashtable into the main list, and sort the list by attachment name
		attachmentList = new ArrayList<Attachment>(tmpTable.values());
		
		Collections.sort(attachmentList, new Comparator<Attachment>()
		{
			public int compare(Attachment a1, Attachment a2)
			{
				return a1.getName().compareTo(a2.getName());
			}
			
		});
		
		return attachmentList;
	}
	
	/**
	 * Retrieves attachments from the archive table that correspond to a specific attachment ID.
	 * 
	 * @param id - The ID of the attachment who's archives are requested
	 * @return	A list of Attachment objects from the attachment archive table
	 * @throws CtdbException	When any database errors occurred.
	 */
	public List<Attachment> getAttachmentAudit(long id) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection();
			return AttachmentDao.getInstance(conn).getAttachmentAudit(id);
		}
		finally
		{
			close(conn);
		}
	}
	
	/**
	 * Determines whether or not an attachment name is not already in the database for a given type and associated ID.
	 * 
	 * @param attachmentName - The name to search for.
	 * @param typeId - The attachment type to search over.
	 * @param associatedId - The associated ID (Study ID most likely) to search over.
	 * @return	True if and only if the given attachment name is not in the database for a given type and associated ID.
	 * @throws CtdbException When there is an error while querying the database.
	 */
	public boolean isAttachmentNameUnique(String attachmentName, int typeId, long associatedId) throws CtdbException
	{
		boolean isUnique = false;
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection();
			isUnique = AttachmentDao.getInstance(conn).isAttachmentNameUnique(attachmentName, typeId, associatedId);
		}
		finally
		{
			close(conn);
		}
		
		return isUnique;
	}

	/**
	 * Gets all attachments of a certain type associated to whatever associated id is, but without the file data.
	 * 
	 * @param typeId - The file type
	 * @param associatedId - The ID of the associated object (i.e. study ID, subject ID, etc)
	 * @param protocolId - The ID of the protocol object
	 * @return A list of Attachment objects from the database that corresponds to the specified type and associated ID.
	 * @throws CtdbException If any database errors occurred during the retrieval.
	 */
	public List<Attachment> getProtocolAttachments(int typeId, long associatedId, long protocolId)
			throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			return AttachmentDao.getInstance(conn).getProtocolAttachments(typeId, associatedId, protocolId);
		} finally {
			close(conn);
		}
	}
}
