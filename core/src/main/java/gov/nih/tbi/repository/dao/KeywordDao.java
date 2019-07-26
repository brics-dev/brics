package gov.nih.tbi.repository.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.Keyword;

import java.util.List;

public interface KeywordDao extends GenericDao<Keyword, Long> {

	/**
	 * Returns all keywords for the specified keyword type.
	 * @param keywordClass - keyword type, ex. StudyKeyword.class or MetaStudyKeyword.class
	 * @return
	 */
	public List<? extends Keyword> getAllKeywords(Class<?> keywordClass);
	
    /**
     * Returns a list of keywords that contain the given string. Search is case insensitive.
     * 
     * @param searchKey
	 * @param keywordClass - keyword type, ex. StudyKeyword.class or MetaStudyKeyword.class
     * @return
     */
    public List<? extends Keyword> search(String searchKey, Class<?> keywordClass);
    
    /**
     * Returns the number of times the keyword has been listed in the table
     * @param keyword
	 * @param keywordClass - keyword type, ex. StudyKeyword.class or MetaStudyKeyword.class
     * @return
     */
    public Long getCountByKeyword(String keyword, Class<?> keywordClass);
}
