package gov.nih.brics.job;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.account.model.AccountEmailReportFrequency;
import gov.nih.tbi.account.model.EmailReportType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountEmailReportSetting;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.HibernateManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.util.BRICSStringUtils;

@Component
@Scope("singleton")
public class AccountEmailReportJob extends EmailJobs {

	private static Logger log = Logger.getLogger(AccountEmailReportJob.class);

	private static final String REQUEST_SUBJECT = "Account Reviewer – Request Dashboard Report";
	private static final String REQUEST_BODY_FORMAT = "Hello,<br><br>"
			+ "This is an automated email report of the contents of the Account Reviewer Request dashboard as of %s at %s.<br><br>"
			+ "The following statuses are included: %s<br><br>" + "<b>Total Rows:</b> %d<br><br>"
			+ "Note: The table will be truncated after " + ServiceConstants.EMAIL_REPORT_MAX_ROW
			+ " rows. Please log in to see remaining entries.<br><br>" + "%s<br>" + "[Include table up to "
			+ ServiceConstants.EMAIL_REPORT_MAX_ROW + " rows]";

	private static final String ACCOUNT_REQUEST_TABLE_HEADER =
			"<tr><td>Username</td><td>Name</td><td>Status</td><td>Submitted Date</td><td>Last Updated</td><td>Last Action</td></tr>";

	private static final String RENEWAL_SUBJECT = "Account Reviewer – Renewal Dashboard Report";
	private static final String RENEWAL_BODY_FORMAT = "Hello,<br><br>"
			+ "This is an automated email report of the contents of the Account Reviewer Renewal dashboard as of %s at %s.<br><br>"
			+ "The following accounts have been flagged as having a module that is expiring soon (within the next "
			+ ServiceConstants.EXPIRATION_SOON_DAYS + " days).<br><br>" + "<b>Total Rows:</b> %d<br><br>"
			+ "Note: The table will be truncated after " + ServiceConstants.EMAIL_REPORT_MAX_ROW
			+ " rows. Please log in to see remaining entries.<br><br>" + "%s<br>" + "[Include table up to "
			+ ServiceConstants.EMAIL_REPORT_MAX_ROW + " rows]";

	private static final String ACCOUNT_RENEWABLE_TABLE_HEADER =
			"<tr><td>Username</td><td>Name</td><td>Expiring Status</td><td>Expiration Date</td><td>Last Updated</td><td>Last Action</td></tr>";

	// this is looking for six arguments to put into a single html table row
	private static final String ACCOUNT_ROW_FORMAT =
			"<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";

	@Autowired
	private AccountManager accountManager;

	@Autowired
	private HibernateManager hibernateManager;
	
	@Override
	public void doJob() {
		hibernateManager.clearMetaHibernateCache();
		String accountRenewalReportBody = buildAccountRenewalReport();
		sendRenewalReportEmail(EmailReportType.ACCOUNT_RENEWAL, RENEWAL_SUBJECT, accountRenewalReportBody);

		sendRequestReportEmail();
	}

	private void sendRequestReportEmail() {
		List<AccountEmailReportSetting> accountRequestReportSettings =
				accountManager.getReportSettingsByType(EmailReportType.ACCOUNT_REQUEST);

		for (AccountEmailReportSetting currentSetting : accountRequestReportSettings) {
			String email = currentSetting.getAccount().getUser().getEmail();
			AccountEmailReportFrequency frequency = currentSetting.getFrequency();

			if (isTimeToSend(frequency)) {
				String reportBody = buildAccountRequestReport(currentSetting.getAccountStatusEnums());

				try {
					sendEmail(REQUEST_SUBJECT, reportBody, email);
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String buildAccountRequestReport(Set<AccountStatus> accountStatuses) {

		Date currentDate = new Date();
		String currentDateString = BRICSTimeDateUtil.dateToDateString(currentDate);
		String currentTime = BRICSTimeDateUtil.dateToUtcTimeStamp(currentDate);

		AccountStatus[] accountStatusArr = new AccountStatus[accountStatuses.size()];

		Iterator<AccountStatus> accountStatusIter = accountStatuses.iterator();

		int i = 0;
		while (accountStatusIter.hasNext()) {
			AccountStatus currentStatus = accountStatusIter.next();
			accountStatusArr[i] = currentStatus;
			i++;
		}

		List<Account> accountRequests = accountManager.getAccountByStatuses(accountStatusArr);

		String accountTable = buildAccountRequestTableBody(accountRequests);

		Set<String> accountStatusesInList = new HashSet<String>();

		for (Account account : accountRequests) {
			accountStatusesInList.add(account.getAccountStatus().getName());
		}

		String statusesString = BRICSStringUtils.concatWithDelimiter(accountStatusesInList, ", ");

		String reportBody = String.format(REQUEST_BODY_FORMAT, currentDateString, currentTime, statusesString,
				accountRequests.size(), accountTable);

		return reportBody;
	}

	private String buildAccountRequestTableBody(List<Account> accounts) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table border=\"1\">");
		buffer.append(ACCOUNT_REQUEST_TABLE_HEADER);

		// we only want to show EMAIL_REPORT_MAX_ROW number of accounts in the table
		for (int i = 0; i < Math.min(ServiceConstants.EMAIL_REPORT_MAX_ROW, accounts.size()); i++) {
			Account currentAccount = accounts.get(i);
			AccountHistory latestAccountHistory = currentAccount.getLatestAccountHistory();

			// it's possible for old accounts to not have an account history, so we need to do a null check.
			String latestAccountHistoryText = latestAccountHistory != null ? latestAccountHistory
					.getActionTypeText(false) : ServiceConstants.EMPTY_STRING;

			String requestSubmitDate = BRICSTimeDateUtil.formatDate(currentAccount.getRequestSubmitDate());
			String lastUpdatedDate = BRICSTimeDateUtil.formatDate(currentAccount.getLastUpdatedDate());

			String rowString = String.format(ACCOUNT_ROW_FORMAT, currentAccount.getUserName(),
					currentAccount.getUser().getFullName(), currentAccount.getAccountStatus().getName(), requestSubmitDate,
					lastUpdatedDate, latestAccountHistoryText);
			buffer.append(rowString);
		}

		buffer.append("</table>");

		return buffer.toString();
	}

	private String buildAccountRenewalReport() {
		Date currentDate = new Date();
		String currentDateString = BRICSTimeDateUtil.dateToDateString(currentDate);
		String currentTime = BRICSTimeDateUtil.dateToUtcTimeStamp(currentDate);
		List<Account> renewableAccounts = accountManager.getAccountsUpForRenewal();

		log.info("There are " + renewableAccounts.size() + " renewable accounts.");

		String accountTable = buildAccountRenewalTableBody(renewableAccounts);

		String reportBody = String.format(RENEWAL_BODY_FORMAT, currentDateString, currentTime, renewableAccounts.size(),
				accountTable);
		return reportBody;
	}

	private String buildAccountRenewalTableBody(List<Account> accounts) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<table border=\"1\">");
		buffer.append(ACCOUNT_RENEWABLE_TABLE_HEADER);

		// we only want to show EMAIL_REPORT_MAX_ROW number of accounts in the table
		for (int i = 0; i < Math.min(ServiceConstants.EMAIL_REPORT_MAX_ROW, accounts.size()); i++) {
			Account currentAccount = accounts.get(i);
			AccountHistory latestAccountHistory = currentAccount.getLatestAccountHistory();

			// it's possible for old accounts to not have an account history, so we need to do a null check.
			String latestAccountHistoryText = latestAccountHistory != null ? latestAccountHistory
					.getActionTypeText(false) : ServiceConstants.EMPTY_STRING;

			String lastUpdatedDate = BRICSTimeDateUtil.formatDate(currentAccount.getLastUpdatedDate());

			String rowString = String.format(ACCOUNT_ROW_FORMAT, currentAccount.getUserName(),
					currentAccount.getUser().getFullName(), RoleStatus.EXPIRING_SOON.getName(),
					currentAccount.getExpirationDate(), lastUpdatedDate, latestAccountHistoryText);
			buffer.append(rowString);
		}

		buffer.append("</table>");

		return buffer.toString();
	}

	private void sendRenewalReportEmail(EmailReportType reportType, String reportSubject, String reportBody) {
		List<AccountEmailReportSetting> accountRequestReportSettings =
				accountManager.getReportSettingsByType(reportType);

		for (AccountEmailReportSetting currentSetting : accountRequestReportSettings) {
			String email = currentSetting.getAccount().getUser().getEmail();
			AccountEmailReportFrequency frequency = currentSetting.getFrequency();
			if (isTimeToSend(frequency)) {
				try {
					sendEmail(reportSubject, reportBody, email);
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns true if the given frequency setting indicates that now is a good time to send the report, false
	 * otherwise.
	 * 
	 * @param frequency
	 * @return
	 */
	private boolean isTimeToSend(AccountEmailReportFrequency frequency) {
		Date currentDate = new Date();
		int dayofWeek = BRICSTimeDateUtil.getDayOfWeek(currentDate);
		int dayOfMonth = BRICSTimeDateUtil.getDayOfMonth(currentDate);

		switch (frequency) {
			case DAILY:
				return true;
			case MONDAY:
				return dayofWeek == Calendar.MONDAY;
			case TUESDAY:
				return dayofWeek == Calendar.TUESDAY;
			case WEDNESDAY:
				return dayofWeek == Calendar.WEDNESDAY;
			case THURSDAY:
				return dayofWeek == Calendar.THURSDAY;
			case FRIDAY:
				return dayofWeek == Calendar.FRIDAY;
			case MONTHLY:
				return dayOfMonth == 1;
			default:
				throw new UnsupportedOperationException(
						"Error occured while checking whether or not to send email report.  Has a new frequency setting not been implemented?");
		}
	}
}
