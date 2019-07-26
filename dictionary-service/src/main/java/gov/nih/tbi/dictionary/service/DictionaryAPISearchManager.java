package gov.nih.tbi.dictionary.service;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

public interface DictionaryAPISearchManager {

	/**
	 * Gets the form structure with the given shortname and version. If version is null, then the latest version of the
	 * data element is retrieved. Version must be in the format <MajorVersion>.<MinorVersion>.
	 * 
	 * @param shortName: shortname of the requested entity. non-null
	 * @param version: version of the requetsed entity. <MajorVersion>.<MinorVersion> required. Can be null.
	 * @return
	 */
	public FormStructure getFormStructure(String shortName, String version);


	/**
	 * Gets the data element with the given shortname and version. If version is null, then the latest version of the
	 * data element is retrieved. Version must be in the format <MajorVersion>.<MinorVersion>.
	 * 
	 * @param shortName: shortname of the requested entity. non-null
	 * @param version: version of the requetsed entity. <MajorVersion>.<MinorVersion> required. Can be null.
	 * @return
	 */
	public DataElement getDataElement(String shortName, String version);

	public StringWriter searchFormStructure(Map<FormStructureFacet, Set<String>> facetMap, PaginationData pageData)
			throws ParserConfigurationException, TransformerException;

	public StringWriter searchDataElement(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchLocMap,
			PaginationData pageData);
	
}
