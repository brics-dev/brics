package gov.nih.tbi.dictionary.dao.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import gov.nih.tbi.commons.dao.sparql.GenericSparqlDaoImpl;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.commons.util.QueryConstructionUtil;
import gov.nih.tbi.commons.util.RDFConstants;
import gov.nih.tbi.commons.util.SearchFormStructureQueryConstructionUtil;
import gov.nih.tbi.dictionary.dao.FormStructureSparqlDao;
import gov.nih.tbi.dictionary.model.FormStructureFacet;
import gov.nih.tbi.dictionary.model.FormStructureStandardization;
import gov.nih.tbi.dictionary.model.InstanceRequiredFor;
import gov.nih.tbi.dictionary.model.NameAndVersion;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.FormLabel;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;

@Transactional
@Repository
public class FormStructureSparqlDaoImpl extends GenericSparqlDaoImpl<SemanticFormStructure> implements FormStructureSparqlDao {

	static Logger logger = Logger.getLogger(FormStructureSparqlDaoImpl.class);

	public FormStructureSparqlDaoImpl() {

	}

	/**
	 * Parses the row with only the basic top-level fields
	 * 
	 * @param row
	 * @return
	 */
	private SemanticFormStructure parseBasicFields(QuerySolution row) {

		String uri = rdfNodeToString(row.get(RDFConstants.URI_VARIABLE_NAME));
		String title = rdfNodeToString(row.get(RDFConstants.TITLE_VARIABLE.getName()));
		String shortName = rdfNodeToString(row.get(RDFConstants.SHORT_NAME_VARIABLE.getName()));
		String version = rdfNodeToString(row.get(RDFConstants.VERSION_VARIABLE.getName()));
		String description = rdfNodeToString(row.get(RDFConstants.DESCRIPTION_VARIABLE.getName()));
		String diseases = rdfNodeToString(row.get(RDFConstants.DISEASE_VARIABLE.getName()));
		String organization = rdfNodeToString(row.get(RDFConstants.ORGANIZATION_VARIABLE.getName()));
		String submissionType = rdfNodeToString(row.get(RDFConstants.SUBMISSION_TYPE_VARIABLE.getName()));
		Date modifiedDate = rdfNodeToDate(row.get(RDFConstants.MODIFIED_DATE_VARIABLE.getName()));
		Date dateCreated = rdfNodeToDate(row.get(RDFConstants.DATE_CREATED_VARIABLE.getName()));
		String status = rdfNodeToString(row.get(RDFConstants.STATUS_VARIABLE.getName()));
		String createdBy = rdfNodeToString(row.get(RDFConstants.CREATED_BY_VARIABLE.getName()));
		String standardization = rdfNodeToString(row.get(RDFConstants.STANDARDIZATION_VARIABLE.getName()));
		String requiredList = rdfNodeToString(row.get(RDFConstants.REQUIRED_VARIABLE.getName()));
		String labelIdList = rdfNodeToString(row.get(RDFConstants.LABEL_ID_VARIABLE.getName()));
		String labelList = rdfNodeToString(row.get(RDFConstants.LABEL_VARIABLE.getName()));
		Boolean isCopyrighted = rdfNodeToBoolean(row.get(RDFConstants.IS_COPYRIGHTED_VARIABLE.getName()));

		SemanticFormStructure newForm = new SemanticFormStructure();
		newForm.setUri(uri);
		newForm.setTitle(title);
		newForm.setShortName(shortName);
		newForm.setVersion(version);
		newForm.setDescription(description);
		newForm.setOrganization(organization);
		newForm.setModifiedDate(modifiedDate);
		newForm.setSubmissionType(submissionType);
		newForm.setDateCreated(dateCreated);
		newForm.setCreatedBy(createdBy);
		newForm.setStandardization(FormStructureStandardization.getByName(standardization));
		newForm.setIsCopyrighted(isCopyrighted);

		List<String> instanceRequiredList = new ArrayList<String>();
		if (requiredList != null) {
			instanceRequiredList = BRICSStringUtils.delimitedStringToList(requiredList, ",");
		}
		newForm.addAllInstancesRequiredForStrings(instanceRequiredList);
		
		List<FormLabel> formLabelList = new ArrayList<FormLabel>();
		if (labelIdList != null && labelList != null) {
			List<String> labelIds = BRICSStringUtils.delimitedStringToList(labelIdList, ",");
			List<String> labels = BRICSStringUtils.delimitedStringToList(labelList, ",");
			
			if (labels != null && labelIds != null && labelIds.size() == labels.size()) {
				for (int i = 0; i < labelIds.size(); i++) {
					FormLabel formLabel = new FormLabel();
					formLabel.setId(Long.parseLong(labelIds.get(i)));
					formLabel.setLabel(labels.get(i));
					formLabelList.add(formLabel);
				}
			}
			newForm.setFormLabels(formLabelList);
		}

		List<Disease> diseaseList = new ArrayList<Disease>();
		diseaseList.add(new Disease(diseases, true));
		newForm.setDiseases(diseaseList);

		/**
		 * Current system does not set status. If it does not exist, set it to "unknown".
		 */
		if (status != null && StatusType.statusOf(status) != null) {
			newForm.setStatus(StatusType.statusOf(status));
		} else {
			newForm.setStatus(StatusType.UNKNOWN);
		}

		return newForm;
	}

	/**
	 * Parses result set row into a Disease object
	 * 
	 * @param row
	 * @return
	 */
	private Disease parseDiseaseField(QuerySolution row) {

		String diseaseName = rdfNodeToString(row.get(RDFConstants.DISEASE_VARIABLE.getName()));
		Disease disease = new Disease();
		disease.setName(diseaseName);

		return disease;
	}

	
	/**
	 * Returns a list of form structures with only their basic fields set
	 * 
	 * @return
	 */
	public List<SemanticFormStructure> getAllBasic() {

		ResultSet results = querySelect(QueryConstructionUtil.getBasicFormStructureQuery());
		List<SemanticFormStructure> formStructures = new ArrayList<SemanticFormStructure>();

		while (results.hasNext()) {
			formStructures.add(parseBasicFields(results.next()));
		}

		return formStructures;
	}

	/**
	 * Returns hashmap of URIs to form structure objects with their basic field already set
	 * 
	 * @return
	 */
	public Map<String, SemanticFormStructure> getAllBasicUriObjectMap() {

		ResultSet results = querySelect(QueryConstructionUtil.getBasicFormStructureQuery());
		Map<String, SemanticFormStructure> formStructures = resultsToObjectMap(results);
		return formStructures;
	}

	/**
	 * This method merges two SemanticFormStructures. Currently the only applicable field is "disease". This loops
	 * through the second argument and adds all its diseases to the first argument's list.
	 * 
	 * @TODO: The algebra for creating a list (or a comma delimited list) SHOULD MOVE TO THE SPARQL QUERY. However, Jena
	 *        2.11.1 does not support aggregate method calls in the select statement (so the "group_concat()" method,
	 *        which would be perfect, actually throws an error).
	 * 
	 * @param original
	 * @param additional
	 * @return
	 */
	private SemanticFormStructure mergeFormStructures(SemanticFormStructure original,
			SemanticFormStructure additional) {

		// cycle through the list of diseases for all subsequent forms returned
		if (additional.getDiseases() != null)
			for (Disease d : additional.getDiseases()) {
				original.getDiseases().add(d);
			}

		return original;
	}

	/**
	 * @inheritDoc
	 */
	public Map<String, SemanticFormStructure> getAllIntoShortNameVersionMap() {

		List<SemanticFormStructure> formStructuresList = getAll();
		Map<String, SemanticFormStructure> formStructuresMap = new HashMap<String, SemanticFormStructure>();

		for (SemanticFormStructure form : formStructuresList) {
			formStructuresMap.put(form.getShortNameAndVersion(), form);
		}

		return formStructuresMap;
	}

	/**
	 * @inheritDoc
	 */
	public List<SemanticFormStructure> getAll() {

		Map<String, SemanticFormStructure> formStructures = getAllBasicUriObjectMap();
		formStructures = loadNestedFields(formStructures, false); // load diseases
		return new ArrayList<SemanticFormStructure>(formStructures.values());
	}

	@Override
	public SemanticFormStructure get(String uri) {

		Query query = QueryConstructionUtil.getBasicFormStructureQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

		if (block == null) {
			block = new ElementTriplesBlock();
			body.addElement(block);
		}
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFS.Nodes.isDefinedBy, NodeFactory.createURI(uri)));

		ResultSet results = querySelect(query);

		SemanticFormStructure formStructure = parseGet(results);
		return formStructure;
	}

	public SemanticFormStructure save(SemanticFormStructure form) {

		return saveOverwrite(form, null, null);
	}

	@Override
	public SemanticFormStructure saveOverwrite(SemanticFormStructure form, String oldName, String oldVersion) {

		// create node + version
		Node baseUri = NodeFactory.createURI(RDFConstants.FORM_STRUCTURE + "/" + form.getShortName());
		Node uri = NodeFactory
				.createURI(QueryConstructionUtil.createFormStructureUri(form.getShortName(), form.getVersion()));

		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		Resource resource = model.createResource(uri.getURI());

		// add all FS specific attributes
		Property predicate = model.createProperty(RDFConstants.FORM_STRUCTURE.concat("#disease"));
		for (Disease disease : form.getDiseases()) {
			resource.addLiteral(predicate, disease.getName());
		}

		// add all BRICS attributes
		model.add(model.createResource(baseUri.getURI()), model.createProperty(RDFConstants.BRICS.concat("#latest")),
				resource);
		model.add(resource, model.createProperty(RDFConstants.BRICS.concat("#description")), form.getDescription());
		model.add(resource, model.createProperty(RDFConstants.BRICS.concat("#title")), form.getTitle());
		model.add(resource, model.createProperty(RDFConstants.BRICS.concat("#version")),
				model.createLiteral(form.getVersion()));
		model.add(resource, model.createProperty(RDFConstants.BRICS.concat("#shortName")), form.getShortName());
		model.add(resource, model.createProperty(RDFConstants.BRICS.concat("#status")), form.getStatus().getType());
		model.add(resource, model.createProperty(RDFConstants.BRICS.concat("#type")),
				model.createResource(baseUri.getURI()));
		// TODO: change this back to storing .getType of submission type
		model.add(resource, model.createProperty(RDFConstants.PROPERTY_FS_SUBMISSION_TYPE_N),
				form.getSubmissionType().toString());

		Resource standardizationResource =
				model.createResource(RDFConstants.FS_STANDARDIZATION_URI + form.getStandardization().name());
		model.add(resource, model.createProperty(RDFConstants.PROPERTY_FS_STANDARDIZATION_TYPE_N),
				standardizationResource);
		model.add(standardizationResource, RDFS.label, form.getStandardization().getName());
		model.add(resource, model.createProperty(RDFConstants.PROPERTY_FS_IS_COPYRIGHTED_N),
				form.getIsCopyrighted().toString());

		if (form.getInstancesRequiredFor() != null) {
			for (InstanceRequiredFor requiredInstance : form.getInstancesRequiredFor()) {

				String requiredInstanceName = requiredInstance.getName();

				if (!requiredInstanceName.isEmpty()) {
					Resource requiredResource =
							model.createResource(RDFConstants.FS_REQUIRED_URI + requiredInstanceName);
					model.add(resource, model.createProperty(RDFConstants.PROPERTY_FS_REQUIRED_TYPE_N),
							requiredResource);
					model.add(requiredResource, RDFS.label, requiredInstanceName);
				}
			}
		}
		
		if (form.getFormLabels() != null) {
			for (FormLabel formLabel : form.getFormLabels()) {
				Resource labelResource = model.createResource(RDFConstants.FS_LABEL_URI + formLabel.getId());
				model.add(resource, model.createProperty(RDFConstants.PROPERTY_FS_LABEL_N), labelResource);
				model.add(labelResource, RDFS.label, formLabel.getLabel());
				model.add(labelResource, model.createProperty(RDFConstants.FS_LABEL.concat("#id")),
						formLabel.getId().toString());
			}
		}

		model.add(resource, model.createProperty(RDF.type.getURI()), model.createResource(RDFConstants.FORM_STRUCTURE));
		model.add(resource, model.createProperty(RDFConstants.PROPERTY_FS_ORGANIZATION_N), form.getOrganization());
		model.add(resource, model.createProperty(RDFConstants.PROPERTY_FS_CREATED_BY_N), form.getCreatedBy());

		form.setModifiedDate(new Date());

		if (form.getDateCreated() == null) {
			form.setDateCreated(new Date());
		}

		String modifiedDateString = BRICSTimeDateUtil.dateToZuluTime(form.getModifiedDate());
		String dateCreatedString = BRICSTimeDateUtil.dateToZuluTime(form.getDateCreated());

		model.add(resource, model.createProperty(RDFConstants.PROPERTY_FS_DATE_CREATED_N),
				ResourceFactory.createTypedLiteral(dateCreatedString, XSDDatatype.XSDdateTime));
		logger.info("Saving date created: " + dateCreatedString);
		model.add(resource, model.createProperty(RDFConstants.PROPERTY_FS_MODIFIED_DATE_N),
				ResourceFactory.createTypedLiteral(modifiedDateString, XSDDatatype.XSDdateTime));
		logger.info("Saving modified date: " + modifiedDateString);

		if (oldName == null && oldVersion == null) {
			remove(form.getShortName(), form.getVersion());
		} else {
			remove(oldName, oldVersion);
		}

		updateLatestVersion(form.getShortName(), form.getVersion());
		storeObject(model);
		model.close();

		return form;
	}

	private void updateLatestVersion(String shortName, String version) {

		String baseUri = RDFConstants.FORM_STRUCTURE + "/" + shortName;
		String uri = RDFConstants.FORM_STRUCTURE + "/" + shortName + "/" + version;
		String latest = RDFConstants.BRICS.concat("#latest");

		String sparqlUpdate = "WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE { <" + baseUri + "> <" + latest
				+ "> ?version } INSERT { <" + baseUri + "> <" + latest + "> <" + uri + "> } WHERE { <" + baseUri + "> <"
				+ latest + "> ?version }";

		update(sparqlUpdate);
	}

	private SemanticFormStructure parseGet(ResultSet results) {

		SemanticFormStructure formStructure = null;

		if (results.hasNext()) {
			QuerySolution row = results.next();
			formStructure = parseBasicFields(row);
			formStructure.getDiseases().add(parseDiseaseField(row));
		}

		if (formStructure != null) {
			while (results.hasNext()) {
				QuerySolution row = results.next();
				formStructure.getDiseases().add(parseDiseaseField(row));
			}
		}

		return formStructure;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SemanticFormStructure get(String shortName, String version) {

		Query query = QueryConstructionUtil.getBasicFormStructureQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				NodeFactory.createLiteral(shortName)));
		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_VERSION_NODE_N,
				NodeFactory.createLiteral(version)));

		ResultSet results = querySelect(query);

		SemanticFormStructure formStructure = parseGet(results);

		return formStructure;
	}

	private Query addLatestTriples(Query query) {
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_TYPE_NODE_N,
				RDFConstants.BASE_URI_VARIABLE));
		block.addTriple(Triple.create(RDFConstants.BASE_URI_VARIABLE, RDFConstants.PROPERTY_BRICS_LATEST_NODE_N,
				RDFConstants.URI_NODE));

		return query;
	}

	public List<SemanticFormStructure> getLatestByNames(List<String> names) {
		Query query = QueryConstructionUtil.getBasicFormStructureQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();

		body.addElementFilter(QueryConstructionUtil.isOneOfUri(RDFConstants.SHORT_NAME_VARIABLE.getName(), names));
		query = addLatestTriples(query); // we want to only get the latest form structures

		ResultSet results = querySelect(query);

		List<SemanticFormStructure> parsedResults = new ArrayList<SemanticFormStructure>(resultsToObjectMap(results).values());
		return parsedResults;
	}

	@Override
	public SemanticFormStructure getLatest(String shortName) {

		Query query = QueryConstructionUtil.getBasicFormStructureQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		ElementTriplesBlock block = (ElementTriplesBlock) body.getElements().get(0);

		block.addTriple(Triple.create(RDFConstants.URI_NODE, RDFConstants.PROPERTY_BRICS_SHORT_NAME_NODE_N,
				NodeFactory.createLiteral(shortName)));
		query = addLatestTriples(query);

		ResultSet results = querySelect(query);

		SemanticFormStructure formStructure = parseGet(results);

		return formStructure;
	}

	/**
	 * Returns a list of form structures by their short name and versions
	 */
	public List<SemanticFormStructure> getByShortNameAndVersions(List<NameAndVersion> nameAndVersions) {

		Query query = QueryConstructionUtil.getBasicFormStructureQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		/*
		 * body.addElementFilter(QueryConstructionUtil.getNameVersionFilter(RDFConstants.SHORT_NAME_VARIABLE,
		 * RDFConstants.VERSION_VARIABLE, nameAndVersions));
		 */
		body.addElementFilter(generateNameVersionFilter(RDFConstants.URI_VARIABLE_NAME, nameAndVersions));
		ResultSet results = querySelect(query);

		Map<String, SemanticFormStructure> formStructures = new HashMap<String, SemanticFormStructure>();

		while (results.hasNext()) {
			QuerySolution row = results.next();
			SemanticFormStructure fs = parseBasicFields(row);
			formStructures.put(fs.getShortNameAndVersion(), fs);
		}

		formStructures = loadNestedFields(formStructures, true);

		return new ArrayList<SemanticFormStructure>(formStructures.values());
	}

	private ElementFilter generateNameVersionFilter(String uri_var, List<NameAndVersion> nameAndVersions) {

		Collection<String> uris = new HashSet<String>();
		for (NameAndVersion item : nameAndVersions) {
			uris.add(RDFConstants.FORM_STRUCTURE_NS + item.getName() + "/" + item.getVersion());
		}
		return QueryConstructionUtil.isOneOfUri(uri_var, uris);
	}

	public Map<String, SemanticFormStructure> getShortNameAndVersionsMap(List<NameAndVersion> nameAndVersions) {

		Map<String, SemanticFormStructure> shortNameAndVersionsMap = new HashMap<String, SemanticFormStructure>();
		for (SemanticFormStructure fs : getByShortNameAndVersions(nameAndVersions)) {
			shortNameAndVersionsMap.put(fs.getShortNameAndVersion(), fs);
		}

		return shortNameAndVersionsMap;
	}

	private Map<String, SemanticFormStructure> loadNestedFields(Map<String, SemanticFormStructure> formStructures,
			boolean needUriFilter) {

		Query query = QueryConstructionUtil.getFormDiseaseQuery();

		if (needUriFilter) {
			Set<String> uris = formStructures.keySet();
			ElementGroup body = (ElementGroup) query.getQueryPattern();
			body.addElementFilter(QueryConstructionUtil.isOneOfUri(RDFConstants.URI_VARIABLE_NAME, uris));
		}

		ResultSet diseaseResults = querySelect(query);

		while (diseaseResults.hasNext()) {
			QuerySolution row = diseaseResults.next();
			String uri = row.get(RDFConstants.URI_VARIABLE_NAME).toString();

			SemanticFormStructure formStructure = formStructures.get(uri);

			if (formStructure != null) {
				formStructure.getDiseases().add(parseDiseaseField(row));
			}
		}

		return formStructures;
	}

	@Override
	public void remove(String shortName, String version) {

		String baseUri = RDFConstants.FORM_STRUCTURE + "/" + shortName;
		String uri = baseUri + "/" + version;

		String sparqlUpdate = "WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE { <" + uri + "> ?p ?o } WHERE { <"
				+ uri + "> ?p ?o }";

		update(sparqlUpdate);

		// need to remove the "base" node if we're removing the "latest" form and reset it in that situation
		// The current system does not change the version numbers unless it's published - and you can't delete
		// a published element, so for now we just remove it entirely.
		String sparqlRemove = "WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE { <" + baseUri
				+ "> ?p ?o } WHERE { <" + baseUri + "> ?p ?o }";
		update(sparqlRemove);
	}

	@Override
	public void removeFormLabel(Long formLabelId) {
		String sparqlRemove = "WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE {" +
				"?form <" + RDFConstants.PROPERTY_FS_LABEL_N + "> ?label . " +
				"?label ?labelP ?labelO . } WHERE {" +
				"?form a <" + RDFConstants.FORM_STRUCTURE + "> . " +
				"?form <" + RDFConstants.PROPERTY_FS_LABEL_N + "> ?label . " +
				"?label <" + RDFConstants.FS_LABEL.concat("#id") + "> \"" + formLabelId + "\" . " +
				"?label ?labelP ?labelO . }";
		
		update(sparqlRemove);
	}

	@Override
	public void updateFormLabel(FormLabel formLabel, String newLabel) {
		String sparqlUpdate = "WITH <http://ninds.nih.gov:8080/allTriples.ttl> DELETE { " +
				"?label rdfs:label ?labelName . } INSERT { " + 
				"?label rdfs:label \"" + newLabel + "\" . } WHERE { " +
				"?label <" + RDFConstants.FS_LABEL.concat("#id") + "> \"" + formLabel.getId() + "\" . " +
				"?label rdfs:label ?labelName . }";
		
		update(sparqlUpdate);
	}
	
	public List<SemanticFormStructure> search(Map<FormStructureFacet, Set<String>> selectedFacets,
			Set<String> searchTerms, boolean exactMatch, PaginationData pageData, boolean onlyOwned) {

		Query query = SearchFormStructureQueryConstructionUtil.buildFormStructureSearchQuery(selectedFacets,
				searchTerms, exactMatch, pageData, onlyOwned);
		ResultSet rs = querySelect(query);

		// need to use linkedhashmap implementation to preserve ordering. sorting will not work with hashmap.
		Map<String, SemanticFormStructure> resultsMap = new LinkedHashMap<String, SemanticFormStructure>();

		while (rs.hasNext()) {
			QuerySolution row = rs.next();

			/**
			 * We parse the URI and the Disease here. The URI will be used to store the item in the map and the disease
			 * will be added to any previously parsed items. The parseBasicFields() method will automatically parse the
			 * first disease.
			 */
			String uri = row.get(RDFConstants.URI_VARIABLE_NAME).toString();
			String disease = rdfNodeToString(row.get(RDFConstants.DISEASE_VARIABLE.getName()));

			/**
			 * Since disease causes a lot of duplicated information, we only check for disease if the uri has already
			 * been added.
			 */
			if (resultsMap.containsKey(uri)) {
				SemanticFormStructure item = resultsMap.get(uri);
				item.getDiseases().add(new Disease(disease, true));
				resultsMap.put(uri, item);
			} else {
				SemanticFormStructure item = parseBasicFields(row);
				resultsMap.put(uri, item);
			}
		}

		/**
		 * This requires a second SPARQL call. The call that actually retrieves the information is limited in scope. The
		 * count requires the full result set.
		 */
		Query queryCount = SearchFormStructureQueryConstructionUtil.buildFormStructureSearchCountQuery(selectedFacets,
				searchTerms, exactMatch, onlyOwned);
		ResultSet rsCount = querySelect(queryCount);

		if (pageData != null) {
			while (rsCount.hasNext()) {
				QuerySolution row = rsCount.next();
				int count = row.get(RDFConstants.COUNT_VARIABLE.getVarName()).asLiteral().getInt();
				pageData.setNumSearchResults(count);
			}
		}

		List<SemanticFormStructure> results = new ArrayList<SemanticFormStructure>(resultsMap.values());
		return results;
	}

	/**
	 * @inheritDoc
	 */
	public int searchCount(Map<FormStructureFacet, Set<String>> selectedFacets, Set<String> searchTerms,
			boolean exactMatch, boolean onlyOwned) {

		Query queryCount = SearchFormStructureQueryConstructionUtil.buildFormStructureSearchCountQuery(selectedFacets,
				searchTerms, exactMatch, onlyOwned);
		ResultSet rsCount = querySelect(queryCount);

		if (rsCount.hasNext()) {
			QuerySolution row = rsCount.next();
			return row.get(RDFConstants.COUNT_VARIABLE.getVarName()).asLiteral().getInt();
		} else {
			return 0;
		}
	}

	public List<SemanticFormStructure> getByStatuses(Set<StatusType> statuses) {

		List<SemanticFormStructure> formStructures = new ArrayList<SemanticFormStructure>();

		if (statuses == null || statuses.isEmpty()) {
			return formStructures;
		}

		List<String> statusNames = new ArrayList<String>();
		for (StatusType status : statuses) {
			statusNames.add(status.getType());
		}
		Query query = QueryConstructionUtil.getBasicFormStructureQuery();
		ElementGroup body = (ElementGroup) query.getQueryPattern();
		body.addElement(QueryConstructionUtil.isOneOfIgnoreCase(RDFConstants.STATUS_VARIABLE.getName(), statusNames));
		ResultSet results = querySelect(query);

		while (results.hasNext()) {
			QuerySolution row = results.next();
			SemanticFormStructure currentFormStructure = parseBasicFields(row);
			formStructures.add(currentFormStructure);
		}

		return formStructures;
	}

	private Map<String, SemanticFormStructure> resultsToObjectMap(ResultSet results) {
		Map<String, SemanticFormStructure> formStructureMap = new HashMap<String, SemanticFormStructure>();
		while (results.hasNext()) {
			SemanticFormStructure newForm = parseBasicFields(results.next());
			if (formStructureMap.containsKey(newForm.getUri())) {
				newForm = mergeFormStructures(formStructureMap.get(newForm.getUri()), newForm);
			}
			formStructureMap.put(newForm.getUri(), newForm);
		}

		return formStructureMap;
	}
}
