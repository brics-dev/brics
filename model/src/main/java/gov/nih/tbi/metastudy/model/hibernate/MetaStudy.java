package gov.nih.tbi.metastudy.model.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;

import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelName;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelType;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapyType;
import gov.nih.tbi.repository.model.hibernate.FundingSource;
import gov.nih.tbi.repository.model.hibernate.StudyType;

@Entity
@Table(name = "Meta_Study")
@XmlRootElement(name = "metaStudy")
@XmlAccessorType(XmlAccessType.FIELD)
public class MetaStudy implements Serializable {

	private static final long serialVersionUID = 157174590316971020L;

	@Expose
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "META_STUDY_SEQ")
	@SequenceGenerator(name = "META_STUDY_SEQ", sequenceName = "META_STUDY_SEQ", allocationSize = 1)
	private Long id;

	@Expose
	@Column(name = "TITLE")
	private String title;

	@Expose
	@Column(name = "ABSTRACT")
	private String abstractText;

	@Expose
	@Column(name = "AIMS")
	private String aimsText;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "RECRUITMENT_STATUS_ID")
	private RecruitmentStatus recruitmentStatus;

	@Expose
	@OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "STUDY_TYPE_ID")
	private StudyType studyType;

	@Column(name = "STUDY_URL")
	private String studyUrl;

	@Column(name = "PREFIX_ID")
	private String prefixId;

	@Expose
	@Column(name = "DOI")
	private String doi;

	@Column(name = "OSTI_ID")
	private Long ostiId;

	@Expose
	@Column(name = "DATE_CREATED")
	private Date dateCreated;

	@Column(name = "LAST_UPDATED_DATE")
	private Date lastUpdatedDate;

	@Expose
	@Column(name = "PUBLISHED_DATE")
	private Date publishedDate;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "STATUS_ID")
	private MetaStudyStatus status;

	@Expose
	@OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name = "FUNDING_SOURCE_ID")
	private FundingSource fundingSource;

	@Expose
	@XmlTransient
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	private Set<ResearchManagementMeta> researchMgmtMetaSet = new HashSet<ResearchManagementMeta>();


	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	private Set<MetaStudyClinicalTrial> clinicalTrialMetaSet = new HashSet<MetaStudyClinicalTrial>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	private Set<MetaStudyGrant> grantMetaSet = new HashSet<MetaStudyGrant>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	private Set<MetaStudyDocumentation> supportingDocumentationSet = new HashSet<MetaStudyDocumentation>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "metaStudy", targetEntity = MetaStudyData.class, orphanRemoval = true)
	private Set<MetaStudyData> metaStudyDataSet = new HashSet<MetaStudyData>();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	@XmlTransient
	private Set<MetaStudyKeyword> metaStudyKeywords = new HashSet<MetaStudyKeyword>();


	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	@XmlTransient
	private Set<MetaStudyLabel> metaStudyLabels = new HashSet<MetaStudyLabel>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	private Set<MetaStudyTherapeuticAgent> therapeuticAgentSet = new HashSet<MetaStudyTherapeuticAgent>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	private Set<MetaStudyTherapeuticTarget> therapeuticTargetSet = new HashSet<MetaStudyTherapeuticTarget>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	private Set<MetaStudyTherapyType> therapyTypeSet = new HashSet<MetaStudyTherapyType>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	private Set<MetaStudyModelName> modelNameSet = new HashSet<MetaStudyModelName>();

	@XmlTransient
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "meta_study_id", nullable = false)
	private Set<MetaStudyModelType> modelTypeSet = new HashSet<MetaStudyModelType>();

	public static final String STUDY_TYPE_META_ANALYSIS = "Meta Analysis";
	public static final String STUDY_ID_SCHEMA_BRICS = "BRICS Instance Generated";


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



	public String getPrefixId() {
		return prefixId;
	}

	public void setPrefixId(String prefixId) {
		this.prefixId = prefixId;
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

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
	}

	public MetaStudyStatus getStatus() {
		return status;
	}

	public void setStatus(MetaStudyStatus status) {
		this.status = status;
	}

	public Set<MetaStudyData> getMetaStudyDataSet() {
		return metaStudyDataSet;
	}

	public void setMetaStudyDataSet(Set<MetaStudyData> metaStudyDataSet) {
		this.metaStudyDataSet = metaStudyDataSet;
	}

	public void addMetaStudyData(MetaStudyData metaStudyData) {
		if (metaStudyDataSet == null) {
			metaStudyDataSet = new HashSet<MetaStudyData>();
		}
		metaStudyDataSet.add(metaStudyData);
		metaStudyData.setMetaStudy(this);
	}


	public Set<MetaStudyDocumentation> getSupportingDocumentationSet() {
		return supportingDocumentationSet;
	}

	public void setSupportingDocumentationSet(Set<MetaStudyDocumentation> supportingDocumentationSet) {
		this.supportingDocumentationSet = supportingDocumentationSet;
	}

	public void addSupportingDocumentationSet(MetaStudyDocumentation supportingDocumentationSet) {
		if (this.supportingDocumentationSet == null) {
			this.supportingDocumentationSet = new HashSet<MetaStudyDocumentation>();
		}
		this.supportingDocumentationSet.add(supportingDocumentationSet);
	}

	public void setMetaStudyKeywords(Set<MetaStudyKeyword> metaStudyKeywords) {
		this.metaStudyKeywords = metaStudyKeywords;
	}

	public Set<MetaStudyKeyword> getMetaStudyKeywords() {
		return metaStudyKeywords;
	}

	public void setMetaStudyLabels(Set<MetaStudyLabel> metaStudyLabels) {
		this.metaStudyLabels = metaStudyLabels;
	}

	public Set<MetaStudyLabel> getMetaStudyLabels() {
		return metaStudyLabels;
	}


	public boolean isPublished() {
		return this.getStatus().equals(MetaStudyStatus.PUBLISHED);
	}

	public boolean getHasPrimaryPI() {
		if (researchMgmtMetaSet != null && !researchMgmtMetaSet.isEmpty()) {
			for (ResearchManagementMeta rshMgmt : researchMgmtMetaSet) {
				if (rshMgmt.isPrimaryPI()) {
					return true;
				}
			}
		}
		return false;
	}

	public ResearchManagementMeta getPrimaryPI() {
		if (researchMgmtMetaSet != null && !researchMgmtMetaSet.isEmpty()) {
			for (ResearchManagementMeta rshMgmt : researchMgmtMetaSet) {
				if (rshMgmt.isPrimaryPI()) {
					return rshMgmt;
				}
			}
		}
		return null;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abstractText == null) ? 0 : abstractText.hashCode());
		result = prime * result + ((dateCreated == null) ? 0 : dateCreated.hashCode());
		result = prime * result + ((doi == null) ? 0 : doi.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastUpdatedDate == null) ? 0 : lastUpdatedDate.hashCode());
		result = prime * result + ((metaStudyDataSet == null) ? 0 : metaStudyDataSet.hashCode());
		result = prime * result + ((metaStudyKeywords == null) ? 0 : metaStudyKeywords.hashCode());
		result = prime * result + ((metaStudyLabels == null) ? 0 : metaStudyLabels.hashCode());
		result = prime * result + ((ostiId == null) ? 0 : ostiId.hashCode());
		result = prime * result + ((prefixId == null) ? 0 : prefixId.hashCode());
		result = prime * result + ((publishedDate == null) ? 0 : publishedDate.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((abstractText == null) ? 0 : abstractText.hashCode());
		result = prime * result + ((aimsText == null) ? 0 : aimsText.hashCode());
		result = prime * result + ((studyUrl == null) ? 0 : studyUrl.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((supportingDocumentationSet == null) ? 0 : supportingDocumentationSet.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MetaStudy)) {
			return false;
		}
		MetaStudy other = (MetaStudy) obj;
		if (abstractText == null) {
			if (other.abstractText != null) {
				return false;
			}
		} else if (!abstractText.equals(other.abstractText)) {
			return false;
		}
		if (dateCreated == null) {
			if (other.dateCreated != null) {
				return false;
			}
		} else if (!dateCreated.equals(other.dateCreated)) {
			return false;
		}
		if (doi == null) {
			if (other.doi != null) {
				return false;
			}
		} else if (!doi.equals(other.doi)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (lastUpdatedDate == null) {
			if (other.lastUpdatedDate != null) {
				return false;
			}
		} else if (!lastUpdatedDate.equals(other.lastUpdatedDate)) {
			return false;
		}
		if (metaStudyDataSet == null) {
			if (other.metaStudyDataSet != null) {
				return false;
			}
		} else if (!metaStudyDataSet.equals(other.metaStudyDataSet)) {
			return false;
		}
		if (metaStudyKeywords == null) {
			if (other.metaStudyKeywords != null) {
				return false;
			}
		} else if (!metaStudyKeywords.equals(other.metaStudyKeywords)) {
			return false;
		}
		if (metaStudyLabels == null) {
			if (other.metaStudyLabels != null) {
				return false;
			}
		} else if (!metaStudyLabels.equals(other.metaStudyLabels)) {
			return false;
		}
		if (ostiId == null) {
			if (other.ostiId != null) {
				return false;
			}
		} else if (!ostiId.equals(other.ostiId)) {
			return false;
		}
		if (prefixId == null) {
			if (other.prefixId != null) {
				return false;
			}
		} else if (!prefixId.equals(other.prefixId)) {
			return false;
		}
		if (publishedDate == null) {
			if (other.publishedDate != null) {
				return false;
			}
		} else if (!publishedDate.equals(other.publishedDate)) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		if (supportingDocumentationSet == null) {
			if (other.supportingDocumentationSet != null) {
				return false;
			}
		} else if (!supportingDocumentationSet.equals(other.supportingDocumentationSet)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		return true;
	}

	public JsonObject toJson() {
		JsonObject output = new JsonObject();
		output.add("id", new JsonPrimitive(id));
		output.add("title", new JsonPrimitive(title));
		output.add("abstractText", new JsonPrimitive(abstractText));
		output.add("aimsText", new JsonPrimitive(aimsText));
		output.add("prefixId", new JsonPrimitive(prefixId));
		output.add("doi", new JsonPrimitive(doi));
		output.add("ostiId", new JsonPrimitive(ostiId));
		output.add("dateCreated", new JsonPrimitive(dateCreated.getTime()));
		output.add("lastUpdatedDate", new JsonPrimitive(lastUpdatedDate.getTime()));
		output.add("publishedDate", new JsonPrimitive(publishedDate.getTime()));

		JsonObject metaStudyStatus = new JsonObject();
		metaStudyStatus.add("id", new JsonPrimitive(status.getId()));
		metaStudyStatus.add("name", new JsonPrimitive(status.getName()));
		output.add("status", metaStudyStatus);
		return output;
	}

	public JsonObject toJsonLight() {
		JsonObject output = new JsonObject();
		output.add("id", new JsonPrimitive(id));
		output.add("title", new JsonPrimitive(title));
		output.add("abstractText", new JsonPrimitive(abstractText));
		if (aimsText != null) {
			output.add("aimsText", new JsonPrimitive(aimsText));
		} else {
			output.add("aimsText", new JsonPrimitive(""));
		}

		if (studyUrl != null) {
			output.add("studyUrl", new JsonPrimitive(studyUrl));
		}
		if (prefixId != null) {
			output.add("prefixId", new JsonPrimitive(prefixId));
		} else {
			output.add("prefixId", new JsonPrimitive(""));
		}
		if (doi != null) {
			output.add("doi", new JsonPrimitive(doi));
		} else {
			output.add("doi", new JsonPrimitive(""));
		}

		output.add("status", new JsonPrimitive(status.getName()));
		return output;
	}

	public String getAimsText() {
		return aimsText;
	}

	public void setAimsText(String aimsText) {
		this.aimsText = aimsText;
	}

	public Set<ResearchManagementMeta> getResearchMgmtMetaSet() {
		return researchMgmtMetaSet;
	}

	public void setResearchMgmtMetaSet(Set<ResearchManagementMeta> researchMgmtMetaSet) {
		this.researchMgmtMetaSet.clear();
		if (researchMgmtMetaSet != null) {
			this.researchMgmtMetaSet.addAll(researchMgmtMetaSet);
		}
	}


	public String getPrincipalInvestigator() {
		if (researchMgmtMetaSet != null && !researchMgmtMetaSet.isEmpty()) {
			for (ResearchManagementMeta rshMgmt : researchMgmtMetaSet) {
				if (rshMgmt.isPrimaryPI()) {
					return rshMgmt.getFullName();
				}
			}
		}

		return "";
	}

	public Set<MetaStudyGrant> getGrantMetaSet() {
		return grantMetaSet;
	}

	public void setGrantMetaSet(Set<MetaStudyGrant> grantMetaSet) {
		this.grantMetaSet.clear();
		if (grantMetaSet != null) {
			this.grantMetaSet.addAll(grantMetaSet);
		}
		this.grantMetaSet = grantMetaSet;
	}

	public Set<MetaStudyClinicalTrial> getClinicalTrialMetaSet() {
		return clinicalTrialMetaSet;
	}

	public void setClinicalTrialMetaSet(Set<MetaStudyClinicalTrial> clinicalTrialMetaSet) {
		this.clinicalTrialMetaSet = clinicalTrialMetaSet;
	}

	public FundingSource getFundingSource() {
		return fundingSource;
	}

	public void setFundingSource(FundingSource fundingSource) {
		this.fundingSource = fundingSource;
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

	public Set<MetaStudyTherapeuticAgent> getTherapeuticAgentSet() {
		return therapeuticAgentSet;
	}

	public void setTherapeuticAgentSet(Set<MetaStudyTherapeuticAgent> therapeuticAgentSet) {
		if (this.therapeuticAgentSet == null) {
			this.therapeuticAgentSet = new HashSet<>();
		}

		this.therapeuticAgentSet.clear();

		if (therapeuticAgentSet != null) {
			this.therapeuticAgentSet.addAll(therapeuticAgentSet);
		}
	}

	public Set<MetaStudyTherapeuticTarget> getTherapeuticTargetSet() {
		return therapeuticTargetSet;
	}

	public void setTherapeuticTargetSet(Set<MetaStudyTherapeuticTarget> therapeuticTargetSet) {
		if (this.therapeuticTargetSet == null) {
			this.therapeuticTargetSet = new HashSet<>();
		}
		
		this.therapeuticTargetSet.clear();
		
		if(therapeuticTargetSet != null) {
			this.therapeuticTargetSet.addAll(therapeuticTargetSet);
		}
	}

	public Set<MetaStudyTherapyType> getTherapyTypeSet() {
		return therapyTypeSet;
	}

	public void setTherapyTypeSet(Set<MetaStudyTherapyType> therapyTypeSet) {
		if (this.therapyTypeSet == null) {
			this.therapyTypeSet = new HashSet<>();
		}
		
		this.therapyTypeSet.clear();
		
		if(therapyTypeSet != null) {
			this.therapyTypeSet.addAll(therapyTypeSet);
		}
	}

	public Set<MetaStudyModelName> getModelNameSet() {
		return modelNameSet;
	}

	public void setModelNameSet(Set<MetaStudyModelName> modelNameSet) {
		if (this.modelNameSet == null) {
			this.modelNameSet = new HashSet<>();
		}
		
		this.modelNameSet.clear();
		
		if(modelNameSet != null) {
			this.modelNameSet.addAll(modelNameSet);
		}
	}

	public Set<MetaStudyModelType> getModelTypeSet() {
		return modelTypeSet;
	}

	public void setModelTypeSet(Set<MetaStudyModelType> modelTypeSet) {
		if (this.modelTypeSet == null) {
			this.modelTypeSet = new HashSet<>();
		}
		
		this.modelTypeSet.clear();
		
		if(modelTypeSet != null) {
			this.modelTypeSet.addAll(modelTypeSet);
		}
	}
}
