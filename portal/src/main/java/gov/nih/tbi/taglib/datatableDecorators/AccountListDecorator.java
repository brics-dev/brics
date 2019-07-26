package gov.nih.tbi.taglib.datatableDecorators;

import java.util.Date;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.account.model.hibernate.Account;

/**
 * Decorator class to extend the capability of the JSP for UI display
 * 
 * @author Ryan Stewart
 *
 */
public class AccountListDecorator extends Decorator {
	/**
	 * Method to build the full name of the active user
	 * 
	 * @return
	 */
	public String getUserName() {
		Account account = (Account) this.getObject();
		Long accountStatusId = account.getAccountStatus().getId();
		String accountUserName = account.getUserName();

		if (accountStatusId == 3 || accountStatusId == 4) {
			return "<a href='/portal/accountAdmin/viewAccountRequest!viewAccountRequest.action?accountId="
					+ account.getId() + "'>" + accountUserName + "</a>";
		} else {
			return "<a href='/portal/accountAdmin/viewUserAccount!viewUserAccount.action?accountId=" + account.getId()
					+ "'>" + accountUserName + "</a>";
		}

	}

	public String getUserFullName() {
		Account account = (Account) this.getObject();

		return account.getUser().getFullName();

	}

	public String getUserEmail() {
		Account account = (Account) this.getObject();

		return account.getUser().getEmail();

	}

	public String getAffiliatedInstitution() {
		Account account = (Account) this.getObject();

		return account.getAffiliatedInstitution();

	}

	/*
	 * public String getApplicationDate() { Account account = (Account) this .getObject();
	 * 
	 * return "";//account.getApplicationDate().toString();
	 * 
	 * }
	 */

	/*
	 * public String getLastUpdatedDate() { Account account = (Account) this .getObject();
	 * 
	 * return "";//account.getLastUpdatedDate().toString();
	 * 
	 * }
	 */

	public String getAccountStatusName() {
		Account account = (Account) this.getObject();

		if (account.getIsCurrentLocked()) {
			return "Locked";
		} else {
			return account.getAccountStatus().getName();
		}
	}
}
