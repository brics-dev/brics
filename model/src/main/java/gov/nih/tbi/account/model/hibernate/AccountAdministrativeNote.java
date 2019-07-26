package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.xml.bind.annotation.XmlTransient;

import javax.persistence.Table;

@Entity
@Table(name = "ACCOUNT_ADMINISTRATIVE_NOTES")
public class AccountAdministrativeNote implements Serializable {
	
	private static final long serialVersionUID = 4972598958519887567L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_ADMINISTRATIVE_NOTES_SEQ")
	@SequenceGenerator(name = "ACCOUNT_ADMINISTRATIVE_NOTES_SEQ", sequenceName = "ACCOUNT_ADMINISTRATIVE_NOTES_SEQ", allocationSize = 1)
	private Long id;
	
	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "ACCOUNT_ID")
	private Account account;
	
	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "ADMIN_ACCOUNT_ID")
	private Account adminAccount;
	

	@Column(name = "NOTE")
	private String note;
	
	@Column(name = "CREATED_DATE")
	private Date date;
	
	public AccountAdministrativeNote(){}
	
	public AccountAdministrativeNote(String note, Account account, Account adminAccount, Date date){
		this.account  = account;
		this.note = note;
		this.date = date;
		this.adminAccount = adminAccount;
	}
	
	
	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
		
	public Account getAdminAccount() {
		return adminAccount;
	}

	public void setAdminAccount(Account adminAccount) {
		this.adminAccount = adminAccount;
	}

	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((adminAccount == null) ? 0 : adminAccount.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((note == null) ? 0 : note.hashCode());
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
		AccountAdministrativeNote other = (AccountAdministrativeNote) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (adminAccount == null) {
			if (other.adminAccount != null)
				return false;
		} else if (!adminAccount.equals(other.adminAccount))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (note == null) {
			if (other.note != null)
				return false;
		} else if (!note.equals(other.note))
			return false;
		return true;
	}


}
