package gov.nih.tbi.dictionary.model.hibernate;

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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.commons.model.hibernate.User;

/**
 * @author hkebed
 *
 */
@Entity
@Table(name = "EVENT_LOG")
public class DictionaryEventLog implements Serializable{
	
	private static final long serialVersionUID = -469724865250707370L;

	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EVENT_LOG_SEQ")
	@SequenceGenerator(name = "EVENT_LOG_SEQ", sequenceName = "EVENT_LOG_SEQ", allocationSize = 1)
	private Long id;
	
    @Column(name = "EVENT_TYPE")
    private String eventTypeStr;

    @Column(name = "OLD_VAL")
    private String oldVal;

    @Column(name = "NEW_VAL")
    private String newVal;

    @Column(name = "COMMENT")
    private String comment;

    @Column(name = "CREATE_TIME")
    private Date createTime;
    
    @Column(name="MINOR_MAJOR_CHANGE")
    private Boolean minorMajorChange;
         
    @Column(name="MINOR_MAJOR_DESC")
    private String minorMajorDesc;
    
    @Column(name="DISEASE_ID")
    private Long diseaseId;
    
    @Column(name="USER_ID")
    private Long userId;
    
    @Column(name = "DATA_STRUCTURE_ID")
    private Long formStructureID;
    
    @Column(name = "DATA_ELEMENT_ID")
    private Long dataElementID;
    
    /**
     * User is made transient as dictionary db doesn't have user table
     * and this will be populated after user detail is returned from
     * web service call to portal
     */
    @Transient
    private User user;
    
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dictionaryEventLog", targetEntity = DictionarySupportingDocumentation.class, orphanRemoval = true)
	private Set<DictionarySupportingDocumentation> supportingDocumentationSet = new HashSet<DictionarySupportingDocumentation>();


    public DictionaryEventLog(Long diseaseId,Long userId){
    	
    	  this.diseaseId = diseaseId;
    	  this.userId = userId;
          this.createTime = new Date();
          this.minorMajorChange=false;
    }
    
    public DictionaryEventLog(){
    	
        this.createTime = new Date();
        this.minorMajorChange=false;
    }
  
    public Long getID() {
        return this.id;
    }
    

    public String getOldValue() {
        return this.oldVal;
    }

    public void setOldValue(String _val) {
        this.oldVal = _val;
    }

    public String getNewValue() {
        return this.newVal;
    }

    public void setNewValue(String _val) {
        this.newVal = _val;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date _val) {
        this.createTime = _val;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

	public String getEventTypeStr() {
		return eventTypeStr;
	}

	public void setEventTypeStr(String eventTypeStr) {
		this.eventTypeStr = eventTypeStr;
	}
	
	public EventType getEventType() {
        return EventType.getFromDatabaseID(this.eventTypeStr);
    }

    public void setEventType(EventType _type) {
        this.eventTypeStr = _type.getId();
    }

	public Boolean getMinorMajorChange() {
		return minorMajorChange;
	}

	public void setMinorMajorChange(Boolean minorMajorChange) {
		this.minorMajorChange = minorMajorChange;
	}

	public String getMinorMajorDesc() {
		return minorMajorDesc;
	}

	public void setMinorMajorDesc(String minorMajorDesc) {
		this.minorMajorDesc = minorMajorDesc;
	}

	public Long getDiseaseId() {
		return diseaseId;
	}

	public void setDiseaseId(Long diseaseId) {
		this.diseaseId = diseaseId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getFormStructureID() {
		return formStructureID;
	}

	public void setFormStructureID(Long formStructureID) {
		this.formStructureID = formStructureID;
	}

	public Long getDataElementID() {
		return dataElementID;
	}

	public void setDataElementID(Long dataElementID) {
		this.dataElementID = dataElementID;
	}

	public Set<DictionarySupportingDocumentation> getSupportingDocumentationSet() {
		
		return supportingDocumentationSet;
	} 
	 public void setSupportingDocumentationSet(Set<DictionarySupportingDocumentation> supportingDocumentationSet) {
		
		 this.supportingDocumentationSet = supportingDocumentationSet;
	}
   
	public void addSupportingDocumentationSet(DictionarySupportingDocumentation supportingDocumentationSet) {
		if (this.supportingDocumentationSet == null) {
			this.supportingDocumentationSet = new HashSet<DictionarySupportingDocumentation>();
		}
		this.supportingDocumentationSet.add(supportingDocumentationSet);
	}
	
	public void addAllSupportingDocumentationSet(Set <DictionarySupportingDocumentation> supportingDocumentationSet ){
		if (this.supportingDocumentationSet == null) {
			this.supportingDocumentationSet = new HashSet<DictionarySupportingDocumentation>();
		}
		this.supportingDocumentationSet.addAll(supportingDocumentationSet);
	}
	
}
