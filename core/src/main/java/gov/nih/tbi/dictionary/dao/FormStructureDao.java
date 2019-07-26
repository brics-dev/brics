
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FormStructureDao extends GenericDao<FormStructure, Long>
{

    /**
     * 
     * @param ids
     *            : Results will be filtered by these ids. If this is an empty list not results will be reutrned. If it
     *            is null then results are not filtered by ids.
     * @param pageData
     * @return
     */
    public List<FormStructure> listDataStructures(Set<Long> ids, PaginationData pageData);

    public List<FormStructure> listByStatus(Set<Long> ids, long[] statusList);

    /**
     * Finds all Data Structures that have a short name like the parameter.
     * 
     * @param shortName
     * @return
     */

    public List<FormStructure> findByShortName(String shortName);

    /**
     * return the number of data structures in the database with the given type. If status is null null, then returns
     * the total number of Data structures
     * 
     * @param status
     * @return
     */
    public Long getStatusCount(StatusType status);
    
    public FormStructure getById(long id);

    public FormStructure get(String shortName, String version);

    /**
     * Returns the form structure with the given shortname. If multiple with the same shortName exist, then returns the
     * one with the highest version number.
     * 
     * @param shortName
     * @return
     */
    public FormStructure getLatestVersionByShortName(String shortName);

    /**
     * Returns all the form structures in a map of it's primary key as the key to the object as the value
     * 
     * @return
     */
    public Map<Long, FormStructure> getAllIntoMap();

    /**
     * Returns a list of datastructure with the ID that exist in dsIdList
     * 
     * @param dsIdList
     * @return
     */
    public List<FormStructure> getAllSortedById(List<Long> dsIdList, PaginationData pageData);

    public List<FormStructure> getAllById(List<Long> dsIdList);

    public List<FormStructure> listDataStructuresByStatus(StatusType status);

    /**
     * Performs a search on the rdf graph using the form structure sparql dao and combines the results with the
     * composite counterparts.
     * 
     * @param selectedFacets
     * @param searchKeywords
     * @param modifiedDate
     * @param pageData
     * @param includeDEList
     *            : if true, then the dataElement list property of the composite form structure object will be
     *            populated.
     * @return
     */
    public List<FormStructure> search(Map<FormStructureFacet, Set<String>> selectedFacets, Set<String> searchTerms,
            boolean exactMatch, PaginationData pageData, boolean includeDEList, boolean onlyOwned);

    /**
     * Returns the list of form structures that have a data element with the specified name and version
     * 
     * @param deName
     * @param deVersion
     * @return
     */
    public List<FormStructure> getAttachedDataStructure(String deName, String deVersion);

    public List<FormStructure> getAttachedDataStructure(String deName, String deVersion, boolean isPublicData);

	public List<String> getNamesByIds(List<Long> formStructureIds);
	
	public Map<Long, FormStructure> getPublishedAndArchivedIntoMap();
}
