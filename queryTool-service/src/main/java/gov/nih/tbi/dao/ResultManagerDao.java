package gov.nih.tbi.dao;

import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.Facet;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.QueryResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.pojo.ResultType;

import java.util.List;

import com.hp.hpl.jena.query.ResultSet;

public interface ResultManagerDao {

	public QueryResult getResultsForResultType(ResultType resultType);

	public QueryResult getCachingDetailsQuery(QueryResult uriSet, List<BeanField> beanFields, ResultType resultType);

	public QueryResult searchFormsByText(String text);

	public QueryResult searchStudiesByText(String text);


	public QueryResult searchDeForms(String text, List<String> selectedDeUris);

	/**
	 * Load all data element items and their count in form results.
	 * 
	 * @param facet
	 * @param addFilter
	 * @return
	 */
	public QueryResult getDeFacetDetails(Facet deFacet, boolean addFilter);

	/**
	 * Generates the query necessary to get the study details of studies that are in forms
	 * 
	 * @param forms
	 * @return
	 */
	public QueryResult getFormDetailsInStudies(List<StudyResult> studies);

	public QueryResult getStudyDetailsInForms(List<FormResult> forms);

	public QueryResult searchFormsByDeNames(List<String> deNames);

	public QueryResult getSeeAlso(List<String> deNames);

	public QueryResult getDataElementToFormStructure(List<String> deNames);

	public QueryResult getDeTitles(List<String> deNames);

	/**
	 * Returns a result set containing the uris of studies with the given prefixed IDS
	 * 
	 * @param prefixedIds - prefixed IDs of the study
	 * @return
	 */
	public QueryResult searchStudiesByPrefixedIds(List<String> prefixedIds);

	public QueryResult searchStudyByFormNames(List<String> formNames);

	/**
	 * Returns a result set containing the uris of form structures that are associated with studies with the given
	 * prefixed IDs
	 * 
	 * @param prefixedIds
	 * @return
	 */
	public QueryResult searchFormsByPrefixedIds(List<String> prefixedIds);

	public QueryResult getFormByShortName(String name);
}
