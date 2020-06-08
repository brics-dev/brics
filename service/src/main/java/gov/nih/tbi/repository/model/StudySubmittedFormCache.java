package gov.nih.tbi.repository.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;
import gov.nih.tbi.repository.model.hibernate.Study;

public class StudySubmittedFormCache {

	private static StudySubmittedFormCache instance = null;
	private LinkedHashMap<Long, List<StudySubmittedForm>> studySubmittedFormData;

	private RepositoryManager repositoryManager;
	private QueryToolManager queryToolManager;

	/**
	 * Instantiate the class. Protected to set up the singleton nature of this cache. NOTE: singletons here are
	 * system-scoped not thread-scoped so the cache is maintained for ALL users by design.
	 * 
	 * @param repositoryManager connection to the web service hosting the queries
	 */
	protected StudySubmittedFormCache(RepositoryManager repositoryManager,QueryToolManager queryToolManager) {
		this.repositoryManager = repositoryManager;
		this.queryToolManager = queryToolManager;
		
		studySubmittedFormData = new LinkedHashMap<Long, List<StudySubmittedForm>>();
	}

	/**
	 * Get the instance of the cache from the system.
	 * 
	 * @param repositoryManager connection to the web service hosting the queries
	 * @return instance of StudySubmittedFormCache that can be used to get StudySubmittedForms
	 */
	public static StudySubmittedFormCache getInstance(RepositoryManager repositoryManager,QueryToolManager queryToolManager) {
		if (instance == null) {
			instance = new StudySubmittedFormCache(repositoryManager,queryToolManager);
		}
		return instance;
	}
	
	/**
	 * Empty the cache.
	 */
	public void clearCache() {

		studySubmittedFormData.clear();
		
		//rebuild the cache
		cacheAllStudySubmittedForms();
		
	}

	public void cacheAllStudySubmittedForms() {
		
		Multimap<Long, StudySubmittedForm> studySubmittedForms = queryToolManager.getAllStudySubmittedForms();
		
		for(Long studyId:studySubmittedForms.keySet()){
			this.studySubmittedFormData.put(studyId, (List<StudySubmittedForm>) studySubmittedForms.get(studyId));
		}
		
	}

	/**
	 * Get JsonObject data for study submitted forms
	 * 
	 * @param studyId 
	 * @return JsonObject representation of list of study submitted forms with total record count
	 */
	public JsonObject getStudySubmittedFormsData(Long studyId) {

		List<StudySubmittedForm> studySubmittedForms = this.getStudySubmittedForms(studyId);

		String totalRecordCount = getTotalRecordCount(studySubmittedForms);

		JsonObject dataJson = new JsonObject();
		dataJson.addProperty("totalRecordCount", totalRecordCount);

		JsonArray studySubmittedFormArray = new JsonArray();

		for (StudySubmittedForm studySubmittedForm : studySubmittedForms) {
			JsonObject studySubmittedFormJson = new JsonObject();
			studySubmittedFormJson.addProperty("studyTitle", studySubmittedForm.getStudyTitle());
			studySubmittedFormJson.addProperty("fSTitle", studySubmittedForm.getfSTitle());
			studySubmittedFormJson.addProperty("fSShortName", studySubmittedForm.getfSShortName());
			studySubmittedFormJson.addProperty("numberOfRecords", studySubmittedForm.getNumberOfRecords());

			studySubmittedFormArray.add(studySubmittedFormJson);
		}

		dataJson.add("submittedFormStructures", studySubmittedFormArray);

		return dataJson;

	}

	/**
	 * Get JsonObject data for study submitted forms without dataset
	 * 
	 * @param studyId 
	 * @return JsonObject representation of list of study submitted forms with total record count
	 */
	public JsonObject getStudySubmittedFormsWithoutData(Long studyId) {

		List<StudySubmittedForm> studySubmittedForms = this.getStudySubmittedFormsWithoutDataset(studyId);

		String totalRecordCount = getTotalRecordCount(studySubmittedForms);

		JsonObject dataJson = new JsonObject();
		dataJson.addProperty("totalRecordCount", totalRecordCount);

		JsonArray studySubmittedFormArray = new JsonArray();

		for (StudySubmittedForm studySubmittedForm : studySubmittedForms) {
			JsonObject studySubmittedFormJson = new JsonObject();
			studySubmittedFormJson.addProperty("studyTitle", studySubmittedForm.getStudyTitle());
			studySubmittedFormJson.addProperty("fSTitle", studySubmittedForm.getfSTitle());
			studySubmittedFormJson.addProperty("fSShortName", studySubmittedForm.getfSShortName());
			studySubmittedFormJson.addProperty("numberOfRecords", studySubmittedForm.getNumberOfRecords());

			studySubmittedFormArray.add(studySubmittedFormJson);
		}

		dataJson.add("submittedFormStructures", studySubmittedFormArray);

		return dataJson;

	}

	


	public List<StudySubmittedForm> getStudySubmittedForms(Long studyId) {

		List<StudySubmittedForm> studySubmittedForms = new ArrayList<StudySubmittedForm>();
		
		Study study = repositoryManager.getStudy(studyId);

		if (this.studySubmittedFormData.containsKey(study.getTitle())) {
			studySubmittedForms = studySubmittedFormData.get(study.getTitle());
			
		} else {

			studySubmittedForms = queryToolManager.getStudySubmittedForms(study.getTitle());

			// cache the objects before returning
			cacheStudySubmittedFormData(study.getId(), studySubmittedForms);
		}

		return studySubmittedForms;
	}

	public List<StudySubmittedForm> getStudySubmittedFormsWithoutDataset(Long studyId) {

		List<StudySubmittedForm> studySubmittedForms = new ArrayList<StudySubmittedForm>();
		
		Study study = repositoryManager.getStudyExcludingDatasets(studyId);

		if (this.studySubmittedFormData.containsKey(study.getTitle())) {
			studySubmittedForms = studySubmittedFormData.get(study.getTitle());
			
		} else {

			studySubmittedForms = queryToolManager.getStudySubmittedForms(study.getTitle());

			// cache the objects before returning
			cacheStudySubmittedFormData(study.getId(), studySubmittedForms);
		}

		return studySubmittedForms;
	}
	
	public void cacheStudySubmittedFormData(Long studyId, List<StudySubmittedForm> studySubmittedForms) {

		this.studySubmittedFormData.put(studyId, studySubmittedForms);
	}

	public String getTotalRecordCount(List<StudySubmittedForm> studySubmittedForms) {

		int totalRecordCount = 0;
		for (StudySubmittedForm studySubmittedForm : studySubmittedForms) {
			int formRecords = getFormStructureRecords(studySubmittedForm.getNumberOfRecords());
			totalRecordCount += formRecords;
		}

		return String.valueOf(totalRecordCount);
	}

	public int getFormStructureRecords(String studySubmittedFormRecord) {

		int formRecords = 0;
		try {
			formRecords = Integer.parseInt(studySubmittedFormRecord);
		} catch (NumberFormatException e) {
			System.out.println("The Record Count for the study submitted form could not be parsed to int.");
			e.printStackTrace();
		}

		return formRecords;
	}
	
	public JsonArray getAllPublicSubmittedFormsJson(){
		JsonArray submittedFormJsonArray = new JsonArray();
		List<PublicSubmittedForm> publicSubmittedFormList = queryToolManager.getAllPublicSubmittedForms();
		for(PublicSubmittedForm publicSubmittedForm : publicSubmittedFormList){
			JsonObject submittedFormJson = new JsonObject();
			submittedFormJson.addProperty("fSShortName", publicSubmittedForm.getfSShortName());
			submittedFormJson.addProperty("fSTitle", publicSubmittedForm.getfSTitle());
			submittedFormJson.addProperty("studyCount", publicSubmittedForm.getNumberOfStudies());
			submittedFormJson.addProperty("recordCount", publicSubmittedForm.getNumberOfRecords());
			submittedFormJson.addProperty("studyTitleList", StringUtils.join(publicSubmittedForm.getStudyTitleList()));
			
			submittedFormJsonArray.add(submittedFormJson);
		}
	
		return submittedFormJsonArray;
	}
	
	public Integer getRowCountByStudy(String studyId){
		return queryToolManager.getRowCountByStudy(studyId);
	}
	
	public Integer getSubjectCountByStudy(String studyId){
		return queryToolManager.getSubjectCountByStudy(studyId);
	}
	
	public Integer getFormCountByStudy(String studyId){
		return queryToolManager.getFormCountByStudy(studyId);
	}
	
	public String getLastSubmitDateByStudy(String studyId){
		return queryToolManager.getLastSubmitDateByStudy(studyId);
	}
	
	public String getSharedStudiesCount(List<BasicStudySearch> basicStudyList){
		
		int sharedStudiesCount =0;
		
		for(BasicStudySearch basicStudySearch: basicStudyList){
			if(basicStudySearch.getSharedDatasetCount().signum() == 1){
				sharedStudiesCount++;
			}
		}
		
		return String.valueOf(sharedStudiesCount);
	}
	
	public String getSharedStudiesCount(Map<Long,BasicStudySearch> basicStudyList){
		
		int sharedStudiesCount =0;
		
		for(Entry<Long, BasicStudySearch> basicStudySearch: basicStudyList.entrySet()){
			if(basicStudySearch.getValue().getSharedDatasetCount().signum() == 1 && Long.valueOf(basicStudySearch.getValue().getStudyStatus()).equals(StudyStatus.PUBLIC.getId())){
				sharedStudiesCount++;
			}
		}
		
		return String.valueOf(sharedStudiesCount);
	}
}
