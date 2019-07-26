
package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FormStructureSqlDao extends GenericDao<StructuralFormStructure, Long>
{

    public StructuralFormStructure get(String shortName, String version);

    /**
     * Returns the form structure with the given shortname. If multiple with the same shortName exist, then returns the
     * one with the highest version number.
     * 
     * @param shortName
     * @return
     */
    public StructuralFormStructure getLatestVersionByShortName(String shortName);

    /**
     * Returns all the form structures in a map of it's primary key as the key to the object as the value
     * 
     * @return
     */
    public Map<Long, StructuralFormStructure> getAllIntoMap();

    /**
     * Returns a list of datastructure with the ID that exist in dsIdList
     * 
     * @param dsIdList
     * @return
     */
    public List<StructuralFormStructure> getAllSortedById(List<Long> dsIdList, PaginationData pageData);

    public List<StructuralFormStructure> getAllById(List<Long> dsIdList);

    public List<StructuralFormStructure> listDataStructuresByStatus(StatusType status);

    /**
     * 
     * @param ids
     *            : Results will be filtered by these ids. If this is an empty list not results will be reutrned. If it
     *            is null then results are not filtered by ids.
     * @param pageData
     * @return
     */
    public List<StructuralFormStructure> listDataStructures(Set<Long> ids, PaginationData pageData);

    public List<StructuralFormStructure> listByStatus(Set<Long> ids, long[] statusList);

    /**
     * Finds all Data Structures that have a short name like the parameter.
     * 
     * @param shortName
     * @return
     */
    public List<StructuralFormStructure> findByShortName(String shortName);

    /**
     * return the number of data structures in the database with the given type. If status is null null, then returns
     * the total number of Data structures
     * 
     * @param status
     * @return
     */
    public Long getStatusCount(StatusType status);

    /**
     * Returns the list of short names that correspond to the given ids. Does not take version into account.
     * 
     * @param dsIdList
     * @return
     */
    public List<String> getAllShortNameById(Set<Long> dsIdList);

    /**
     * Returns the list of short names that correspond to the given ids. Does not take version into account. Only
     * returns published and shared draft fs
     * 
     * @param dsIdList
     * @return
     */
    public List<String> getAllPublishedAndSharedDraftById(Set<Long> dsIdList);

    /**
     * Calls getByShortNameAndVersions and then returns those form structures mapped to their shortname/version
     * represented as a string. Seems unnecessary but created to map the sparql dao.
     * 
     * @param nameAndVersions
     * @return
     */
    public Map<String, StructuralFormStructure> getShortNameAndVersionsMap(List<NameAndVersion> nameAndVersions);

    /**
     * Retrieves a list of form structures specified by their short name and versions.
     * 
     * @param nameAndVersions
     * @return
     */
    public List<StructuralFormStructure> getByShortNameAndVersions(List<NameAndVersion> nameAndVersions);

    /**
     * Returns the list of structural form structures that have a data element with the specified name and version
     * 
     * @param deName
     * @param deVersion
     * @return
     */
    public List<StructuralFormStructure> getAttachedDataStructure(String deName, String deVersion);

    public List<StructuralFormStructure> getAttachedDataStructure(String deName, String deVersion, boolean isPublicData);

    /**
     * This dao call is used to help set up permissions when performing a FS search. It should not be used for any other
     * purpose and after permissions are moved into the dictionary module it should be removed.
     * 
     * @param dsIdList
     * @return
     */
    public List<NameAndVersion> getAllDraftAndAPById(Set<Long> dsIdList);

    /**
     * This dao call is used to hep set up permission when performing a FS search. It should not be sued for any other
     * purpose and after permissoin are moved into the dictioanry module it should be removed.
     * 
     * @param dsIdList
     * @return
     */
    public List<NameAndVersion> getAllShortNameAndVersionById(Set<Long> dsIdList);
    
    /**
     * Returns a list of form structure names with the given IDs
     * @param formStructureIds
     * @return
     */
	public List<String> getNamesByIds(List<Long> formStructureIds);

	public List<StructuralFormStructure> listDataStructuresByStatuses(Set<StatusType> statuses);

    List<StructuralFormStructure> getAllNoChildren();
    
    public StructuralFormStructure getOriginalFormStructureByName(String name);
}
