
package gov.nih.tbi.query.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.query.model.hibernate.SavedQuery;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface SavedQueryDao extends GenericDao<SavedQuery, Long>
{
	/** this gets a list of saved queries by ids
	 * 
	 * @param ids
	 * @return
	 */
    public List<SavedQuery> getByIds(List<Long> ids);
    
    
	/** this gets a list of saved queries without populating xml by ids
	 * 
	 * @param ids
	 * @return
	 */
    public List<SavedQuery> getSavedQueryInfoByIds(Set<Long> ids);
    
    
	/**
     * @return all SavedQuery instances without fetching the xml contents. 
     */
    public List<SavedQuery> getAllSavedQueries();
    
    /**
     * Returns true if the given savedQueryName does not exist in the database.
     * 
     * @param savedQueryName
     * @return
     */
    public boolean isSavedQueryNameUnique(String savedQueryName);
    
    /**
     * Returns saved query by name
     * @param savedQueryName
     * @return
     */
    public SavedQuery getByName(String savedQueryName);
    
   /**
    * This method takes in all fields in the saved query. it will return a list of saved queries.
    * if a field is null it will ignore it in the search.
    * @param savedQueryIdsList
    * @param savedQueryName
    * @param savedQueryDescription
    * @param startDateRange
    * @param endDateRange
    * @param isCopy
    * @return
    */
    public List<SavedQuery> searchSavedQuery(Set<Long> savedQueryIdsList, String savedQueryName, 
			String savedQueryDescription, Date startDateRange, Date endDateRange, boolean includeCopies);
}
