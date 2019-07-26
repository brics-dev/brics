package gov.nih.tbi.taglib.datatableDecorators;

import java.util.Date;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.idt.ws.IdtDecorator;

/**
 * Decorator class to extend the capability of the JSP for UI display
 * 
 * @author Ryan Stewart
 *
 */
public class AccountListIdtDecorator extends IdtDecorator {
	
	/**
	 * Method to build the full name of the active user
	 * 
	 * @return
	 */
	public String getUserName() {
		Account account = (Account) this.getObject();
		AccountStatus accountStatus = account.getAccountStatus();
		String accountUserName = account.getUserName();

		switch(accountStatus) {
			case REQUESTED:
			case CHANGE_REQUESTED:
			case PENDING:
				return "<a href='/portal/accountAdmin/viewAccountRequest!viewAccountRequest.action?accountId="
				+ account.getId() + "'>" + accountUserName + "</a>";
			default:
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

	  public String getApplicationDate() { Account account = (Account) this .getObject();
	  	return BRICSTimeDateUtil.formatDate(account.getApplicationDate());
	  }
	 

	
	 public String getLastUpdatedDate() { Account account = (Account) this .getObject();
	 	return BRICSTimeDateUtil.formatDate(account.getLastUpdatedDate());
	  }
	 

	public String getAccountStatusName() {
		Account account = (Account) this.getObject();

		if (account.getIsCurrentLocked()) {
			return "Locked";
		} else {
			return account.getAccountStatus().getName();
		}
	}
}
