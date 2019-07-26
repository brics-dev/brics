package gov.nih.tbi.account.service;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.account.service.hibernate.AccountHistoryManager;

@Service
@Scope("singleton")
public class AccountHistoryManagerImpl implements Serializable, AccountHistoryManager {

	private static final long serialVersionUID = -5468420702667196047L;

	@Autowired
	private AccountDao accountDao;

	/**
	 * {@inheritDoc}
	 */
	public Account recordAccountHistory(Account account, AccountHistory accountHistory) {
		account.addAccountHistory(accountHistory);
		return accountDao.save(account);
	}
}
