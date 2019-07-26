
package gov.nih.tbi.repository.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.alzped.ModelName;
import gov.nih.tbi.repository.model.alzped.ModelType;
import gov.nih.tbi.repository.model.alzped.StudyModelName;
import gov.nih.tbi.repository.model.alzped.StudyModelType;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapeuticTargetData;
import gov.nih.tbi.repository.model.alzped.TherapyType;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.Study;

public interface StudyDao extends GenericDao<Study, Long> {

	/**
	 * Gets the study with the specified name
	 * 
	 * @param studyName
	 * @return
	 */
	public Study getStudyByName(String studyName);

	/**
	 * Gets the study with the specified name
	 * 
	 * @param studyName
	 * @return
	 */
	public BasicStudy getBasicStudyByName(String studyName);

	/**
	 * return the number of studies in the database with the given type
	 * 
	 * @param status
	 */
	public Long getStatusCount(StudyStatus status);

	/**
	 * Fetches for a list of studies that contain the following ids. Studies are sorted by id.
	 * 
	 * @param ids : The list of ids to match with studies. If null, all studies will be retrieved.
	 * @return The list of studies that match the given ids
	 */
	public List<Study> getByIds(Set<Long> ids);

	/**
	 * Fetches the list of submission types for the given studies.
	 * 
	 * @param ids set of study IDs to fetch
	 * @return
	 */
	public Map<Long, Set<SubmissionType>> getStudySumissionTypes(Set<Long> ids);

	/**
	 * Returns the study by prefixed id
	 * 
	 * @param studyId
	 * @return
	 */
	public Study getByPrefixedId(String studyId);

	public Study getByPrefixedIdExcludingDataset(String studyId);

	public Study getPublicStudySearchById(Long studyId);

	public Long getStudyIdByDatasetFileId(Long datasetFileId);

	public Study getStudySites(long studyId);

	public List<Study> getAllForRDFGen();

	public List<Study> getStudiesWithPrivateSharedDatasets();

	public boolean isStudyPublic(Long studyId);

	/**
	 * This is the same as the regular study get by name except the datasets are now eager loaded.
	 * 
	 * @param studyTitle
	 * @return
	 */
	public Study getStudyWithDatasetsByName(String studyTitle);
	
	public Study getStudyWithChildren(Long id);
	
	public Study getPublicSubmittedDataStudyById(Long studyId, StudyStatus studyStatus);
	
	public Study getStudyGraphicFileById(Integer studyId);
	
	public Study getStudyWithOutDatasets(Long id);
	
	public Study getLazy(Long id);

	public List<Study> getAllStudiesWithKeyword();
	
	public List<TherapeuticAgent> getAllTherapeuticAgents();
	
	public List<TherapyType> getAllTherapyTypes();
		
	public List<TherapeuticTarget> getAllTherapeuticTargets();
		
	public List<ModelName> getAllModelNames();
	
	public List<ModelType> getAllModelTypes();

	public List<TherapeuticAgent> getTherapeuticAgentsFromListOfStrings(List<String> agents);
	
	public List<TherapeuticTarget> getTherapeuticTargetsFromListOfStrings(List<String> agents);
	
	public List<TherapyType> getTherapyTypesFromListOfStrings(List<String> agents);
	
	public List<ModelName> getModelNamesFromListOfStrings(List<String> agents);
	
	public List<ModelType> getModelTypesFromListOfStrings(List<String> agents);
	
	public List<TherapeuticAgent> getStudyTherapeuticAgents(String studyId);
	
	public List<TherapeuticTarget> getStudyTherapeuticTargets(String studyId);
	
	public List<TherapyType> getStudyTherapyTypes(String studyId);
	
	public List<ModelName> getStudyModelNames(String studyId);
	
	public List<ModelType> getStudyModelTypes(String studyId);
	
	public List<StudyModelType> getModelTypesForStudy(String studyId);
	
	public List<StudyModelName> getModelNamesForStudy(String studyId);
	
	public List<StudyTherapyType> getTherapyTypesForStudy(String studyId);
	
	public List<StudyTherapeuticTarget> getTherapeuticTargetsForStudy(String studyId);
	
	public List<StudyTherapeuticAgent> getTherapeuticAgentsForStudy(String studyId);
}
