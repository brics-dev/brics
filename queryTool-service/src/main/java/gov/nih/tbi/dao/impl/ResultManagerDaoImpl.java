package gov.nih.tbi.dao.impl;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dao.ResultManagerDao;
import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.Facet;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.RDFStoreManager;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hp.hpl.jena.query.ResultSet;

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

	public ResultManagerDaoImpl() {}

	public ResultSet getResultsForResultType(ResultType resultType) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT ?uri").append(QueryToolConstants.NL).append("WHERE {\n").append("?uri ")
				.append(QueryToolConstants.SUBCLASSOF).append(" <").append(resultType.getUri()).append("> .\n")
				.append("}");

		String query = sb.toString();

		log.debug("ResultManagerDao - getFormResultsForResultType query:");
		log.debug(query);
		return rdfStoreManager.querySelect(query);
	}

	public ResultSet getCachingDetailsQuery(ResultSet uriSet, List<BeanField> beanFields, ResultType resultType) {

		if (!uriSet.hasNext()) { // no results, don't need to build query
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


	public ResultSet searchFormsByText(String text) {

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

	public ResultSet searchStudiesByText(String text) {
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


	public ResultSet searchDeForms(String text, List<String> selectedDeUris) {

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
				sb.append(" { ?uri").append(QueryToolConstants.deFacetConfig[1]).append("<").append(deUri).append("> } \n");
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
	public ResultSet getDeFacetDetails(Facet deFacet, boolean addFilter) {

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
	 * Generates the query necessary to get the study details of studies that are in forms
	 * 
	 * @param forms
	 * @return
	 */
	public ResultSet getFormDetailsInStudies(List<StudyResult> studies) {

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


	public ResultSet getStudyDetailsInForms(List<FormResult> forms) {

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

		// add the filter to the variable ?r1 since all the queries are being done using this variable as the subject
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

}
