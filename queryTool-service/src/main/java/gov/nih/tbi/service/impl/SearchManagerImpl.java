package gov.nih.tbi.service.impl;

import gov.nih.tbi.pojo.Facet;
import gov.nih.tbi.pojo.FacetItem;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.service.QueryAccountManager;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.SearchManager;
import gov.nih.tbi.service.cache.QueryMetaDataCache;
import gov.nih.tbi.service.model.MetaDataCache;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.SearchResultUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@Component
@Scope("application")
public class SearchManagerImpl implements SearchManager, Serializable {

	private static final long serialVersionUID = 4374230640631951558L;

	@Autowired
	ResultManager resultManager;

	@Autowired
	QueryAccountManager queryAccountManager;

	@Autowired
	MetaDataCache metaDataCache;

	public List<StudyResult> getStudiesFromCache(PermissionModel permissionModel) {

		if (metaDataCache.isResultCacheEmpty(ResultType.STUDY)) {
			SearchResultUtil.cacheStudyResults(metaDataCache, resultManager);
		}

		List<StudyResult> studyResults = new ArrayList<StudyResult>();
		studyResults.addAll(metaDataCache.getStudyResultCacheMap().values());

		queryAccountManager.hidePrivateStudyToNonAdmin(studyResults, permissionModel);
		SearchResultUtil.sortStudyResults(studyResults);

		return studyResults;
	}

	public List<FormResult> getFormsFromCache() {

		if (metaDataCache.isResultCacheEmpty(ResultType.FORM_STRUCTURE)) {
			SearchResultUtil.cacheFormResults(metaDataCache, resultManager);
		}

		List<FormResult> formResults = new ArrayList<FormResult>();
		formResults.addAll(metaDataCache.getFormResultCacheMap().values());

		SearchResultUtil.sortFormResults(formResults);
		return formResults;
	}


	public List<FacetItem> getDeFacetItems() {

		Facet deFacet = resultManager.getDeFacetDetails(false);
		return deFacet.getItems();
	}

	public List<StudyResult> searchStudies(String textValue, PermissionModel permissionModel) {

		if (textValue == null || textValue.isEmpty()) {
			return getStudiesFromCache(permissionModel);

		} else {
			List<StudyResult> studyResultList = resultManager.searchStudies(textValue);

			queryAccountManager.hidePrivateStudyToNonAdmin(studyResultList, permissionModel);
			SearchResultUtil.sortStudyResults(studyResultList);

			return studyResultList;
		}
	}

	public List<FormResult> searchForms(String textValue) {
		if (textValue == null || textValue.isEmpty()) {
			return getFormsFromCache();
		}

		return resultManager.searchForms(textValue);
	}


	public List<FormResult> searchDeForms(String textValue, List<String> deUris) {

		if (deUris == null || deUris.isEmpty()) {
			return resultManager.searchForms(textValue);
		} else {
			return resultManager.searchDeForms(textValue, deUris);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Multimap<String, FormResult> searchFormsByDeNames(List<String> deNames) {
		if (deNames == null || deNames.isEmpty()) {
			return ArrayListMultimap.create();
		}

		return resultManager.searchFormsByDeNames(deNames);
	}

	/**
	 * {@inheritDoc}
	 */
	public Multimap<String, String> getSeeAlso(List<String> deNames) {
		return resultManager.getSeeAlso(deNames);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getDeTitles(List<String> deNames) {
		return resultManager.getDeTitles(deNames);
	}
}
