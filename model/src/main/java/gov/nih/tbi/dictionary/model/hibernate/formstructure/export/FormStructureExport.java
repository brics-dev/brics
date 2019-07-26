package gov.nih.tbi.dictionary.model.hibernate.formstructure.export;

import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.repository.model.SubmissionType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "dataStructure")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema", propOrder = { "id", "shortName", "version", "title",
		"description", "status", "publicationDate", "organization", "fileType", "standardization", "required",
		"isCopyrighted", "isCAT", "catOid", "measurementType", "repeatableGroups", "diseaseList" })
public class FormStructureExport {

	public static final int DEFAULT_STATUS = 0;

	public static final String ID = "id";
	public static final String SHORT_NAME = "shortName";
	public static final String VERSION = "version";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String STATUS = "status";
	public static final String PUBLICATION_DATE = "publicationDate";
	public static final String VALIDATABLE = "validatable";
	public static final String ORGANIZATION = "organization";
	public static final String STANDARDIZATION = "standardization";
	public static final String REQUIRED = "required";

	public static final String DOCUMENTATION_URL = "documentationUrl";
	public static final String DOCUMENTATION_FILE_ID = "documentationFileId";

	private Long id;
	private String shortName;
	private String version;
	private String title;
	private String description;
	private StatusType status;
	private Date publicationDate;
	private String organization;
	private SubmissionType fileType;
	private Boolean isCopyrighted;
	private String standardization;
	private boolean required;
	private Set<RepeatableGroup> repeatableGroups;
	private Set<DiseaseStructure> diseaseList;
	// added by Ching-Heng
	private boolean isCAT;
	private String catOid;
	private String measurementType;
	
	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getShortName() {

		return shortName;
	}

	public void setShortName(String shortName) {

		this.shortName = shortName != null ? shortName.trim() : null;
	}

	public String getVersion() {

		return version;
	}

	public void setVersion(String version) {

		this.version = version;
	}

	public String getTitle() {

		return title;
	}

	public void setTitle(String title) {

		this.title = title;
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(String description) {

		this.description = description;
	}

	public StatusType getStatus() {

		return status;
	}

	public void setStatus(StatusType status) {

		this.status = status;
	}

	public Date getPublicationDate() {

		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {

		this.publicationDate = publicationDate;
	}

	public String getOrganization() {

		return organization;
	}

	public void setOrganization(String organization) {

		this.organization = organization;
	}

	public SubmissionType getFileType() {

		return fileType;
	}

	public void setFileType(SubmissionType fileType) {

		this.fileType = fileType;
	}

	public String getStandardization() {
		return this.standardization;
	}

	public void setStandardization(String standardization) {
		this.standardization = standardization;
	}

	public boolean getRequired() {
		return this.required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public Boolean getIsCopyrighted() {

		return isCopyrighted;
	}

	public void setIsCopyrighted(Boolean isCopyrighted) {

		this.isCopyrighted = isCopyrighted;
	}

	public Set<RepeatableGroup> getRepeatableGroups() {

		if (repeatableGroups == null) {
			repeatableGroups = new HashSet<RepeatableGroup>();
		}

		return repeatableGroups;
	}

	public void setRepeatableGroups(Set<RepeatableGroup> repeatableGroups) {

		if (this.repeatableGroups == null) {
			this.repeatableGroups = new HashSet<RepeatableGroup>();
		}

		this.repeatableGroups.clear();

		if (repeatableGroups != null) {
			this.repeatableGroups.addAll(repeatableGroups);
		}
	}

	public Set<DiseaseStructure> getDiseaseList() {

		return diseaseList;
	}

	public void setDiseaseList(Set<DiseaseStructure> diseaseList) {

		this.diseaseList = diseaseList;
	}
	public boolean isCAT() {
		return isCAT;
	}

	public void setCAT(boolean isCAT) {
		this.isCAT = isCAT;
	}

	public String getCatOid() {
		return catOid;
	}

	public void setCatOid(String catOid) {
		this.catOid = catOid;
	}

	public String getMeasurementType() {
		return measurementType;
	}

	public void setMeasurementType(String measurementType) {
		this.measurementType = measurementType;
	}
	
}
