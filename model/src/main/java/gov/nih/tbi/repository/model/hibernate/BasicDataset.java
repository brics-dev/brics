package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.model.AbstractDataset;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Basic model for dataset that does not contain and many-to-one or one-to-many relations
 * 
 * @author Francis Chen
 * 
 */
@Entity
@Table(name = "DATASET")
public class BasicDataset extends AbstractDataset implements Serializable {

	private static final long serialVersionUID = -2627365259205294466L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATASET_SEQ")
	@SequenceGenerator(name = "DATASET_SEQ", sequenceName = "DATASET_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "NAME")
	private String name;

	@ManyToOne
	@JoinColumn(name = "SUBMITTER_ID")
	private User submitter;

	@Column(name = "SUBMIT_DATE")
	private Date submitDate;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "DATASET_STATUS_ID")
	private DatasetStatus datasetStatus;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "DATASET_REQUEST_STATUS_ID")
	private DatasetStatus datasetRequestStatus;

	@Column(name = "PREFIX_ID")
	private String prefixedId;

	@ManyToOne
	@JoinColumn(name = "STUDY_ID")
	private Study study;

	@ManyToOne
	@JoinColumn(name = "REVIEWER_ID")
	private User reviewer;

	@ManyToOne
	@JoinColumn(name = "VERIFIER_ID")
	private User verifier;

	@Column(name = "PUBLICATION_DATE")
	private Date publicationDate;

	@Column(name = "IS_DERIVED")
	private Boolean isDerived;

	@Column(name = "RECORD_COUNT")
	private Long recordCount;

	@Column(name = "IS_PROFORMS_SUBMISSION")
	private Boolean isProformsSubmission;

	@Column(name = "ADMINISTERED_FORM_ID")
	private Long administeredFormId;

	@Column(name = "IS_SUBJECT_SUBMITTED")
	private Boolean isSubjectSubmitted;

	public BasicDataset() {

	}

	public BasicDataset(Dataset dataset) {
		this.id = dataset.getId();
		this.name = dataset.getName();
		this.submitter = dataset.getSubmitter();
		this.submitDate = dataset.getSubmitDate();
		this.datasetStatus = dataset.getDatasetStatus();
		this.datasetRequestStatus = dataset.getDatasetRequestStatus();
		this.prefixedId = dataset.getPrefixedId();
		this.study = dataset.getStudy();
		this.reviewer = dataset.getReviewer();
		this.verifier = dataset.getVerifier();
		this.publicationDate = dataset.getPublicationDate();
		this.isDerived = dataset.getIsDerived();
		this.recordCount = dataset.getRecordCount();
		this.isProformsSubmission = dataset.getIsProformsSubmission();
		this.administeredFormId = dataset.getAdministeredFormId();
		this.isSubjectSubmitted = dataset.getIsSubjectSubmitted();
	}

	public Long getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public User getSubmitter() {

		return submitter;
	}

	public Date getSubmitDate() {

		return submitDate;
	}

	public DatasetStatus getDatasetStatus() {

		return datasetStatus;
	}

	public Study getStudy() {

		return study;
	}

	public User getReviewer() {

		return reviewer;
	}

	public User getVerifier() {

		return verifier;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public void setName(String name) {

		this.name = name;
	}

	public void setSubmitter(User submitter) {

		this.submitter = submitter;
	}

	public void setSubmitDate(Date submitDate) {

		this.submitDate = submitDate;
	}

	public void setDatasetStatus(DatasetStatus datasetStatus) {

		this.datasetStatus = datasetStatus;
	}

	public void setStudy(Study study) {

		this.study = study;
	}

	public void setReviewer(User reviewer) {

		this.reviewer = reviewer;
	}

	public void setVerifier(User verifier) {

		this.verifier = verifier;
	}

	public String getPrefixedId() {

		return prefixedId;
	}

	public void setPrefixedId(String prefixedId) {

		this.prefixedId = prefixedId;
	}

	public Date getPublicationDate() {

		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {

		this.publicationDate = publicationDate;
	}

	public DatasetStatus getDatasetRequestStatus() {

		return datasetRequestStatus;
	}

	public void setDatasetRequestStatus(DatasetStatus datasetRequestStatus) {

		this.datasetRequestStatus = datasetRequestStatus;
	}

	@Override
	public Date getSubmissionDate() {

		return getSubmitDate();
	}

	public Boolean getIsDerived() {
		return isDerived;
	}

	public void setIsDerived(Boolean isDerived) {
		this.isDerived = isDerived;
	}

	public Long getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(Long recordCount) {
		this.recordCount = recordCount;
	}

	public Boolean getIsProformsSubmission() {
		return isProformsSubmission;
	}

	public void setIsProformsSubmission(Boolean isProformsSubmission) {
		this.isProformsSubmission = isProformsSubmission;
	}

	public Long getAdministeredFormId() {
		return administeredFormId;
	}

	public void setAdministeredFormId(Long administeredFormId) {
		this.administeredFormId = administeredFormId;
	}

	public Boolean getIsSubjectSubmitted() {
		return isSubjectSubmitted;
	}

	public void setIsSubjectSubmitted(Boolean isSubjectSubmitted) {
		this.isSubjectSubmitted = isSubjectSubmitted;
	}
}
