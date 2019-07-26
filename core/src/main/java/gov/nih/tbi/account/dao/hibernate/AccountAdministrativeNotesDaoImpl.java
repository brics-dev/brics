package gov.nih.tbi.account.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.AccountAdministrativeNotesDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountAdministrativeNote;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;

@Transactional("metaTransactionManager")
@Repository
public class AccountAdministrativeNotesDaoImpl extends GenericDaoImpl<AccountAdministrativeNote, Long> implements AccountAdministrativeNotesDao {
	
	private static Logger logger = Logger.getLogger(AccountAdministrativeNotesDao.class);

	@Autowired
	public AccountAdministrativeNotesDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {
		super(AccountAdministrativeNote.class, sessionFactory);
	}
	
	@Override
	public List<AccountAdministrativeNote> getAccountAdministrativeNotes(Account account) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<AccountAdministrativeNote> query = cb.createQuery(AccountAdministrativeNote.class);
		
		Root<AccountAdministrativeNote> root = query.from(AccountAdministrativeNote.class);
		query.where(cb.equal(root.get("account"), account)).distinct(true);
		
		List<AccountAdministrativeNote> accountHistory = createQuery(query).getResultList();
		return accountHistory;
	}

	@Override
	public Map<Long, String> getLatestAdminNoteForAccounts(List<Account> accounts) {
		Map<Long, String> adminNoteMap = new HashMap<Long, String>();
		
		String sql = "select account_id, note from account_administrative_notes an " +
				"where id in (select max(an2.id) from account_administrative_notes an2 group by an2.account_id)";
		NativeQuery query = getSession().createNativeQuery(sql);
		
		List<Object[]> results = query.getResultList();
		for (Object[] obj : results) {
			Long accountId = ((Integer) obj[0]).longValue();
			String note = String.valueOf(obj[1]);
			adminNoteMap.put(accountId, note);
		}
		
		return adminNoteMap;
	}
	
}
