package gov.nih.tbi.dictionary.dao;

import gov.nih.tbi.commons.dao.GenericSparqlDao;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataElementSparqlDao extends GenericSparqlDao<SemanticDataElement> {

	/**
	 * Returns the data element with the specified variable name
	 * 
	 * @param dataElementName
	 * @return
	 */
	public SemanticDataElement getByName(String name);

	/**
	 * Returns the data elements with the specified names
	 * 
	 * @param names
	 * @return
	 */
	@Deprecated
	public Map<String, SemanticDataElement> getByNameList(Collection<String> names);

	/**
	 * Returns all the data elements in the database in a hashmap of data element variable names to data elements
	 * 
	 * @return
	 */
	public Map<String, SemanticDataElement> getAllInNameMap();

	/**
	 *
	 * @param selectedFacets
	 * @param searchKeywords
	 * @param modifiedDate
	 * @param pageData
	 * @return
	 */
	public List<SemanticDataElement> search(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchKeywords,
			boolean exactMatch, PaginationData pageData, boolean onlyOwned);

	/**
	 * Retreives a semantic data element from the RDF that is labelled as the latest version.
	 * 
	 * @param name The short name of the desired element
	 * @return the semantic data element, null if the element doesn't exist.
	 */
	public SemanticDataElement getLatestByName(String name);

	public SemanticDataElement getLatestByNameInsens(String name);
	
	/**
	 * Retreives a specific semantic data element from the RDF based on its version.
	 * 
	 * @param name The short name of the desired element
	 * @param version The version of the desired element
	 * @return the semantic data element of the specified version, null if that version (or element) doesn't exist.
	 */
	public SemanticDataElement getByNameAndVersion(String name, String version);

	/**
	 * Retreives a list of semantic data elements from the RDF that are labelled as the latest version.
	 * 
	 * @param names A collection of short names of the desired elements
	 * @return a map of the elements, their shortnames being the keys for the map, null if none of the elements exist
	 *         (otherwise only the valid entries)
	 */
	public Map<String, SemanticDataElement> getLatestByNameList(Set<String> names);

	public Map<String, SemanticDataElement> getBasicLatestByNameList(Collection<String> names);

	/**
	 * Removes an element from the semantic graph
	 * 
	 * @param element
	 */
	public void remove(SemanticDataElement element);

	public Map<String, SemanticDataElement> getByNameAndVersionsMap(List<NameAndVersion> nameAndVersions);

	public List<SemanticDataElement> searchDetailed(DictionarySearchFacets facets,
			Map<FacetType, Set<String>> searchKeywords, boolean exactMatch, PaginationData pageData, boolean onlyOwned);

	public int searchCount(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchKeywords,
			boolean exactMatch, boolean onlyOwned);

	public void remove(String name, String version);

	public SemanticDataElement saveOverwrite(SemanticDataElement de, String oldName, String oldVersion);

	/**
	 * Returns all data elements where the until date field exists (not null)
	 * 
	 * @return
	 */
	public List<SemanticDataElement> getAllWithUntilDate();

	/**
	 * Returns all data elements where the until date is null and the status of the data element is 'Retired' or
	 * 'Deprecated'
	 * 
	 * @return
	 */
	public List<SemanticDataElement> getAllWithoutUntilDate();

	public Map<String, SemanticDataElement> getBasicByNameAndVersionsMap(List<NameAndVersion> nameAndVersions);

	public Map<String, SemanticDataElement> loadNestedFields(Map<String, SemanticDataElement> dataElements,
			boolean needUriFilter);
	
	public Map<String, SemanticDataElement> listByStatuses(Set<DataElementStatus> statuses);
	
	public String getSemeticDEShortNameByNameIgnoreCases(String name);
}
