package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;
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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "EMAIL_TRIGGER")
@XmlRootElement(name = "EmailTrigger")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailTrigger implements Serializable{

	private static final long serialVersionUID = -7696604234189883823L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EMAIL_TRIGGER_SEQ")
	@SequenceGenerator(name = "EMAIL_TRIGGER_SEQ", sequenceName = "EMAIL_TRIGGER_SEQ", allocationSize = 1)
	@XmlTransient
	private Long id;
	
	@Column(name = "VERSION")
	@XmlTransient
	private Long version;
	
	@Column(name = "TO_EMAIL_ADDRESS")
	private String toEmailAddress;
	
	@Column(name = "CC_EMAIL_ADDRESS")
	private String ccEmailAddress;
	
	@Column(name = "SUBJECT")
	private String subject;
	
	@Column(name = "BODY")
	private String body;
	
	@XmlElement(name ="EmailTriggerValue")
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "EMAIL_TRIGGER_ID", nullable = true)
	private Set<EmailTriggerValue> triggerValues;
	
	public EmailTrigger(){}
	
	public EmailTrigger(EmailTrigger emailTrigger){
		this.setId(null);
		this.setVersion(emailTrigger.getVersion());
		this.setToEmailAddress(emailTrigger.getToEmailAddress());
		this.setCcEmailAddress(emailTrigger.getCcEmailAddress());
		this.setSubject(emailTrigger.getSubject());
		this.setBody(emailTrigger.getBody());
		Set<EmailTriggerValue> triggerValues = new HashSet<EmailTriggerValue>();
		if(emailTrigger.getTriggerValues() != null && !emailTrigger.getTriggerValues().isEmpty()){
			for(EmailTriggerValue etv : emailTrigger.getTriggerValues()){
				triggerValues.add(new EmailTriggerValue(etv));
			}
		}
		this.setTriggerValues(triggerValues);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getToEmailAddress() {
		return toEmailAddress;
	}

	public void setToEmailAddress(String toEmailAddress) {
		this.toEmailAddress = toEmailAddress;
	}

	public String getCcEmailAddress() {
		return ccEmailAddress;
	}

	public void setCcEmailAddress(String ccEmailAddress) {
		this.ccEmailAddress = ccEmailAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Set<EmailTriggerValue> getTriggerValues() {
		if(this.triggerValues == null){
			this.triggerValues = new HashSet<EmailTriggerValue>();
		}
		return triggerValues;
	}

	public void setTriggerValues(Set<EmailTriggerValue> triggerValues) {
		this.triggerValues = triggerValues;
	}
	
	public void addToTriggerValues(EmailTriggerValue triggerValue){
		getTriggerValues().add(triggerValue);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((ccEmailAddress == null) ? 0 : ccEmailAddress.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result + ((toEmailAddress == null) ? 0 : toEmailAddress.hashCode());
		/*result = prime * result + ((triggerValues == null) ? 0 : triggerValues.hashCode());*/
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmailTrigger other = (EmailTrigger) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (ccEmailAddress == null) {
			if (other.ccEmailAddress != null)
				return false;
		} else if (!ccEmailAddress.equals(other.ccEmailAddress))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (toEmailAddress == null) {
			if (other.toEmailAddress != null)
				return false;
		} else if (!toEmailAddress.equals(other.toEmailAddress))
			return false;
		/*if (triggerValues == null) {
			if (other.triggerValues != null)
				return false;
		} else if (!triggerValues.equals(other.triggerValues))
			return false;*/
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString(){
		return "EmailTrigger [EmailTrigger Id=" + id + ", toEmailAddress=" + toEmailAddress + ", ccEmailAddress=" + ccEmailAddress + "]";
	}
	
}