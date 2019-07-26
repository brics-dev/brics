package gov.nih.tbi.account.dao;

import java.util.List;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.commons.dao.GenericDao;

public interface AccountHistoryDao extends GenericDao<AccountHistory, Long>{
	
	public List<AccountHistory> getAccountHistory (Account account);

}
