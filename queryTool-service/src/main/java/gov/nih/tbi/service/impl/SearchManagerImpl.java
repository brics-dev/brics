package gov.nih.tbi.service.impl;

import gov.nih.tbi.pojo.Facet;
import gov.nih.tbi.pojo.FacetItem;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.QueryAccountManager;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.SearchManager;
import gov.nih.tbi.service.model.MetaDataCache;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.SearchResultUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("application")
public class SearchManagerImpl implements SearchManager, Serializable {

	private static final long serialVersionUID = 4374230640631951558L;
	
	@Autowired
	ResultManager resultManager;
	
	@Autowired
	QueryAccountManager queryAccountManager;
	
	public List<StudyResult> getStudiesFromCache(PermissionModel permissionModel) {
		
		if (MetaDataCache.isResultCacheEmpty(ResultType.STUDY)) {
			SearchResultUtil.cacheStudyResults(resultManager);
		}
		
		List<StudyResult> studyResults = new ArrayList<StudyResult>();
		studyResults.addAll(MetaDataCache.getStudyResultCacheMap().values());
		
		queryAccountManager.hidePrivateStudyToNonAdmin(studyResults, permissionModel);
		SearchResultUtil.sortStudyResults(studyResults);
		
		return studyResults;
	}
	
	public List<FormResult> getFormsFromCache() {
		
		if (MetaDataCache.isResultCacheEmpty(ResultType.FORM_STRUCTURE)) {
			SearchResultUtil.cacheFormResults(resultManager);
		}
		
		List<FormResult> formResults = new ArrayList<FormResult>();
		formResults.addAll(MetaDataCache.getFormResultCacheMap().values());
		
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
}
