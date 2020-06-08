package gov.nih.tbi.commons.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudySite;
import gov.nih.tbi.repository.model.hibernate.VisualizationStudy;

public interface AdvancedVisualizationManager extends Serializable {
	
	public void buildFile();
	
	/**
	 * Returns a complete list of all studies with joins set to eager.  This is for the
	 * advanced visualization service so the chosen join fields are driven by that.
	 * 
	 * @return
	 */
	public Map<Long, VisualizationStudy> getAllStudiesVisualization();

	public void buildSummaryData();

	public JsonArray getStudyDataTypes(Study study, Map<Long, Set<SubmissionType>> studySubmissionTypes);

	public JsonArray getStudyJsonForms(Study study);

	public JsonObject getStudySummaryGraphData(List<String> studyChartNames, Study study);

	public JsonElement valueOrEmpty(String string);

	public JsonArray getStudyPis(Study study);

	public JsonArray getStudySites(Study study);

	public String getStudyGrantIdList(Study study);

	public String getStudySharingStatus(Study study);

	public Map<Long, Set<SubmissionType>> getSubmissionTypes(Map<Long, VisualizationStudy> vStudies);

	public String generateStudyUrl(Study study);

	public JsonObject getProgramSummaryGraphs();

	public List<String> getStudySummaryGraphNames();

	public JsonObject performStudyCountSummaryQuery(String studyId);

}
