package gov.nih.tbi.account.dao;

import java.util.Date;
import java.util.List;

import gov.nih.tbi.account.model.AccountReportType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountReportingLog;
import gov.nih.tbi.commons.dao.GenericDao;

public interface AccountReportingDao extends GenericDao<AccountReportingLog, Long> {
	
	public List<AccountReportingLog> getAllAccountReportingLog();
	
	public List<AccountReportingLog> getAccountReportingForAccount(Account account);
	
	public List<AccountReportingLog> getReportsWithinDateRange(AccountReportType reportType, Date startDate, Date endDate);

	public Integer getRunningTally(AccountReportType reportType);
}
