package gov.nih.tbi.dao;

import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.Facet;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.pojo.StudyResult;

import java.util.List;

import com.hp.hpl.jena.query.ResultSet;

public interface ResultManagerDao {

	public ResultSet getResultsForResultType(ResultType resultType);

	public ResultSet getCachingDetailsQuery(ResultSet uriSet, List<BeanField> beanFields, ResultType resultType);

	public ResultSet searchFormsByText(String text) ;

	public ResultSet searchStudiesByText(String text);


	public ResultSet searchDeForms(String text, List<String> selectedDeUris);

	/**
	 * Load all data element items and their count in form results.
	 * 
	 * @param facet
	 * @param addFilter
	 * @return
	 */
	public ResultSet getDeFacetDetails(Facet deFacet, boolean addFilter);

	/**
	 * Generates the query necessary to get the study details of studies that are in forms
	 * 
	 * @param forms
	 * @return
	 */
	public ResultSet getFormDetailsInStudies(List<StudyResult> studies);

	public ResultSet getStudyDetailsInForms(List<FormResult> forms);

}
