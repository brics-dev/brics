package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;
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
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.account.model.AccountActionType;
import gov.nih.tbi.commons.model.hibernate.User;

@Entity
@Table(name = "ACCOUNT_HISTORY")
public class AccountHistory implements Serializable {

	private static final long serialVersionUID = 2339424874356842737L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ACCOUNT_HISTORY_SEQ")
	@SequenceGenerator(name = "ACCOUNT_HISTORY_SEQ", sequenceName = "ACCOUNT_HISTORY_SEQ", allocationSize = 1)
	private Long id;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "ACCOUNT_ID")
	private Account account;

	@Enumerated(EnumType.STRING)
	@Column(name = "ACTION_TYPE", nullable = false)
	private AccountActionType accountActionType;

	// this is a "pipe" | delimited list of arguments that will be formatted using accountActionType
	@Column(name = "ACTION_TYPE_ARGUMENTS")
	private String actionTypeArguments;

	@Column(name = "COMMENT")
	private String comment;

	@Column(name = "CREATED_DATE")
	private Date createdDate;

	@XmlTransient
	@ManyToOne
	@JoinColumn(name = "CHANGED_BY_USER_ID")
	private User changedByUser;

	public AccountHistory() {

	}

	public AccountHistory(Account account, AccountActionType accountActionType, String actionTypeArguments,
			String comment, Date createdDate, User changedByUser) {
		this.account = account;
		this.accountActionType = accountActionType;
		this.actionTypeArguments = actionTypeArguments;
		this.comment = comment;
		this.createdDate = createdDate;
		this.changedByUser = changedByUser;
	}

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

	public AccountActionType getAccountActionType() {
		return accountActionType;
	}

	public void setAccountActionType(AccountActionType accountActionType) {
		this.accountActionType = accountActionType;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getActionTypeArguments() {
		return actionTypeArguments;
	}

	public void setActionTypeArguments(String actionTypeArguments) {
		this.actionTypeArguments = actionTypeArguments;
	}

	protected String[] getActionTypeArgumentArray() {
		String actionTypeArguments[] = this.getActionTypeArguments().split("\\|");
		return actionTypeArguments;
	}

	/**
	 * Returns the final string of the string format using the template <b>accountActionType</b> and arguments from
	 * <b>actionTypeArguments</b>.
	 * 
	 * @param maskUser - If true, replaces the user Last Name, First Name with 'System User.' If false, just display the
	 *        Last Name, First Name as is.
	 * @return
	 */
	public String getActionTypeText(boolean maskUser) {
		String actionTypeFormat = this.accountActionType.getStringFormat();
		String actionTypeArguments[] = getActionTypeArgumentArray();
		String actionTypeText = "";

		if (actionTypeArguments.length > 0) {
			actionTypeText = String.format(actionTypeFormat, (Object[]) actionTypeArguments);
		} else {
			actionTypeText = actionTypeFormat;
		}

		// replace username with System User
		if (this.getChangedByUser() != null) {
			if (maskUser) {
				actionTypeText = actionTypeText.concat(ModelConstants.USER_NAME_MASK);
			} else {
				actionTypeText = actionTypeText.concat(this.getChangedByUser().getFullName());
			}
		}

		return actionTypeText;
	}

	public User getChangedByUser() {
		return changedByUser;
	}

	public void setChangedByUser(User changedByUser) {
		this.changedByUser = changedByUser;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((accountActionType == null) ? 0 : accountActionType.hashCode());
		result = prime * result + ((actionTypeArguments == null) ? 0 : actionTypeArguments.hashCode());
		result = prime * result + ((changedByUser == null) ? 0 : changedByUser.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		AccountHistory other = (AccountHistory) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (accountActionType != other.accountActionType)
			return false;
		if (actionTypeArguments == null) {
			if (other.actionTypeArguments != null)
				return false;
		} else if (!actionTypeArguments.equals(other.actionTypeArguments))
			return false;
		if (changedByUser == null) {
			if (other.changedByUser != null)
				return false;
		} else if (!changedByUser.equals(other.changedByUser))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (createdDate == null) {
			if (other.createdDate != null)
				return false;
		} else if (!createdDate.equals(other.createdDate))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AccountHistory [id=" + id + ", account=" + account + ", accountActionType=" + accountActionType
				+ ", actionTypeArguments=" + actionTypeArguments + ", comment=" + comment + ", createdDate="
				+ createdDate + ", changedByUser=" + changedByUser + "]";
	}
}
