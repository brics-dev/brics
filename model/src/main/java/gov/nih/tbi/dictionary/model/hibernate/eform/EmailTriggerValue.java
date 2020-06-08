package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@Entity
@Table(name = "EMAIL_TRIGGER_VALUES")
@XmlRootElement(name = "EformEmailTriggerValue")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailTriggerValue implements Serializable{

	private static final long serialVersionUID = -7329390039992679828L;
	
	@Id																
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EMAIL_TRIGGER_VALUES_SEQ")
	@SequenceGenerator(name = "EMAIL_TRIGGER_VALUES_SEQ", sequenceName = "EMAIL_TRIGGER_VALUES_SEQ", allocationSize = 1)
	@XmlTransient
	private Long id;
	
	@Column(name = "ANSWER")
	private String answer;
	
	@Column(name = "TRIGGER_CONDITION")
	private String triggerCondition;

	public EmailTriggerValue(){}
	
	public EmailTriggerValue(EmailTriggerValue emailTriggerValue){
		this.setId(null);
		this.setAnswer(emailTriggerValue.getAnswer());
		this.setTriggerCondition(emailTriggerValue.getTriggerCondition());
	}
	
	public EmailTriggerValue(String answer, String triggerCond) {
		this.setId(null);
		this.setAnswer(answer);
		this.setTriggerCondition(triggerCond);
	}

	public EmailTriggerValue(String answer){
		this.answer = answer;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getTriggerCondition() {
		return triggerCondition;
	}

	public void setTriggerCondition(String triggerCondition) {
		this.triggerCondition = triggerCondition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answer == null) ? 0 : answer.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((triggerCondition == null) ? 0 : triggerCondition.hashCode());
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
		EmailTriggerValue other = (EmailTriggerValue) obj;
		if (answer == null) {
			if (other.answer != null)
				return false;
		} else if (!answer.equals(other.answer))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (triggerCondition == null) {
			if (other.triggerCondition != null)
				return false;
		} else if (!triggerCondition.equals(other.triggerCondition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EmailTriggerValue [answer= " + answer + ", condition= " + triggerCondition + "]";
	}
}