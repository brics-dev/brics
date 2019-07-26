package gov.nih.tbi.dictionary.service.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.FormStructureDao;
import gov.nih.tbi.dictionary.dao.sparql.DataElementServiceSparqlDao;
import gov.nih.tbi.dictionary.dao.sparql.FormStructureServiceSparqlDao;
import gov.nih.tbi.dictionary.model.BaseDictionaryFacet;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.StringFacet;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.service.DictionaryAPISearchManager;
import gov.nih.tbi.dictionary.xml.XmlGenerationUtil;

@Service
@Scope("singleton")
public class DictionaryAPISearchManagerImpl implements DictionaryAPISearchManager {

	@Autowired
	FormStructureDao formStructureDao;

	@Autowired
	DataElementDao dataElementDao;

	@Autowired
	FormStructureServiceSparqlDao formStructureServiceDao;

	@Autowired
	DataElementServiceSparqlDao dataElementServiceDao;

	/**
	 * {@inheritDoc}
	 */
	public FormStructure getFormStructure(String shortName, String version) {

		FormStructure fs;
		if (version == null) {
			fs = formStructureDao.getLatestVersionByShortName(shortName);
		} else {
			fs = formStructureDao.get(shortName, version);
		}
		return fs;
	}

	/**
	 * {@inheritDoc}
	 */
	public DataElement getDataElement(String shortName, String version) {

		DataElement de;
		if (version == null) {
			de = dataElementDao.getLatestByName(shortName);
		} else {
			de = dataElementDao.getByNameAndVersion(shortName, version);
		}
		return de;
	}

	public StringWriter searchFormStructure(Map<FormStructureFacet, Set<String>> facetMap, PaginationData pageData)
			throws ParserConfigurationException, TransformerException {

		ResultSet rs = formStructureServiceDao.search(facetMap, pageData);

		StringWriter sw = XmlGenerationUtil.generateFormStructureSearchXml(rs);

		return sw;
	}
	
	public StringWriter searchDataElement(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchLocMap, PaginationData pageData) {

		facets = new DictionarySearchFacets();
		
		List<String> testList = new ArrayList<String>();
		testList.add("Unique Data Element");
		BaseDictionaryFacet bdf = new StringFacet(FacetType.CATEGORY, testList);
		facets.getFacetMap().put(FacetType.CATEGORY, bdf);
		
		searchLocMap = new HashMap<FacetType, Set<String>>();
		Set<String> testSet = new HashSet<String>();
		testSet.add("age");
		searchLocMap.put(FacetType.DESCRIPTION, testSet);
		
		ResultSet rs = dataElementServiceDao.search(facets, searchLocMap, pageData);
		while (rs.hasNext()) {
			QuerySolution row = rs.next();
		}
		return null;
	}
	
}
