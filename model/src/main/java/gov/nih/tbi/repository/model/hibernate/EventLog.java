package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.commons.model.hibernate.User;

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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by amakar on 8/31/2016.
 */


@NamedQueries(
        {
                @NamedQuery(
                        name = "EventLog.searchForEntityID",
                        query = "from EventLog e where e.entityID = :entityID " +
                                "order by id"
                ),
                @NamedQuery(
                        name = "EventLog.searchByTypeForEntityID",
                        query = "from EventLog e where e.entityID = :entityID " +
                                "and e.typeStr = :typeStr order by id"
                ),
                @NamedQuery(
                        name = "EventLog.findByID",
                        query = "from EventLog e where e.id = :eventLogID"
                )
        }
)
@Entity
@Table(name = "EVENT_LOG")
@XmlRootElement(name = "eventLog")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventLog  implements Serializable {

    private static final long serialVersionUID = 4746993022798328810L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EVENT_LOG_SEQ")
    @SequenceGenerator(name = "EVENT_LOG_SEQ", sequenceName = "EVENT_LOG_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "ENTITY_ID")
    private Long entityID;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "TYPE_ID")
    private EntityType type;

    //@Enumerated(EnumType.STRING)
    @Column(name = "EVENT_TYPE")
    private String typeStr;

    @Column(name = "OLD_VAL")
    private String oldVal;

    @Column(name = "NEW_VAL")
    private String newVal;

    @Column(name = "COMMENT")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @Column(name = "CREATE_TIME")
    private Date createTime;
    
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "eventLog", targetEntity = EventLogDocumentation.class, orphanRemoval = true)
	private Set<EventLogDocumentation> supportingDocumentationSet = new HashSet<EventLogDocumentation>();

    public EventLog() {};

   

	public EventLog(long _entityID, EventType _type, User _user) {

        this.entityID = _entityID;
        this.user = _user;
        this.setEventType(_type);
        this.createTime = new Date();
    }

    public Long getID() {
        return this.id;
    }

	public void setEntityID(Long entityID) {
		this.entityID = entityID;
	}

	public Long getEntityID() {
        return this.entityID;
    }
    
    public EntityType getType() {
		return type;
	}

    public void setType(EntityType type) {
		this.type = type;
	}

	public EventType getEventType() {
        return EventType.getFromDatabaseID(this.typeStr);
    }

    public void setEventType(EventType _type) {
        this.typeStr = _type.getId();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

	public String getTypeStr() {
		return typeStr;
	}

	public void setTypeStr(String typeStr) {
		this.typeStr = typeStr;
	}

	public Set<EventLogDocumentation> getSupportingDocumentationSet() {
		
		return supportingDocumentationSet;
	} 
	 public void setSupportingDocumentationSet(Set<EventLogDocumentation> supportingDocumentationSet) {
		
		 this.supportingDocumentationSet = supportingDocumentationSet;
	}
   
	public void addSupportingDocumentationSet(EventLogDocumentation supportingDocumentationSet) {
		if (this.supportingDocumentationSet == null) {
			this.supportingDocumentationSet = new HashSet<EventLogDocumentation>();
		}
		this.supportingDocumentationSet.add(supportingDocumentationSet);
	}
	
	public void addAllSupportingDocumentationSet(Set <EventLogDocumentation> supportingDocumentationSet ){
		if (this.supportingDocumentationSet == null) {
			this.supportingDocumentationSet = new HashSet<EventLogDocumentation>();
		}
		this.supportingDocumentationSet.addAll(supportingDocumentationSet);
	}
	
}
