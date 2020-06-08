package gov.nih.tbi.dictionary.model.rdf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.Expose;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.FormStructureStandardization;
import gov.nih.tbi.dictionary.model.InstanceRequiredFor;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.FormLabel;
import gov.nih.tbi.repository.model.SubmissionType;

@XmlRootElement(name = "SemanticFormStructure")
@XmlAccessorType(XmlAccessType.FIELD)
public class SemanticFormStructure implements Serializable {

	private static final long serialVersionUID = -5079308011912861658L;

	private String version;
	private String uri;
	@Expose
	private String title;
	@Expose
	private String shortName;
	private String description;
	private Date modifiedDate;
	private Date dateCreated;
	private Long modifiedUserId;
	private List<Disease> diseases = new ArrayList<Disease>();
	private List<FormLabel> formLabels = new ArrayList<FormLabel>();
	private String organization;
	private SubmissionType submissionType;
	private StatusType status;
	private String createdBy;
	private FormStructureStandardization standardization;
	private List<InstanceRequiredFor> instancesRequiredFor; // this list will contain all org names that this form has
															// been marked as
	private Boolean isCopyrighted;
	// required

	public String getVersion() {

		return version;
	}

	public void setVersion(String version) {

		this.version = version;
	}

	public String getUri() {

		return uri;
	}

	public void setUri(String uri) {

		this.uri = uri;
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

	public List<Disease> getDiseases() {

		return diseases;
	}

	public void setDiseases(List<Disease> diseases) {

		this.diseases = diseases;
	}

	public List<FormLabel> getFormLabels() {
		return formLabels;
	}

	public void setFormLabels(List<FormLabel> formLabels) {
		this.formLabels = formLabels;
	}

	/**
	 * @return the modifiedDate
	 */
	public Date getModifiedDate() {

		return modifiedDate;
	}

	/**
	 * @param modifiedDate the modifiedDate to set
	 */
	public void setModifiedDate(Date modifiedDate) {

		this.modifiedDate = modifiedDate;
	}

	/**
	 * @return the modifiedUserId
	 */
	public Long getModifiedUserId() {

		return modifiedUserId;
	}

	/**
	 * @param modifiedAccount the modifiedUserId to set
	 */
	public void setModifiedUserId(Long modifiedUserId) {

		this.modifiedUserId = modifiedUserId;
	}

	public String getShortName() {

		return shortName;
	}

	public void setShortName(String shortName) {

		this.shortName = shortName;
	}

	public String getShortNameAndVersion() {

		return shortName + "V" + version;
	}

	public void setOrganization(String organization) {

		this.organization = organization;
	}

	public String getOrganization() {

		return organization;
	}

	/**
	 * @return the status
	 */
	public StatusType getStatus() {

		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(StatusType status) {

		this.status = status;
	}

	public SubmissionType getSubmissionType() {

		return submissionType;
	}

	public void setSubmissionType(String submissionType) {

		this.submissionType = SubmissionType.getObject(submissionType);
	}

	public void setSubmissionType(SubmissionType submissionType) {

		this.submissionType = submissionType;
	}

	public Date getDateCreated() {

		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {

		this.dateCreated = dateCreated;
	}

	public String getCreatedBy() {

		return createdBy;
	}

	public void setCreatedBy(String createdBy) {

		this.createdBy = createdBy;
	}

	public void setDateCreated(String dateCreated) {

		this.dateCreated = BRICSTimeDateUtil.stringToDate(dateCreated);
	}

	public void setDateCreated(long dateCreated) {

		this.dateCreated = new Date(dateCreated);
	}

	public FormStructureStandardization getStandardization() {
		return standardization;
	}

	public void setStandardization(FormStructureStandardization standardization) {
		this.standardization = standardization;
	}
	
	public Boolean getIsCopyrighted() {

		return isCopyrighted;
	}

	public void setIsCopyrighted(Boolean isCopyrighted) {

		this.isCopyrighted = isCopyrighted;
	}

	// gets the list of instances that marked this form structure as required
	// if empty no instances marked form as required
	public List<InstanceRequiredFor> getInstancesRequiredFor() {
		return instancesRequiredFor;
	}

	public void addAllInstancesRequiredForStrings(List<String> instancesRequiredForStrings) {
		if (this.instancesRequiredFor == null) {
			this.instancesRequiredFor = new ArrayList<InstanceRequiredFor>();
		}

		for (String requiredInstance : instancesRequiredForStrings) {
			InstanceRequiredFor instanceRequiredFor = new InstanceRequiredFor(requiredInstance);
			if (!this.instancesRequiredFor.contains(instanceRequiredFor)) {
				this.instancesRequiredFor.add(instanceRequiredFor);
			}
		}
	}

	public void addAllInstancesRequiredFor(List<InstanceRequiredFor> instancesRequiredFor) {
		if (this.instancesRequiredFor == null) {
			this.instancesRequiredFor = new ArrayList<InstanceRequiredFor>();
		}

		for (InstanceRequiredFor requiredInstance : instancesRequiredFor) {
			if (!this.instancesRequiredFor.contains(requiredInstance)) {
				this.instancesRequiredFor.add(requiredInstance);
			}
		}
	}

	public void addInstancesRequiredFor(String instancesRequiredForString) {
		if (this.instancesRequiredFor == null) {
			this.instancesRequiredFor = new ArrayList<InstanceRequiredFor>();
		}

		InstanceRequiredFor instanceRequiredFor = new InstanceRequiredFor(instancesRequiredForString);

		if (instancesRequiredForString != null && !instancesRequiredForString.trim().equals("")
				&& !this.instancesRequiredFor.contains(instanceRequiredFor)) {
			this.instancesRequiredFor.add(instanceRequiredFor);
		}
	}
}
