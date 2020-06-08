package gov.nih.tbi.service;

import gov.nih.tbi.exceptions.ResultSetTranslationException;
import gov.nih.tbi.pojo.Facet;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;


/**
 * Stores the query for getting the result as well as facets, filters, and search keyword.
 * 
 * @author Francis Chen, Nimesh Patel
 * 
 */
public interface ResultManager {

	/**
	 * Runs the stored meta data query, parse the resultset, and get the detailed properties of each result object For
	 * ever object it gets back, it will insert it into the meta data cache maps
	 * 
	 * @throws ResultSetTranslationException
	 */
	public List<FormResult> runFormQueryForCaching() throws ResultSetTranslationException;

	/**
	 * Runs the stored meta data query, parse the resultset, and get the detailed properties of each result object For
	 * ever object it gets back, it will insert it into the meta data cache maps
	 * 
	 * @throws ResultSetTranslationException
	 */
	public List<StudyResult> runStudyQueryForCaching() throws ResultSetTranslationException;


	/**
	 * Queries the dictionary meta data using the in-memory meta data cache maps
	 */
	public List<FormResult> searchForms(String text);

	/**
	 * Queries the dictionary meta data using the in-memory meta data cache maps
	 */
	public List<StudyResult> searchStudies(String text);

	public List<FormResult> searchDeForms(String text, List<String> deUris);

	// public ResultSet getFacetPopulationDetails(Facet facet, boolean addFilter);

	public Facet getDeFacetDetails(boolean addFilter);

	public List<StudyResult> getStudyDetailsInfo(List<FormResult> forms)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException,
			InvocationTargetException, NoSuchMethodException;

	public List<FormResult> getFormDetailsInfo(List<StudyResult> studies)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException,
			InvocationTargetException, NoSuchMethodException;

	public Multimap<String, FormResult> searchFormsByDeNames(List<String> deNames);

	public Multimap<String, String> getSeeAlso(List<String> deNames);

	public Map<String, String> getDeTitles(List<String> deNames);

	/**
	 * Returns a list of studyResult objects with the given prefixedIds
	 * @param prefixedIds
	 * @return
	 */
	public List<StudyResult> getStudyByPrefixedIds(List<String> prefixedIds);
	
	/**
	 * Returns a list of studyResult objects that are associated with one of the given form structure short names
	 * @param formNames
	 * @return
	 */
	public List<StudyResult> searchStudyByFormNames(List<String> formNames);
	
	/**
	 * Returns a list of FormResult objects that are associated with one of the given study prefixed IDs
	 * @param prefixedIds - Prefixed IDs of the study
	 * @return
	 */
	public Multimap<String, FormResult> searchFormsByStudyPrefixedIds(List<String> prefixedIds);
	
	public FormResult getFormByShortName(String name);
}
