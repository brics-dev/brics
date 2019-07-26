package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import gov.nih.tbi.account.model.AccountMessageTemplateType;




@Entity
@Table(name = "ACCOUNT_AUTOMATED_MESSAGES")
public class AccountMessageTemplate implements Serializable {

	
	private static final long serialVersionUID = -288792190452003023L;
	/**********************************************************************/
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_AUTOMATED_MESSAGES_SEQ")
	@SequenceGenerator(name = "ACCOUNT_AUTOMATED_MESSAGES_SEQ", sequenceName = "ACCOUNT_AUTOMATED_MESSAGES_SEQ", allocationSize = 1)
	private Long id;
	
	
	@Column(name = "CHECKBOX_TEXT")
	private String checkboxText;
	
	
	@Column(name = "DEFAULT_CHECKED")
	private Boolean defaultChecked;
	
	
	@Column(name = "MESSAGE")
	private String message;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "TYPEID")
	private AccountMessageTemplateType accountMessageTemplateType;
	
	
	@Transient
	private String sessionMessage;
	
	
	

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getCheckboxText() {
		return checkboxText;
	}


	public void setCheckboxText(String checkboxText) {
		this.checkboxText = checkboxText;
	}


	public Boolean getDefaultChecked() {
		return defaultChecked;
	}


	public void setDefaultChecked(Boolean defaultChecked) {
		this.defaultChecked = defaultChecked;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public String getSessionMessage() {
		return sessionMessage;
	}


	public void setSessionMessage(String sessionMessage) {
		this.sessionMessage = sessionMessage;
	}

	
	
	public String getEmailMessage() {
		if(message == null && !message.equals("")) {
			return sessionMessage;
		}else {
			return message;
		}
	}
	
	




	public AccountMessageTemplateType getAccountMessageTemplateType() {
		return accountMessageTemplateType;
	}


	public void setAccountMessageTemplateType(AccountMessageTemplateType accountMessageTemplateType) {
		this.accountMessageTemplateType = accountMessageTemplateType;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountMessageTemplateType == null) ? 0 : accountMessageTemplateType.hashCode());
		result = prime * result + ((checkboxText == null) ? 0 : checkboxText.hashCode());
		result = prime * result + ((defaultChecked == null) ? 0 : defaultChecked.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((sessionMessage == null) ? 0 : sessionMessage.hashCode());
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
		AccountMessageTemplate other = (AccountMessageTemplate) obj;
		if (accountMessageTemplateType != other.accountMessageTemplateType)
			return false;
		if (checkboxText == null) {
			if (other.checkboxText != null)
				return false;
		} else if (!checkboxText.equals(other.checkboxText))
			return false;
		if (defaultChecked == null) {
			if (other.defaultChecked != null)
				return false;
		} else if (!defaultChecked.equals(other.defaultChecked))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (sessionMessage == null) {
			if (other.sessionMessage != null)
				return false;
		} else if (!sessionMessage.equals(other.sessionMessage))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "AccountMessageTemplate [id=" + id + ", checkboxText=" + checkboxText + ", defaultChecked="
				+ defaultChecked + ", message=" + message + ", accountMessageTemplateType=" + accountMessageTemplateType
				+ ", sessionMessage=" + sessionMessage + "]";
	}




	
	
	
	
	
	
	
	


	
	
	
	
	
	
	
	
}
