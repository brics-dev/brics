package gov.nih.tbi.metastudy.model.hibernate;

import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "Meta_Study_Data")
@XmlRootElement(name = "metaStudyData")
public class MetaStudyData implements Serializable {

	private static final long serialVersionUID = -1781333841819781405L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "META_STUDY_DATA_SEQ")
	@SequenceGenerator(name = "META_STUDY_DATA_SEQ", sequenceName = "META_STUDY_DATA_SEQ", allocationSize = 1)
	private Long id;
	
    @Column(name = "DESCRIPTION")
    private String description;
	
    @Column(name = "SOURCE")
    private String source;
    
    @Column(name = "VERSION")
    private String version;
    
    @Column(name = "DATE_CREATED")
    private Date dateCreated;
    
    @ManyToOne()
    @JoinColumn(name = "META_STUDY_ID")
    private MetaStudy metaStudy;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_FILE_ID")
    private UserFile userFile;

    @OneToOne
    @JoinColumn(name = "FILE_TYPE_ID")
    private FileType fileType;
    
    @OneToOne(orphanRemoval=true)
    @JoinColumn(name = "SAVED_QUERY_ID")
    private SavedQuery savedQuery;
    
    //This needs to be eager in order to remove the access record when the meta study data is removed
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "metaStudyData", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<MetaStudyAccessRecord> metaStudyAccessRecords = new HashSet<MetaStudyAccessRecord>();
    
    @Transient
    private String doi = "";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public MetaStudy getMetaStudy() {
		return metaStudy;
	}

	public void setMetaStudy(MetaStudy metaStudy) {
		this.metaStudy = metaStudy;
	}

	public UserFile getUserFile() {
		return userFile;
	}

	public void setUserFile(UserFile userFile) {
		this.userFile = userFile;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public String getName() {
		return (savedQuery == null ? userFile.getName() : savedQuery.getName());
	}

	public SavedQuery getSavedQuery() {
		return savedQuery;
	}

	public void setSavedQuery(SavedQuery savedQuery) {
		this.savedQuery = savedQuery;
	}
	
	public void setDoi(String doi){
		this.doi = doi;
	}
	public String getDoi(){
		return this.doi;
	}
	
	public void setMetaStudyAccessRecords(Set<MetaStudyAccessRecord> metaStudyAccessRecords) {
		this.metaStudyAccessRecords = metaStudyAccessRecords;
	}

	public Set<MetaStudyAccessRecord> getMetaStudyAccessRecords() {
		return metaStudyAccessRecords;
	}

}
