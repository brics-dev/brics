package gov.nih.tbi.taglib.datatableDecorators;

import java.util.Date;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class AccountRequestListIdtDecorator extends IdtDecorator {
	Account account;

	public String initRow(Object obj, int viewIndex) {

		String feedback = super.initRow(obj, viewIndex);

		if (obj instanceof Account) {
			account = (Account) obj;
		}
		return feedback;
	}

	public String getUserName() {

		// AccountStatus accountStatus = account.getAccountStatus();
		String accountUserName = account.getUserName();
		//
		// switch (accountStatus) {
		// case REQUESTED:
		// case PENDING:
		// return "<a href='/portal/accountReviewer/viewAccountRequest!viewAccountRequest.action?accountId="
		// + account.getId() + "'>" + accountUserName + "</a>";
		// default:
		// return "<a href='/portal/accountReviewer/viewUserAccount!viewUserAccount.action?accountId="
		// + account.getId() + "'>" + accountUserName + "</a>";
		// }
		//
		return "<a href='/portal/accountReviewer/viewAccountRequest!viewAccountRequest.action?accountId="
				+ account.getId() + "'>" + accountUserName + "</a>";
	}

	public String getStatus() {

		return account.getAccountStatus().getName();
	}

	public String getRequestSubmitDate() {
		return BRICSTimeDateUtil.formatDate(account.getRequestSubmitDate());
	}

	public String getLastUpdated() {

		if (account.getLatestAccountHistory() != null) {
			Date lastUpdatedDate = account.getLatestAccountHistory().getCreatedDate();

			return BRICSTimeDateUtil.formatDate(lastUpdatedDate);
		}

		if (account.getLastUpdatedDate() != null) {
			return BRICSTimeDateUtil.formatDate(account.getLastUpdatedDate());
		}

		return "";
	}

	public String getLastAction() {

		if (account.getLatestAccountHistory() != null) {
			return account.getLatestAccountHistory().getActionTypeText(false);
		}

		return "";
	}
}
