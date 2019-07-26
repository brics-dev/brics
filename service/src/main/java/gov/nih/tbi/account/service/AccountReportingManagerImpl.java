package gov.nih.tbi.account.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.account.dao.AccountReportingDao;
import gov.nih.tbi.account.model.AccountReportType;
import gov.nih.tbi.account.model.AccountType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountReportingLog;
import gov.nih.tbi.account.service.hibernate.AccountReportingManager;

@Service
@Scope("singleton")

public class AccountReportingManagerImpl implements AccountReportingManager{
	
	@Autowired
	AccountReportingDao accountReportingDao;
	
	
	
	public List<AccountReportingLog> generateReport(AccountReportType reportType, Date startDate, Date endDate)
	{
		return accountReportingDao.getReportsWithinDateRange(reportType, startDate, endDate);
	}
	
	public AccountType getLatestAccountTypeRequest(Account account) {
		
		List<AccountReportingLog> accountReportingLogList = accountReportingDao.getAccountReportingForAccount(account);
		
		Collections.sort(accountReportingLogList,Collections.reverseOrder());
		
		if(accountReportingLogList.isEmpty()) {
			return null;
		}
		return accountReportingLogList.get(0).getRequestedAccountType();
	}
	
	public Integer getRunningTally(AccountReportType reportType) {
		return accountReportingDao.getRunningTally(reportType);		
	}

}
