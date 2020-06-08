package gov.nih.tbi.taglib.datatableDecorators;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.nih.tbi.account.model.AccountActionType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.hibernate.User;
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

		String accountUserName = account.getUserName();

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
	
	public String getReviewerStatus() {
		Set<AccountHistory> accountHistorySet = account.getAccountHistory();
		if (accountHistorySet == null || accountHistorySet.isEmpty()) {
			return "";
		}

		Set<AccountHistory> reviewHistorySet = new HashSet<AccountHistory>();
		for (AccountHistory accountHistory : accountHistorySet) {
			AccountActionType actionType = accountHistory.getAccountActionType();
			if (actionType == AccountActionType.PARTIAL_APPROVE || actionType == AccountActionType.TEMP_REJECT) {
				reviewHistorySet.add(accountHistory);
			}
		}
		if (reviewHistorySet == null || reviewHistorySet.isEmpty()) {
			return "";
		}

		// sort the list based on the created date.
		Comparator<AccountHistory> comparator = Comparator.comparing(AccountHistory::getCreatedDate).reversed();
		List<AccountHistory> sortedHistoryList = reviewHistorySet.stream().sorted(comparator)
				.collect(Collectors.toList());

		JsonArray output = new JsonArray();
		for (AccountHistory reviewHistory : sortedHistoryList) {
			User user = reviewHistory.getChangedByUser();
			JsonObject historyJson = new JsonObject();
			historyJson.addProperty("type", reviewHistory.getAccountActionType().toString());
			historyJson.addProperty("fullName", user.getFullName());
			historyJson.addProperty("initial", "" + user.getFirstName().charAt(0) + user.getLastName().charAt(0));
			output.add(historyJson);
		}
		
		return output.toString();
	}
}
