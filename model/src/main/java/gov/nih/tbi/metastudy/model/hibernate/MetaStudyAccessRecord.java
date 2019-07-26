package gov.nih.tbi.metastudy.model.hibernate;

import gov.nih.tbi.account.model.hibernate.Account;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "meta_study_access_record")
@XmlRootElement(name = "metaStudyAccessRecord")
public class MetaStudyAccessRecord implements Serializable {

	private static final long serialVersionUID = 157174590316971020L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "META_STUDY_ACCESS_RECORD_SEQ")
	@SequenceGenerator(name = "META_STUDY_ACCESS_RECORD_SEQ", sequenceName = "META_STUDY_ACCESS_RECORD_SEQ", allocationSize = 1)
	private Long id;
	
	@Column(name = "DATE_CREATED", nullable=false)
    private Date dateCreated;
    
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="account_id", nullable=false)
    private Account account;
    
    @ManyToOne(cascade= CascadeType.ALL)
    @JoinColumn(name = "supporting_documentation_id")
    private MetaStudyDocumentation supportingDocumentation;
    
    @ManyToOne(cascade= CascadeType.ALL)
	@JoinColumn(name="meta_study_data_id")
    private MetaStudyData metaStudyData;

    @ManyToOne(fetch=FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name="meta_study_id", nullable=false)
    private MetaStudy metaStudy;
    
    //default constructor used by hibernate
    public MetaStudyAccessRecord(){
    }
    
    public MetaStudyAccessRecord(Account account, MetaStudyDocumentation supportingDocumentation,
    		MetaStudyData metaStudyData, MetaStudy metaStudy) {

		this.dateCreated  = new Date();
		this.account = account;
		this.supportingDocumentation = supportingDocumentation;
		this.metaStudyData = metaStudyData;
		this.metaStudy = metaStudy;

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public MetaStudyDocumentation getSupportingDocumentation() {
		return supportingDocumentation;
	}

	public void setSupportingDocumentation(
			MetaStudyDocumentation supportingDocumentation) {
		this.supportingDocumentation = supportingDocumentation;
	}

	public MetaStudyData getMetaStudyData() {
		return metaStudyData;
	}

	public void setMetaStudyData(MetaStudyData metaStudyData) {
		this.metaStudyData = metaStudyData;
	}    
	
	public void setMetaStudy(MetaStudy metaStudy){
		this.metaStudy = metaStudy;
	}
	
	public MetaStudy getMetaStudy(){
		return metaStudy;
	}
}
