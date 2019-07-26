package gov.nih.tbi.metastudy.dao;

import java.util.List;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyKeyword;


public interface MetaStudyKeywordDao extends GenericDao<MetaStudyKeyword, Long> {
    /**
     * Returns a keyword object by the given name. This keyword object contains a name, uri, and count. Returns null if
     * keyword does not exist
     * 
     * @param name
     * @return
     */
    public MetaStudyKeyword getByName(String name);

    /**
     * Returns a list of keywords that contain the given string. Search is case insensitive.
     * 
     * @param searchKey
     * @return
     */
    public List<MetaStudyKeyword> search(String searchKey);
    
    /**
     * Returns the number of times the keyword has been listed in the table
     * @param keyword
     * @return
     */
    public Long getCountByKeyword(String keyword);
}