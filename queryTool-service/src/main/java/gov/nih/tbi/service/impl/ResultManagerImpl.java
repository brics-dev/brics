package gov.nih.tbi.service.impl;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dao.ResultManagerDao;
import gov.nih.tbi.exceptions.ResultSetTranslationException;
import gov.nih.tbi.pojo.Facet;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.QueryAccountManager;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.model.MetaDataCache;
import gov.nih.tbi.util.ResultSetToFormStructure;
import gov.nih.tbi.util.ResultSetToStudy;
import gov.nih.tbi.util.SearchResultUtil;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


/**
 * Stores the query for getting the result as well as facets, filters, and search keyword.
 * 
 */
@Service
@Scope("singleton")
public class ResultManagerImpl implements ResultManager, Serializable {
	
	private static final long serialVersionUID = 5181012449461176357L;
	private static final Logger log = LogManager.getLogger(ResultManagerImpl.class);

	@Autowired
	private ResultManagerDao resultManagerDao;
	
	@Autowired
	private QueryAccountManager queryAccountManager;
	

	/**
	 * Runs the stored meta data query, parse the resultset, and get the detailed properties of each result object For
	 * ever object it gets back, it will insert it into the meta data cache maps
	 * 
	 * @throws ResultSetTranslationException
	 */
	public List<FormResult> runFormQueryForCaching() throws ResultSetTranslationException {

		ResultSet uriSet = resultManagerDao.getResultsForResultType(ResultType.FORM_STRUCTURE);
		ResultSetToFormStructure resultSetToForm = new ResultSetToFormStructure();
		List<FormResult> formResults = new LinkedList<FormResult>();

		if (uriSet.hasNext()) {
			ResultSet formResultSet =
					resultManagerDao.getCachingDetailsQuery(uriSet, QueryToolConstants.FORM_FIELDS,
							ResultType.FORM_STRUCTURE);

			try {
				formResults = new LinkedList<FormResult>(resultSetToForm.getBeans(formResultSet));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new ResultSetTranslationException(e.getMessage(), e);
			}
		}
		return formResults;
	}

	/**
	 * Runs the stored meta data query, parse the resultset, and get the detailed properties of each result object For
	 * ever object it gets back, it will insert it into the meta data cache maps
	 * 
	 * @throws ResultSetTranslationException
	 */
	public List<StudyResult> runStudyQueryForCaching() throws ResultSetTranslationException {

		ResultSet uriSet = resultManagerDao.getResultsForResultType(ResultType.STUDY);
		ResultSetToStudy resultSetToStudy = new ResultSetToStudy();
		List<StudyResult> studyResults = new LinkedList<StudyResult>();

		if (uriSet.hasNext()) {
			ResultSet studyResultSet =
					resultManagerDao.getCachingDetailsQuery(uriSet, QueryToolConstants.STUDY_FIELDS, ResultType.STUDY);

			try {
				studyResults = new LinkedList<StudyResult>(resultSetToStudy.getBeans(studyResultSet));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new ResultSetTranslationException(e.getMessage(), e);
			}
		}
		return studyResults;
	}


	/**
	 * Queries the dictionary meta data using the in-memory meta data cache maps
	 */
	public List<FormResult> searchForms(String text) {

		ResultSet uriSet = resultManagerDao.searchFormsByText(text);

		if (uriSet.hasNext()) { // pass back form result
			List<FormResult> formResults = getFormResultsFromCache(uriSet);
			return formResults;
		} else {
			return new LinkedList<FormResult>();
		}
	}

	/**
	 * Queries the dictionary meta data using the in-memory meta data cache maps
	 */
	public List<StudyResult> searchStudies(String text) {

		List<StudyResult> studyResults = new ArrayList<StudyResult>();
		ResultSet uriSet = resultManagerDao.searchStudiesByText(text);

		if (uriSet.hasNext()) {
			studyResults = getStudyResultsFromCache(uriSet);
		}
		return studyResults;
	}

	/*public ResultSet getFacetPopulationDetails(Facet facet, boolean addFilter) {
		return resultManagerDao.getDeFacetDetails(facet, addFilter);
	} */

	/**
	 * Return Data Element with list of DE facet items populated 
	 */
	public Facet getDeFacetDetails(boolean addFilter) {

		Facet deFacet = new Facet();
		deFacet.setHeadingLabel(QueryToolConstants.deFacetConfig[0]);
		deFacet.setPropertyURI(QueryToolConstants.deFacetConfig[1]);
		deFacet.setClassURI(QueryToolConstants.deFacetConfig[2]);

		ResultSet deResultSet = resultManagerDao.getDeFacetDetails(deFacet, addFilter);
		deFacet.populateItemsFromJena(deResultSet);

		return deFacet;
	}

	
	public List<FormResult> searchDeForms(String text, List<String> deUris) {
		ResultSet uriSet = resultManagerDao.searchDeForms(text, deUris);

		if (uriSet.hasNext()) { // pass back form result
			List<FormResult> formResults = getFormResultsFromCache(uriSet);
			return formResults;
		} else {
			return new LinkedList<FormResult>();
		}
	}
	
	public List<StudyResult> getStudyDetailsInfo(List<FormResult> forms) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {

		ResultSetToStudy resultSetToStudy = new ResultSetToStudy();
		ResultSet studySet = resultManagerDao.getStudyDetailsInForms(forms);

		return resultSetToStudy.getBeans(studySet);
	}

	public List<FormResult> getFormDetailsInfo(List<StudyResult> studies) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {

		ResultSetToFormStructure resultSetToForm = new ResultSetToFormStructure();
		ResultSet formSet = resultManagerDao.getFormDetailsInStudies(studies);

		return resultSetToForm.getBeans(formSet);
	}

	/**
	 * Uses the list of uri's from the result set and retrieve the list of objects from the cache
	 * 
	 * @param formResultSet
	 * @return
	 */
	private List<FormResult> getFormResultsFromCache(ResultSet formResultSet) {

		if (MetaDataCache.isResultCacheEmpty(ResultType.FORM_STRUCTURE)) {
			SearchResultUtil.cacheFormResults(this);
		}
		
		List<FormResult> formResultList = new LinkedList<FormResult>();

		while (formResultSet.hasNext()) {
			QuerySolution qs = formResultSet.next();
			if (qs.get("uri") != null) {
				String formUri = qs.get("uri").toString();
				formResultList.add(MetaDataCache.getFormResult(formUri));
			}
		}

		return formResultList;
	}

	/**
	 * Uses the list of uri's from the result set and retrieve the list of objects from the cache
	 * 
	 * @param studyResultSet
	 * @return
	 */
	private List<StudyResult> getStudyResultsFromCache(ResultSet studyResultSet) {

		if (MetaDataCache.isResultCacheEmpty(ResultType.STUDY)) {
			SearchResultUtil.cacheStudyResults(this);
		}
		
		List<StudyResult> studyResultList = new LinkedList<StudyResult>();

		while (studyResultSet.hasNext()) {
			QuerySolution qs = studyResultSet.next();
			if (qs.get("uri") != null) {
				String studyUri = qs.get("uri").toString();
				studyResultList.add(MetaDataCache.getStudyResult(studyUri));
			}
		}

		return studyResultList;
	}

}
