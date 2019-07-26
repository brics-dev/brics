package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@Embeddable
@XmlRootElement(name="EmailTriggerValue")
@XmlAccessorType(XmlAccessType.FIELD)
public class EmailTriggerValuePk implements Serializable {

	private static final long serialVersionUID = -65251530297509385L;

	@XmlTransient
	@ManyToOne(optional = false)
	@JoinColumn(name = "EMAIL_TRIGGER_ID")
	private EmailTrigger emailTrigger;
	
	@Column(name = "answer")
	private String answer;
	
	public EmailTriggerValuePk(){}
	
	public EmailTriggerValuePk(EmailTrigger emailTrigger, String answer){
		this.emailTrigger = emailTrigger;
		this.answer = answer;
	}

	public EmailTrigger getEmailTrigger() {
		return emailTrigger;
	}

	public void setEmailTrigger(EmailTrigger emailTrigger) {
		this.emailTrigger = emailTrigger;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answer == null) ? 0 : answer.hashCode());
		result = prime * result + ((emailTrigger == null) ? 0 : emailTrigger.hashCode());
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
		EmailTriggerValuePk other = (EmailTriggerValuePk) obj;
		if (answer == null) {
			if (other.answer != null)
				return false;
		} else if (!answer.equals(other.answer))
			return false;
		if (emailTrigger == null) {
			if (other.emailTrigger != null)
				return false;
		} else if (!emailTrigger.equals(other.emailTrigger))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EmailTriggerValuePk [emailTrigger=" + emailTrigger.getId() + ", answer=" + answer + "]";
	}
	
}
