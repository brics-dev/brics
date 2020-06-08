package gov.nih.tbi.dao.impl;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDFS;

import gov.nih.tbi.commons.util.SparqlConstructionUtil;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dao.ResultManagerDao;
import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.Facet;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.QueryResult;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.semantic.model.DataElementRDF;
import gov.nih.tbi.semantic.model.E_Distinct;
import gov.nih.tbi.semantic.model.FormStructureRDF;
import gov.nih.tbi.semantic.model.StudyRDF;
import gov.nih.tbi.service.RDFStoreManager;
import gov.nih.tbi.util.InstancedDataUtil;

@Repository
@Transactional
public class ResultManagerDaoImpl implements ResultManagerDao, Serializable {

	private static final long serialVersionUID = 6999452529415924840L;
	private static final Logger log = LogManager.getLogger(ResultManagerDaoImpl.class.getName());

	private final String getPropertyValuesForClassQuery = "SELECT ?o (COUNT(?o) AS ?n) ?title ?label " + "WHERE { "
			+ "?r1 rdfs:subClassOf CLASSURI_REPLACE . " + "?r1 PROPERTYURI_REPLACE ?o . ";

	private final String groupByQuery = "FILTER(?o!=\"\" && (!isBlank(?o) || bound(?label)) ) . "
			+ "OPTIONAL{ ?o rdfs:label ?label . FILTER(LANG(?label)='en' || LANG(?label)='')} "
			+ "OPTIONAL{ ?o ?titleProp ?title . FILTER regex(str(?titleProp), \"title$\")} "
			+ "} GROUP BY ?o ?label ?title ORDER BY ASC(UCASE(?title)) ";

	@Autowired
	RDFStoreManager rdfStoreManager;

	public ResultManagerDaoImpl() {
	}

	public QueryResult getResultsForResultType(ResultType resultType) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT ?uri").append(QueryToolConstants.NL).append("WHERE {\n").append("?uri ")
				.append(QueryToolConstants.SUBCLASSOF).append(" <").append(resultType.getUri()).append("> .\n")
				.append("}");

		String query = sb.toString();

		log.debug("ResultManagerDao - getFormResultsForResultType query:");
		log.debug(query);
		return rdfStoreManager.querySelect(query);
	}

	public QueryResult getCachingDetailsQuery(QueryResult uriSet, List<BeanField> beanFields, ResultType resultType) {

		if (!uriSet.hasData()) { // no results, don't need to build query
			return null;
		}

		// Generates the rest of the 'where' query for getting form details
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT *").append(QueryToolConstants.NL).append("WHERE {\n").append("?uri ")
				.append("rdfs:subClassOf <").append(resultType.getUri()).append("> .\n");

		for (BeanField currentField : beanFields) {
			sb.append("OPTIONAL { ").append("?").append("uri ").append(currentField.getPropertyUri())
					.append(QueryToolConstants.WS).append("?").append(currentField.getName()).append(" .").append(" }")
					.append(QueryToolConstants.NL);
		}

		sb.append("}");

		return rdfStoreManager.querySelect(sb.toString());
	}

	public QueryResult searchFormsByText(String text) {

		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT ?uri").append(QueryToolConstants.NL).append("WHERE {\n").append("?uri ")
				.append(QueryToolConstants.SUBCLASSOF).append(" <").append(QueryToolConstants.FORM_STRUCTURE_URI)
				.append("> .\n");

		// Adds the search criteria
		String[] elements = QueryToolConstants.TEXT_SEARCH_FORM_PREDICATE_VALUE.split("\\^");
		sb.append(generateSearchCriteriaString(text, elements));
		sb.append("}");

		log.debug("ResultManagerDao - searchFormsByText query:");
		log.debug(sb.toString());

		return rdfStoreManager.querySelect(sb.toString());
	}

	public QueryResult searchStudiesByText(String text) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT ?uri").append(QueryToolConstants.NL).append("WHERE {\n").append("?uri ")
				.append(QueryToolConstants.SUBCLASSOF).append(" <").append(QueryToolConstants.STUDY_URI)
				.append("> .\n");

		// Adds the search criteria
		String[] elements = QueryToolConstants.TEXT_SEARCH_STUDY_PREDICATE_VALUE.split("\\^");
		sb.append(generateSearchCriteriaString(text, elements));
		sb.append("}");

		log.debug("ResultManagerDao - searchStudiesByText query:");
		log.debug(sb.toString());

		return rdfStoreManager.querySelect(sb.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryResult searchStudiesByPrefixedIds(List<String> ids) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.addResultVar(QueryToolConstants.URI_VAR);

		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		query.setQueryPattern(body);
		body.addElement(block);

		block.addTriple(Triple.create(QueryToolConstants.URI_VAR, RDFS.subClassOf.asNode(),
				NodeFactory.createURI(QueryToolConstants.STUDY_URI)));
		block.addTriple(Triple.create(QueryToolConstants.URI_VAR, StudyRDF.PROPERTY_PREFIXED_ID.asNode(),
				QueryToolConstants.PREFIXED_ID_VAR));
		ElementFilter filter = SparqlConstructionUtil.isOneOfUri(QueryToolConstants.PREFIXED_ID_VAR.getName(), ids);
		body.addElement(filter);
		return rdfStoreManager.querySelect(query);
	}

	public QueryResult searchDeForms(String text, List<String> selectedDeUris) {

		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT ?uri").append(QueryToolConstants.NL).append("WHERE {\n").append("?uri ")
				.append(QueryToolConstants.SUBCLASSOF).append(" <").append(QueryToolConstants.FORM_STRUCTURE_URI)
				.append("> .\n");

		// Add the selected DE criteria
		if (selectedDeUris != null && !selectedDeUris.isEmpty()) {
			sb.append("{ \n");
			int i = 0;

			for (String deUri : selectedDeUris) {
				if (i > 0) {
					sb.append(" UNION \n");
				}
				sb.append(" { ?uri").append(QueryToolConstants.deFacetConfig[1]).append("<").append(deUri)
						.append("> } \n");
				i++;
			}
			sb.append("} . \n");
		}

		// Adds the search text criteria
		String[] elements = QueryToolConstants.TEXT_SEARCH_FORM_PREDICATE_VALUE.split("\\^");
		sb.append(generateSearchCriteriaString(text, elements));
		sb.append("}");

		log.debug("ResultManagerDao - searchDeForms query:");
		log.debug(sb.toString());

		return rdfStoreManager.querySelect(sb.toString());
	}

	/**
	 * Load all data element items and their count in form results.
	 * 
	 * @param facet
	 * @param addFilter
	 * @return
	 */
	public QueryResult getDeFacetDetails(Facet deFacet, boolean addFilter) {

		String query = getPropertyValuesForClassQuery;

		if (addFilter) {
			query += deFacet.getFilterQuery();
		}
		query += groupByQuery;

		query = query.replaceAll("CLASSURI_REPLACE", deFacet.getClassURI());
		query = query.replaceAll("PROPERTYURI_REPLACE", deFacet.getPropertyURI());

		log.debug("ResultManagerDao - getDeFacetDetails query:" + query);

		return rdfStoreManager.querySelect(query);
	}

	/**
	 * Generates the query necessary to get the study details of studies that are in
	 * forms
	 * 
	 * @param forms
	 * @return
	 */
	public QueryResult getFormDetailsInStudies(List<StudyResult> studies) {

		StringBuffer sb = new StringBuffer();

		if (studies.isEmpty()) {
			return null;
		}

		// TODO:URICONSTANTS NEED TO MOVE THESE URI's to CONSTNATS FILE
		// Also need to make this dynamic and not randomly harcoded here
		sb.append("SELECT DISTINCT *").append(QueryToolConstants.NL).append("WHERE {").append(QueryToolConstants.NL)
				.append("?study study:facetedForm ?uri .").append(QueryToolConstants.NL);

		for (BeanField field : QueryToolConstants.FORM_FIELDS) // append every pattern except for the uri and the
																// faceted studies field
		{
			if (!"uri".equals(field.getName()) && !"studies".equals(field.getName())) {
				sb.append("OPTIONAL { ?uri ").append(field.getPropertyUri()).append(QueryToolConstants.WS).append("?")
						.append(field.getName()).append(" . }").append(QueryToolConstants.NL);
			}
		}

		sb.append("}");
		return rdfStoreManager.querySelect(sb.toString());
	}

	public QueryResult getStudyDetailsInForms(List<FormResult> forms) {

		StringBuffer sb = new StringBuffer();
		if (forms.isEmpty()) {
			return null;
		}

		// TODO:URICONSTANTS NEED TO MOVE THESE URI's to CONSTNATS FILE
		sb.append("SELECT DISTINCT *").append(QueryToolConstants.NL).append("WHERE {").append(QueryToolConstants.NL)
				.append("?form study:facetedStudy ?uri .").append(QueryToolConstants.NL);

		for (BeanField field : QueryToolConstants.STUDY_FIELDS) {
			// append every pattern except for the uri and the faceted studies field
			if (!"uri".equals(field.getName()) && !"forms".equals(field.getName())) {
				sb.append("OPTIONAL { ?uri ").append(field.getPropertyUri()).append(QueryToolConstants.WS).append("?")
						.append(field.getName()).append(" . }").append(QueryToolConstants.NL);
			}
		}

		sb.append("}");
		log.debug("ResultManagerDao - getStudyDetailsInForms query:");
		log.debug(sb.toString());
		return rdfStoreManager.querySelect(sb.toString());
	}

	private String generateSearchCriteriaString(String textValue, String[] elements) {

		if (textValue == null || textValue.isEmpty()) {
			return "";
		}

		StringBuffer sb = new StringBuffer();
		int index = 1;

		// add the filter to the variable ?r1 since all the queries are being done using
		// this variable as the subject
		for (String element : elements) {
			if (index != 1) {
				sb.append(QueryToolConstants.NL + "union" + QueryToolConstants.NL);
			}
			sb.append(QueryToolConstants.NL + "{" + QueryToolConstants.NL);
			sb.append(QueryToolConstants.NL).append("?uri").append(" <" + element + "> ")
					.append("?searchVar" + index + " .");
			sb.append(QueryToolConstants.NL).append("FILTER (CONTAINS(LCASE(").append("?searchVar" + (index++) + "), ")
					.append("\"" + textValue.toLowerCase() + "\"").append(")).");
			sb.append(QueryToolConstants.NL + "}" + QueryToolConstants.NL);
		}
		return sb.toString();
	}

	@Override
	public QueryResult searchFormsByDeNames(List<String> deNames) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.addResultVar(QueryToolConstants.URI_VAR);
		block.addTriple(Triple.create(QueryToolConstants.URI_VAR, RDFS.subClassOf.asNode(),
				NodeFactory.createURI(QueryToolConstants.FORM_STRUCTURE_URI)));
		block.addTriple(Triple.create(QueryToolConstants.DATA_ELEMENT_VARIABLE,
				QueryToolConstants.DATA_ELEMENT_PROP_NAME_N, QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.URI_VAR, QueryToolConstants.FACETED_DE_N,
				QueryToolConstants.DATA_ELEMENT_VARIABLE));
		body.addElementFilter(new ElementFilter(
				InstancedDataUtil.isOneOfString(new ExprVar(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE), deNames)));
		return rdfStoreManager.querySelect(query);
	}

	@Override
	public QueryResult getSeeAlso(List<String> deNames) {

		if (deNames == null || deNames.isEmpty()) {
			return null;
		}

		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.addResultVar(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE);
		query.addResultVar(QueryToolConstants.SEE_ALSO_VAR,
				new ExprAggregator(QueryToolConstants.SEE_ALSO_VAR, AggregatorFactory.createGroupConcat(false,
						new E_Distinct(new ExprVar(QueryToolConstants.SEE_ALSO_VAR)), ",", null)));
		block.addTriple(Triple.create(QueryToolConstants.DATA_ELEMENT_VARIABLE,
				DataElementRDF.PROPERTY_ELEMENT_NAME.asNode(), QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.DATA_ELEMENT_VARIABLE,
				DataElementRDF.PROPERTY_SEE_ALSO.asNode(), QueryToolConstants.SEE_ALSO_VAR));
		ElementFilter deNameFilter = new ElementFilter(
				InstancedDataUtil.isOneOfString(new ExprVar(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE), deNames));
		body.addElement(deNameFilter);
		return rdfStoreManager.querySelect(query);
	}

	@Override
	public QueryResult getDataElementToFormStructure(List<String> deNames) {
		if (deNames == null || deNames.isEmpty()) {
			return null;
		}

		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.addResultVar(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE);
		query.addResultVar(QueryToolConstants.FS_URI_VAR,
				new ExprAggregator(QueryToolConstants.FS_URI_VAR, AggregatorFactory.createGroupConcat(false,
						new E_Distinct(new ExprVar(QueryToolConstants.FS_URI_VAR)), ",", null)));
		block.addTriple(Triple.create(QueryToolConstants.DATA_ELEMENT_VARIABLE,
				DataElementRDF.PROPERTY_ELEMENT_NAME.asNode(), QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.DATA_ELEMENT_VARIABLE,
				StudyRDF.RELATION_PROPERTY_FACETED_FORM.asNode(), QueryToolConstants.FS_URI_VAR));
		ElementFilter deNameFilter = new ElementFilter(
				InstancedDataUtil.isOneOfString(new ExprVar(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE), deNames));
		body.addElement(deNameFilter);
		return rdfStoreManager.querySelect(query);
	}

	@Override
	public QueryResult getDeTitles(List<String> deNames) {
		if (deNames == null || deNames.isEmpty()) {
			return null;
		}

		Query query = QueryFactory.make();
		query.setQuerySelectType();
		ElementGroup body = new ElementGroup();
		query.setQueryPattern(body);
		ElementTriplesBlock block = new ElementTriplesBlock();
		body.addElement(block);
		query.addResultVar(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE);
		query.addResultVar(QueryToolConstants.DE_TITLE_VARIABLE);
		block.addTriple(Triple.create(QueryToolConstants.DATA_ELEMENT_VARIABLE,
				DataElementRDF.PROPERTY_ELEMENT_NAME.asNode(), QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE));
		block.addTriple(Triple.create(QueryToolConstants.DATA_ELEMENT_VARIABLE, DataElementRDF.PROPERTY_TITLE.asNode(),
				QueryToolConstants.DE_TITLE_VARIABLE));
		ElementFilter deNameFilter = new ElementFilter(
				InstancedDataUtil.isOneOfString(new ExprVar(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE), deNames));
		body.addElement(deNameFilter);
		return rdfStoreManager.querySelect(query);
	}

	@Override
	public QueryResult searchStudyByFormNames(List<String> formNames) {
		if (formNames == null || formNames.isEmpty()) {
			return null;
		}

		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.addResultVar(QueryToolConstants.URI_VAR);

		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		query.setQueryPattern(body);
		body.addElement(block);
		block.addTriple(Triple.create(QueryToolConstants.URI_VAR, RDFS.subClassOf.asNode(),
				NodeFactory.createURI(QueryToolConstants.STUDY_URI)));
		block.addTriple(Triple.create(QueryToolConstants.URI_VAR, StudyRDF.RELATION_PROPERTY_FACETED_FORM.asNode(),
				QueryToolConstants.FORM_VAR));
		block.addTriple(Triple.create(QueryToolConstants.FORM_VAR, FormStructureRDF.PROPERTY_SHORT_NAME.asNode(),
				QueryToolConstants.FORM_NAME_VAR));
		ElementFilter filter = SparqlConstructionUtil.isOneOfUri(QueryToolConstants.FORM_NAME_VAR.getName(), formNames);
		body.addElement(filter);
		return rdfStoreManager.querySelect(query);
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryResult searchFormsByPrefixedIds(List<String> prefixedIds) {
		if (prefixedIds == null || prefixedIds.isEmpty()) {
			return null;
		}
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.addResultVar(QueryToolConstants.PREFIXED_ID_VAR);
		query.addResultVar(QueryToolConstants.FS_URI_VAR,
				new ExprAggregator(QueryToolConstants.FS_URI_VAR, AggregatorFactory.createGroupConcat(false,
						new E_Distinct(new ExprVar(QueryToolConstants.FS_URI_VAR)), ",", null)));

		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		query.setQueryPattern(body);
		body.addElement(block);
		block.addTriple(Triple.create(QueryToolConstants.FS_URI_VAR, RDFS.subClassOf.asNode(),
				NodeFactory.createURI(QueryToolConstants.FORM_STRUCTURE_URI)));
		block.addTriple(Triple.create(QueryToolConstants.FS_URI_VAR, StudyRDF.RELATION_PROPERTY_FACETED_STUDY.asNode(),
				QueryToolConstants.STUDY_VAR));
		block.addTriple(Triple.create(QueryToolConstants.STUDY_VAR, StudyRDF.PROPERTY_PREFIXED_ID.asNode(),
				QueryToolConstants.PREFIXED_ID_VAR));
		ElementFilter filter = SparqlConstructionUtil.isOneOfUri(QueryToolConstants.PREFIXED_ID_VAR.getName(),
				prefixedIds);
		body.addElement(filter);

		return rdfStoreManager.querySelect(query);
	}

	@Override
	public QueryResult getFormByShortName(String name) {
		Query query = QueryFactory.make();
		query.setQuerySelectType();
		query.addResultVar(QueryToolConstants.URI_VAR);

		ElementGroup body = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		query.setQueryPattern(body);
		body.addElement(block);
		block.addTriple(Triple.create(QueryToolConstants.URI_VAR, RDFS.subClassOf.asNode(),
				NodeFactory.createURI(QueryToolConstants.FORM_STRUCTURE_URI)));
		block.addTriple(Triple.create(QueryToolConstants.URI_VAR, FormStructureRDF.PROPERTY_SHORT_NAME.asNode(),
				NodeFactory.createLiteral(name)));
		return rdfStoreManager.querySelect(query);
	}
}
