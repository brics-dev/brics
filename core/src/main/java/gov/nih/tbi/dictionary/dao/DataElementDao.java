
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.util.DataElementFilter;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataElementDao extends GenericDao<DataElement, Long>
{

    /**
     * Returns the latest version of the data element with the given short name.
     * 
     * @param dataElementName
     * @return
     */
    public DataElement getLatestByName(String dataElementName);
    
    /**
     * Get data element by the element name.  The argument for data element name in this method is case insensitive.
     * @param dataElementName
     * @return
     */
    public DataElement getLatestByNameCaseInsensitive(String dataElementName);

    /**
     * Returns the data element with the given name and version.
     * 
     * @param dataElementName
     * @param version
     * @return
     */
    public DataElement getByNameAndVersion(String dataElementName, String version);

    /**
     * Searches Data Elements for anything with the search key somewhere in it.
     * 
     * @param ids
     * @param category
     *            the type of elements to select
     * @param searchKey
     * @param pageData
     * @param dataElementFilter
     * @return
     */
    public List<DataElement> search(Set<Long> ids, Category category, DataElementStatus status, String searchKey,
            PaginationData pageData, DataElementFilter dataElementFilter);

    /**
     * Searches Data Elements for anything with the search key somewhere in it.
     * 
     * @param ids
     * @param category
     *            the type of elements to select
     * @param searchKey
     * @param pageData
     * @param dataElementFilter
     * @return
     */
    public List<DataElement> searchDetailed(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchKeywords,
            boolean exactMatch, PaginationData pageData, boolean onlyOwned);

    /**
     * Searches Data Elements for all unattached Data Elements the have the search key somewhere in it
     * 
     * @param ids
     * @param searchKey
     * @param sort
     * @param pageSize
     * @return
     */

    public List<DataElement> getLatestByNameList(Set<String> dataElementNames);
    
    public List<DataElement> getBasicLatestByNameList(Set<String> dataElementNames);

    /**
     * Returns a list of DataElement for the given list of DataElement ids.
     * 
     * @param ids
     *            list of DataElement ids
     * @return list of DataElements
     */
    public List<DataElement> getByIdList(List<Long> ids);

    /**
     * return the number of data elements in the database with the given status and category. If either variable is null
     * then ignore it
     * 
     * @param status
     * @param category
     * @return
     */
    public Long getStatusCount(DataElementStatus status, Category category);

    public List<DataElement> listByStatus(DataElementStatus status);
    public List<DataElement> listLatestByStatus(DataElementStatus status);

    /**
     * returns the data elements by status
     * 
     * @param dataElement
     *            status
     * @return list of data elements
     */

    public DataElement getByMapElementId(Long id);

    /**
     * Gets data elements by map element IDs.
     * 
     * @param ids
     * @return map of the mapelement id to their respective data element object
     */
    public Map<Long, DataElement> getByMapElementIds(Set<Long> ids);

    public void remove(DataElement dataElement);

    public List<DataElement> getByNameAndVersions(List<NameAndVersion> nameAndVersions);

    /**
     * Given a list of data element names, return a hashmap of the data element names to their respective objects
     * 
     * @param dataElementNames
     * @return
     */
    public Map<String, DataElement> getLatestByNameListIntoMap(Set<String> dataElementNames);

    /**
     * Given a shortname, this function gets all version of the given data element.
     * 
     * @param name
     * @return
     */
    public List<DataElement> getAllByName(String name);
    
    /**
     * Get a list of data elements that might need a status update. It includes two subsets:
     * 1. All data elements with until_date
     * 2. All data elements without until_date and in either Retired or Deprecated status.
     * 
     * @return
     */
    public List<DataElement> getDataElementsForStatusUpdate();

	public List<DataElement> listByStatuses(Set<DataElementStatus> deStatuses);
	
	public Set<Long> getDEIdsFormListOfFSIds(Set<Long> fSIds);
	
	public String getDEShortNameFromVirtuosoIgnoreCases(String deName);

}