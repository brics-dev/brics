package gov.nih.tbi.dictionary.dao.sparql;

import gov.nih.tbi.commons.dao.sparql.GenericSparqlDaoImpl;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.commons.util.SearchDataElementQueryConstructionUtil;
import gov.nih.tbi.dictionary.dao.ClassificationSparqlDao;
import gov.nih.tbi.dictionary.dao.DataElementSparqlDao;
import gov.nih.tbi.dictionary.model.DictionarySearchFacets;
import gov.nih.tbi.dictionary.model.FacetType;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Domain;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SubDomain;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.modify.request.UpdateDataInsert;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDFS;

@Transactional
@Repository
public class DataElementSparqlDaoImpl extends GenericSparqlDaoImpl<SemanticDataElement> implements DataElementSparqlDao {

	private final boolean USE_IS_ONE_OF = true;

	@Autowired
	ClassificationSparqlDao classificationDao;

	private static final Logger log = LogManager.getLogger(DataElementSparqlDaoImpl.class.getName());

	/**
	 * Given a paginated result query and a unpaginated count query, return the search result and update the
	 * PaginationData with the proper count
	 * 
	 * @param resultQuery
	 * @param countQuery
	 * @param pageData
	 * @return
	 */
	private List<SemanticDataElement> searchWithQueries(Query resultQuery, Query countQuery, PaginationData pageData) {

		ResultSet rs;
		if (pageData == null) {
			rs = queryLargeResultSelect(resultQuery);
		} else {
			rs = querySelect(resultQuery);
		}

		Map<String, SemanticDataElement> resultsMap = new LinkedHashMap<String, SemanticDataElement>();

		while (rs.hasNext()) {
			QuerySolution row = rs.next();
			String uri = row.get(RDFConstants.URI_VARIABLE_NAME).toString();
			SemanticDataElement item = parseBasicFields(row);
			resultsMap.put(uri, item);
		}

		/**
		 * This requires a second SPARQL call. The call that actually retrieves the information is limited in scope. The
		 * count requires the full result set.
		 */
		ResultSet rsCount = querySelect(countQuery);

		while (rsCount.hasNext()) {
			QuerySolution row = rsCount.next();
			int count = row.get(RDFConstants.COUNT_VARIABLE.getVarName()).asLiteral().getInt();
			if (pageData != null) {
				pageData.setNumSearchResults(count);
			}
		}

		List<SemanticDataElement> results = new ArrayList<SemanticDataElement>(resultsMap.values());
		return results;
	}

	/**
	 * @inheritDoc
	 */
	public List<SemanticDataElement> search(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchKeywords,
			boolean exactMatch, PaginationData pageData, boolean onlyOwned) {

		Query resultQuery =
				SearchDataElementQueryConstructionUtil.buildSearchDataElementQuery(facets, searchKeywords, exactMatch,
						pageData, onlyOwned);

		Query countQuery =
				SearchDataElementQueryConstructionUtil.buildDataElementCountQuery(facets, searchKeywords, exactMatch,
						onlyOwned);

		return searchWithQueries(resultQuery, countQuery, pageData);
	}

	public int searchCount(DictionarySearchFacets facets, Map<FacetType, Set<String>> searchKeywords,
			boolean exactMatch, boolean onlyOwned) {

		Query countQuery =
				SearchDataElementQueryConstructionUtil.buildDataElementCountQuery(facets, searchKeywords, exactMatch,
						onlyOwned);

		ResultSet rsCount = querySelect(countQuery);

		QuerySolution row = rsCount.next();
		int count = row.get(RDFConstants.COUNT_VARIABLE.getVarName()).asLiteral().getInt();

		return count;
	}

	/**
	 * @inheritDoc
	 */
	public List<SemanticDataElement> searchDetailed(DictionarySearchFacets facets,
			Map<FacetType, Set<String>> searchKeywords, boolean exactMatch, PaginationData pageData, boolean onlyOwned) {

		Query resultQuery =
				SearchDataElementQueryConstructionUtil.getDetailedDataElementSearchQuery(facets, searchKeywords,
						exactMatch, pageData, onlyOwned);
		Query countQuery =
				SearchDataElementQueryConstructionUtil.buildDataElementCountQuery(facets, searchKeywords, exactMatch,
						onlyOwned);

		List<SemanticDataElement> deList = searchWithQueries(resultQuery, countQuery, pageData);

		Map<String, SemanticDataElement> deMap = new HashMap<String, SemanticDataElement>();

		if (deList != null && !deList.isEmpty()) {
			for (SemanticDataElement de : deList) {
				deMap.put(de.getUri(), de);
			}
		}

		loadNestedFields(deMap, !USE_IS_ONE_OF);

		return new ArrayList<SemanticDataElement>(deMap.values());
	}

	public Map<String, SemanticDataElement> listByStatuses(Set<DataElementStatus> statuses) {
		if (statuses == null || statuses.isEmpty()) {
			return new HashMap<String, SemanticDataElement>();
		}

		List<String> statusStrings = new ArrayList<String>();
		for (DataElementStatus status : statuses) {
			statusStrings.add(status.getName());
		}

		Query query = QueryConstructionUtil.getBasicDataElementQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		body.addElement(QueryConstructionUtil.isOneOfIgnoreCase(RDFConstants.STATUS_VARIABLE.getName(), statusStrings));
		ResultSet results = querySelect(query);

		Map<String, SemanticDataElement> dataElements = new HashMap<String, SemanticDataElement>();

		while (results.hasNext()) {
			QuerySolution row = results.next();
			SemanticDataElement de = parseBasicFields(row);
			dataElements.put(de.getUri(), de);
		}

		dataElements = loadNestedFields(dataElements, false);

		Map<String, SemanticDataElement> output = new HashMap<String, SemanticDataElement>();

		for (SemanticDataElement de : dataElements.values()) {
			output.put(de.getShortNameAndVersion(), de);
		}

		return output;
	}

	/**
	 * @inheritDoc
	 */
	public List<SemanticDataElement> getAll() {

		Map<String, SemanticDataElement> dataElementMap = getAllBasicUriObjectMap();
		dataElementMap = loadNestedFields(dataElementMap, false);
		return new ArrayList<SemanticDataElement>(dataElementMap.values());
	}

	/**
	 * @inheritDoc
	 */
	public List<SemanticDataElement> getAllWithUntilDate() {
		Query query = QueryConstructionUtil.getWithUntilDateQuery();
		query = QueryConstructionUtil.addLatestTriples(query);
		ResultSet results = querySelect(query);

		Map<String, SemanticDataElement> dataElements = new HashMap<String, SemanticDataElement>();

		while (results.hasNext()) {
			QuerySolution row = results.next();
			SemanticDataElement de = parseBasicFields(row);
			dataElements.put(de.getUri(), de);
		}

		dataElements = loadNestedFields(dataElements, true);
		return new ArrayList<SemanticDataElement>(dataElements.values());
	}

	/**
	 * @inheritDoc
	 */
	public List<SemanticDataElement> getAllWithoutUntilDate() {
		Query query = QueryConstructionUtil.getWithoutUntilDateQuery();
		query = QueryConstructionUtil.addLatestTriples(query);
		ResultSet results = querySelect(query);

		Map<String, SemanticDataElement> dataElements = new HashMap<String, SemanticDataElement>();

		while (results.hasNext()) {
			QuerySolution row = results.next();
			SemanticDataElement de = parseBasicFields(row);
			dataElements.put(de.getUri(), de);
		}

		dataElements = loadNestedFields(dataElements, true);
		return new ArrayList<SemanticDataElement>(dataElements.values());
	}

	/**
	 * @inheritDoc
	 */
	public Map<String, SemanticDataElement> getAllInNameMap() {

		// gets the basic data element map
		Map<String, SemanticDataElement> dataElementMap = getAllBasicUriObjectMap();

		// loads all the nested object fields
		dataElementMap = loadNestedFields(dataElementMap, false);

		// create a new hashmap of data element name to data element object
		Map<String, SemanticDataElement> nameToDataElementMap = new HashMap<String, SemanticDataElement>();

		for (SemanticDataElement de : dataElementMap.values()) {
			nameToDataElementMap.put(de.getShortNameAndVersion(), de);
		}

		return nameToDataElementMap;
	}

	/**
	 * @inheritDoc
	 */
	public SemanticDataElement get(String uri) {

		Query query = QueryConstructionUtil.getBasicDataElementQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

		if (block == null) {
			block = new ElementTriplesBlock();
			body.addElement(block);
		}

		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFS.Nodes.isDefinedBy, NodeFactory.createURI(uri)));

		ResultSet results = querySelect(query);

		SemanticDataElement dataElement = null;

		if (results.hasNext()) {
			dataElement = parseBasicFields(results.next());
		}

		dataElement = loadNestedFields(dataElement);

		return dataElement;
	}

	/**
	 * @inheritDoc
	 */
	public SemanticDataElement saveOverwrite(SemanticDataElement de, String oldName, String oldVersion) {

		if (de.getVersion() == null) {
			de.setVersion("1.0");
		}

		if (oldName == null && oldVersion == null) {
			remove(de);
		} else {
			remove(oldName, oldVersion);
		}

		if (de.getClassificationElementList() != null) {
			for (ClassificationElement ce : de.getClassificationElementList()) {
				Classification classification = ce.getClassification();
				// if(!classificationDao.exists(classification))
				// {
				// classificationDao.save(classification);
				// }
			}
		}

		de.setModifiedDate(new Date());
		UpdateDataInsert updateInsert = null;
		try {
			updateInsert = new UpdateDataInsert(QueryConstructionUtil.generateDataElementTriples(de));
		} catch (DatatypeFormatException e) {
			e.printStackTrace();
			log.error("Error occured while saving a data element.");
		} catch (DateParseException e) {
			e.printStackTrace();
			log.error("Error occured while parsing a date during data element saving.");
		}

		UpdateRequest request = UpdateFactory.create();
		request.add(updateInsert);

		updateLatestVersion(de.getName(), de.getVersion());
		virtuosoStore.update(request);

		return de;
	}

	/**
	 * @inheritDoc
	 */
	public SemanticDataElement save(SemanticDataElement de) {

		return saveOverwrite(de, null, null);
	}

	private void updateLatestVersion(String shortName, String version) {

		String baseUri = RDFConstants.DATA_ELEMENT + "/" + shortName;
		String uri = RDFConstants.DATA_ELEMENT + "/" + shortName + "/" + version;
		String latest = RDFConstants.BRICS.concat("#latest");

		String sparqlUpdate =
				"WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE { <" + baseUri + "> <" + latest
						+ "> ?version } INSERT { <" + baseUri + "> <" + latest + "> <" + uri + "> } WHERE { <"
						+ baseUri + "> <" + latest + "> ?version }";

		update(sparqlUpdate);
	}

	/**
	 * @inheritDoc
	 */
	public SemanticDataElement getByName(String name) {

		return getByName(name, true, null);
	}

	private SemanticDataElement getByName(String name, boolean latestFlag, String version) {

		Query query = QueryConstructionUtil.getBasicDataElementQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

		if (block == null) {
			block = new ElementTriplesBlock();
			body.addElement(block);
		}

		if (latestFlag) {
			block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TYPE_NODE_N,
					RDFConstants.BASE_URI_VARIABLE));
			block.addTriple(Triple.create(RDFConstants.BASE_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_LATEST_NODE_N,
					RDFConstants.URI_NODE));
		}

		if (version != null) {
			block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_VERSION_NODE_N,
					NodeFactory.createLiteral(version)));
		}

		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				NodeFactory.createLiteral(name)));

		ResultSet results = querySelect(query);
		if (results.hasNext()) {
			SemanticDataElement de = parseBasicFields(results.next());
			if (de != null) {
				de = loadNestedFields(de);
				return de;
			}
		}
		return null;
	}
	
	private SemanticDataElement getByNameInsens(String name, boolean latestFlag, String version) {

		Query query = QueryConstructionUtil.getBasicDataElementQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

		if (block == null) {
			block = new ElementTriplesBlock();
			body.addElement(block);
		}

		if (latestFlag) {
			block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TYPE_NODE_N,
					RDFConstants.BASE_URI_VARIABLE));
			block.addTriple(Triple.create(RDFConstants.BASE_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_LATEST_NODE_N,
					RDFConstants.URI_NODE));
		}

		if (version != null) {
			block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_VERSION_NODE_N,
					NodeFactory.createLiteral(version)));
		}

		body.addElement(QueryConstructionUtil.regexFilter(RDFConstants.SHORT_NAME_VARIABLE, name));

		ResultSet results = querySelect(query);
		
		if (results.hasNext()) {
			SemanticDataElement de = parseBasicFields(results.next());
			if (de != null) {
				de = loadNestedFields(de);
				return de;
			}
		}
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Map<String, SemanticDataElement> getByNameList(Collection<String> names) {

		return getByNameList(names, true);

	}

	/**
	 * @inheritDoc
	 */
	public Map<String, SemanticDataElement> getBasicLatestByNameList(Collection<String> names) {

		return getBasicLatestByNameList(names, true);

	}

	private Map<String, SemanticDataElement> getBasicLatestByNameList(Collection<String> names, boolean latestFlag) {
		Query query = QueryConstructionUtil.getBasicDataElementQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

		if (block == null) {
			block = new ElementTriplesBlock();
			body.addElement(block);
		}

		body.addElementFilter(QueryConstructionUtil.isOneOfUri(RDFConstants.SHORT_NAME_VARIABLE.getName(), names));

		if (latestFlag) {
			block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TYPE_NODE_N,
					RDFConstants.BASE_URI_VARIABLE));
			block.addTriple(Triple.create(RDFConstants.BASE_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_LATEST_NODE_N,
					RDFConstants.URI_NODE));
		}

		ResultSet results = querySelect(query);

		Map<String, SemanticDataElement> uriToDataElementMap = new HashMap<String, SemanticDataElement>();

		while (results.hasNext()) {
			QuerySolution row = results.next();
			SemanticDataElement de = parseBasicFields(row);
			uriToDataElementMap.put(de.getUri(), de);
		}

		Map<String, SemanticDataElement> nameToDataElementMap = new HashMap<String, SemanticDataElement>();

		for (SemanticDataElement dataElement : uriToDataElementMap.values()) {
			nameToDataElementMap.put(dataElement.getShortNameAndVersion(), dataElement);
		}

		return nameToDataElementMap;
	}

	private Map<String, SemanticDataElement> getByNameList(Collection<String> names, boolean latestFlag) {
		Map<String, SemanticDataElement> nameToDataElementMap = getBasicLatestByNameList(names, latestFlag);

		for (SemanticDataElement dataElement : nameToDataElementMap.values()) {
			dataElement = loadNestedFields(dataElement);
		}

		return nameToDataElementMap;
	}

	/********* PRIVATE AUXILIARY METHODS ****************/

	/**
	 * Parse a single row into a single semantic data element object (but only the basic fields i.e. non-collection/pojo
	 * fields)
	 * 
	 * @param row
	 * @return
	 * @throws ParseException
	 */
	private SemanticDataElement parseBasicFields(QuerySolution row) {

		// get each column values into their respective string variable
		String uri = rdfNodeToString(row.get(RDFConstants.URI_VARIABLE_NAME));
		String name = StringEscapeUtils.unescapeHtml(rdfNodeToString(row.get(RDFConstants.SHORT_NAME_VARIABLE.getName())));
		String description = StringEscapeUtils.unescapeHtml(rdfNodeToString(row.get(RDFConstants.DESCRIPTION_VARIABLE.getName())));
		String shortDescription = StringEscapeUtils.unescapeHtml(rdfNodeToString(row.get(RDFConstants.SHORT_DESCRIPTION_VARIABLE.getName())));
		String format = rdfNodeToString(row.get(RDFConstants.FORMAT_VARIABLE.getName()));
		String notes = StringEscapeUtils.unescapeHtml(rdfNodeToString(row.get(RDFConstants.NOTES_VARIABLE.getName())));
		String historicalNotes = StringEscapeUtils.unescapeHtml(rdfNodeToString(row.get(RDFConstants.HISTORICAL_NOTES_VARIABLE.getName())));
		String references = StringEscapeUtils.unescapeHtml(rdfNodeToString(row.get(RDFConstants.REFERENCES_VARIABLE.getName())));
		String populationString = rdfNodeToString(row.get(RDFConstants.POPULATION_VARIABLE.getName()));
		String guidelines = StringEscapeUtils.unescapeHtml(rdfNodeToString(row.get(RDFConstants.GUIDELINES_VARIABLE.getName())));
		String title = StringEscapeUtils.unescapeHtml(rdfNodeToString(row.get(RDFConstants.TITLE_VARIABLE.getName())));
		String categoryTitle = rdfNodeToString(row.get(RDFConstants.CATEGORY_TITLE_VARIABLE.getName()));
		String categoryShortName = rdfNodeToString(row.get(RDFConstants.CATEGORY_SHORT_VARIABLE.getName()));
		String statusString = rdfNodeToString(row.get(RDFConstants.STATUS_VARIABLE.getName()));
		String elementTypeString = rdfNodeToString(row.get(RDFConstants.ELEMENT_TYPE_VARIABLE.getName()));
		String versionString = rdfNodeToString(row.get(RDFConstants.VERSION_VARIABLE.getName()));

		Date modifiedDate = rdfNodeToDate(row.get(RDFConstants.MODIFIED_DATE_VARIABLE.getName()));
		Date dateCreated = rdfNodeToDate(row.get(RDFConstants.DATE_CREATED_VARIABLE.getName()));
		String createdBy = rdfNodeToString(row.get(RDFConstants.CREATED_BY_VARIABLE.getName()));

		String submittingContactInfo =
				rdfNodeToString(row.get(RDFConstants.SUBMITTING_CONTACT_INFO_VARIABLE.getName()));
		String submittingContactName =
				rdfNodeToString(row.get(RDFConstants.SUBMITTING_CONTACT_NAME_VARIABLE.getName()));
		String submittingOrgName = rdfNodeToString(row.get(RDFConstants.SUBMITTING_ORG_NAME_VARIABLE.getName()));
		String stewardContactInfo = rdfNodeToString(row.get(RDFConstants.STEWARD_CONTACT_INFO_VARIABLE.getName()));
		String stewardContactName = rdfNodeToString(row.get(RDFConstants.STEWARD_CONTACT_NAME_VARIABLE.getName()));
		String stewardOrgName = rdfNodeToString(row.get(RDFConstants.STEWARD_ORG_NAME_VARIABLE.getName()));

		Date effectiveDate = rdfNodeToDate(row.get(RDFConstants.EFFECTIVE_DATE_VARIABLE.getName()));
		Date untilDate = rdfNodeToDate(row.get(RDFConstants.UNTIL_DATE_VARIABLE.getName()));

		String seeAlso = rdfNodeToString(row.get(RDFConstants.SEE_ALSO_VARIABLE.getName()));

		// parse population in string to population object
		Population population = new Population();
		population.setName(populationString);

		Category category = new Category();
		category.setShortName(categoryShortName);
		category.setName(categoryTitle);

		/**
		 * Status is currently broken. If no status then set it to "Draft"
		 */
		DataElementStatus status = DataElementStatus.DRAFT;
		if (statusString != null && !statusString.isEmpty() && DataElementStatus.getByName(statusString) != null) {
			status = DataElementStatus.getByName(statusString);
		}
		// set the fields
		SemanticDataElement de = new SemanticDataElement();
		de.setUri(uri);
		de.setName(name);
		de.setDescription(description);
		de.setShortDescription(shortDescription);
		de.setFormat(format);
		de.setNotes(notes);
		de.setHistoricalNotes(historicalNotes);
		de.setReferences(references);
		de.setPopulation(population);
		de.setGuidelines(guidelines);
		de.setTitle(title);
		de.setCategory(category);
		de.setStatus(status);
		de.setModifiedDate(modifiedDate);
		de.setDateCreated(dateCreated);
		de.setCreatedBy(createdBy);
		de.setSubmittingContactInfo(submittingContactInfo);
		de.setSubmittingContactName(submittingContactName);
		de.setSubmittingOrgName(submittingOrgName);
		de.setStewardContactInfo(stewardContactInfo);
		de.setStewardContactName(stewardContactName);
		de.setStewardOrgName(stewardOrgName);
		de.setEffectiveDate(effectiveDate);
		de.setUntilDate(untilDate);
		de.setSeeAlso(seeAlso);

		if (versionString != null && !versionString.isEmpty()) {
			try {
				de.setVersion(versionString);
			} catch (NumberFormatException nfe) {
				// if the version isn't parsable, just don't set it
			}

		}

		if (elementTypeString != null) {
			DataType elementType = DataType.valueOf(elementTypeString);
			de.setType(elementType);
		}

		return de;
	}

	/**
	 * Queries for all the data elements and only load the basic top-level fields, not unlike the results from a
	 * hibernate lazy get.
	 * 
	 * @return All the results in a map of the data element's uri to the data element object.
	 */
	private Map<String, SemanticDataElement> getAllBasicUriObjectMap() {

		Query query = QueryConstructionUtil.getBasicDataElementQuery();
		ResultSet results = querySelect(query);

		Map<String, SemanticDataElement> dataElements = new HashMap<String, SemanticDataElement>();

		while (results.hasNext()) {
			QuerySolution row = results.next();
			SemanticDataElement de = parseBasicFields(row);
			dataElements.put(de.getUri(), de);
		}

		return dataElements;
	}

	/**
	 * Loads all the classification elements for all the data elements in the provided map
	 * 
	 * @param dataElements - Map of data element uri to the data element object
	 * @param needUriFilter - true if this is not a load for a get all, should be false otherwise.
	 * @return
	 */
	private Map<String, SemanticDataElement> loadClassificationElements(Map<String, SemanticDataElement> dataElements,
			boolean needUriFilter) {

		Query classificationElementQuery = QueryConstructionUtil.getClassificationElementQuery();

		// insert filter to filter our results by the data elements we want
		if (needUriFilter) {
			Set<String> uris = dataElements.keySet();
			ElementGroup body = (ElementGroup) classificationElementQuery.getQueryPattern();
			body.addElementFilter(QueryConstructionUtil.isOneOfUri(RDFConstants.DATA_ELEMENT_VARIABLE.getName(), uris));
		}

		// map of all the classifications in the database. This is as map of classification URIs to classification
		// objects
		// Map<String, Classification> classificationMap = classificationDao.getClassificationMap();

		// get the query result for classifications
		ResultSet results = querySelect(classificationElementQuery);

		while (results.hasNext()) {
			QuerySolution row = results.next();

			String subgroupName = rdfNodeToString(row.get(RDFConstants.SUBGROUP_VARIABLE.getName()));
			String classificationUri = rdfNodeToString(row.get(RDFConstants.CLASSIFICATION_VARIABLE.getName()));
			String deUri = rdfNodeToString(row.get(RDFConstants.DATA_ELEMENT_VARIABLE.getName()));
			String diseaseString = rdfNodeToString(row.get(RDFConstants.DISEASE_VARIABLE.getName()));

			// Classification classification = classificationMap.get(classificationUri);
			Classification classification = new Classification(classificationUri, true, true);
			Subgroup subgroup = new Subgroup(subgroupName);
			SemanticDataElement de = dataElements.get(deUri);
			Disease disease = new Disease(diseaseString, true);

			if (de != null && classification != null) {
				ClassificationElement ce = new ClassificationElement(disease, classification, subgroup);
				de.addClassificationElement(ce);
			}
		}

		return dataElements;
	}

	/**
	 * Loads all the nested fields for the provided data element
	 * 
	 * @param de - data element object to load for. Needs at least the URI field set.
	 * @return
	 */
	private SemanticDataElement loadNestedFields(SemanticDataElement de) {

		if (de == null || de.getUri() == null) {
			return null;
		}

		// insert the data element into a map, because we are using the same methods for bulk loading for nested fields
		Map<String, SemanticDataElement> dataElements = new HashMap<String, SemanticDataElement>();
		dataElements.put(de.getUri(), de);

		dataElements = loadClassificationElements(dataElements, true);
		dataElements = loadKeywords(dataElements, true);
		dataElements = loadLabels(dataElements, true);
		dataElements = loadExternalIds(dataElements, true);
		dataElements = loadPermissibleValues(dataElements, true);
		dataElements = loadSubdomainValues(dataElements, true);

		return dataElements.values().iterator().next();
	}

	private Map<String, SemanticDataElement> loadSubdomainValues(Map<String, SemanticDataElement> dataElements,
			boolean needUriFilter) {

		Query subdomainElementQuery = QueryConstructionUtil.getSubdomainQuery();

		// insert filter to filter our results by the data elements we want
		if (needUriFilter) {
			Set<String> uris = dataElements.keySet();
			ElementGroup body = (ElementGroup) subdomainElementQuery.getQueryPattern();
			body.addElementFilter(QueryConstructionUtil.isOneOfUri(RDFConstants.DATA_ELEMENT_VARIABLE.getName(), uris));
		}

		// map of all the classifications in the database. This is as map of classification URIs to classification
		// objects
		// Map<String, Classification> classificationMap = classificationDao.getClassificationMap();

		// get the query result for classifications
		ResultSet results = querySelect(subdomainElementQuery);

		while (results.hasNext()) {
			QuerySolution row = results.next();

			String deUri = rdfNodeToString(row.get(RDFConstants.DATA_ELEMENT_VARIABLE.getName()));
			String diseaseString = rdfNodeToString(row.get(RDFConstants.DISEASE_VARIABLE.getName()));
			String domainString = rdfNodeToString(row.get(RDFConstants.DOMAIN_VARIABLE.getName()));
			String subGroupString = rdfNodeToString(row.get(RDFConstants.SUBGROUP_VARIABLE.getName()));

			SubDomain sd = new SubDomain(subGroupString);
			Domain domain = new Domain(domainString);
			SemanticDataElement de = dataElements.get(deUri);
			Disease disease = new Disease(diseaseString, true);
			SubDomainElement sde = new SubDomainElement(disease, domain, sd);

			if (de != null && sde != null) {
				de.addSubDomainElement(sde);
			}
		}

		return dataElements;
	}

	/**
	 * Loads all the nested fields for the map of data elements.
	 * 
	 * @param dataElements
	 * @param needUriFilter - true means data elements map contains a list of ALL data elements in the system. False
	 *        means data elements only contains a subset of the data elements in the system.
	 * @return
	 */
	public Map<String, SemanticDataElement> loadNestedFields(Map<String, SemanticDataElement> dataElements,
			boolean needUriFilter) {

		if (!dataElements.isEmpty()) {
			dataElements = loadClassificationElements(dataElements, needUriFilter);
			dataElements = loadKeywords(dataElements, needUriFilter);
			dataElements = loadLabels(dataElements, needUriFilter);
			dataElements = loadExternalIds(dataElements, needUriFilter);
			dataElements = loadPermissibleValues(dataElements, needUriFilter);
			dataElements = loadSubdomainValues(dataElements, needUriFilter);
		}

		return dataElements;
	}

	/**
	 * Loads all the permissible values for all the data elements in the provided map
	 * 
	 * @param dataElements - Map of data element uri to the data element object
	 * @param needUriFilter - true if this is not a load for a get all, should be false otherwise.
	 * @return
	 */
	private Map<String, SemanticDataElement> loadPermissibleValues(Map<String, SemanticDataElement> dataElements,
			boolean needUriFilter) {

		Query permissibleValueQuery = QueryConstructionUtil.getPermissibleValueQuery();

		// insert filter to filter our results by the data elements we want
		if (needUriFilter) {
			Set<String> uris = dataElements.keySet();
			ElementGroup body = (ElementGroup) permissibleValueQuery.getQueryPattern();
			body.addElement(QueryConstructionUtil.buildUriValuesSubQuery(RDFConstants.DATA_ELEMENT_VARIABLE, uris));
		}

		ResultSet results = querySelect(permissibleValueQuery);

		while (results.hasNext()) {
			// parse the current value range
			QuerySolution row = results.next();
			ValueRange valueRange = parseValueRangeRow(row);
			String dataElementUri = rdfNodeToString(row.get(RDFConstants.DATA_ELEMENT_VARIABLE.getName()));
			SemanticDataElement de = dataElements.get(dataElementUri);

			if (de != null) {
				de.addValueRange(valueRange);
			}
		}

		return dataElements;
	}

	/**
	 * Parses a JENA row into a single value range
	 * 
	 * @param row
	 * @return
	 */
	private ValueRange parseValueRangeRow(QuerySolution row) {

		String uri = rdfNodeToString(row.get(RDFConstants.PERMISSIBLE_VALUE_NODE_VARIABLE.getName()));
		String valueRangeString = rdfNodeToString(row.get(RDFConstants.PERMISSIBLE_VALUE_VARIABLE.getName()));
		String description = rdfNodeToString(row.get(RDFConstants.PERMISSIBLE_VALUE_DESCRIPTION_VARIABLE.getName()));

		ValueRange valueRange = new ValueRange();
		valueRange.setUri(uri);
		valueRange.setValueRange(valueRangeString);
		valueRange.setDescription(description);

		return valueRange;
	}

	/**
	 * Parses a single JENA row into an single ExternalId
	 * 
	 * @param row
	 * @return
	 */
	private ExternalId parseExternalIdRow(QuerySolution row) {

		String uri = rdfNodeToString(row.get(RDFConstants.EXTERNAL_ID_VARIABLE.getName()));
		String value = rdfNodeToString(row.get(RDFConstants.VALUE_VARIABLE.getName()));
		String schemaName = rdfNodeToString(row.get(RDFConstants.TYPE_VARIABLE.getName()));
		Schema schema = new Schema(schemaName);

		ExternalId externalId = new ExternalId();
		externalId.setUri(uri);
		externalId.setValue(value);
		externalId.setSchema(schema);

		return externalId;
	}

	/**
	 * Loads all the external IDs for all the data elements in the provided map
	 * 
	 * @param dataElements - Map of data element uri to the data element object
	 * @param needUriFilter - true if this is not a load for a get all, should be false otherwise.
	 * @return
	 */
	private Map<String, SemanticDataElement> loadExternalIds(Map<String, SemanticDataElement> dataElements,
			boolean needUriFilter) {

		Query externalIdQuery = QueryConstructionUtil.getExternalIdQuery();

		// insert filter to filter our results by the data elements we want
		if (needUriFilter) {
			Set<String> uris = dataElements.keySet();
			ElementGroup body = (ElementGroup) externalIdQuery.getQueryPattern();
			body.addElementFilter(QueryConstructionUtil.isOneOfUri(RDFConstants.DATA_ELEMENT_VARIABLE.getName(), uris));
		}

		ResultSet results = querySelect(externalIdQuery);

		while (results.hasNext()) {
			QuerySolution row = results.next();
			ExternalId externalId = parseExternalIdRow(row);
			String dataElementUri = rdfNodeToString(row.get(RDFConstants.DATA_ELEMENT_VARIABLE.getName()));
			SemanticDataElement de = dataElements.get(dataElementUri);

			if (de != null) {
				de.addExternalId(externalId);
			}
		}

		return dataElements;
	}

	public void changeStatus(String name, String version, DataElementStatus status) {
		// TODO: impl
	}

	/**
	 * Loads all the the keywords for the data elements in the hashmap
	 * 
	 * @param dataElements
	 * @param needUriFilter
	 * @return
	 */
	private Map<String, SemanticDataElement> loadKeywords(Map<String, SemanticDataElement> dataElements,
			boolean needUriFilter) {

		Query keywordQuery = null;

		if (needUriFilter) {
			keywordQuery = QueryConstructionUtil.getKeywordElementQuery();
		} else {
			keywordQuery = QueryConstructionUtil.getKeywordElementQueryWithoutCount();
		}

		// insert filter to filter our results by the data elements we want
		if (needUriFilter) {
			Set<String> uris = dataElements.keySet();
			ElementGroup body = (ElementGroup) keywordQuery.getQueryPattern();
			body.addElementFilter(QueryConstructionUtil.isOneOfUri(RDFConstants.DATA_ELEMENT_VARIABLE.getName(), uris));
		}

		ResultSet results = querySelect(keywordQuery);

		// Map<String, Keyword> keywordMap = new HashMap<String, Keyword>();

		while (results.hasNext()) {
			QuerySolution row = results.next();

			Keyword keyword = parseKeywordRow(row);

			/*
			 * if (!keywordMap.containsKey(keyword.getUri())) { keywordMap.put(keyword.getUri(), keyword); }
			 */

			String deUri = rdfNodeToString(row.get(RDFConstants.DATA_ELEMENT_VARIABLE.getName()));
			SemanticDataElement de = dataElements.get(deUri);

			if (de != null) {
				if (de.getKeywords() == null) {
					de.setKeywords(new HashSet<Keyword>());
				}
				de.getKeywords().add(keyword);
			}
		}

		/**
		 * This is no longer required thanks to being able to pull all required information in the initial query.
		 * */

		/*
		 * Map<String, Set<String>> dataElementToKeywordMap = getDataElementKeywordMap(keywordMap, needUriFilter);
		 * 
		 * for (Entry<String, Set<String>> dataElementToKeywordEntry : dataElementToKeywordMap.entrySet()) { String
		 * dataElementUri = dataElementToKeywordEntry.getKey(); SemanticDataElement de =
		 * dataElements.get(dataElementUri);
		 * 
		 * if (de != null) { for (String keywordUri : dataElementToKeywordEntry.getValue()) { Keyword keyword =
		 * keywordMap.get(keywordUri);
		 * 
		 * if (keyword != null) { if (de.getKeywords() == null) { de.setKeywords(new HashSet<Keyword>()); }
		 * de.getKeywords().add(keyword); } } } }
		 */

		return dataElements;
	}

	/**
	 * Loads all the the labels for the data elements in the hashmap
	 * 
	 * @param dataElements
	 * @param needUriFilter
	 * @return
	 */
	private Map<String, SemanticDataElement> loadLabels(Map<String, SemanticDataElement> dataElements,
			boolean needUriFilter) {

		Query labelQuery = QueryConstructionUtil.getLabelElementQuery();

		// insert filter to filter our results by the data elements we want
		if (needUriFilter) {
			Set<String> uris = dataElements.keySet();
			ElementGroup body = (ElementGroup) labelQuery.getQueryPattern();
			body.addElementFilter(QueryConstructionUtil.isOneOfUri(RDFConstants.DATA_ELEMENT_VARIABLE.getName(), uris));
		}

		ResultSet results = querySelect(labelQuery);

		// Map<String, Keyword> labelMap = new HashMap<String, Keyword>();

		while (results.hasNext()) {
			QuerySolution row = results.next();

			Keyword label = parseLabelRow(row);

			/*
			 * if (!labelMap.containsKey(label.getUri())) { labelMap.put(label.getUri(), label); }
			 */

			String deUri = rdfNodeToString(row.get(RDFConstants.DATA_ELEMENT_VARIABLE.getName()));
			SemanticDataElement de = dataElements.get(deUri);

			if (de != null) {
				if (de.getLabels() == null) {
					de.setLabels(new HashSet<Keyword>());
				}
				de.getLabels().add(label);
			}
		}

		/*
		 * Map<String, Set<String>> dataElementToLabelMap = getDataElementLabelMap(labelMap, needUriFilter);
		 * 
		 * for (Entry<String, Set<String>> dataElementToLabelEntry : dataElementToLabelMap.entrySet()) { String
		 * dataElementUri = dataElementToLabelEntry.getKey(); SemanticDataElement de = dataElements.get(dataElementUri);
		 * 
		 * if (de != null) { for (String labelUri : dataElementToLabelEntry.getValue()) { Keyword label =
		 * labelMap.get(labelUri);
		 * 
		 * if (label != null) { if (de.getLabels() == null) { de.setLabels(new HashSet<Keyword>()); }
		 * de.getLabels().add(label); } } } }
		 */

		return dataElements;
	}

	/**
	 * Returns a hashmap of data element uri to a set of keyword uris
	 * 
	 * @param keywordMap
	 * @param needUriFilter
	 * @return
	 */
	private Map<String, Set<String>> getDataElementKeywordMap(Map<String, Keyword> keywordMap, boolean needUriFilter) {

		Query keywordDataElementQuery = QueryConstructionUtil.getKeywordDataElementQuery();

		if (needUriFilter) {
			Set<String> uris = keywordMap.keySet();
			ElementGroup body = (ElementGroup) keywordDataElementQuery.getQueryPattern();
			body.addElementFilter(QueryConstructionUtil.isOneOfUri(RDFConstants.KEYWORD_VARIABLE.getName(), uris));
		}

		Map<String, Set<String>> dataElementToKeywordMap = new HashMap<String, Set<String>>();

		ResultSet results = querySelect(QueryConstructionUtil.getKeywordDataElementQuery());

		while (results.hasNext()) {
			QuerySolution row = results.next();
			String keywordUri = rdfNodeToString(row.get(RDFConstants.KEYWORD_VARIABLE.getName()));
			String dataElementUri = rdfNodeToString(row.get(RDFConstants.DATA_ELEMENT_VARIABLE.getName()));

			Set<String> keywordUriSet = dataElementToKeywordMap.get(dataElementUri);

			if (keywordUriSet == null) {
				keywordUriSet = new HashSet<String>();
				dataElementToKeywordMap.put(dataElementUri, keywordUriSet);
			}

			keywordUriSet.add(keywordUri);
		}

		return dataElementToKeywordMap;
	}

	/**
	 * Returns a hashmap of data element uri to a set of label uris
	 * 
	 * @param labelMap
	 * @param needUriFilter
	 * @return
	 */
	private Map<String, Set<String>> getDataElementLabelMap(Map<String, Keyword> labelMap, boolean needUriFilter) {

		Query labelDataElementQuery = QueryConstructionUtil.getLabelDataElementQuery();

		if (needUriFilter) {
			Set<String> uris = labelMap.keySet();
			ElementGroup body = (ElementGroup) labelDataElementQuery.getQueryPattern();
			body.addElementFilter(QueryConstructionUtil.isOneOfUri(RDFConstants.LABEL_VARIABLE.getName(), uris));
		}

		Map<String, Set<String>> dataElementToLabelMap = new HashMap<String, Set<String>>();

		ResultSet results = querySelect(QueryConstructionUtil.getLabelDataElementQuery());

		while (results.hasNext()) {
			QuerySolution row = results.next();
			String labelUri = rdfNodeToString(row.get(RDFConstants.LABEL_VARIABLE.getName()));
			String dataElementUri = rdfNodeToString(row.get(RDFConstants.DATA_ELEMENT_VARIABLE.getName()));

			Set<String> labelUriSet = dataElementToLabelMap.get(dataElementUri);

			if (labelUriSet == null) {
				labelUriSet = new HashSet<String>();
				dataElementToLabelMap.put(dataElementUri, labelUriSet);
			}

			labelUriSet.add(labelUri);
		}

		return dataElementToLabelMap;
	}

	/**
	 * Parse row into keywordElement object
	 * 
	 * @param row
	 * @param needUriFilter
	 * @return
	 */
	private Keyword parseKeywordRow(QuerySolution row) {

		String uri = rdfNodeToString(row.get(RDFConstants.KEYWORD_VARIABLE.getName()));
		String keywordString = rdfNodeToString(row.get(RDFConstants.VALUE_VARIABLE.getName()));
		String countString = rdfNodeToString(row.get(RDFConstants.COUNT_VARIABLE.getName()));
		// temporary fix until the sparql query returns the correct count
		// countString = "0";
		if (countString == null) {
			countString = "0";
		}
		Long count = Long.valueOf(countString);

		Keyword keyword = new Keyword();
		keyword.setUri(uri);
		keyword.setKeyword(keywordString);
		keyword.setCount(count);

		return keyword;
	}

	/**
	 * Parse row into labelElement object
	 * 
	 * @param row
	 * @param needUriFilter
	 * @return
	 */
	private Keyword parseLabelRow(QuerySolution row) {

		String uri = rdfNodeToString(row.get(RDFConstants.LABEL_VARIABLE.getName()));
		String keywordString = rdfNodeToString(row.get(RDFConstants.VALUE_VARIABLE.getName()));
		String countString = rdfNodeToString(row.get(RDFConstants.COUNT_VARIABLE.getName()));
		// temporary fix until the sparql query returns the correct count
		// countString = "0";
		if (countString == null) {
			countString = "0";
		}
		Long count = Long.valueOf(countString);

		Keyword label = new Keyword();
		label.setUri(uri);
		label.setKeyword(keywordString);
		label.setCount(count);

		return label;
	}

	/**
	 * {@inheritDoc}
	 */
	public SemanticDataElement getLatestByName(String name) {

		return getByName(name, true, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SemanticDataElement getLatestByNameInsens(String name) {

		return getByNameInsens(name, true, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public SemanticDataElement getByNameAndVersion(String name, String version) {

		return getByName(name, false, version);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, SemanticDataElement> getLatestByNameList(Set<String> names) {

		return getByNameList(names, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(String name, String version) {

		if (name == null || version == null) {
			throw new NullPointerException("Cannot delete data element with null name or version");
		}
		/*
		 * UpdateDeleteWhere updateDelete = new UpdateDeleteWhere(
		 * QueryConstructionUtil.generateDataElementDeleteTriples(de)); UpdateRequest request = UpdateFactory.create();
		 * request.add(updateDelete); virtuosoStore.update(request);
		 */
		String uri = RDFConstants.DATA_ELEMENT_NS + name + "/" + version;

		String sparqlUpdate =
				"WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE { <" + uri + "> ?p ?o } WHERE { <" + uri
						+ "> ?p ?o }";

		update(sparqlUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(SemanticDataElement element) {

		if (element.getName() == null || element.getVersion() == null) {
			throw new NullPointerException("Cannot delete null data element!");
		}

		remove(element.getName(), element.getVersion());
	}

	private ElementFilter generateNameVersionFilter(String uri_var, List<NameAndVersion> nameAndVersions) {

		Collection<String> uris = new HashSet<String>();
		for (NameAndVersion item : nameAndVersions) {
			uris.add(RDFConstants.ELEMENT_NS + item.getName() + "/" + item.getVersion());
		}
		return QueryConstructionUtil.isOneOfUri(uri_var, uris);
	}

	/**
	 * Returns a list of form structures by their short name and versions
	 */
	public Map<String, SemanticDataElement> getByNameAndVersionsMap(List<NameAndVersion> nameAndVersions) {

		Query query = QueryConstructionUtil.getBasicDataElementQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		/*
		 * body.addElementFilter(QueryConstructionUtil.getNameVersionFilter(RDFConstants.SHORT_NAME_VARIABLE,
		 * RDFConstants.VERSION_VARIABLE, nameAndVersions));
		 */
		body.addElementFilter(generateNameVersionFilter(RDFConstants.URI_VARIABLE_NAME, nameAndVersions));
		ResultSet results = querySelect(query);

		Map<String, SemanticDataElement> des = new HashMap<String, SemanticDataElement>();

		while (results.hasNext()) {
			QuerySolution row = results.next();
			SemanticDataElement de = parseBasicFields(row);
			des.put(de.getUri(), de);
		}

		des = loadNestedFields(des, true);

		Map<String, SemanticDataElement> output = new HashMap<String, SemanticDataElement>();

		for (SemanticDataElement de : des.values()) {
			output.put(de.getShortNameAndVersion(), de);
		}

		return output;
	}

	/**
	 * Returns a list of form structures by their short name and versions
	 */
	public Map<String, SemanticDataElement> getBasicByNameAndVersionsMap(List<NameAndVersion> nameAndVersions) {

		Query query = QueryConstructionUtil.getBasicDataElementQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		/*
		 * body.addElementFilter(QueryConstructionUtil.getNameVersionFilter(RDFConstants.SHORT_NAME_VARIABLE,
		 * RDFConstants.VERSION_VARIABLE, nameAndVersions));
		 */
		body.addElementFilter(generateNameVersionFilter(RDFConstants.URI_VARIABLE_NAME, nameAndVersions));
		ResultSet results = querySelect(query);

		Map<String, SemanticDataElement> des = new HashMap<String, SemanticDataElement>();

		while (results.hasNext()) {
			QuerySolution row = results.next();
			SemanticDataElement de = parseBasicFields(row);
			des.put(de.getUri(), de);
		}

		Map<String, SemanticDataElement> output = new HashMap<String, SemanticDataElement>();

		for (SemanticDataElement de : des.values()) {
			output.put(de.getShortNameAndVersion(), de);
		}

		return output;
	}

	/**
	 * Returns a list of form structures by their short name and versions
	 */
	public List<SemanticDataElement> getByNameAndVersions(List<NameAndVersion> nameAndVersions) {

		return new ArrayList<SemanticDataElement>(getByNameAndVersionsMap(nameAndVersions).values());
	}
	
	@Override
	public String getSemeticDEShortNameByNameIgnoreCases(String name){
		
		String shortNameInVirtuoso = "";

		Query query = QueryConstructionUtil.getDataElementShortNameQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		body.addElement(new ElementFilter(QueryConstructionUtil.buildEqualsExpressionCaseInsensitive(RDFConstants.SHORT_NAME_VARIABLE, name)));

		ResultSet resultset = querySelect(query);
		if (resultset.hasNext()) {
			QuerySolution row = resultset.next();
			shortNameInVirtuoso = row.get(RDFConstants.SHORT_NAME_VARIABLE.getName()).asLiteral().toString();
		}
		return shortNameInVirtuoso;
	}

}
