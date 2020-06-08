package gov.nih.tbi.service.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.QuerySolution;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dao.ResultManagerDao;
import gov.nih.tbi.exceptions.ResultSetTranslationException;
import gov.nih.tbi.pojo.Facet;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.QueryResult;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.model.MetaDataCache;
import gov.nih.tbi.util.InstancedDataUtil;
import gov.nih.tbi.util.ResultSetToFormStructure;
import gov.nih.tbi.util.ResultSetToStudy;
import gov.nih.tbi.util.SearchResultUtil;

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
	private MetaDataCache metaDataCache;

	/**
	 * Runs the stored meta data query, parse the resultset, and get the detailed properties of each result object For
	 * ever object it gets back, it will insert it into the meta data cache maps
	 * 
	 * @throws ResultSetTranslationException
	 */
	public List<FormResult> runFormQueryForCaching() throws ResultSetTranslationException {

		QueryResult uriSet = resultManagerDao.getResultsForResultType(ResultType.FORM_STRUCTURE);
		ResultSetToFormStructure resultSetToForm = new ResultSetToFormStructure();
		List<FormResult> formResults = new LinkedList<FormResult>();

		if (uriSet.hasData()) {
			QueryResult formResultSet = resultManagerDao.getCachingDetailsQuery(uriSet, QueryToolConstants.FORM_FIELDS,
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

		QueryResult uriSet = resultManagerDao.getResultsForResultType(ResultType.STUDY);
		ResultSetToStudy resultSetToStudy = new ResultSetToStudy();
		List<StudyResult> studyResults = new LinkedList<StudyResult>();

		if (uriSet.hasData()) {
			QueryResult studyResultSet =
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

		QueryResult uriSet = resultManagerDao.searchFormsByText(text);

		if (uriSet.hasData()) { // pass back form result
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
		QueryResult uriSet = resultManagerDao.searchStudiesByText(text);

		if (uriSet.hasData()) {
			studyResults = getStudyResultsFromCache(uriSet);
		}
		return studyResults;
	}

	/*
	 * public ResultSet getFacetPopulationDetails(Facet facet, boolean addFilter) { return
	 * resultManagerDao.getDeFacetDetails(facet, addFilter); }
	 */

	/**
	 * Return Data Element with list of DE facet items populated
	 */
	public Facet getDeFacetDetails(boolean addFilter) {

		Facet deFacet = new Facet();
		deFacet.setHeadingLabel(QueryToolConstants.deFacetConfig[0]);
		deFacet.setPropertyURI(QueryToolConstants.deFacetConfig[1]);
		deFacet.setClassURI(QueryToolConstants.deFacetConfig[2]);

		QueryResult deResultSet = resultManagerDao.getDeFacetDetails(deFacet, addFilter);
		deFacet.populateItemsFromJena(deResultSet);

		return deFacet;
	}

	public List<StudyResult> getStudyByPrefixedIds(List<String> prefixedIds) {
		QueryResult rs = resultManagerDao.searchStudiesByPrefixedIds(prefixedIds);
		if (!rs.hasData()) {
			return new ArrayList<StudyResult>();
		} else {
			return getStudyResultsFromCache(rs);
		}
	}

	public List<StudyResult> searchStudyByFormNames(List<String> formNames) {
		QueryResult uriSet = resultManagerDao.searchStudyByFormNames(formNames);

		if (uriSet != null && uriSet.hasData()) {
			List<StudyResult> studyResults = getStudyResultsFromCache(uriSet);
			return studyResults;
		} else {
			return new ArrayList<StudyResult>();
		}
	}

	public List<FormResult> searchDeForms(String text, List<String> deUris) {
		QueryResult uriSet = resultManagerDao.searchDeForms(text, deUris);

		if (uriSet.hasData()) { // pass back form result
			List<FormResult> formResults = getFormResultsFromCache(uriSet);
			return formResults;
		} else {
			return new LinkedList<FormResult>();
		}
	}

	public FormResult getFormByShortName(String name) {
		QueryResult rs = resultManagerDao.getFormByShortName(name);

		if (rs.hasData()) { // pass back form result
			List<FormResult> formResults = getFormResultsFromCache(rs);
			if (formResults.isEmpty()) {
				return null;
			} else {
				return new FormResult(formResults.get(0));
			}
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Multimap<String, FormResult> searchFormsByStudyPrefixedIds(List<String> prefixedIds) {
		Multimap<String, FormResult> prefixedIdToFormMap = ArrayListMultimap.create();

		if (prefixedIds == null || prefixedIds.isEmpty()) {
			return prefixedIdToFormMap;
		}

		QueryResult rs = resultManagerDao.searchFormsByPrefixedIds(prefixedIds);

		// we are going to be using the form structure cache. initialize the cache just
		// in case.
		if (metaDataCache.isResultCacheEmpty(ResultType.FORM_STRUCTURE)) {
			SearchResultUtil.cacheFormResults(metaDataCache, this);
		}

		for (QuerySolution row : rs.getQueryData()) {
			String prefixedId =
					InstancedDataUtil.trimRdfType(row.get(QueryToolConstants.PREFIXED_ID_VAR.getName()).toString());
			String formUriGrouped =
					InstancedDataUtil.trimRdfType(row.get(QueryToolConstants.FS_URI_VAR.getName()).toString());
			String[] formUriArr = formUriGrouped.split(",");
			for (String formUri : formUriArr) {
				FormResult currentFormResult = new FormResult(metaDataCache.getFormResult(formUri));
				prefixedIdToFormMap.put(prefixedId, currentFormResult);
			}
		}

		return prefixedIdToFormMap;
	}

	public Multimap<String, FormResult> searchFormsByDeNames(List<String> deNames) {
		Multimap<String, FormResult> deToFormMap = ArrayListMultimap.create();

		if (deNames == null || deNames.isEmpty()) {
			return deToFormMap;
		}

		QueryResult rs = resultManagerDao.getDataElementToFormStructure(deNames);

		if (metaDataCache.isResultCacheEmpty(ResultType.FORM_STRUCTURE)) {
			SearchResultUtil.cacheFormResults(metaDataCache, this);
		}

		for (QuerySolution row : rs.getQueryData()) {
			String deName = InstancedDataUtil
					.trimRdfType(row.get(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE.getName()).toString());
			String formUriGrouped =
					InstancedDataUtil.trimRdfType(row.get(QueryToolConstants.FS_URI_VAR.getName()).toString());
			String[] formUriArr = formUriGrouped.split(",");

			for (String formUri : formUriArr) {
				FormResult currentFormResult = metaDataCache.getFormResult(formUri);
				deToFormMap.put(deName, currentFormResult);
			}
		}

		return deToFormMap;
	}

	public List<StudyResult> getStudyDetailsInfo(List<FormResult> forms) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {

		ResultSetToStudy resultSetToStudy = new ResultSetToStudy();
		QueryResult studySet = resultManagerDao.getStudyDetailsInForms(forms);

		return resultSetToStudy.getBeans(studySet);
	}

	public List<FormResult> getFormDetailsInfo(List<StudyResult> studies) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {

		ResultSetToFormStructure resultSetToForm = new ResultSetToFormStructure();
		QueryResult formSet = resultManagerDao.getFormDetailsInStudies(studies);

		return resultSetToForm.getBeans(formSet);
	}

	/**
	 * Uses the list of uri's from the result set and retrieve the list of objects from the cache
	 * 
	 * @param formResultSet
	 * @return
	 */
	private List<FormResult> getFormResultsFromCache(QueryResult formResultSet) {

		if (metaDataCache.isResultCacheEmpty(ResultType.FORM_STRUCTURE)) {
			SearchResultUtil.cacheFormResults(metaDataCache, this);
		}

		List<FormResult> formResultList = new LinkedList<FormResult>();

		for (QuerySolution qs : formResultSet.getQueryData()) {
			if (qs.get("uri") != null) {
				String formUri = qs.get("uri").toString();
				formResultList.add(metaDataCache.getFormResult(formUri));
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
	private List<StudyResult> getStudyResultsFromCache(QueryResult studyResultSet) {

		if (metaDataCache.isResultCacheEmpty(ResultType.STUDY)) {
			SearchResultUtil.cacheStudyResults(metaDataCache, this);
		}

		List<StudyResult> studyResultList = new LinkedList<StudyResult>();

		for (QuerySolution qs : studyResultSet.getQueryData()) {
			if (qs.get("uri") != null) {
				String studyUri = qs.get("uri").toString();
				studyResultList.add(metaDataCache.getStudyResult(studyUri));
			}
		}

		return studyResultList;
	}

	public Multimap<String, String> getSeeAlso(List<String> deNames) {

		Multimap<String, String> deToSeeAlsoMap = ArrayListMultimap.create();

		if (deNames == null || deNames.isEmpty()) {
			return deToSeeAlsoMap;
		}

		QueryResult rs = resultManagerDao.getSeeAlso(deNames);
		for (QuerySolution row : rs.getQueryData()) {
			String seeAlsoGrouped =
					InstancedDataUtil.trimRdfType(row.get(QueryToolConstants.SEE_ALSO_VAR.getName()).toString());
			String deName = InstancedDataUtil
					.trimRdfType(row.get(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE.getName()).toString());

			String[] seeAlsoArr = seeAlsoGrouped.split(",");

			for (String seeAlso : seeAlsoArr) {
				deToSeeAlsoMap.put(deName, seeAlso);
			}
		}

		return deToSeeAlsoMap;
	}

	@Override
	public Map<String, String> getDeTitles(List<String> deNames) {
		Map<String, String> deToDeTitleMap = new HashMap<>();

		if (deNames == null || deNames.isEmpty()) {
			return deToDeTitleMap;
		}

		QueryResult rs = resultManagerDao.getDeTitles(deNames);

		for (QuerySolution row : rs.getQueryData()) {
			String deName = InstancedDataUtil
					.trimRdfType(row.get(QueryToolConstants.DATA_ELEMENT_NAME_VARIABLE.getName()).toString());
			String deTitle =
					InstancedDataUtil.trimRdfType(row.get(QueryToolConstants.DE_TITLE_VARIABLE.getName()).toString());
			deToDeTitleMap.put(deName, deTitle);
		}

		return deToDeTitleMap;
	}

}
