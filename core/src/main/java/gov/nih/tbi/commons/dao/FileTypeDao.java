
package gov.nih.tbi.commons.dao;

import java.util.List;

import gov.nih.tbi.commons.model.FileClassification;
import gov.nih.tbi.commons.model.hibernate.FileType;

/**
 * Interface for FileTypeDaoImpl
 * 
 * @author Francis Chen
 */
public interface FileTypeDao extends GenericDao<FileType, Long>
{

    /**
     * Gets the file types that have a certain classification
     * 
     * @param classification
     * @return
     */
    public List<FileType> getFileTypeByClassification(FileClassification classification);

    /**
     * This version of the get method gets the file type by name.
     * 
     * @param name
     *            (String) - name of desired file type
     * @return FileType object associated with the name
     */
    public FileType get(String name);
    
    public FileType getFileTypeByNameAndClassification(String name, FileClassification fileClassification);
    
}
