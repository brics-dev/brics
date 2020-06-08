package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.DatasetFileStatus;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.model.AbstractDataset;
import gov.nih.tbi.repository.model.SubmissionType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.gson.annotations.Expose;

/**
 * Data model for Datasets
 * 
 * @author Francis Chen
 * 
 */
@Entity
@Table(name = "DATASET")
@XmlRootElement(name = "dataset")
@XmlAccessorType(XmlAccessType.FIELD)
public class Dataset extends AbstractDataset implements Serializable {

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

	@Expose
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
	@JoinColumn(name = "STUDY_ID", nullable=false)
	private Study study;

	@ManyToOne
	@JoinColumn(name = "REVIEWER_ID")
	private User reviewer;

	@ManyToOne
	@JoinColumn(name = "VERIFIER_ID")
	private User verifier;

	@Column(name = "PUBLICATION_DATE")
	private Date publicationDate;

	@Column(name = "SHARE_DATE")
	private Date shareDate;

	@Column(name = "IS_DERIVED")
	private Boolean isDerived;

	@Column(name = "IS_PROFORMS_SUBMISSION")
	private Boolean isProformsSubmission;

	@Column(name = "IS_SUBJECT_SUBMITTED")
	private Boolean isSubjectSubmitted;

	@ElementCollection(targetClass = SubmissionType.class, fetch = FetchType.EAGER)
	@CollectionTable(name = "DATASET_SUBMISSIONTYPE", joinColumns = @JoinColumn(name = "DATASET_ID"))
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "SUBMISSIONTYPE_ID")
	private Set<SubmissionType> submissionTypes;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataset", targetEntity = DatasetFile.class, orphanRemoval = true)
	private Set<DatasetFile> datasetFileSet;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataset", targetEntity = DatasetDataStructure.class, orphanRemoval = true)
	@XmlTransient
	private Set<DatasetDataStructure> datasetDataStructure;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataset", targetEntity = DatasetSubject.class, orphanRemoval = true)
	@XmlTransient
	private Set<DatasetSubject> datasetSubject;

	@Column(name = "RECORD_COUNT")
	private Long recordCount;

	@Column(name = "ADMINISTERED_FORM_ID")
	private Long administeredFormId;

	//
	// @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataset", targetEntity =
	// UserDownloadQueue.class, orphanRemoval = true)
	// @XmlTransient
	// private Set<UserDownloadQueue> userDownloadQueue;

	public Dataset() {

		submissionTypes = new HashSet<SubmissionType>();
		datasetFileSet = new HashSet<DatasetFile>();
		datasetDataStructure = new HashSet<DatasetDataStructure>();
		datasetSubject = new HashSet<DatasetSubject>();
		// userDownloadQueue = new HashSet<UserDownloadQueue> ();
	}

	public Dataset(Dataset clone) {
		this.id = clone.id;
		this.name = clone.name;
		this.submitter = clone.submitter;

		if (clone.getSubmissionDate() != null) {
			this.submitDate = new Date(clone.submitDate.getTime());
		}

		this.datasetStatus = clone.datasetStatus;

		this.datasetRequestStatus = clone.datasetRequestStatus;

		this.prefixedId = clone.prefixedId;

		this.study = clone.study;

		this.verifier = clone.verifier;

		this.isDerived = clone.isDerived;

		if (clone.publicationDate != null) {
			this.publicationDate = new Date(clone.publicationDate.getTime());
		}

		if (clone.shareDate != null) {
			this.shareDate = new Date(clone.shareDate.getTime());
		}

		if (clone.datasetFileSet != null) {
			this.datasetFileSet = new HashSet<DatasetFile>();

			for (DatasetFile cloneFile : clone.datasetFileSet) {
				datasetFileSet.add(new DatasetFile(cloneFile));
			}
		}



		if (clone.submissionTypes != null) {
			this.submissionTypes = new HashSet<SubmissionType>();
			for (SubmissionType s : clone.submissionTypes) {
				submissionTypes.add(s);
			}
		}

		this.datasetDataStructure = clone.datasetDataStructure;
		this.datasetSubject = clone.datasetSubject;
		this.isProformsSubmission = clone.isProformsSubmission;
		this.administeredFormId = clone.administeredFormId;
		this.isSubjectSubmitted = clone.isSubjectSubmitted;
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

	public String getPrefixedId() {

		return prefixedId;
	}

	public void setPrefixedId(String prefixedId) {

		this.prefixedId = prefixedId;
	}

	public Study getStudy() {

		return study;
	}

	public Set<DatasetFile> getDatasetFileSet() {

		return datasetFileSet;
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

	public void setSubmissionTypes(Set<SubmissionType> set) {
		this.submissionTypes = set;
	}

	public Set<SubmissionType> getSubmissionTypes() {
		return this.submissionTypes;
	}

	public void setDatasetFileSet(Set<DatasetFile> datasetFileSet) {

		if (this.datasetFileSet == null) {
			this.datasetFileSet = new HashSet<DatasetFile>();
		}

		this.datasetFileSet.clear();

		if (datasetFileSet != null) {
			this.datasetFileSet.addAll(datasetFileSet);
		}
	}

	public Date getPublicationDate() {

		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {

		this.publicationDate = publicationDate;
	}


	public Date getShareDate() {
		return shareDate;
	}

	public void setShareDate(Date shareDate) {
		this.shareDate = shareDate;
	}


	public DatasetStatus getDatasetRequestStatus() {

		return datasetRequestStatus;
	}

	public void setDatasetRequestStatus(DatasetStatus datasetRequestStatus) {

		this.datasetRequestStatus = datasetRequestStatus;
	}
	
	public String getDatasetStatusWithRequestStatus() {
		if(datasetRequestStatus != null) {
			return datasetStatus.getName() + "--Requested " + datasetRequestStatus.getVerb();
		}
		return getDatasetStatus().getName();
	}

	/**
	 * Returns the list of file types in this dataset as a single comma separated list ie- "Genomics, Clinical
	 * Assessment, Imaging"
	 * 
	 * @return
	 */
	public String getFileTypeString() {

		List<SubmissionType> fileTypeList = new ArrayList<SubmissionType>(this.getSubmissionTypes());
		String out = ModelConstants.EMPTY_STRING;

		if (fileTypeList != null) {
			for (int i = 0; i < fileTypeList.size(); i++) {
				if (!SubmissionType.DATA_FILE.equals(fileTypeList.get(i))) {
					if (!ModelConstants.EMPTY_STRING.equals(out)) {
						out += ", ";
					}

					out += fileTypeList.get(i).getType();
				}
			}
		}

		return out;
	}

	public User getReviewer() {

		return reviewer;
	}

	public User getVerifier() {

		return verifier;
	}

	public void setReviewer(User reviewer) {

		this.reviewer = reviewer;
	}

	public void setVerifier(User verifier) {

		this.verifier = verifier;
	}

	public List<DatasetFile> getPendingDatasetFile() {

		List<DatasetFile> fileList = new ArrayList<DatasetFile>();
		for (DatasetFile datasetFile : this.datasetFileSet) {
			if (DatasetFileStatus.PENDING.equals(datasetFile.getDatasetFileStatus())) {
				fileList.add(datasetFile);
			}
		}
		return fileList;
	}

	public Set<DatasetDataStructure> getDatasetDataStructure() {

		return datasetDataStructure;
	}

	public void setDatasetDataStructure(Set<DatasetDataStructure> datasetDataStructure) {

		this.datasetDataStructure = datasetDataStructure;
	}

	public Set<DatasetSubject> getDatasetSubject() {

		return datasetSubject;
	}

	public void setDatasetSubject(Set<DatasetSubject> datasetSubject) {

		if (this.datasetSubject == null) {
			this.datasetSubject = new HashSet<DatasetSubject>();
		}

		this.datasetSubject.clear();

		if (datasetSubject != null) {
			this.datasetSubject.addAll(datasetSubject);
		}
	}

	public boolean containsGuid(String guid) {

		for (DatasetSubject subject : datasetSubject) {
			if (guid.equalsIgnoreCase(subject.getGuid())) {
				return true;
			}
		}

		return false;
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

	public boolean hasData() {
		return DatasetStatus.PRIVATE.equals(this.getDatasetStatus())
				|| DatasetStatus.SHARED.equals(this.getDatasetStatus())
				|| DatasetStatus.ARCHIVED.equals(this.getDatasetStatus());
	}

	/*
	 * Generate dataset detail when email OPS team to report Error status
	 */
	public String getDatasetDetail() {
		return this.getName() + ", " + this.getId() + " ["
				+ this.getSubmitter().getFullName() + ": " + this.getSubmitter().getEmail() + "]";
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

	public Boolean getIsSubjectSubmitted() {
		return isSubjectSubmitted;
	}

	public void setIsSubjectSubmitted(Boolean isSubjectSubmitted) {
		this.isSubjectSubmitted = isSubjectSubmitted;
	}

	public Long getAdministeredFormId() {
		return administeredFormId;
	}

	public void setAdministeredFormId(Long administeredFormId) {
		this.administeredFormId = administeredFormId;
	}

	public Dataset getClonedDataset(Dataset dataset) {
		Dataset datasetClone = new Dataset();
		datasetClone.id = dataset.id;
		datasetClone.name = dataset.name;
		datasetClone.submitter = dataset.submitter;
		datasetClone.submitDate = dataset.submitDate;
		datasetClone.datasetStatus = dataset.datasetStatus;
		datasetClone.datasetRequestStatus = dataset.datasetRequestStatus;
		datasetClone.reviewer = dataset.reviewer;
		datasetClone.prefixedId = dataset.prefixedId;
		datasetClone.study = dataset.study;
		datasetClone.verifier = dataset.verifier;
		datasetClone.isDerived = dataset.isDerived;
		datasetClone.publicationDate = dataset.publicationDate;
		datasetClone.shareDate = dataset.shareDate;
		datasetClone.recordCount = dataset.recordCount;
		datasetClone.isProformsSubmission = dataset.isProformsSubmission;
		datasetClone.administeredFormId = dataset.administeredFormId;
		datasetClone.isSubjectSubmitted = dataset.isSubjectSubmitted;
		return datasetClone;
	}

	// public Set<UserDownloadQueue> getUserDownloadQueue()
	// {
	//
	// return userDownloadQueue;
	// }
	//
	//
	// public void setUserDownloadQueue(Set<UserDownloadQueue> userDownloadQueue)
	// {
	// if(this.userDownloadQueue == null)
	// {
	// this.userDownloadQueue = new HashSet<UserDownloadQueue> ();
	// }
	//
	// this.userDownloadQueue.clear();
	//
	// if(userDownloadQueue != null)
	// {
	// this.userDownloadQueue.addAll(userDownloadQueue);
	// }
	// }
}
