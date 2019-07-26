package gov.nih.tbi.account.model.hibernate;



import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import gov.nih.tbi.account.model.AccountReportType;
import gov.nih.tbi.account.model.AccountType;

@Entity
@Table(name = "ACCOUNT_REPORTING_LOG")
public class AccountReportingLog implements Comparable<AccountReportingLog> {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_REPORTING_LOG_SEQ")
	@SequenceGenerator(name = "ACCOUNT_REPORTING_LOG_SEQ", sequenceName = "ACCOUNT_REPORTING_LOG_SEQ", allocationSize = 1)
	private Long id;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "ACCOUNT_ID")
	private Account account;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "CURRENT_ACCOUNT_TYPE_ID")
	private AccountType currentAccountType;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "REQUESTED_ACCOUNT_TYPE_ID")
	private AccountType requestedAccountType;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "ACCOUNT_REPORT_TYPE_ID")
	private AccountReportType accountReportType;
	
	@Column(name="SUBMITTED_DATE")
	private Date submitedDate;
	
	public AccountReportingLog() {
			
	}
		

	public AccountReportingLog(Account account, AccountType currentAccountType, AccountType requestedAccountType, AccountReportType accountReportType,
			Date submitedDate) {
		this.account = account;
		this.currentAccountType = currentAccountType;
		this.requestedAccountType = requestedAccountType;
		this.accountReportType = accountReportType;
		this.submitedDate = submitedDate;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	
	public AccountType getCurrentAccountType() {
		return currentAccountType;
	}


	public void setCurrentAccountType(AccountType currentAccountType) {
		this.currentAccountType = currentAccountType;
	}


	public AccountType getRequestedAccountType() {
		return requestedAccountType;
	}


	public void setRequestedAccountType(AccountType requestedAccountType) {
		this.requestedAccountType = requestedAccountType;
	}


	public AccountReportType getAccountReportType() {
		return accountReportType;
	}

	public void setAccountReportType(AccountReportType accountReportType) {
		this.accountReportType = accountReportType;
	}

	public Date getSubmitedDate() {
		return submitedDate;
	}

	public void setSubmitedDate(Date submitedDate) {
		this.submitedDate = submitedDate;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((accountReportType == null) ? 0 : accountReportType.hashCode());
		result = prime * result + ((currentAccountType == null) ? 0 : currentAccountType.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((requestedAccountType == null) ? 0 : requestedAccountType.hashCode());
		result = prime * result + ((submitedDate == null) ? 0 : submitedDate.hashCode());
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
		AccountReportingLog other = (AccountReportingLog) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (accountReportType != other.accountReportType)
			return false;
		if (currentAccountType != other.currentAccountType)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (requestedAccountType != other.requestedAccountType)
			return false;
		if (submitedDate == null) {
			if (other.submitedDate != null)
				return false;
		} else if (!submitedDate.equals(other.submitedDate))
			return false;
		return true;
	}
	
	public int compareTo(AccountReportingLog o) {
        return this.getSubmitedDate().compareTo(o.getSubmitedDate());
    }
}
