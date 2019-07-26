package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.commons.model.StudyStatus;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A model for study without any Many to One relationships for proper searching and and listing of studies.
 * 
 * @author mvalei
 */
@Entity
@Table(name = "STUDY")
@XmlRootElement(name = "BasicStudy")
public class BasicStudy implements Serializable {

	private static final String BASIC_STUDY = "This operation is unsupported for a BasicStudy.";

	private static final long serialVersionUID = -3963107794602437604L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDY_SEQ")
	@SequenceGenerator(name = "STUDY_SEQ", sequenceName = "STUDY_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "TITLE")
	private String title;

	@Transient
	private String uri;

	// abstract is reserved in java
	@Column(name = "ABSTRACT")
	private String abstractText;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "RECRUITMENT_STATUS_ID")
	private RecruitmentStatus recruitmentStatus;

	@Column(name = "PREFIX_ID")
	private String prefixedId;

	@Column(name = "DATE_CREATED")
	private Date dateCreated;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "STUDY_STATUS_ID")
	private StudyStatus studyStatus;
	
	@Transient
	private Set<ResearchManagement> researchManagement = new HashSet<ResearchManagement>();
	
	@Transient
	private Integer privateDatasetCount;
	
	@Transient
	private Integer sharedDatasetCount;
	
	@Transient
	private Integer clinicalDataCount;
	
	@Transient
	private Integer genomicDataCount;
	
	@Transient
	private Integer imagingDataCount;
	
	//do not persist to the database

	public BasicStudy() {

	}

	public BasicStudy(Study study) {
		this.id = study.getId();
		this.title = study.getTitle();
		this.abstractText = study.getAbstractText();
		this.recruitmentStatus = study.getRecruitmentStatus();
		this.prefixedId = study.getPrefixedId();
		this.dateCreated = study.getDateCreated();
		this.studyStatus = study.getStudyStatus();
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

	public RecruitmentStatus getRecruitmentStatus() {

		return recruitmentStatus;
	}

	public void setAbstractText(String abstractText) {

		this.abstractText = abstractText;
	}

	public void setRecruitmentStatus(RecruitmentStatus recruitmentStatus) {

		this.recruitmentStatus = recruitmentStatus;
	}

	public Date getDateCreated() {

		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {

		this.dateCreated = dateCreated;
	}

	public StudyStatus getStudyStatus() {

		return studyStatus;
	}

	public void setStudyStatus(StudyStatus studyStatus) {

		this.studyStatus = studyStatus;
	}
	
	public void setResearchManagement(Set<ResearchManagement> researchManagement){
		this.researchManagement = researchManagement;
	}
	
	public void setResearchManagement(ResearchManagement researchManagement){
		this.researchManagement.add(researchManagement);
	}
	
	public Set<ResearchManagement> getResearchManagement(){
		return this.researchManagement;
	}

	public String getPrefixedId() {

		return prefixedId;
	}

	public void setPrefixedId(String prefixedId) {

		this.prefixedId = prefixedId;
	}

	public boolean getIsPrivate() {

		return StudyStatus.PRIVATE.equals(studyStatus);
	}

	public boolean getIsPublic() {

		return StudyStatus.PUBLIC.equals(studyStatus);
	}

	public boolean getIsGenomic() {

		throw new UnsupportedOperationException(BasicStudy.BASIC_STUDY);
	}

	public boolean getIsImaging() {

		throw new UnsupportedOperationException(BasicStudy.BASIC_STUDY);
	}

	public boolean getIsClinical() {

		throw new UnsupportedOperationException(BasicStudy.BASIC_STUDY);
	}

	public String getUri() {

		return uri;
	}

	public void setUri(String uri) {

		this.uri = uri;
	}
	
	public void setPrivateDatasetCount(Integer privateDatasetCount){
		this.privateDatasetCount = privateDatasetCount;
	}
	
	public Integer getPrivateDatasetCount(){
		return this.privateDatasetCount;
	}
	
	public void setSharedDatasetCount(Integer sharedDatasetCount){
		this.sharedDatasetCount = sharedDatasetCount;
	}
	
	public Integer getSharedDatasetCount(){
		return this.sharedDatasetCount;
	}

	public void setClinicalDataCount(Integer clinicalDataCount){
		this.clinicalDataCount = clinicalDataCount;
	}
	
	public Integer getClinicalDataCount(){
		return this.clinicalDataCount;
	}
	
	public void setGenomicDataCount(Integer genomicDataCount){
		this.genomicDataCount = genomicDataCount;
	}
	
	public Integer getGenomicDataCount(){
		return this.genomicDataCount;
	}
	
	public void setImagingDataCount(Integer imagingDataCount){
		this.imagingDataCount = imagingDataCount;
	}
	
	public Integer getImagingDataCount(){
		return this.imagingDataCount;
	}
}
