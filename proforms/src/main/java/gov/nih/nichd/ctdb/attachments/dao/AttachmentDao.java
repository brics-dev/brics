package gov.nih.nichd.ctdb.attachments.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.postgresql.util.PSQLException;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.attachments.domain.AttachmentCategory;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateArchiveObjectException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.ServerFileSystemException;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Dec 13, 2006
 * Time: 11:23:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentDao extends CtdbDao
{
	private static Logger logger = Logger.getLogger(AttachmentDao.class);
	
    /**
       * Private Constructor to hide the instance
       * creation implementation of the AttachmentDao object
       * in memory. This will provide a flexible architecture
       * to use a different pattern in the future without
       * refactoring the AttachmentDao.
       */
      private AttachmentDao() {

      }

      /**
       * Method to retrieve the instance of the AttachmentDao.
       *
       * @return AttachmentDao data object
       */
      public static synchronized AttachmentDao getInstance() {
          return new AttachmentDao();
      }

      /**
       * Method to retrieve the instance of the AttachmentDao. This method
       * accepts a Database Connection to be used internally by the DAO. All
       * transaction management will be handled at the BusinessManager level.
       *
       * @param conn Database connection to be used within this data object
       * @return AttachmentDao data object
       */
      public static synchronized AttachmentDao getInstance(Connection conn) {
          AttachmentDao dao = new AttachmentDao();
          dao.setConnection(conn);
          return dao;
      }
    
  /**
   * Creates an attachment in the database and sets the attachment objects ID.  Auto committing of database transactions 
   * must be disabled before calling this method.
   * 
   * @param a - The attachment to be created in the database
   * @throws CtdbException	If an error occurs while creating the database record
   */
    public void createAttachment(Attachment a) throws CtdbException
    {
        PreparedStatement stmt = null;

        try
        {
            stmt = this.conn.prepareStatement(getCreateAttachmentStatement());
            stmt.setString(1, a.getName());
            stmt.setString(2, a.getFileName());
            stmt.setString(3, a.getDescription());
            stmt.setLong(4, a.getUpdatedBy());
            stmt.setLong(5, a.getCreatedBy());
            stmt.setString(6, a.getChangeReason());
            
            if ( a.getPublicationType() != null )
            {
            	stmt.setLong(7, a.getPublicationType().getId());
            }
            else
            {
            	stmt.setNull(7, Types.BIGINT);
            }
            
            stmt.setString(8, a.getAuthors());
            stmt.setString(9, a.getUrl());
            stmt.setString(10, a.getPubMedId());
            stmt.executeUpdate();
            
            // Set the ID of the recently created attachment record
            a.setId(this.getInsertId(conn, "attachment_seq"));
	    }
        catch ( SQLException sqle )
        {
        	rollback();
	        throw new CtdbException("Failure storing attachment " + a.getName() + " : " + sqle.getLocalizedMessage(), sqle);
	    }
        finally
        {
	        close(stmt);
	    }
    }
    
    
    /**
     * Retrieves the insert statement for the "attachment" table.
     * 
     * @return	The attachment insert statement.
     */
    private String getCreateAttachmentStatement()
    {
    	String sqlStmt = "insert into attachment (attachmentid, name, filename, description, " +
				 		 "updatedby, updateddate, createdby, createddate, version, changereason, " +
				 		 "xpublicationtypeid, authors, url, pubmedid) values " +
				 		 "(DEFAULT, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, 1, ?, ?, ?, ?, ?) ";
    	
    	return sqlStmt;
    }

    /**
     * Creates an attachment organization record including type, category, and associated id. 
     * Should only be called when creating an attachment.
     * 
     * @param a - The attachment to create the association
     * @throws DuplicateObjectException	When a duplicate mapping insertion was attempted
     * @throws CtdbException	When any other database error occurs while creating the mapping
     */
    public void createAttachmentOrganization (Attachment a) throws DuplicateObjectException, CtdbException
    {
        PreparedStatement stmt = null;
        
        try
        {
            stmt = conn.prepareStatement(getCreateAttachmentOrgStatement());
            
            stmt.setLong(1, a.getId());
            stmt.setLong(2, a.getType().getId());
            
            if ( a.getCategory().getId() > 0 )
            {
            	stmt.setLong(3, a.getCategory().getId());
            }
            else
            {
            	stmt.setNull(3, Types.BIGINT);
            }
            
            stmt.setLong(4, a.getAssociatedId());
            
            stmt.executeUpdate();
        }
        catch ( PSQLException psqle )
		{
        	rollback();
        	
            if ( psqle.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate")) )
            {
				throw new DuplicateObjectException("The attachment assoication already exists in the database : " + psqle.getLocalizedMessage(), psqle);
			}
			else
			{
				throw new CtdbException("Unable to create the attachment association : " + psqle.getLocalizedMessage(), psqle);
            }
        }
		catch( SQLException sqle )
		{
			rollback();
			
			if( sqle.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code")) )
			{
				throw new DuplicateObjectException("The attachment assoication already exists in the database : " + sqle.getLocalizedMessage(), sqle);
			}
			else
			{
				throw new CtdbException("Unable to create the attachment association : " + sqle.getLocalizedMessage(), sqle);
			}
		}
        finally
        {
            this.close(stmt);
        }
    }
    
    /**
     * Retrieves the insert statement for the "attachmentorganization" table.
     * 
     * @return	The attachmentorganization insert statement.
     */
    private String getCreateAttachmentOrgStatement()
    {
    	String sqlStmt = "insert into attachmentorganization (attachmentid, xattachmenttypeid, attachmentcategoryid, associatedid ) " +
    					 " values (?, ?, ?, ?) ";
    	
    	return sqlStmt;
    }
    
    /**
	 * Creates a new file or saves changes to an existing file on the server.
	 * 
	 * @param file - The file to be saved on the server
	 * @param associatedObjId - The ID of the associated object (i.e. study ID or form ID)
	 * @param attachType - The attachment type ID, which is used to identify the file's storage directory
	 * @throws ServerFileSystemException	If the file could not be written to, if a security violation occurred, or
	 * 										if the storage location could not be determined.
	 */
	public void saveFile (Attachment file, long associatedObjId, int attachType) throws ServerFileSystemException
	{
		String fileDir = getStorageDir(associatedObjId, file.getId(), attachType);
		File physFile = new File(fileDir + File.separator + file.getFileName());
		FileOutputStream out = null;
		InputStream in = null;
		byte[] buffer = new byte[4096];  // 4KB buffer
		int bytesRead = 0;
		
		try
		{
			if ( verifyDirectory(fileDir) )
			{
				// Create the file on the server
				physFile.createNewFile();
				out = new FileOutputStream(physFile, false);
				
				// Create the input stream and write out to the file
				if ( file.getAttachFileContent() != null ) {
					in = file.getAttachFileContent();
				}
				else if ( (file.getAttachFile() != null) && (file.getAttachFile().length() > 0) ) {
					in = new FileInputStream(file.getAttachFile());
				}
				else {
					throw new ServerFileSystemException("Could not create a input stream form the " + file.getName() + " attachment.");
				}
				
				while ((bytesRead = in.read(buffer)) > 0) {
					out.write(buffer, 0, bytesRead);
					out.flush();
				}
			}
			else
			{
				throw new ServerFileSystemException("Something went wrong while verifing the \"" + fileDir + "\" directory.");
			}
		}
		catch (SecurityException se )
		{
			throw new ServerFileSystemException("Could not create the file on the server:  " + se.getMessage(), se);
		}
		catch ( FileNotFoundException fnfe )
		{
			throw new ServerFileSystemException("Could not access the " + file.getFileName() + " file.", fnfe);
		}
		catch ( IOException ie )
		{
			throw new ServerFileSystemException("Failed to write to the system: " + ie.getMessage(), ie);
		}
		finally
		{
			close(in);
			close(out);
		}
	}
	
	/**
	 * Helper method that will check for the existence of the passed in directory. If the directory doesn't exist,
	 * it will be created.
	 * 
	 * @param dirPath - The directory path to be verified
	 * @return	True if and only if the directory represented in the path exists or the directory was created successfully
	 * @throws SecurityException	If a security violation occurs while reading or creating the directory
	 */
	private boolean verifyDirectory(String dirPath) throws SecurityException
	{
		boolean wasSuccessful = false;
		File dir = new File(dirPath);
		
		if ( !dir.exists() )
		{
			wasSuccessful = dir.mkdirs();
		}
		else
		{
			wasSuccessful = true;
		}
		
		return wasSuccessful;
	}

   /**
    * Updates an attachment record in the database.  Auto committing of database transactions must be
    * disabled before calling this method.
    *
    * @param a - The updated attachment used to persist changes to the database
    * @throws CtdbException	When any database errors occurred while updating the attachment record
    */
    public void updateAttachment(Attachment a) throws CtdbException
    {
        PreparedStatement stmt = null;
        
        try
        {
            stmt = this.conn.prepareStatement(getUpdateAttachmentStatement());
            stmt.setString(1, a.getName());
            stmt.setString(2, a.getFileName());
            stmt.setString(3, a.getDescription());
            stmt.setString(4, a.getChangeReason());
            stmt.setLong(5, a.getUpdatedBy());
            
            if ( a.getPublicationType() != null )
            {
            	stmt.setLong(6, a.getPublicationType().getId());
            }
            else
            {
            	stmt.setNull(6, Types.BIGINT);
            }
            
            stmt.setString(7, a.getAuthors());
            stmt.setString(8, a.getUrl());
            stmt.setString(9, a.getPubMedId());
            stmt.setLong(10, a.getId());
            stmt.executeUpdate();
        }
        catch ( SQLException sqle )
        {
        	rollback();
            throw new CtdbException("Failure updating attachment : " + sqle.getLocalizedMessage(), sqle);
        }
        finally
        {
            close(stmt);
        }
    }
    
    
    /**
     * Retrieves the update statement for the "attachment" table.
     * 
     * @return	The attachment update statement.
     */
    private String getUpdateAttachmentStatement()
    {
    	String sqlStmt = "update attachment set name = ?, filename = ? , description = ?, changereason = ?, updatedby = ?, " +
    					 "updateddate = CURRENT_TIMESTAMP, version = version + 1, xpublicationtypeid = ?, authors = ?, url = ?, pubmedid = ? " +
    					 "where attachmentid = ? ";
    	
    	return sqlStmt;
    }

    /**
     * Creates an archive record for an attachment, occurs with every attachment edit.  Auto committing of database
     * transactions must be disabled before calling this method.
     * 
     * @param attachmentId - The ID of the attachment that is changing
     * @throws CtdbException	When a database error occurred while creating the archive.
     * @throws DuplicateArchiveObjectException When a duplicate archive record insertion is attempted.
     */
    public void createAttachmentArchive(long attachmentId) throws CtdbException, DuplicateArchiveObjectException
    {
        logger.info("AttachmentDAO->createAttachmentArchive->attachmentId:\t"+attachmentId);
    	PreparedStatement stmt = null;
        
        try
        {
            stmt = this.conn.prepareStatement(getCreateAttachmentArchiveStatement());
            stmt.setLong(1, attachmentId);
            stmt.executeUpdate();

        }
        catch ( PSQLException psqle )
		{
        	rollback();
        	
            if ( psqle.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate")) )
            {
				throw new DuplicateArchiveObjectException("The attachment archive record already exists in the database : " + 
						psqle.getLocalizedMessage(), psqle);
			}
			else
			{
				throw new CtdbException("Failure creating attachment archive with ID " + Long.toString(attachmentId) + " : " + 
						psqle.getLocalizedMessage(), psqle);
            }
        }
        catch ( SQLException sqle )
        {
        	rollback();
        	
        	if( sqle.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code")) )
			{
				throw new DuplicateArchiveObjectException("The attachment archive record already exists in the database : " + 
						sqle.getLocalizedMessage(), sqle);
			}
        	else
        	{
        		throw new CtdbException("Failure creating attachment archive with ID " + Long.toString(attachmentId) + " : " + 
        				sqle.getLocalizedMessage(), sqle);
        	}
        }
        finally
        {
            this.close(stmt);
        }
    }
    
    /**
     * Retrieves the insert statement for the "attachmentarchive" table.
     * 
     * @return	The attachment archive insert statement.
     */
    private String getCreateAttachmentArchiveStatement()
    {
    	String sqlStmt = "insert into attachmentarchive select * from attachment where attachmentid = ? ";
    	
    	return sqlStmt;
    }

    /**
     * Updates attachment's organization mapping data, which is limited to only updating the category. 
     * Should only be called when updating an attachment.  Auto committing of database transcations must be
     * disabled before calling this method.
     * 
     * @param a - The attachment who's organization data needs updating
     * @throws DuplicateObjectException When the association is not unique
     * @throws CtdbException 	When other database errors are encountered while updating the mapping.
     */
    public void updateAttachmentOrganization(Attachment a) throws DuplicateObjectException, CtdbException
    {
        PreparedStatement stmt = null;
        
        try
        {
            stmt = conn.prepareStatement(getUpdateAttachmentOrgStatement());
            
            if ( a.getCategory().getId() > 0 )
            {
            	stmt.setLong(1, a.getCategory().getId());
            }
            else
            {
            	stmt.setNull(1, Types.BIGINT);
            }
            
            stmt.setLong(2, a.getId());
            stmt.setLong(3, a.getType().getId());
            stmt.setLong(4, a.getAssociatedId());
            stmt.executeUpdate();
        }
        catch ( PSQLException e )
		{
        	rollback();
        	
            if ( e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate")) )
            {
				throw new DuplicateObjectException("The attachment assoication already exists in the database : " + e.getLocalizedMessage(), e);
			}
			else
			{
				throw new CtdbException("Unable to create the attachment association : " + e.getLocalizedMessage(), e);
            }
        }
		catch( SQLException e )
		{
			rollback();
			
			if( e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code")) )
			{
				throw new DuplicateObjectException("The attachment assoication already exists in the database : " + e.getLocalizedMessage(), e);
			}
			else
			{
				throw new CtdbException("Unable to create the attachment association : " + e.getLocalizedMessage(), e);
			}
		}
        finally
        {
            this.close(stmt);
        }
    }
    
    /**
     * Retrieves the update statement for the "attachmentorganization" table.
     * 
     * @return	The attachment organization update statement.
     */
    private String getUpdateAttachmentOrgStatement()
    {
    	String sqlStmt = "update attachmentorganization set attachmentcategoryid = ? " +
    					 "where attachmentid = ? and xattachmenttypeid = ? and associatedid = ? ";
    	
    	return sqlStmt;
    }

    /**
     * Deletes an attachment and attachment organization record. Auto committing of database transactions 
     * must be disabled before calling this method.
     * 
     * @param attachmentId - The ID of the attachment to be deleted
     * @throws CtdbException	If an error occurred while deleting the attachment from the database
     */
    public void deleteAttachment(long attachmentId) throws CtdbException
    {
    	logger.info("AttachmentDAO->deleteAttachment->attachmentId:\t"+attachmentId);
    	PreparedStatement stmt = null;
        
        try
        {
        	// Delete records from the "attachmentorganization" mapping table
            stmt = this.conn.prepareStatement(getDeleteAttachmentOrgStatement());
            stmt.setLong(1, attachmentId);
            stmt.executeUpdate();
            close(stmt);
            
            // Delete the attachment from the "attachment" table
            stmt = this.conn.prepareStatement(getDeleteAttachmentStatement());
            stmt.setLong(1, attachmentId);
            stmt.executeUpdate();
        }
        catch ( SQLException sqle )
        {
        	rollback();
            throw new CtdbException("Failure deleteing attachemnt with id " + Long.toString(attachmentId) + " : " + sqle.getMessage(), sqle);
        }
        finally
        {
            this.close(stmt);
        }
    }
    
    /**
     * Deletes the listed attachments from the database and the server's file system.  Auto committing of database transactions 
     * must be disabled before calling this method.
     * 
     * @param attachList - A list of attachments to be deleted
     * @param errorAttachmentList - Used to record attachments that could not be deleted
     * @return	A list of Attachment objects that were deleted from the database and server file system
     * @throws CtdbException	If the deletion SQL statements could not be prepared.
     */
    public List<Attachment> deleteAttachments(List<Attachment> attachList, List<String> errorAttachmentList) throws CtdbException
    {
    	PreparedStatement attachArchStmt = null;
    	PreparedStatement delAttachOrgStmt = null;
    	PreparedStatement delAttachStmt = null;
    	List<Attachment> deletedAttachList = new ArrayList<Attachment>(attachList.size());
    	
    	try
    	{
    		// Prepare the insert into the "attachmentarchive" table statement
    		attachArchStmt = conn.prepareStatement(getCreateAttachmentArchiveStatement());
    		
    		// Prepare the delete from "attachmentorganization" statement
    		delAttachOrgStmt = conn.prepareStatement(getDeleteAttachmentOrgStatement());
    		
    		// Prepare the delete from "attachment" statement
    		delAttachStmt = conn.prepareStatement(getDeleteAttachmentStatement());
    		
    		for ( Attachment a : attachList )
    		{
    			try
    			{
    				// Archive the current attachment data
    				attachArchStmt.setLong(1, a.getId());
    				attachArchStmt.executeUpdate();
    				
    				// Delete records from the "attachmentorganization" mapping table
    				delAttachOrgStmt.setLong(1, a.getId());
    				delAttachOrgStmt.executeUpdate();
    				
    				// Delete the attachment from the "attachment" table
    				delAttachStmt.setLong(1, a.getId());
    				delAttachStmt.executeUpdate();
    				
    				// Delete the file from the server's file system
    				deleteFileFromSystem(a.getId(), a.getAssociatedId(), a.getType().getId());
    				
    				// Commit the database deletions
    				conn.commit();
    				
    				// Add the attachment to the deleted list
    				deletedAttachList.add(a);
    			}
    			catch ( SQLException sqle )
    			{
    				// Roll back the transaction
    				rollback();
    				
    				// Log errors to system log
    				logger.error("Could not remove the attachment record from the database.", sqle);
    				
    				// Add attachment name to the error attachment list
    				errorAttachmentList.add(a.getName());
    			}
    			catch ( SecurityException se )
    			{
    				// Roll back the transaction
    				rollback();
    				
    				// Log errors to system log
    				logger.error("Could not access the attachment on the server's file system.", se);
    				
    				// Add attachment name to the error attachment list
    				errorAttachmentList.add(a.getName());

    			}
    			catch ( ServerFileSystemException sfse )
    			{
    				// Roll back the transaction
    				rollback();
    				
    				// Log errors to system log
    				logger.error("Could not delete the attachment from the server's file system.", sfse);
    				
    				// Add attachment name to the error attachment list
    				errorAttachmentList.add(a.getName());
    			}
    		}
    	}
    	catch ( SQLException sqle )
        {
            throw new CtdbException("Could not prepare delete statements : " + sqle.getLocalizedMessage(), sqle);
        }
    	finally
    	{
    		close(attachArchStmt);
    		close(delAttachOrgStmt);
    		close(delAttachStmt);
    	}
    	
    	return deletedAttachList;
    }
    
    /**
     * Retrieves the delete statement for the "attachmentorganization" table.
     * 
     * @return The attachment organization delete statement.
     */
    private String getDeleteAttachmentOrgStatement()
    {
    	String sqlStmt = "delete from attachmentorganization where attachmentid = ? ";
    	
    	return sqlStmt;
    }
    
    /**
     * Retrieves the delete statement for the "attachment" table.
     * 
     * @return	The attachment delete statement.
     */
    private String getDeleteAttachmentStatement()
    {
    	String sqlStmt = "delete from attachment where attachmentid = ? ";
    	
    	return sqlStmt;
    }
    
    /**
     * Deletes a file from the server's file system.  If the target file is not on the file
     * system, it will be ignored.
     * 
     * @param attachmentId - The file to be deleted
     * @param associatedObjId - The ID of the associated object (i.e. study ID or form ID)
     * @param attachType - The attachment's type ID, which is used to identify the file's storage directory
     * @throws SecurityException	If there is a security violation while accessing any of the files
     * @throws ServerFileSystemException	When a storage location cannot be determined.
     */
    public void deleteFileFromSystem(long attachmentId, long associatedObjId, int attachType) throws SecurityException, ServerFileSystemException
    {
    	logger.info("AttachmentManager)->deleteFileFromSystem->attachmentId:\t"+attachmentId+"associatedObjId:\t"+associatedObjId+"attachType:\t"+attachType);
    	String fileDir = getStorageDir(associatedObjId, attachmentId, attachType);
    	File dir = new File(fileDir);
    	File[] sysFiles = null;
		
		if ( dir.exists() )
		{
			sysFiles = dir.listFiles(); // Get a list of files in the storage directory.
			
			// Check if errors occurred while getting the list of files
			if ( sysFiles != null )
			{
				// Delete all files in the storage directory
				for ( int i = 0; i < sysFiles.length; i++ )
				{
					sysFiles[i].delete();
				}
				
				// Delete the storage directory
				dir.delete();
			}
			else
			{
				throw new ServerFileSystemException("Could not delete the files in the " + fileDir + " directory.");
			}
		}
    }
    
    /**
     * Deletes files from the server's file system.  Any requested files that are not on the file
     * system will be ignored.
     * 
     * @param fileNameList - The list of files to be deleted
     * @param associatedObjId - The ID of the associated object (i.e. study ID or form ID)
	 * @param attachType - The attachment type ID, which is used to identify the file's storage directory
     * @throws SecurityException	If there is a security violation while accessing any of the files
     * @throws ServerFileSystemException	When a storage location cannot be determined.
     */
    public void deleteFilesFromSystem(List<Attachment> fileList, long associatedObjId, int attachType) throws SecurityException, ServerFileSystemException
    {
    	for ( Attachment file : fileList )
    	{
    		deleteFileFromSystem(file.getId(), associatedObjId, attachType);
    	}
    }
	
    /**
     * Checks if the given list of files exists in the server's file system.
     * 
     * @param fileList - A list of attachments to check for
     * @param objId - The ID of the associated object (i.e. study ID or form ID)
	 * @param attachType - The attachment type ID, which is used to identify the file's storage directory
     * @return	A list of attachments that are confirmed to be on the file system.  This is
     * 			either a copy of the passed in list, a subset of the list, or an empty list
     * 			if none of the files can be found in the server's file system
     * @throws ServerFileSystemException	When a storage location cannot be determined.
     */
	public List<Attachment> checkIfFilesExist(List<Attachment> fileList, long associatedObjID, int attachType) throws ServerFileSystemException
	{
		List<Attachment> existingFiles = new ArrayList<Attachment>(fileList.size());
		String fileDir = "";
		File sysFile = null;
		
		for ( Attachment file : fileList )
		{
			fileDir = getStorageDir(associatedObjID, file.getId(), attachType);
			sysFile = new File(fileDir + File.separator + file.getFileName());
			
			if ( sysFile.exists() )
			{
				existingFiles.add(file);
			}
		}
		
		return existingFiles;
	}

    /**
     * Gets an attachment including its attachment organization information.
     * 
     * @param attachmentId - The ID of the attachment record used to retrieve its data
     * @return	An Attachment object representing the data stored in the database
     * @throws ObjectNotFoundException When the attachment cannot be found in the database
     * @throws CtdbException	When there are any other database errors thrown while retrieving the attachment
     */
    public Attachment getAttachment(long attachmentId) throws ObjectNotFoundException, CtdbException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Attachment a = null;
        
        try
        {
            String sql = getSelectAttachmentStatement() + "where a.attachmentid = ? ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, attachmentId);
            rs =  stmt.executeQuery();
            
            if ( rs.next() )
            {
                a = rsToAttachment(rs, false);
            }
            else
            {
                throw new ObjectNotFoundException("Could not find the attachment identified by " + Long.toString(attachmentId));
            }
        }
        catch ( SQLException sqle )
        {
            throw new CtdbException("Failure getting attachemnt with id " + Long.toString(attachmentId) + " : " + sqle.getMessage(), sqle);
        }
        finally
        {
            this.close(stmt);
            this.close(rs);
        }
        
        return a;
    }
    
    /**
     * Gets an attachment including its attachment organization information.
     * 
     * @param attachmentId - The ID of the attachment record
     * @param assoicatedObjId - The ID of the associated object (i.e. study ID, subject ID, etc.)
     * @param typeId - The type ID of the attachment (see the "xattachmenttypes" table for details)
     * @return	An Attachment object that corresponds to the passed in parameters
     * @throws ObjectNotFoundException	Thrown if there is no record associated with the passed in parameters (i.e. no attachment record was found)
     * @throws CtdbException	Thrown if there are any other database errors
     */
    public Attachment getAttachment(long attachmentId, long assoicatedObjId, int typeId) throws ObjectNotFoundException, CtdbException
    {
    	PreparedStatement stmt = null;
        ResultSet rs = null;
        Attachment a = null;
        
        try
        {
        	String sql = getSelectAttachmentStatement() + "where ao.attachmentid = ? and ao.associatedid = ? and ao.xattachmenttypeid = ? ";
        	
        	stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, attachmentId);
            stmt.setLong(2, assoicatedObjId);
            stmt.setLong(3, typeId);
            rs =  stmt.executeQuery();
            
            if ( rs.next() )
            {
            	a = rsToAttachment(rs, false);
            }
            else
            {
            	throw new ObjectNotFoundException("Could not find the attachment identified by Attachment ID = " + Long.toString(attachmentId) + 
            			", Assoc. ID = " + Long.toString(assoicatedObjId) + ", and Type ID = " + Integer.toString(typeId));
            }
        }
        catch ( SQLException sqle )
        {
            throw new CtdbException("Failure getting attachemnt with id " + Long.toString(attachmentId) + " : " + sqle.getMessage(), sqle);
        }
        finally
        {
        	close(rs);
        	close(stmt);
        }
        
        return a;
    }
    
    /**
     * Retrieves the select statement for an attachment.  A "where" clause is not included in the returned string.  The calling
     * method must append a "where" clause in the returned select statement to complete the query.
     * 
     * @return	The attachment object select statement minus the needed where clause.
     */
    private String getSelectAttachmentStatement()
    {
    	String sqlStmt = "select a.*, ao.*, at.xattachmenttypeid at_id, at.name at_name, at.description at_desc, ac.attachmentcategoryid ac_id, " +
    					 "ac.name ac_name, ac.description ac_desc, ac.protocolid ac_protocolid, updated.username from attachment a inner join " +
    					 "attachmentorganization ao on a.attachmentid = ao.attachmentid inner join xattachmenttype at on " +
    					 "ao.xattachmenttypeid = at.xattachmenttypeid inner join usr updated on a.updatedby = updated.usrid left outer join " +
    					 "attachmentcategory ac on ao.attachmentcategoryid = ac.attachmentcategoryid ";
    	
    	return sqlStmt;
    }
    
    /**
	 * Retrieves a file from the server's file system
	 * 
	 * @param file - The file to retrieve
	 * @return	The file requested as a File object
	 * @throws ServerFileSystemException	When a storage location cannot be determined.
     * @throws ObjectNotFoundException If the file is not found in the file system.
	 */
	public File getFileFromSystem(Attachment file) throws ServerFileSystemException, ObjectNotFoundException
	{
		String fileDir = getStorageDir(file.getAssociatedId(), file.getId(), file.getType().getId());
		File sysFile = new File(fileDir + File.separator + file.getFileName());
		
		if ( !sysFile.exists() )
		{
			throw new ObjectNotFoundException("Could not find the " + file.getName() + " attachment in the file system.");
		}
		
		return sysFile;
	}
    
    /**
     * Get a list of attachments that corresponds to the list of attachment IDs, the associated object id, and the attachment type
     * 
     * @param attachIds - A list of attachment IDs to search for
     * @param associatedId - The ID of the associated object (i.e. study ID, patient ID, etc.)
     * @param typeId - The attachments type
     * @return	A list of Attachment objects from the database that corresponds to the IDs, object ID, and attachment type ID
     * @throws CtdbException	If an error occurs while preparing the select statement or when executing the select statement.
     */
    public List<Attachment> getAttachments(List<Long> attachIds, long associatedId, int typeId) throws CtdbException
    {
    	PreparedStatement stmt = null;
		ResultSet rs = null;
		Attachment a = null;
		List<Attachment> attchList = new ArrayList<Attachment>(attachIds.size());
		
		String sql = getSelectAttachmentStatement() + "where ao.attachmentid = ? and ao.associatedid = ? and ao.xattachmenttypeid = ? ";
		
		try
		{
			// Prepare the attachment select statement
			stmt = conn.prepareStatement(sql);
			
			for ( Long id : attachIds )
			{
				stmt.setLong(1, id.longValue());
				stmt.setLong(2, associatedId);
				stmt.setLong(3, typeId);
				rs = stmt.executeQuery();
				
				if ( rs.next() )
				{
					try
					{
						a = rsToAttachment(rs, false);
						attchList.add(a);
					}
					catch ( SQLException sqle )
					{
						logger.error("Could not get the attachment.", sqle);
					}
				}
				
				close(rs);
			}
		}
		catch ( SQLException sqle )
		{
			throw new CtdbException("Could not prepare the select statement, or create an Attachment from the result set : " + sqle.getLocalizedMessage(), sqle);
		}
		finally
		{
			close(rs);
			close(stmt);
		}
		
		return attchList;
    }
    
    /**
     * Gets a list of attachments from the database.  The retrieval is based on its type and associated ID.
     * 
     * @param typeId - The type of the attachment (see "xattachmenttype" table for type listing)
     * @param associatedId - The ID of the associated object (i.e. study ID, subject ID, etc.)
     * @return	A list of attachment objects, or an empty list if there are no attachments matching the specified type
     * 			and associated ID.
     * @throws CtdbException	If any database errors occurred during the retrieval.
     */
	public List<Attachment> getAttachments (int typeId, long associatedId) throws CtdbException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<Attachment> attchList = new ArrayList<Attachment>();
		
		try
		{
			String sql = getSelectAttachmentStatement() + "where ao.xattachmenttypeid = ? and ao.associatedid = ? order by a.name, ac_name ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, typeId);
			stmt.setLong(2, associatedId);
			rs = stmt.executeQuery();
			
			while ( rs.next() )
			{
				attchList.add(rsToAttachment(rs, false));
			}
			
		}
		catch (SQLException sqle)
		{
			throw new CtdbException("Failure getting attachemnts with associatedid " + Long.toString(associatedId) + " : " + sqle.getMessage(), sqle);
		}
		finally
		{
			this.close(stmt);
			this.close(rs);
		}
		
		return attchList;
	}
    
    /**
     * Gets the information to display audit trail of the specified attachment
     * 
     * @param attachId - The attachment 
     * @return	Attachment objects to be displayed, or an empty list if no data is found.
     * @throws CtdbException	If any database errors occurred during the query.
     */
	public List<Attachment> getAttachmentAudit(long attachId) throws CtdbException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Attachment> results = new ArrayList<Attachment>();
		
		try
		{
			String sql = getSelectAttachmentStatement() + "where a.attachmentid = ? union all " +
						 "select aa.*, ao.*, at.xattachmenttypeid at_id, at.name at_name, at.description at_desc, ac.attachmentcategoryid ac_id, " +
						 "ac.name ac_name, ac.description ac_desc, ac.protocolid ac_protocolid, updated.username from attachmentarchive aa " +
						 "inner join attachmentorganization ao on aa.attachmentid = ao.attachmentid inner join xattachmenttype at on " +
						 "ao.xattachmenttypeid = at.xattachmenttypeid inner join usr updated on aa.updatedby = updated.usrid left outer join " +
						 "attachmentcategory ac on ao.attachmentcategoryid = ac.attachmentcategoryid where aa.attachmentid = ? order by version ";
			
			stmt = this.conn.prepareStatement(sql);
			System.out.println("Retrieving the audit trail of attachment " + Long.toString(attachId) + "...");
			stmt.setLong(1, attachId);
			stmt.setLong(2, attachId);
			rs = stmt.executeQuery();
			
			while ( rs.next() )
			{
				results.add(rsToAttachment(rs, false));
			}
		}
		catch (SQLException sqle)
		{
			throw new CtdbException("Failure getting attachemnt audit trail with id " + Long.toString(attachId) + " : " + sqle.getMessage(), sqle);
		}
		finally
		{
			this.close(stmt);
			this.close(rs);
		}
		
		return results;
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
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			String sql = "select a.name from attachment a join attachmentorganization ao on a.attachmentid = ao.attachmentid " +
				"where a.name = ? and ao.xattachmenttypeid = ? and ao.associatedid = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, attachmentName);
			stmt.setLong(2, typeId);
			stmt.setLong(3, associatedId);
			rs = stmt.executeQuery();
			
			isUnique = !rs.next();
		}
		catch ( SQLException sqle )
		{
			throw new CtdbException("Failure querying attachemnt with type => " + Integer.toString(typeId) + " and assocaited ID => " + 
				Long.toString(associatedId) + " : " + sqle.getMessage(), sqle);
		}
		finally
		{
			close(rs);
			close(stmt);
		}
		
		return isUnique;
	}

    /**
     * Transforms one row of the result set to an attachment object
     * 
     * @param rs - The result set
     * @return	An Attachment object constructed from a result set record
     * @throws SQLException	If there are any errors while retrieving data from the result set
     */
    private Attachment rsToAttachment(ResultSet rs, boolean hasSampleName) throws SQLException
    {
        Attachment a = new Attachment();
        AttachmentCategory ac = null;
        CtdbLookup lu = null;
        
        a.setId(rs.getInt("attachmentid"));
        a.setVersion(new Version (rs.getInt("version")));
        a.setName(rs.getString("name"));
        a.setFileName(rs.getString("filename"));
        a.setDescription(rs.getString("description"));
        a.setChangeReason(rs.getString("changereason"));
        lu = new CtdbLookup(rs.getInt("at_id"), rs.getString("at_name"), rs.getString("at_desc"));
        a.setType(lu);
        
        // set attachment category information
        if ( rs.getInt("attachmentcategoryid") > 0 )
        {
        	ac = new AttachmentCategory(rs.getInt("ac_id"), rs.getString("ac_desc"), rs.getString("ac_name"), rs.getInt("ac_protocolid"));
        }
        else
        {
        	ac = new AttachmentCategory(Integer.MIN_VALUE, "", "", Integer.MIN_VALUE);
        }
        
        ac.setType(lu);
        a.setCategory(ac);
        
        a.setAssociatedId(rs.getInt("associatedId"));
        a.setUpdatedBy(rs.getInt("updatedby"));
        a.setUpdatedDate(rs.getTimestamp("updateddate"));
        a.setCreatedBy(rs.getInt("createdby"));
        a.setCreatedDate(rs.getTimestamp("createddate"));
        a.setUpdatedByUsername(rs.getString("username"));
        lu = new CtdbLookup(rs.getInt("xpublicationtypeid"));
        a.setPublicationType(lu);
        a.setAuthors(rs.getString("authors"));
        a.setUrl(rs.getString("url"));
        a.setPubMedId(rs.getString("pubmedid"));
        
        if ( hasSampleName )
        {
        	a.setSampleName(rs.getString("sampleName"));
        }
        
        return a;
    }
    
    /**
	 * Generates the file path to a given file storage location, which is determined by the storage type.
	 * 
	 * @param associatedObjId - The ID of the associated object (i.e. study ID or form ID)
	 * @param attachId - The ID of the attachment to be stored
	 * @param attachType - The attachment type ID, which is used to identify which storage path to generate
	 * @return	The path to the directory on the server where the attachment is or will be stored.
	 * @throws ServerFileSystemException	When a storage location cannot be determined.
	 */
	private String getStorageDir(long associatedObjId, long attachId, int attachType) throws ServerFileSystemException
	{
		String path = SysPropUtil.getProperty(CtdbConstants.FILE_STORAGE_DIR);
		
		switch ( attachType )
		{
			case 1: // Get the path for study files
				path += File.separator + SysPropUtil.getProperty("filesystem.directory.study") + File.separator + "study_" + 
					Long.toString(associatedObjId) + File.separator + "attachment_" + Long.toString(attachId);
				break;
			case 2: // Get the path for subject files
				path += File.separator + SysPropUtil.getProperty("filesystem.directory.patient") + File.separator + "patient_" + 
						Long.toString(associatedObjId) + File.separator + "attachment_" + Long.toString(attachId);
				break;
			case 3: // Get the path for data collection files
				path += File.separator + SysPropUtil.getProperty("filesystem.directory.datacollection") + File.separator + "form_" + 
						Long.toString(associatedObjId) + File.separator + "attachment_" + Long.toString(attachId);
				break;
			case 4: // Get the path for form files
				path += File.separator + SysPropUtil.getProperty("filesystem.directory.form") + File.separator + "form_" + 
					Long.toString(associatedObjId) + File.separator + "attachment_" + Long.toString(attachId);
				break;
			case 5: // Get the path for study data share agreement files
				path += File.separator + SysPropUtil.getProperty("filesystem.directory.studyagreement") + File.separator + "study_" + 
						Long.toString(associatedObjId) + File.separator + "attachment_" + Long.toString(attachId);
				break;
			case 6: // Get the path for study e-binder files
				path += File.separator + SysPropUtil.getProperty("filesystem.directory.studyebinder") + File.separator + "study_" + 
						Long.toString(associatedObjId) + File.separator + "attachment_" + Long.toString(attachId);
				break;
			default:  // Could not determine a correct storage path throw an exception
				throw new ServerFileSystemException("Could not determine the storage location based on the ID \"" + Integer.toString(attachType) + ".\"");
		}
		
		return path;
	}
	
	/**
	 * Convenience method to close a file output stream and silently log any IOExceptions as a warning.
	 * 
	 * @param out - The output stream to close.
	 */
	private void close(FileOutputStream out)
	{
		if ( out != null )
		{
			try
			{
				out.close();
			}
			catch ( IOException ioe )
			{
				logger.warn("Could not close file output stream.", ioe);
			}
		}
	}
	
	/**
	 * Convenience method to close a file input stream and silently log any IOExceptions as a warning.
	 * 
	 * @param in - The input stream to close.
	 */
	private void close(InputStream in)
	{
		if ( in != null )
		{
			try
			{
				in.close();
			}
			catch ( IOException ioe )
			{
				logger.warn("Could not close file input stream.", ioe);
			}
		}
	}
}
