
package gov.nih.tbi.dictionary.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

public interface StructuralDataElementDao extends GenericDao<StructuralDataElement, Long>
{

    public StructuralDataElement getLatestByName(String dataElementName);

    /**
     * Returns all data elements with the given shortname, regardless of version. Naming convention matches from
     * structure.
     * 
     * @param dataElementName
     * @return
     */
    public List<StructuralDataElement> findByShortName(String dataElementName);

    /**
     * Returns a list of data elements that match the short names provided. Only the latest version of each data element
     * is returned.
     * 
     * @param dataElementNames
     * @return
     */
    public List<StructuralDataElement> getLatestByNameList(Set<String> dataElementNames);

    /**
     * Returns a list of DataElement for the given list of DataElement ids.
     * 
     * @param ids
     *            list of DataElement ids
     * @return list of DataElements
     */
    public List<StructuralDataElement> getByIdList(List<Long> ids);

    /**
     * return the number of data elements in the database with the given status and category. If either variable is null
     * then ignore it
     * 
     * @param status
     * @param category
     * @return
     */
    public Long getStatusCount(DataElementStatus status, Category category);

    public List<StructuralDataElement> listByStatus(DataElementStatus status);

    /**
     * returns the data elements by status
     * 
     * @param dataElement
     *            status
     * @return list of data elements
     */

    public StructuralDataElement getByMapElementId(Long id);

    /**
     * Gets data elements by map element IDs.
     * 
     * @param ids
     * @return map of the mapelement id to their respective data element object
     */
    public Map<Long, StructuralDataElement> getByMapElementIds(Set<Long> ids);

    public List<NameAndVersion> getAllDraftAndArchivedById(Set<Long> dsIdList);
    
    public List<NameAndVersion> getAllDraftAndArchivedByIdMax(Set<Long> dsIdList);
    
    public StructuralDataElement getByNameAndVersion(String dataElementName, String version);

    /**
     * Returns a map of name and versions to structuralDataElement object, using the specified list of names and
     * versions
     * 
     * @param nameAndVersions
     * @return
     */
    public Map<String, StructuralDataElement> getByNameAndVersionsMap(List<NameAndVersion> nameAndVersions);

    public List<StructuralDataElement> getByNameAndVersions(List<NameAndVersion> nameAndVersions);
    
    public List<NameAndVersion> getNameVersionByIdList(Set<Long> dsIdList);
    
    public Map<String, Map<String, ValueRange>> getDEValueRangeMap(Set<String> deNames);

	/**
	 * Given a DataType, returns the short names of all the data elements with the given type. Not
	 * production code.
	 * 
	 * @param dataType
	 * @return
	 */
	public List<String> getDataElementNamesByType(DataType dataType);
	
	public List<StructuralDataElement> listByStatuses(Set<DataElementStatus> statuses);

    List<StructuralDataElement> getAllByName(String name);
    
    public StructuralDataElement getOriginalDataElementByName(String name);
}