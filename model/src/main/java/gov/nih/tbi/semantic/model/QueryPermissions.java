package gov.nih.tbi.semantic.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * NOTE THIS CLASS IS MIRRORED IN THE QUERY TOOL (pojo package). ANY CHANGES WILL NEED TO BE COPIED THERE AS WELL
 * 
 * THIS WAS TO PREVENT DEPENDENCIES BETWEEN THE TWO (AT SOME POINT JUST NEED TO ADD THE DEPENDENCY)
 */
@XmlRootElement(name = "qtPermissions")
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryPermissions implements Serializable {
	private static final long serialVersionUID = 8383756927840112610L;
	private List<StudyResultPermission> studyResultPermissions = new ArrayList<StudyResultPermission>();
	private List<FormResultPermission> formResultPermissions = new ArrayList<FormResultPermission>();
	private Boolean hasAccessToProforms;
	private Boolean hasAccessToGUID;
	private Boolean hasAccessToDictionary;
	private Boolean hasAccessToRepository;
	private Boolean hasAccessToAccount;
	private Boolean hasAccessToWorkspace;
	private Boolean hasAccessToQuery;
	private Boolean hasAccessToMetaStudy;

	public StudyResultPermission getStudyResultPermissionByStudyURI(String studyURI) {

		for (StudyResultPermission studyResultPermission : studyResultPermissions) {
			if (studyURI.equals(studyResultPermission.getStudyURI())) {
				return studyResultPermission;
			}
		}

		return null;
	}

	public FormResultPermission getFormResultPermissionByFormId(Long formId) {

		for (FormResultPermission formResultPermission : formResultPermissions) {
			if (formResultPermission.getFormId() != null && formId.equals(formResultPermission.getFormId())
					&& !formResultPermission.getStudyDatasets().isEmpty()) {
				return formResultPermission;
			}
		}

		return null;
	}

	/*******************************
	 * 
	 * 
	 * Getters and Setters
	 * 
	 ********************************/

	public List<StudyResultPermission> getStudyResultPermissions() {

		return studyResultPermissions;
	}

	public void setStudyResultPermissions(List<StudyResultPermission> studyResultPermissions) {

		this.studyResultPermissions = studyResultPermissions;
	}

	public List<FormResultPermission> getFormResultPermissions() {

		return formResultPermissions;
	}

	public void setFormResultPermissions(List<FormResultPermission> formResultPermissions) {

		this.formResultPermissions = formResultPermissions;
	}

	public Boolean getHasAccessToProforms() {

		return hasAccessToProforms;
	}

	public void setHasAccessToProforms(Boolean hasAccessToProforms) {

		this.hasAccessToProforms = hasAccessToProforms;
	}

	public Boolean getHasAccessToGUID() {

		return hasAccessToGUID;
	}

	public void setHasAccessToGUID(Boolean hasAccessToGUID) {

		this.hasAccessToGUID = hasAccessToGUID;
	}

	public Boolean getHasAccessToDictionary() {

		return hasAccessToDictionary;
	}

	public void setHasAccessToDictionary(Boolean hasAccessToDictionary) {

		this.hasAccessToDictionary = hasAccessToDictionary;
	}

	public Boolean getHasAccessToRepository() {

		return hasAccessToRepository;
	}

	public void setHasAccessToRepository(Boolean hasAccessToRepository) {

		this.hasAccessToRepository = hasAccessToRepository;
	}

	public Boolean getHasAccessToMetaStudy() {

		return hasAccessToMetaStudy;
	}

	public void setHasAccessToMetaStudy(Boolean hasAccessToMetaStudy) {

		this.hasAccessToMetaStudy = hasAccessToMetaStudy;
	}

	public Boolean getHasAccessToAccount() {

		return hasAccessToAccount;
	}

	public void setHasAccessToAccount(Boolean hasAccessToAccount) {

		this.hasAccessToAccount = hasAccessToAccount;
	}

	public Boolean getHasAccessToWorkspace() {

		return hasAccessToWorkspace;
	}

	public void setHasAccessToWorkspace(Boolean hasAccessToWorkspace) {

		this.hasAccessToWorkspace = hasAccessToWorkspace;
	}

	public Boolean getHasAccessToQuery() {
		return hasAccessToQuery;
	}

	public void setHasAccessToQuery(Boolean hasAccessToQuery) {
		this.hasAccessToQuery = hasAccessToQuery;
	}

	/**
	 * 
	 * Nested Class Study Result Permission
	 * 
	 */
	@XmlRootElement(name = "studyResultPermission")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class StudyResultPermission implements Serializable {

		private static final long serialVersionUID = -2620811128130987489L;

		// Using form Id so I can save webservice calls to dictionary
		private String studyURI;

		private Set<Long> formIds = new HashSet<Long>();

		public String getStudyURI() {

			return studyURI;
		}

		public void setStudyURI(String studyURI) {

			this.studyURI = studyURI;
		}

		public Set<Long> getFormIds() {

			return formIds;
		}

		public void setFormIds(Set<Long> formIds) {

			this.formIds = formIds;
		}

	}

	/**
	 * 
	 * Nested Class FormResultPermission
	 * 
	 */
	@XmlRootElement(name = "formResultPermission")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class FormResultPermission implements Serializable {

		private static final long serialVersionUID = 205581158205627060L;

		private Long formId;
		private Set<StudyDataset> studyDatasets = new HashSet<StudyDataset>();

		public Long getFormId() {

			return formId;
		}

		public void setFormId(Long formId) {

			this.formId = formId;
		}

		public Set<StudyDataset> getStudyDatasets() {

			return studyDatasets;
		}

		public void setStudyDatasets(Set<StudyDataset> studyDatasets) {

			this.studyDatasets = studyDatasets;
		}

	}

	/**
	 * 
	 * Nested Class StudyDataset
	 * 
	 */
	@XmlRootElement(name = "studyDataset")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class StudyDataset implements Serializable {

		private static final long serialVersionUID = -7613906070637753686L;

		private Long datasetId;
		private String datasetURI;
		private String StudyURI;

		public Long getDatasetId() {

			return datasetId;
		}

		public void setDatasetId(Long datasetId) {

			this.datasetId = datasetId;
		}

		public String getDatasetURI() {

			return datasetURI;
		}

		public void setDatasetURI(String datasetURI) {

			this.datasetURI = datasetURI;
		}

		public String getStudyURI() {

			return StudyURI;
		}

		public void setStudyURI(String studyURI) {

			StudyURI = studyURI;
		}
	}
}
