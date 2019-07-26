package gov.nih.tbi.account.service.hibernate;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountHistory;

public interface AccountHistoryManager {

	/**
	 * Inserts the account history into the account and persist into the database
	 * 
	 * @param account
	 * @param accountHistory
	 * @return
	 */
	public Account recordAccountHistory(Account account, AccountHistory accountHistory);
}
