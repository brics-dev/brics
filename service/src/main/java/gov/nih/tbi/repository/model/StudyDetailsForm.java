package gov.nih.tbi.repository.model;

import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.repository.model.hibernate.StudyType;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.dictionary.model.StaticField;
import gov.nih.tbi.repository.dao.StudyDao;
import gov.nih.tbi.repository.model.alzped.ModelName;
import gov.nih.tbi.repository.model.alzped.ModelType;
import gov.nih.tbi.repository.model.alzped.StudyModelName;
import gov.nih.tbi.repository.model.alzped.StudyModelType;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapyType;
import gov.nih.tbi.repository.model.hibernate.FundingSource;
import gov.nih.tbi.repository.model.hibernate.Study;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class StudyDetailsForm {

	static Logger logger = Logger.getLogger(StudyDetailsForm.class);

	@Autowired
	StaticReferenceManager staticManager;

	@Autowired
	private StudyDao studyDao;

	private Long id;
	private String title;
	private String abstractText;
	private String goals;
	private RecruitmentStatus recruitmentStatus;
	private StudyType studyType;
	private String studyUrl;

	private Integer numberOfSubjects;
	private FundingSource fundingSource;
	private String studyStartDate;
	private String studyEndDate;

	// Study Image
	private File upload;
	private String uploadFileName;
	
	private File graphicFile;
	private String graphicFileName;
	// Alzped
	private Set<StudyTherapeuticAgent> therapeuticAgentSet;
	private Set<StudyTherapyType> therapyTypeSet;
	private Set<StudyTherapeuticTarget> therapeuticTargetSet;
	private Set<StudyModelType> modelTypeSet;
	private Set<StudyModelName> modelNameSet;

	public StudyDetailsForm() {

	}

	/**
	 * Constructor fetches data for each column in dataElement object
	 * 
	 * @param dataElement
	 */
	public StudyDetailsForm(Study study) {

		Field[] fields = this.getClass().getDeclaredFields();
		SimpleDateFormat isoFormatting = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < fields.length; i++) {
			Field current = fields[i];
			Object value = null;

			if (!current.getName().equals("logger") && !current.getName().equals("staticManager")) {

				try {
					String getMethodName =
							"get" + current.getName().substring(0, 1).toUpperCase() + current.getName().substring(1);

					Method setMethod = study.getClass().getMethod(getMethodName);

					try {
						value = setMethod.invoke(study);
						if (value instanceof Date) {
							value = isoFormatting.format(value);
						}
					} catch (InvocationTargetException ex) {
						if (ex.getCause() instanceof UnsupportedOperationException) {
							logger.error("Could not call method defined by setMethod.");
						} else {
							throw ex;
						}
					}

					current.set(this, value);
				} catch (Exception e) {
					logger.error("There was an error caught in studyDetailsForm StudyDetailsForm()" + e.toString());

				}
			}
		}
	}

	/**
	 * Read the form fields on the page and set the columns in the study column.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void adapt(Study study, Boolean enforceStaticFields) {

		Field[] fields = this.getClass().getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field current = fields[i];

			if (!current.getName().equals("logger") && !current.getName().equals("staticManager")) {

				try {
					if (enforceStaticFields == false || current.getAnnotation(StaticField.class) == null) {
						Object value = current.get(this);

						String setMethodName = "set" + current.getName().substring(0, 1).toUpperCase()
								+ current.getName().substring(1);

						Method setMethod = study.getClass().getMethod(setMethodName, current.getType());

						try {
							setMethod.invoke(study, value);
						} catch (InvocationTargetException ex) {
							if (ex.getCause() instanceof UnsupportedOperationException) {
								logger.error(
										"Cannot invoke method defined in setMethod of the StudyDetailsForm adapt().");
							} else {
								throw ex;
							}
						}
					}
				} catch (Exception e) {
					logger.error("There was an error caught in studyDetailsForm adapt()" + e.toString());
				}
			}
		}
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public String getGoals() {
		return goals;
	}

	public void setGoals(String goals) {
		this.goals = goals;
	}

	public RecruitmentStatus getRecruitmentStatus() {
		return recruitmentStatus;
	}

	public void setRecruitmentStatus(Long recruitmentStatus) {

		for (RecruitmentStatus status : RecruitmentStatus.values()) {
			if (status.getId().equals(recruitmentStatus)) {
				this.recruitmentStatus = status;
				break;
			}
		}
	}

	public StudyType getStudyType() {
		return studyType;
	}

	public void setStudyType(Long studyTypeId) {
		for (StudyType type : staticManager.getStudyTypeList()) {
			if (type.getId().equals(studyTypeId)) {
				this.studyType = type;
				break;
			}
		}
	}

	public String getStudyUrl() {
		return studyUrl;
	}

	public void setStudyUrl(String studyUrl) {
		this.studyUrl = studyUrl;
	}

	public Integer getNumberOfSubjects() {
		return numberOfSubjects;
	}

	public void setNumberOfSubjects(Integer numberOfSubjects) {
		this.numberOfSubjects = numberOfSubjects;
	}

	public FundingSource getFundingSource() {
		return fundingSource;
	}

	public void setFundingSource(Long fundingSourceId) {

		for (FundingSource fundingSource : staticManager.getFundingSourceList()) {
			if (fundingSource.getId().equals(fundingSourceId)) {
				this.fundingSource = fundingSource;
				break;
			}
		}
	}

	public String getStudyStartDate() {
		return studyStartDate;
	}

	public void setStudyStartDate(String studyStartDate) {
		this.studyStartDate = studyStartDate;
	}

	public String getStudyEndDate() {
		return studyEndDate;
	}

	public void setStudyEndDate(String studyEndDate) {
		this.studyEndDate = studyEndDate;
	}


	public File getUpload() {
		return upload;
	}

	public void setUpload(File uploadFile) {
		this.upload = uploadFile;
	}

	public String getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	public void setModelNameSelect(String[] modelNameSelect) {
		if (modelNameSet == null) {
			modelNameSet = new HashSet<>();
		}
		
		modelNameSet.clear();

		List<ModelName> allModelNames = studyDao.getAllModelNames();
		for (String modelNameId : modelNameSelect) {
			Long modelNameIdLong = Long.valueOf(modelNameId);
			for (ModelName currentModelName : allModelNames) {
				if (modelNameIdLong.equals(currentModelName.getId())) {
					this.modelNameSet.add(new StudyModelName(currentModelName));
					break;
				}
			}
		}
	}

	public void setModelTypeSelect(String[] modelTypeSelect) {
		if (modelTypeSet == null) {
			modelTypeSet = new HashSet<>();
		}
		
		modelTypeSet.clear();

		List<ModelType> allModelTypes = studyDao.getAllModelTypes();
		for (String modelTypeId : modelTypeSelect) {
			Long modelTypeIdLong = Long.valueOf(modelTypeId);
			for (ModelType currentModelType : allModelTypes) {
				if (modelTypeIdLong.equals(currentModelType.getId())) {
					this.modelTypeSet.add(new StudyModelType(currentModelType));
					break;
				}
			}
		}
	}

	public void setTherapeuticTargetSelect(String[] therapeuticTargetSelect) {
		if (therapeuticTargetSet == null) {
			therapeuticTargetSet = new HashSet<>();
		}
		
		therapeuticTargetSet.clear();
		
		List<TherapeuticTarget> allTherapeuticTargets = studyDao.getAllTherapeuticTargets();
		for (String therapeuticTargetId : therapeuticTargetSelect) {
			Long therapeuticTargetIdLong = Long.valueOf(therapeuticTargetId);
			for (TherapeuticTarget currentTherapeuticTarget : allTherapeuticTargets) {
				if (therapeuticTargetIdLong.equals(currentTherapeuticTarget.getId())) {
					this.therapeuticTargetSet.add(new StudyTherapeuticTarget(currentTherapeuticTarget));
					break;
				}
			}
		}
	}

	public void setTherapyTypeSelect(String[] therapyTypeSelect) {
		if (therapyTypeSet == null) {
			therapyTypeSet = new HashSet<>();
		}

		therapyTypeSet.clear();
		
		List<TherapyType> allTherapyTypes = studyDao.getAllTherapyTypes();
		for (String therapyTypeId : therapyTypeSelect) {
			Long therapyTypeIdLong = Long.valueOf(therapyTypeId);
			for (TherapyType currentTherapy : allTherapyTypes) {
				if (therapyTypeIdLong.equals(currentTherapy.getId())) {
					this.therapyTypeSet.add(new StudyTherapyType(currentTherapy));
					break;
				}
			}
		}
	}
	
	public String[] getTherapeuticAgentSelect() {
		String[] out = {"1"};
		return out;
	}

	public void setTherapeuticAgentSelect(String[] therapeuticAgentSelect) {
		if (therapeuticAgentSet == null) {
			therapeuticAgentSet = new HashSet<>();
		}
		
		therapeuticAgentSet.clear();

		List<TherapeuticAgent> allTherapeuticAgents = studyDao.getAllTherapeuticAgents();
		for (String therapeuticAgentId : therapeuticAgentSelect) {
			Long therapeuticAgentIdLong = Long.valueOf(therapeuticAgentId);
			for (TherapeuticAgent currentTherapeuticAgent : allTherapeuticAgents) {
				if (therapeuticAgentIdLong.equals(currentTherapeuticAgent.getId())) {
					this.therapeuticAgentSet.add(new StudyTherapeuticAgent(currentTherapeuticAgent));
					break;
				}
			}
		}
		
		
		System.out.println(therapeuticAgentSet);
	}

	public Set<StudyTherapeuticAgent> getTherapeuticAgentSet() {

		return therapeuticAgentSet;
	}

	public void setTherapeuticAgentSet(Set<StudyTherapeuticAgent> therapeuticAgentSet) {
		this.therapeuticAgentSet = therapeuticAgentSet;
	}

	public Set<StudyTherapyType> getTherapyTypeSet() {
		return therapyTypeSet;
	}

	public void setTherapyTypeSet(Set<StudyTherapyType> therapyTypeSet) {
		this.therapyTypeSet = therapyTypeSet;
	}

	public Set<StudyTherapeuticTarget> getTherapeuticTargetSet() {
		return therapeuticTargetSet;
	}

	public void setTherapeuticTargetSet(Set<StudyTherapeuticTarget> therapeuticTargetSet) {
		this.therapeuticTargetSet = therapeuticTargetSet;
	}

	public Set<StudyModelType> getModelTypeSet() {
		return modelTypeSet;
	}

	public void setModelTypeSet(Set<StudyModelType> modelTypeSet) {
		this.modelTypeSet = modelTypeSet;
	}

	public Set<StudyModelName> getModelNameSet() {
		return modelNameSet;
	}

	public void setModelNameSet(Set<StudyModelName> modelNameSet) {
		this.modelNameSet = modelNameSet;
	}

	public File getGraphicFile() {
		return graphicFile;
	}

	public void setGraphicFile(File graphicFile) {
		this.graphicFile = graphicFile;
	}

	public String getGraphicFileName() {
		return graphicFileName;
	}

	public void setGraphicFileName(String graphicFileName) {
		this.graphicFileName = graphicFileName;
	}
	
}
