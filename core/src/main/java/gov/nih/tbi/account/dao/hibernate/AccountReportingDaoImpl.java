package gov.nih.tbi.account.dao.hibernate;

import java.time.LocalDate;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

import java.util.Calendar;
import java.util.Date;
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
import gov.nih.tbi.account.dao.AccountReportingDao;
import gov.nih.tbi.account.model.AccountReportType;
import gov.nih.tbi.account.model.AccountType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountAdministrativeNote;
import gov.nih.tbi.account.model.hibernate.AccountReportingLog;
import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;

@Transactional("metaTransactionManager")
@Repository
public class AccountReportingDaoImpl extends GenericDaoImpl<AccountReportingLog, Long> implements AccountReportingDao {

	
	@Autowired
	public AccountReportingDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {
		super(AccountReportingLog.class, sessionFactory);
	}
	
	public List<AccountReportingLog> getAllAccountReportingLog(){	
		return getAll();
	}
		
	public List<AccountReportingLog> getAccountReportingForAccount(Account account){
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<AccountReportingLog> query = cb.createQuery(AccountReportingLog.class);
		
		Root<AccountReportingLog> root = query.from(AccountReportingLog.class);
		query.where(cb.equal(root.get("account"), account));
		
		List<AccountReportingLog> accountLog = createQuery(query).getResultList();
		return accountLog;
		
	}
	
	public List<AccountReportingLog> getReportsWithinDateRange(AccountReportType reportType, Date startDate, Date endDate){
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<AccountReportingLog> query = cb.createQuery(AccountReportingLog.class);
		
		Root<AccountReportingLog> root = query.from(AccountReportingLog.class);
		
		if(reportType == AccountReportType.ALL){
			query.where(cb.between(root.get("submitedDate"), startDate, endDate));
		}
		else {
		query.where(cb.and(cb.equal(root.get("accountReportType"), reportType),
				cb.between(root.get("submitedDate"), startDate, endDate)));
		}
		List<AccountReportingLog> accountLog = createQuery(query).getResultList();
		return accountLog;
		
	}
	public Integer getRunningTally(AccountReportType reportType) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<AccountReportingLog> query = cb.createQuery(AccountReportingLog.class);

		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.DAY_OF_YEAR, 1);
		Date startDate = cal.getTime();
		Date endDate = new Date();
		
		Root<AccountReportingLog> root = query.from(AccountReportingLog.class);
		query.where(cb.and(cb.equal(root.get("accountReportType"), reportType), cb.equal(root.get("requestedAccountType"), AccountType.DAR),
				cb.between(root.get("submitedDate"), startDate, endDate)));
		
		List<AccountReportingLog> accountLog = createQuery(query).getResultList();
		return accountLog.size();
		
	}
	
}
