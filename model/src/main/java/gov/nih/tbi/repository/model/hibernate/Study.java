package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;

import com.google.gson.annotations.Expose;

import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.alzped.StudyModelName;
import gov.nih.tbi.repository.model.alzped.StudyModelType;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;

/**
 * Model for storing studies
 * 
 * @author Francis Chen
 */
@Entity
@Table(name = "STUDY")
@XmlRootElement(name = "study")
@XmlAccessorType(XmlAccessType.FIELD)
public class Study implements Serializable {

	private static final long serialVersionUID = -3963107794602437604L;

	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDY_SEQ")
	@SequenceGenerator(name = "STUDY_SEQ", sequenceName = "STUDY_SEQ", allocationSize = 1)
	private Long id;

	@Transient
	private Set<SubmissionType> subTypes; // A cache of the submission types that are part of this

	@Expose
	@Column(name = "TITLE")
	private String title;

	@Column(name = "PREFIX_ID")
	private String prefixedId;

	@Expose
	@Column(name = "ABSTRACT")
	private String abstractText;	// abstract is reserved in java

	@Expose
	@Column(name = "GOALS")
	private String goals;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "RECRUITMENT_STATUS_ID")
	private RecruitmentStatus recruitmentStatus;

	@Expose
	@OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "STUDY_TYPE_ID")
	private StudyType studyType;

	@Column(name = "STUDY_URL")
	private String studyUrl;

	@Column(name = "NUMBER_OF_SUBJECTS", nullable = true)
	private Integer numberOfSubjects;

	@OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "FUNDING_SOURCE_ID")
	private FundingSource fundingSource;

	@Expose
	@Column(name = "DATE_CREATED")
	private Date dateCreated;

	@Column(name = "STUDY_STARTED")
	private Date studyStartDate;

	@Expose
	@Column(name = "STUDY_ENDED")
	private Date studyEndDate;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "STUDY_STATUS_ID")
	private StudyStatus studyStatus;

	@Expose
	@Column(name = "DOI")
	private String doi;

	@Column(name = "OSTI_ID")
	private Long ostiId;

	@XmlTransient
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "GRAPHIC_FILE_ID")
	private UserFile graphicFile;

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<StudySponsorInfo> sponsorInfoSet = new HashSet<StudySponsorInfo>();

	@Expose
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	@XmlElementWrapper(name = "researchMgmtSet")
	@XmlElement(name = "researchManagement")
	private Set<ResearchManagement> researchMgmtSet = new HashSet<ResearchManagement>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<StudySite> studySiteSet = new HashSet<StudySite>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<ClinicalTrial> clinicalTrialSet = new HashSet<ClinicalTrial>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<Grant> grantSet = new HashSet<Grant>();

	@XmlTransient
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<StudyKeyword> keywordSet = new HashSet<StudyKeyword>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<StudyTherapeuticAgent> therapeuticAgentSet = new HashSet<StudyTherapeuticAgent>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<StudyTherapeuticTarget> therapeuticTargetSet = new HashSet<StudyTherapeuticTarget>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<StudyTherapyType> therapyTypeSet = new HashSet<StudyTherapyType>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<StudyModelName> modelNameSet = new HashSet<StudyModelName>();


	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<StudyModelType> modelTypeSet = new HashSet<StudyModelType>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<StudySupportingDocumentation> supportingDocumentationSet = new HashSet<StudySupportingDocumentation>();

	@XmlTransient
	@Expose
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "study", targetEntity = Dataset.class, orphanRemoval = true)
	@javax.persistence.OrderBy("submitDate")
	private List<Dataset> datasetSet = new ArrayList<Dataset>();

	@XmlTransient
	@Expose
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "study_id", nullable = false)
	private Set<StudyForm> studyForms = new HashSet<StudyForm>();

	@XmlTransient
	@Expose
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "DATA_SUBMISSION_DOCUMENT_ID")
	private UserFile dataSubmissionDocument;

	@Transient
	private String principalInvestigator;


	@Transient
	private Long datasetCount;

	private static SimpleDateFormat isoFormatting = new SimpleDateFormat("yyyy-MM-dd");

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<SubmissionType> getSubTypes() {
		return subTypes;
	}

	public void setSubTypes(Set<SubmissionType> subTypes) {
		this.subTypes = subTypes;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPrefixedId() {
		return prefixedId;
	}

	public void setPrefixedId(String prefixedId) {
		this.prefixedId = prefixedId;
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

	public void setRecruitmentStatus(RecruitmentStatus recruitmentStatus) {
		this.recruitmentStatus = recruitmentStatus;
	}

	public StudyType getStudyType() {
		return studyType;
	}

	public void setStudyType(StudyType studyType) {
		this.studyType = studyType;
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

	public void setFundingSource(FundingSource fundingSource) {
		this.fundingSource = fundingSource;
	}

	public StudyStatus getStudyStatus() {
		return studyStatus;
	}

	public void setStudyStatus(StudyStatus studyStatus) {
		this.studyStatus = studyStatus;
	}

	public Set<StudySponsorInfo> getSponsorInfoSet() {
		return sponsorInfoSet;
	}

	public void setSponsorInfoSet(Set<StudySponsorInfo> sponsorInfoSet) {
		this.sponsorInfoSet.clear();
		if (sponsorInfoSet != null) {
			this.sponsorInfoSet.addAll(sponsorInfoSet);
		}
	}

	public Set<ResearchManagement> getResearchMgmtSet() {
		return researchMgmtSet;
	}

	public void setResearchMgmtSet(Set<ResearchManagement> researchMgmtSet) {
		this.researchMgmtSet.clear();
		if (researchMgmtSet != null) {
			this.researchMgmtSet.addAll(researchMgmtSet);
		}
	}

	public Set<StudySite> getStudySiteSet() {
		return studySiteSet;
	}

	public void setStudySiteSet(Set<StudySite> studySiteSet) {
		this.studySiteSet.clear();
		if (studySiteSet != null) {
			this.studySiteSet.addAll(studySiteSet);
		}
	}

	public Set<Grant> getGrantSet() {
		return grantSet;
	}

	public void setGrantSet(Set<Grant> grantSet) {
		this.grantSet.clear();
		if (grantSet != null) {
			this.grantSet.addAll(grantSet);
		}
	}

	public Set<StudyKeyword> getKeywordSet() {
		return keywordSet;
	}

	public void setKeywordSet(Set<StudyKeyword> keywordSet) {
		this.keywordSet = keywordSet;
	}

	public Set<StudySupportingDocumentation> getSupportingDocumentationSet() {
		return supportingDocumentationSet;
	}

	public void setSupportingDocumentationSet(Set<StudySupportingDocumentation> supportingDocumentationSet) {
		this.supportingDocumentationSet = supportingDocumentationSet;
	}

	public UserFile getDataSubmissionDocument() {
		return dataSubmissionDocument;
	}

	public void setDataSubmissionDocument(UserFile dataSubmissionDocument) {
		this.dataSubmissionDocument = dataSubmissionDocument;
	}

	public Set<ClinicalTrial> getClinicalTrialSet() {
		return clinicalTrialSet;
	}

	public void setClinicalTrialSet(Set<ClinicalTrial> clinicalTrialSet) {
		this.clinicalTrialSet.clear();
		if (clinicalTrialSet != null) {
			this.clinicalTrialSet.addAll(clinicalTrialSet);
		}
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setDateCreated(String dateCreated) {

		try {
			setDateCreated(isoFormatting.parse(dateCreated));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public Date getStudyStartDate() {
		return studyStartDate;
	}

	public void setStudyStartDate(Date studyStartDate) {
		this.studyStartDate = studyStartDate;
	}

	public String getStartDate() {

		Date startDate = getStudyStartDate();
		if (startDate == null) {
			return "";
		}
		return isoFormatting.format(startDate);
	}

	public void setStudyStartDate(String studyStartDate) {

		try {
			Date date = isoFormatting.parse(studyStartDate);
			setStudyStartDate(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public Date getStudyEndDate() {
		return studyEndDate;
	}

	public void setStudyEndDate(Date studyEndDate) {
		this.studyEndDate = studyEndDate;
	}

	public String getEndDate() {

		Date endDate = getStudyEndDate();
		if (endDate == null) {
			return "";
		}
		return isoFormatting.format(endDate);
	}

	public void setStudyEndDate(String studyEndDate) {

		try {
			setStudyEndDate(isoFormatting.parse(studyEndDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public String getStudyDuration() {
		if (studyStartDate != null && studyEndDate != null) {
			long diffTime = studyEndDate.getTime() - studyStartDate.getTime();
			int diffInDays = (int) (diffTime / (1000 * 60 * 60 * 24));
			return diffInDays + " days";
		} else {
			return null;
		}
	}

	public boolean getIsPrivate() {
		return StudyStatus.PRIVATE.equals(studyStatus);
	}

	public boolean getIsPublic() {
		return StudyStatus.PUBLIC.equals(studyStatus);
	}

	public List<Dataset> getDatasetSet() {
		return datasetSet;
	}

	public void setDatasetSet(List<Dataset> datasetSet) {

		if (this.datasetSet == null) {
			this.datasetSet = new ArrayList<Dataset>();
		} else {
			this.datasetSet.clear();
		}

		if (datasetSet != null) {
			this.datasetSet.addAll(datasetSet);
		}
	}

	public Set<StudyForm> getStudyForms() {
		return studyForms;
	}

	public void setStudyForms(Set<StudyForm> studyForms) {

		if (this.studyForms == null) {
			this.studyForms = new HashSet<StudyForm>();
		} else {
			this.studyForms.clear();
		}

		this.studyForms.addAll(studyForms);
	}


	/**
	 * Returns true if this study contains a genomic file in a dataset, false otherwise
	 * 
	 * @return
	 */
	public boolean getIsGenomic() {
		return subTypes != null && subTypes.contains(SubmissionType.GENOMICS);
	}

	public boolean getIsClinical() {
		return subTypes != null && subTypes.contains(SubmissionType.CLINICAL);
	}

	public boolean getIsImaging() {
		return subTypes != null && subTypes.contains(SubmissionType.IMAGING);
	}

	public boolean getHasPrimaryPI() {
		if (researchMgmtSet != null && !researchMgmtSet.isEmpty()) {
			for (ResearchManagement rshMgmt : researchMgmtSet) {
				if (rshMgmt.isPrimaryPI()) {
					return true;
				}
			}
		}
		return false;
	}

	public ResearchManagement getPrimaryPI() {
		if (researchMgmtSet != null && !researchMgmtSet.isEmpty()) {
			for (ResearchManagement rshMgmt : researchMgmtSet) {
				if (rshMgmt.isPrimaryPI()) {
					return rshMgmt;
				}
			}
		}
		return null;
	}

	public void setPrincipalInvestigator(String principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	// Returns the full name of the primary principal investigator
	public String getPrincipalInvestigator() {

		if (StringUtils.isEmpty(principalInvestigator)) {
			if (researchMgmtSet != null && !researchMgmtSet.isEmpty()) {
				for (ResearchManagement rshMgmt : researchMgmtSet) {
					if (rshMgmt.isPrimaryPI()) {
						this.setPrincipalInvestigator(rshMgmt.getFullName());
						break;
					}
				}
			}
		}
		return principalInvestigator;
	}

	public boolean getHasPrimarySite() {
		if (studySiteSet != null && !studySiteSet.isEmpty()) {
			for (StudySite site : studySiteSet) {
				if (site.isPrimary()) {
					return true;
				}
			}
		}

		return false;
	}

	public StudySite getPrimarySite() {
		if (studySiteSet != null && !studySiteSet.isEmpty()) {
			for (StudySite site : studySiteSet) {
				if (site.isPrimary()) {
					return site;
				}
			}
		}

		return null;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public Long getOstiId() {
		return ostiId;
	}

	public void setOstiId(Long ostiId) {
		this.ostiId = ostiId;
	}

	public UserFile getGraphicFile() {
		return graphicFile;
	}

	public void setGraphicFile(UserFile graphicFile) {
		this.graphicFile = graphicFile;
	}

	public String getDisplayKeywordSet() {
		StringBuffer displayKeywordSet = new StringBuffer();

		for (Iterator<StudyKeyword> it = getKeywordSet().iterator(); it.hasNext();) {
			StudyKeyword keyword = it.next();
			displayKeywordSet.append(keyword.getKeyword());
			if (it.hasNext()) {
				displayKeywordSet.append(", ");
			}
		}

		return displayKeywordSet.toString();
	}

	public Set<StudyTherapeuticTarget> getTherapeuticTargetSet() {
		return therapeuticTargetSet;
	}

	public void setTherapeuticTargetSet(Set<StudyTherapeuticTarget> therapeuticTargetSet) {
		if (this.therapeuticTargetSet == null) {
			this.therapeuticTargetSet = new HashSet<>();
		}

		this.therapeuticTargetSet.clear();

		if (therapeuticTargetSet != null) {
			this.therapeuticTargetSet.addAll(therapeuticTargetSet);
		}
	}

	public Set<StudyTherapeuticAgent> getTherapeuticAgentSet() {
		return therapeuticAgentSet;
	}

	public void setTherapeuticAgentSet(Set<StudyTherapeuticAgent> therapeuticAgentSet) {
		if (this.therapeuticAgentSet == null) {
			this.therapeuticAgentSet = new HashSet<>();
		}

		this.therapeuticAgentSet.clear();

		if (therapeuticAgentSet != null) {
			this.therapeuticAgentSet.addAll(therapeuticAgentSet);
		}
	}

	public Set<StudyTherapyType> getTherapyTypeSet() {
		return therapyTypeSet;
	}

	public void setTherapyTypeSet(Set<StudyTherapyType> therapyTypeSet) {
		if (this.therapyTypeSet == null) {
			this.therapyTypeSet = new HashSet<>();
		}

		this.therapyTypeSet.clear();

		if (therapyTypeSet != null) {
			this.therapyTypeSet.addAll(therapyTypeSet);
		}
	}

	public Set<StudyModelName> getModelNameSet() {
		return modelNameSet;
	}

	public void setModelNameSet(Set<StudyModelName> modelNameSet) {
		if (this.modelNameSet == null) {
			this.modelNameSet = new HashSet<>();
		}

		this.modelNameSet.clear();

		if (modelNameSet != null) {
			this.modelNameSet.addAll(modelNameSet);
		}
	}

	public Set<StudyModelType> getModelTypeSet() {
		return modelTypeSet;
	}

	public void setModelTypeSet(Set<StudyModelType> modelTypeSet) {
		if (this.modelTypeSet == null) {
			this.modelTypeSet = new HashSet<>();
		}

		this.modelTypeSet.clear();

		if (modelTypeSet != null) {
			this.modelTypeSet.addAll(modelTypeSet);
		}
	}

	// Do not change this. This is used by the submission tool.
	@Override
	public String toString() {
		return this.getTitle();
	}


	public Long getDatasetCount() {
		return datasetCount;
	}

	public void setDatasetCount(Long datasetCount) {
		this.datasetCount = datasetCount;
	}
}
