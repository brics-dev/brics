package gov.nih.tbi.metastudy.model;


import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.repository.model.hibernate.StudyType;
import gov.nih.tbi.commons.service.MetaStudyManager;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.dictionary.model.StaticField;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.repository.dao.ModelNameDao;
import gov.nih.tbi.repository.dao.ModelTypeDao;
import gov.nih.tbi.repository.dao.TherapeuticAgentDao;
import gov.nih.tbi.repository.dao.TherapeuticTargetDao;
import gov.nih.tbi.repository.dao.TherapyTypeDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelName;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelType;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapyType;
import gov.nih.tbi.repository.model.alzped.ModelName;
import gov.nih.tbi.repository.model.alzped.ModelType;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapyType;
import gov.nih.tbi.repository.model.hibernate.FundingSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class MetaStudyDetailsForm {

	private static Logger logger = Logger.getLogger(MetaStudyDetailsForm.class);

	@Autowired
	StaticReferenceManager staticManager;

    @Autowired
    MetaStudyManager metaStudyManager;
	
	private Long id;
	private String title;
	private String abstractText;
	private String aimsText;
	private String studyUrl;
	private FundingSource fundingSource;
	private RecruitmentStatus recruitmentStatus;
	private StudyType studyType;


	// Alzped
	private Set<MetaStudyTherapeuticAgent> therapeuticAgentSet;
	private Set<MetaStudyTherapyType> therapyTypeSet;
	private Set<MetaStudyTherapeuticTarget> therapeuticTargetSet;
	private Set<MetaStudyModelType> modelTypeSet;
	private Set<MetaStudyModelName> modelNameSet;

	public MetaStudyDetailsForm() {

	}

	public MetaStudyDetailsForm(MetaStudy metaStudy) {

		this.id = metaStudy.getId();
		this.title = metaStudy.getTitle();
		this.abstractText = metaStudy.getAbstractText();
		this.aimsText = metaStudy.getAimsText();
		this.studyUrl = metaStudy.getStudyUrl();
		this.fundingSource = metaStudy.getFundingSource();
		this.recruitmentStatus = metaStudy.getRecruitmentStatus();
		this.studyType = metaStudy.getStudyType();
		this.therapeuticAgentSet = metaStudy.getTherapeuticAgentSet();
		this.therapeuticTargetSet = metaStudy.getTherapeuticTargetSet();
		this.therapyTypeSet = metaStudy.getTherapyTypeSet();
		this.modelNameSet = metaStudy.getModelNameSet();
		this.modelTypeSet = metaStudy.getModelTypeSet();
	}

	public Set<MetaStudyTherapeuticAgent> getTherapeuticAgentSet() {
		return therapeuticAgentSet;
	}

	public void setTherapeuticAgentSet(Set<MetaStudyTherapeuticAgent> therapeuticAgentSet) {
		this.therapeuticAgentSet = therapeuticAgentSet;
	}

	public Set<MetaStudyTherapyType> getTherapyTypeSet() {
		return therapyTypeSet;
	}

	public void setTherapyTypeSet(Set<MetaStudyTherapyType> therapyTypeSet) {
		this.therapyTypeSet = therapyTypeSet;
	}

	public Set<MetaStudyTherapeuticTarget> getTherapeuticTargetSet() {
		return therapeuticTargetSet;
	}

	public void setTherapeuticTargetSet(Set<MetaStudyTherapeuticTarget> therapeuticTargetSet) {
		this.therapeuticTargetSet = therapeuticTargetSet;
	}

	public Set<MetaStudyModelType> getModelTypeSet() {
		return modelTypeSet;
	}

	public void setModelTypeSet(Set<MetaStudyModelType> modelTypeSet) {
		this.modelTypeSet = modelTypeSet;
	}

	public Set<MetaStudyModelName> getModelNameSet() {
		return modelNameSet;
	}

	public void setModelNameSet(Set<MetaStudyModelName> modelNameSet) {
		this.modelNameSet = modelNameSet;
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

	public void setModelNameSelect(String[] modelNameSelect) {
		if (modelNameSet == null) {
			modelNameSet = new HashSet<>();
		}

		modelNameSet.clear();

		List<ModelName> allModelNames = metaStudyManager.getAllModelNames();
		for (String modelNameId : modelNameSelect) {
			Long modelNameIdLong = Long.valueOf(modelNameId);
			for (ModelName currentModelName : allModelNames) {
				if (modelNameIdLong.equals(currentModelName.getId())) {
					this.modelNameSet.add(new MetaStudyModelName(currentModelName));
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

		List<ModelType> allModelTypes = metaStudyManager.getAllModelTypes();
		for (String modelTypeId : modelTypeSelect) {
			Long modelTypeIdLong = Long.valueOf(modelTypeId);
			for (ModelType currentModelType : allModelTypes) {
				if (modelTypeIdLong.equals(currentModelType.getId())) {
					this.modelTypeSet.add(new MetaStudyModelType(currentModelType));
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

		List<TherapeuticTarget> allTherapeuticTargets = metaStudyManager.getAllTherapeuticTargets();
		for (String therapeuticTargetId : therapeuticTargetSelect) {
			Long therapeuticTargetIdLong = Long.valueOf(therapeuticTargetId);
			for (TherapeuticTarget currentTherapeuticTarget : allTherapeuticTargets) {
				if (therapeuticTargetIdLong.equals(currentTherapeuticTarget.getId())) {
					this.therapeuticTargetSet.add(new MetaStudyTherapeuticTarget(currentTherapeuticTarget));
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

		List<TherapyType> allTherapyTypes = metaStudyManager.getAllTherapyTypes();
		for (String therapyTypeId : therapyTypeSelect) {
			Long therapyTypeIdLong = Long.valueOf(therapyTypeId);
			for (TherapyType currentTherapy : allTherapyTypes) {
				if (therapyTypeIdLong.equals(currentTherapy.getId())) {
					this.therapyTypeSet.add(new MetaStudyTherapyType(currentTherapy));
					break;
				}
			}
		}
	}

	public void setTherapeuticAgentSelect(String[] therapeuticAgentSelect) {
		if (therapeuticAgentSet == null) {
			therapeuticAgentSet = new HashSet<>();
		}

		therapeuticAgentSet.clear();

		List<TherapeuticAgent> allTherapeuticAgents = metaStudyManager.getAllTherapeuticAgents();
		for (String therapeuticAgentId : therapeuticAgentSelect) {
			Long therapeuticAgentIdLong = Long.valueOf(therapeuticAgentId);
			for (TherapeuticAgent currentTherapeuticAgent : allTherapeuticAgents) {
				if (therapeuticAgentIdLong.equals(currentTherapeuticAgent.getId())) {
					this.therapeuticAgentSet.add(new MetaStudyTherapeuticAgent(currentTherapeuticAgent));
					break;
				}
			}
		}
	}

	/**
	 * Read the form fields on the page and set the columns in the study column.
	 */
	public void adapt(MetaStudy metaStudy, Boolean enforceStaticFields) {

		Field[] fields = this.getClass().getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field current = fields[i];

			if (!current.getName().equals("logger") && !current.getName().equals("staticManager") && !current.getName().equals("metaStudyManager")) {

				try {
					if (enforceStaticFields == false || current.getAnnotation(StaticField.class) == null) {
						Object value = current.get(this);

						String setMethodName = "set" + current.getName().substring(0, 1).toUpperCase()
								+ current.getName().substring(1);

						Method setMethod = metaStudy.getClass().getMethod(setMethodName, current.getType());

						try {
							setMethod.invoke(metaStudy, value);
						} catch (InvocationTargetException ex) {
							if (ex.getCause() instanceof UnsupportedOperationException) {
								logger.error(
										"Cannot invoke method defined in setMethod of the MetaStudyDetailsForm adapt().");
							} else {
								throw ex;
							}
						}
					}
				} catch (Exception e) {
					logger.error("There was an error caught in MetaStudyDetailsForm adapt()" + e.toString());
				}
			}
		}
	}

	public String getStudyUrl() {
		return studyUrl;
	}

	public void setStudyUrl(String studyUrl) {
		this.studyUrl = studyUrl;
	}

	public String getAimsText() {
		return aimsText;
	}

	public void setAimsText(String aimsText) {
		this.aimsText = aimsText;
	}

	public FundingSource getFundingSource() {
		return fundingSource;
	}

	public void setFundingSource(Long fundingSource) {
		for (FundingSource fs : staticManager.getFundingSourceList()) {
			if (fs.getId().equals(fundingSource)) {
				this.fundingSource = fs;
				break;
			}
		}
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
}
