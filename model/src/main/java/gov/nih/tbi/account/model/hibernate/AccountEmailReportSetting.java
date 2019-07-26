package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import gov.nih.tbi.account.model.AccountEmailReportFrequency;
import gov.nih.tbi.account.model.EmailReportType;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.util.BRICSStringUtils;

@Entity
@Table(name = "ACCOUNT_EMAIL_REPORT")
public class AccountEmailReportSetting implements Serializable {

	private static final long serialVersionUID = -3619054509170120133L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_EMAIL_REPORT_SEQ")
	@SequenceGenerator(name = "ACCOUNT_EMAIL_REPORT_SEQ", sequenceName = "ACCOUNT_EMAIL_REPORT_SEQ", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "account_id")
	private Account account;

	@Enumerated(EnumType.STRING)
	@Column(name = "REPORT_TYPE")
	private EmailReportType reportType;

	@Enumerated(EnumType.STRING)
	@Column(name = "FREQUENCY")
	private AccountEmailReportFrequency frequency;

	@Column(name = "STATUSES")
	private String accountStatuses;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public EmailReportType getReportType() {
		return reportType;
	}

	public void setReportType(EmailReportType reportType) {
		this.reportType = reportType;
	}

	public AccountEmailReportFrequency getFrequency() {
		return frequency;
	}

	public void setFrequency(AccountEmailReportFrequency frequency) {
		this.frequency = frequency;
	}

	public String getAccountStatuses() {
		return accountStatuses;
	}

	public void setAccountStatuses(String accountStatuses) {
		this.accountStatuses = accountStatuses;
	}

	public void setAccountStatuses(Set<AccountStatus> accountStatuses) {
		if (accountStatuses == null || accountStatuses.isEmpty()) {
			this.accountStatuses = null;
		} else {

			Set<String> accountStatusStrings = new HashSet<>();

			for (AccountStatus accountStatus : accountStatuses) {
				accountStatusStrings.add(accountStatus.name());
			}

			String delimitedStatuses = String.join(",", accountStatusStrings);

			this.accountStatuses = delimitedStatuses;
		}
	}

	public Set<AccountStatus> getAccountStatusEnums() {

		Set<AccountStatus> accountStatusSet = new HashSet<>();

		if (this.accountStatuses == null) {
			return accountStatusSet;
		}

		String[] accountStatusStrings = this.accountStatuses.split(",");

		for (String accountStatusString : accountStatusStrings) {
			if (!accountStatusString.isEmpty()) {
				AccountStatus currentAccountStatus = AccountStatus.valueOf(accountStatusString);
				accountStatusSet.add(currentAccountStatus);
			}
		}

		return accountStatusSet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((accountStatuses == null) ? 0 : accountStatuses.hashCode());
		result = prime * result + ((frequency == null) ? 0 : frequency.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((reportType == null) ? 0 : reportType.hashCode());
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
		AccountEmailReportSetting other = (AccountEmailReportSetting) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (accountStatuses == null) {
			if (other.accountStatuses != null)
				return false;
		} else if (!accountStatuses.equals(other.accountStatuses))
			return false;
		if (frequency != other.frequency)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (reportType != other.reportType)
			return false;
		return true;
	}
}
