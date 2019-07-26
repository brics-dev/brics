package gov.nih.tbi.service;

import gov.nih.tbi.exceptions.ResultSetTranslationException;
import gov.nih.tbi.pojo.Facet;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;

import java.lang.reflect.InvocationTargetException;
import java.util.List;


/**
 * Stores the query for getting the result as well as facets, filters, and search keyword.
 * 
 * @author Francis Chen, Nimesh Patel
 * 
 */
public interface ResultManager{

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

	//public ResultSet getFacetPopulationDetails(Facet facet, boolean addFilter);

	public Facet getDeFacetDetails(boolean addFilter);

	public List<StudyResult> getStudyDetailsInfo(List<FormResult> forms)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException,
			InvocationTargetException, NoSuchMethodException;

	public List<FormResult> getFormDetailsInfo(List<StudyResult> studies)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, SecurityException,
			InvocationTargetException, NoSuchMethodException;

}
