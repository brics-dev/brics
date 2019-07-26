package gov.nih.tbi.account.dao.hibernate;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.AccountHistoryDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;


@Transactional("metaTransactionManager")
@Repository
public class AccountHistoryDaoImpl extends GenericDaoImpl<AccountHistory, Long> implements AccountHistoryDao {

	@Autowired
	public AccountHistoryDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {
		super(AccountHistory.class, sessionFactory);
	}
	
	@Override
	public List<AccountHistory> getAccountHistory(Account account) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<AccountHistory> query = cb.createQuery(AccountHistory.class);
		
		Root<AccountHistory> root = query.from(AccountHistory.class);
		query.where(cb.equal(root.get("account"), account)).distinct(true);
		
		List<AccountHistory> accountHistory = createQuery(query).getResultList();
		return accountHistory;
	}

}
