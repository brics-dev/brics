package gov.nih.nichd.ctdb.attachments.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.postgresql.util.PSQLException;

import gov.nih.nichd.ctdb.attachments.domain.AttachmentCategory;
import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.InvalidRemovalException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jan 8, 2007
 * Time: 2:49:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentCategoryDao extends CtdbDao {

    /**
     * Private Constructor to hide the instance
     * creation implementation of the AttachmentCategoryDao object
     * in memory. This will provide a flexible architecture
     * to use a different pattern in the future without
     * refactoring the AttachmentCategoryDao.
     */
    private AttachmentCategoryDao() {

    }

    /**
     * Method to retrieve the instance of the AttachmentCategoryDao.
     *
     * @return AttachmentCategoryDao data object
     */
    public static synchronized AttachmentCategoryDao getInstance() {
        return new AttachmentCategoryDao();
    }

    /**
     * Method to retrieve the instance of the AttachmentCategoryDao. This method
     * accepts a Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     * @return AttachmentCategoryDao data object
     */
    public static synchronized AttachmentCategoryDao getInstance(Connection conn) {
        AttachmentCategoryDao dao = new AttachmentCategoryDao();
        dao.setConnection(conn);
        return dao;
    }
    
    /**
     * Checks if the passed in attachment category already exists in the database.
     * 
     * @param ac - The attachment category to search for
     * @return	True if and only if the given attachment category exists with in the database.
     * @throws CtdbException	When there is a database error.
     */
    public boolean isCategoryExisted(AttachmentCategory ac) throws CtdbException {
    	PreparedStatement stmt = null;
    	ResultSet rs = null;
    	boolean isDuplicated = false;
    	
    	try {
    		String sql = "select count(1) from attachmentcategory where upper(name) = ? and xattachmenttypeid = ? and protocolid = ? ";
    		
    		if ( ac.getId() != Integer.MIN_VALUE ) { // update mode
    			sql += "and attachmentcategoryid != ? ";
    		}
    		
    		stmt = this.conn.prepareStatement(sql);
    		stmt.setString(1, ac.getName().trim().toUpperCase());
    		stmt.setLong(2, ac.getType().getId());
    		stmt.setLong(3, ac.getProtocolId());
    		
    		if ( ac.getId() != Integer.MIN_VALUE ) { // update mode
    			stmt.setLong(4, ac.getId());
    		}
    		
    		rs = stmt.executeQuery();
    		int count = 0;
    		
    		if ( rs.next() ) {
    			count = rs.getInt(1);
    			
    			if ( count > 0 ) {
        			isDuplicated = true;
        		}
    		}
    	}
    	catch (SQLException se) {
    		throw new CtdbException("Unable to check if the category name exists in the system: " + se.getMessage(), se);
    	}
    	finally {
    		this.close(rs);
    		this.close(stmt);
    	}
    	
    	return isDuplicated;
    }
    
   /**
    * Creates an attachment category record, and the AttachmentCategory ID is set.
    * 
    * @param ac - The attachment category record to be created
    * @throws DuplicateObjectException	When a duplicate record is found.
    * @throws CtdbException	When there is a database error.
    */
    public void createAttachmentCategory(AttachmentCategory ac) throws DuplicateObjectException, CtdbException {

        PreparedStatement stmt = null;

        try {
        	if ( this.isCategoryExisted(ac) ) {
                throw new DuplicateObjectException("attachment category " + ac.getName());
        	}
            
            String sql = "insert into attachmentcategory (attachmentcategoryid, xattachmenttypeid, protocolid, name, description, " +
            	"updatedby, updateddate, createdby, createddate) values (DEFAULT, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP) ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, ac.getType().getId());
            stmt.setLong(2, ac.getProtocolId());
            stmt.setString(3, ac.getName());
            stmt.setString(4, ac.getDescription());
            stmt.setLong(5, ac.getUpdatedBy());
            stmt.setLong(6, ac.getCreatedBy());
            stmt.executeUpdate();
        }
        catch (SQLException sqle) {
            throw new CtdbException("Failure creating attachment category : " + sqle.getMessage(), sqle);
        }
        finally {
            this.close(stmt);
        }
    }
    
   /**
    * Persists changes to an attachment category to the database.
    * 
    * @param ac - The attachment category to update.
    * @throws CtdbException	When there is a database error.
    */
    public void updateAttachmentCategory(AttachmentCategory ac) throws CtdbException {
        PreparedStatement stmt = null;

        try {
        	if( this.isCategoryExisted(ac) ) {
                throw new DuplicateObjectException("attachment category " + ac.getName());
        	}

            String sql = "update attachmentcategory set name = ?, description = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP " +
            	"where attachmentcategoryid = ? ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setString(1, ac.getName());
            stmt.setString(2, ac.getDescription());
            stmt.setLong(3, ac.getUpdatedBy());
            stmt.setLong(4, ac.getId());
            stmt.executeUpdate();
        }
        catch (SQLException sqle) {
            throw new CtdbException("Failure updating attachment category : " + sqle.getMessage(), sqle);
        }
        finally {
            this.close(stmt);
        }
    }

    /**
     * Gets an attachment category by its ID
     * 
     * @param id - The ID of the attachment category to search for.
     * @return	The AttachmentCategory object corresponding to the given ID.
     * @throws ObjectNotFoundException	When no attachment category record can be found in the database.
     * @throws CtdbException	When there are other database errors.
     */
     public AttachmentCategory getAttachmentCategory(long id) throws ObjectNotFoundException, CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        AttachmentCategory category = null;

        try {
            String sql = "select * from attachmentcategory where attachmentCategoryid = ? ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, id);
            
            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
            	category = this.rsToAttachmentCategory(rs);
            }
            else {
                throw new ObjectNotFoundException("Failure obtaining attachemnt Category, not found : " + id);
            }
        }
        catch (SQLException sqle) {
            throw new CtdbException("Failure getting attachment category : " + sqle.getMessage(), sqle);
        }
        finally {
            this.close(stmt);
            this.close(rs);
        }
        
        return category;
    }

    /**
     * Gets all attachment categories of given type for given study ID.
     * 
     * @param protocolId - An ID of a study to search over.
     * @param typeid - A type ID to search over.
     * @return	A list of attachment categories that match the given type and study ID. The list will be empty
     * if no categories can be found.
     * @throws CtdbException	When there are database errors.
     */
    public LinkedList<AttachmentCategory> getAttachmentCategories(long protocolId, long typeid) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        LinkedList<AttachmentCategory> al = new LinkedList<AttachmentCategory>();
        
        try {
            String sql = "select * from attachmentcategory where protocolid = ? and xattachmenttypeid = ? order by name ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, protocolId);
            stmt.setLong(2, typeid);

            rs = stmt.executeQuery();
            
            while ( rs.next() ) {
                al.add(this.rsToAttachmentCategory(rs));
            }
        }
        catch (SQLException sqle) {
            throw new CtdbException("Failure getting attachment categories : " + sqle.getMessage(), sqle);
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        }
        
        return al;
    }
    
    /**
     * Retrieves the default category for a given type and study ID.
     * 
     * @param protocolId - An ID of a study to search over.
     * @param typeid - A type ID to search over.
     * @return	The default AttachmentCategory object for the given type and study ID.
     * @throws CtdbException	When there is a database error.
     */
    public AttachmentCategory getDefaultCategory(long protocolId, long typeid) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        AttachmentCategory category = null;
 
        try {
            String sql = "select * from attachmentcategory where protocolid = ? and xattachmenttypeid = ? and upper(name) ='NONE' ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, protocolId);
            stmt.setLong(2, typeid);

            rs = stmt.executeQuery();
            
            if ( rs.next() ) {
            	category =  this.rsToAttachmentCategory(rs);
            }
        }
        catch (SQLException sqle) {
            throw new CtdbException("Failure getting default attachment categories : " + sqle.getMessage(), sqle);
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        }
        
        return category;
    }

    /**
     * Gets all attachment categories for a given study ID regardless of type.
     * @param protocolId - An ID of a study to search over.
     * @return	A map of attachment categories for the given study.
     * @throws CtdbException	When there are any database errors.
     */
    public Map<Integer, List<AttachmentCategory>> getAttachmentCategories (long protocolId) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        HashMap<Integer, List<AttachmentCategory>> map = new HashMap<Integer, List<AttachmentCategory>>();

        try {
            String sql = "select * from attachmentcategory where protocolid = ?  order by xattachmenttypeid, name ";
            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, protocolId);

            rs = stmt.executeQuery();
            int curType = Integer.MIN_VALUE;
            List<AttachmentCategory> al = new ArrayList<AttachmentCategory>();
            
            while (rs.next()) {
                if (curType == Integer.MIN_VALUE) {
                    curType = rs.getInt("xattachmenttypeid");
                }
                
                if (curType != rs.getInt("xattachmenttypeid")) {
                    map.put(new Integer(curType), al);
                    al = new ArrayList<AttachmentCategory>();
                    curType = rs.getInt("xattachmenttypeid");
                }
                
                al.add(this.rsToAttachmentCategory(rs));
            }
            
            map.put(new Integer(curType), al);
        }
        catch (SQLException sqle) {
            throw new CtdbException("Failure getting all attachments for protocol : " + sqle.getMessage(), sqle);
        }
        finally {
            this.close(stmt);
            this.close(rs);
        }
        
        return map;
    }


    /**
     * Transforms row of result set to attachment category object.
     * 
     * @param rs - The result set to transform.
     * @return	An AttachmentCategory representing the data from the given result set.
     * @throws SQLException	When there is an error while transforming the result set.
     */
    private AttachmentCategory rsToAttachmentCategory(ResultSet rs) throws SQLException {
        AttachmentCategory ac = new AttachmentCategory();
        
        ac.setId(rs.getInt("attachmentCategoryid"));
        ac.setType(new CtdbLookup(rs.getInt("xattachmenttypeid")));
        ac.setProtocolId(rs.getInt("protocolid"));
        ac.setName(rs.getString("Name"));
        ac.setDescription(rs.getString("description"));
        ac.setUpdatedBy(rs.getInt("UpdatedBy"));
        ac.setUpdatedDate(rs.getTimestamp("updateddate"));
        ac.setCreatedBy(rs.getInt("createdby"));
        ac.setCreatedDate(rs.getTimestamp("createddate"));
        
        return ac;
    }
    
    /**
     * Removes an attachment category record from the database.
     * 
     * @param id - The ID of the attachment category to be removed.
     * @throws ObjectNotFoundException	When a record could not be found to be deleted.
     * @throws InvalidRemovalException	When there is a foreign key violation while attempting to remove the attachment category record.
     * @throws CtdbException	When there are other database errors.
     */
    public void deleteAttachmentCategory(long id) throws ObjectNotFoundException, InvalidRemovalException, CtdbException {
        PreparedStatement stmt = null;
        
        try {
            String sql = "delete FROM attachmentcategory where attachmentcategoryid = ? ";

            stmt = this.conn.prepareStatement(sql);
            stmt.setLong(1, id);

            int recordDeleted = stmt.executeUpdate();

            if (recordDeleted == 0) {
                throw new ObjectNotFoundException("Failed attachment category deletion: attachment category ID: " + id + " does not exist in the system.");
            }
        }
        catch (PSQLException e) {
            if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.foreignkey_constraint_sqlstate"))) { //23503
                throw new InvalidRemovalException("Integrety violated due to foreign key constraint: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Failure deleting attachment category: " + e.getMessage() + e);
            }
        }
        catch (SQLException sqle) {
            if (sqle.getErrorCode() == 2292) {
                throw new InvalidRemovalException("Integrety violated due to foreign key constraint:" + sqle.getMessage(), sqle);
            }
            else {
                throw new CtdbException("Failure deleting attachment category: " + sqle.getMessage() + sqle);
            }
        }
        finally {
            this.close(stmt);
        }
    }
    
    /**
     * Removes attachment categories from the database identified by a given list.
     * 
     * @param categoriesToDelete - A list of attachment category IDs to be deleted.
     * @return	A list containing the IDs of categories that were successfully deleted.
     * @throws NumberFormatException	When one of the list elements is not a number.
     * @throws InvalidRemovalException	When there is a foreign key violation while deleting a category.
     * @throws CtdbException	When there are other database errors.
     */
    public void deleteAttachmentCategories(String[] categoriesToDelete, List<Long> deletedCategories) throws NumberFormatException, InvalidRemovalException, CtdbException {
    	PreparedStatement stmt = null;
    	
    	try {
    		String sql = "delete FROM attachmentcategory where attachmentcategoryid = ? ";
    		stmt = this.conn.prepareStatement(sql);
    		
    		// Loop through the listing of attachment categories and delete them
    		for ( int i = 0; i < categoriesToDelete.length; i++ ) {
    			long id = Long.parseLong(categoriesToDelete[i]);
    			
    			stmt.setLong(1, id);
    			stmt.executeUpdate();
    			deletedCategories.add(new Long(id));
    		}
    	}
    	catch (PSQLException e) {
            if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.foreignkey_constraint_sqlstate"))) { //23503
                throw new InvalidRemovalException("Integrety violated due to foreign key constraint: " + e.getMessage(), e);
            }
            else {
                throw new CtdbException("Failure deleting attachment category: " + e.getMessage() + e);
            }
        }
        catch (SQLException sqle) {
            if (sqle.getErrorCode() == 2292) {
                throw new InvalidRemovalException("Integrety violated due to foreign key constraint:" + sqle.getMessage(), sqle);
            }
            else {
                throw new CtdbException("Failure deleting attachment category: " + sqle.getMessage() + sqle);
            }
        }
    	finally {
    		close(stmt);
    	}
    }
}
