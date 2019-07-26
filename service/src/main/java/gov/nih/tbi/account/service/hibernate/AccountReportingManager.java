package gov.nih.tbi.account.service.hibernate;

import java.util.Date;
import java.util.List;

import gov.nih.tbi.account.model.AccountReportType;
import gov.nih.tbi.account.model.AccountType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountReportingLog;

public interface AccountReportingManager {
	
	public List<AccountReportingLog> generateReport(AccountReportType reportType, Date startDate, Date endDate);
	
	public AccountType getLatestAccountTypeRequest(Account account);
	
	public Integer getRunningTally(AccountReportType reportType);

}
