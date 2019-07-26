package gov.nih.tbi.metastudy.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyLabel;


public interface MetaStudyLabelDao extends GenericDao<MetaStudyLabel, Long> {
    /**
     * Returns a keyword object by the given name. This keyword object contains a name, uri, and count. Returns null if
     * keyword does not exist
     * 
     * @param name
     * @return
     */
    public MetaStudyLabel getByName(String name);

    /**
     * Returns a list of keywords that contain the given string. Search is case insensitive.
     * 
     * @param searchKey
     * @return
     */
    public List<MetaStudyLabel> search(String searchKey);
    
    /**
     * Returns the number of times the label has been listed in the table
     * @param label
     * @return
     */
    public Long getCountByLabel(String label);
}