
package gov.nih.tbi.repository.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.util.List;

/**
 * Interface for User File Dao
 * 
 * @author Andrew Johnson
 * 
 */
public interface UserFileDao extends GenericDao<UserFile, Long>
{

    /**
     * Gets all the files that have been uploaded by the user.
     * 
     * @param user
     *            (User) - limits the userFiles returned by user
     * 
     * @return returns a list of all the files available to this user
     */
    public List<UserFile> getByUserId(Long userId);

    /**
     * Gets all the files that have been uploaded by the user in a specific file type.
     * 
     * @param user
     *            (User) - limits the userFiles returned by user
     * @param type
     *            (FileType) - limits the userFiles returned by file type
     * 
     * @return returns a list of all the files available to this user given the file type
     */
    public List<UserFile> getByUserId(Long userId, FileType type);
    
    /**
     * Gets files by ids.
     * 
     * @param ids
     *            (List<Long>) - list of userfile id's to return
     * 
     * @return returns a list of all the files by given ids
     */
    public List<UserFile> getById(List<Long> ids);

}
