package gov.nih.nichd.ctdb.attachments.manager;

import java.sql.Connection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gov.nih.nichd.ctdb.attachments.dao.AttachmentCategoryDao;
import gov.nih.nichd.ctdb.attachments.domain.AttachmentCategory;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;

public class AttachmentCategoryManager extends CtdbManager {
	
	public static final String DEFAULT_CATEGORY_NAME = "None";

	public void createAttachmentCategory (AttachmentCategory ac) throws CtdbException {
		Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            AttachmentCategoryDao.getInstance(conn).createAttachmentCategory(ac);
        }
        finally {
            close(conn);
        }
    }

	public void updateAttachmentCategory (AttachmentCategory ac) throws CtdbException {
		Connection conn = null;
		
        try {
            conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
            AttachmentCategoryDao.getInstance(conn).updateAttachmentCategory(ac);
            commit(conn);
        }
        finally {
        	rollback(conn);
            close(conn);
        }
    }

    public AttachmentCategory getAttachmentCategory (long attachemntCategoryId) throws CtdbException {
		Connection conn = null;

        try {
            conn = CtdbManager.getConnection();
            return AttachmentCategoryDao.getInstance(conn).getAttachmentCategory(attachemntCategoryId);
        }
        finally {
            close(conn);
        }
    }
	
    /**
     * Retrieves a list of attachment categories form the database that are associated to the given study ID.
     * 
     * @param protocolId - The ID of the study used in searching for attachment categories.
     * @param typeid - The type ID of an attachment organization, which is also needed to search for categories.
     * @return	A listing of AttachmentCategory objects that are a part of the current study. The default category
     * will always be the first element of the list.
     * @throws CtdbException	When there is a database error.
     */
	public List<AttachmentCategory> getAttachmentCategories (long protocolId, long typeid) throws CtdbException {
		Connection conn = null;
		LinkedList<AttachmentCategory> categoryList = null;
		
		// Get the listing of attachment categories that are associated with the given study ID.
		try {
			conn = CtdbManager.getConnection();
			categoryList = AttachmentCategoryDao.getInstance(conn).getAttachmentCategories(protocolId, typeid);
		}
		finally {
			close(conn);
		}
		
		// Ensure that the default category ("None") is at the top of the list.
		for ( Iterator<AttachmentCategory> it = categoryList.iterator(); it.hasNext(); ) {
			AttachmentCategory ac = it.next();
			
			// Check if the current category is the default. If it is, move it to the beginning of the list.
			if ( ac.getName().equals(AttachmentCategoryManager.DEFAULT_CATEGORY_NAME) ) {
				it.remove();
				categoryList.addFirst(ac);
				break;
			}
		}
		
		return categoryList;
	}

	public AttachmentCategory getDefaultCategoryId(long protocolId, long typeid) throws CtdbException {
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection();
			return AttachmentCategoryDao.getInstance(conn).getDefaultCategory(protocolId, typeid);
		}
		finally {
			close(conn);
		}
	}

	public Map<Integer, List<AttachmentCategory>> getAttachmentCategories (long protocolId) throws CtdbException {
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection();
			return AttachmentCategoryDao.getInstance(conn).getAttachmentCategories(protocolId);
		}
		finally {
			close(conn);
		}
	}
	
	/**
	 * Removes all the listed attachment categories from the system.
	 * 
	 * @param selectedIds - A list of attachment category IDs to be removed.
	 * @return	A list of attachment category IDs that have been removed from the system.
	 * @throws NumberFormatException	When one or more of the listed IDs is not a number.
	 * @throws ObjectNotFoundException	When one of the listed attachment category IDs could not be found in the system.
	 * @throws CtdbException	When there are any other database errors.
	 */
	public void deleteAttachmentCategories(String[] selectedIds, List<Long> deletedList) throws NumberFormatException, ObjectNotFoundException, CtdbException {
		
		if( (selectedIds == null) || (selectedIds.length == 0) ) {
			return;
		}
		
        Connection conn = null;
        
        try {
            conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
            AttachmentCategoryDao dao = AttachmentCategoryDao.getInstance(conn);
            dao.deleteAttachmentCategories(selectedIds, deletedList);
        }
        finally {
        	this.close(conn);
        }
    }
}
