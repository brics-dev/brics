package gov.nih.tbi.account.portal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.result.StreamResult;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.dao.AccountReportingDao;
import gov.nih.tbi.account.model.AccountActionType;
import gov.nih.tbi.account.model.AccountEmailReportFrequency;
import gov.nih.tbi.account.model.AccountMessageTemplateType;
import gov.nih.tbi.account.model.AccountReportType;
import gov.nih.tbi.account.model.AccountType;
import gov.nih.tbi.account.model.EmailReportType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountEmailReportSetting;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.account.model.hibernate.AccountMessageTemplate;
import gov.nih.tbi.account.model.hibernate.AccountReportingLog;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.service.AccountReportingReportGenerator;
import gov.nih.tbi.account.service.hibernate.AccountHistoryManager;
import gov.nih.tbi.account.service.hibernate.AccountReportingManager;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.service.AccountMessageTemplateManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.repository.service.util.AccessReportExportUtil;
import gov.nih.tbi.taglib.datatableDecorators.AccountRenewalListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.AccountReportingLogListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.AccountRequestListIdtDecorator;

public class AccountReportsAction extends AccountAction {

	private static final long serialVersionUID = -693579168816692280L;

	private static Logger logger = Logger.getLogger(AccountReportsAction.class);

	private String emailReportFrequency;
	private String emailReportType;
	private String emailReportStatus;

	private AccountEmailReportSetting currentEmailReportSetting;
	private List<AccountMessageTemplate> accountMessageTemplates;
	private String accountMessages;
	private String expirationDate;
	private String renewPrivilegesComment;

	private String filterStartDate;
	private String filterEndDate;
	private String filterReportType;

	private InputStream inputStream;
	private String fileName;
	private String contentType;

	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private static final String ACCOUNT_REPORT = "Account Report";
	private static final String FILE_TYPE = ".pdf";
	private static final String MIN_DATE = "1900-01-01";
	private static final String MAX_DATE = "9999-12-31";

	/*** page indicator ****/

	public boolean inAccountRenewal = false;

	@Autowired
	protected AccountMessageTemplateManager accountMessageTemplateManager;

	@Autowired
	AccountHistoryManager accountHistoryManager;

	@Autowired
	protected ModulesConstants modulesConstants;

	@Autowired
	AccountReportingDao accountReportingDao;

	@Autowired
	AccountReportingManager accountReportingManager;
	
	@Autowired
	AccountReportingReportGenerator generator;

	public String renewalList() {

		return PortalConstants.ACTION_ACCOUNT_RENEWAL_LIST;
	}

	public String requestList() {

		return PortalConstants.ACTION_ACCOUNT_REQUEST_LIST;
	}

	public String reportingList() {

		return PortalConstants.ACTION_ACCOUNT_REPORTING_LIST;
	}

	public String viewAccountRenewal() {

		adminLoadUser();
		inAccountRenewal = true;
		return PortalConstants.ACTION_VIEW_ACCOUNT;
	}

	/**
	 * Returns true if we are in renewal page'
	 */
	public boolean getInAccountRenewal() {

		return inAccountRenewal;
	}

	public String getEmailReportStatus() {
		return emailReportStatus;
	}

	public void setEmailReportStatus(String emailReportStatus) {
		this.emailReportStatus = emailReportStatus;
	}

	public String getAccountRequests() {

		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<Account> outputList = new ArrayList<Account>(accountManager.getAccountByStatuses(
					AccountStatus.REQUESTED, AccountStatus.PENDING, AccountStatus.CHANGE_REQUESTED));
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountRequestListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url:
	// http://fitbir-portal-local-cit.nih.gov:8080/portal/accountReviewer/AccountReportsAction!getAccountsForRenewal.action
	public String getAccountsForRenewal() {

		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<Account> outputList = new ArrayList<Account>(accountManager.getAccountsUpForRenewal());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountRenewalListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	public String getAccountReportingLogs() {

		try {
			// current user is not an admin, don't show anything in the table
			if (!getIsAccountAdmin() && !getIsAccountReviewer()) {
				return null;
			}

			List<AccountReportingLog> accountReportingLog = accountReportingDao.getAll();

			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<AccountReportingLog> outputList = new ArrayList<AccountReportingLog>(accountReportingLog);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountReportingLogListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	public String getAutomatedRenewalReportLightbox() {
		getAutomatedReportSetting(EmailReportType.ACCOUNT_RENEWAL);

		return PortalConstants.ACCOUNT_AUTOMATED_RENEWAL_REPORT_LIGHTBOX;

	}

	public String getAutomatedRequestReportLightbox() {
		getAutomatedReportSetting(EmailReportType.ACCOUNT_REQUEST);

		return PortalConstants.ACCOUNT_AUTOMATED_REQUEST_REPORT_LIGHTBOX;

	}

	public void getAutomatedReportSetting(EmailReportType emailReportType) {

		Set<AccountEmailReportSetting> accountEmailSettings = new HashSet<AccountEmailReportSetting>();

		Account currentAccount = accountManager.getAccountWithEmailReport(getAccount().getId());

		accountEmailSettings = currentAccount.getAccountEmailReportSettings();

		for (AccountEmailReportSetting emailReportSetting : accountEmailSettings) {
			if (emailReportType == emailReportSetting.getReportType()) {
				currentEmailReportSetting = emailReportSetting;
			}
		}
	}

	public String getContactUserDashBoard() {

		accountMessageTemplates = accountMessageTemplateManager
				.getAccountMessageTemplateListByType(AccountMessageTemplateType.ACCOUNT_RENEWAL);

		return PortalConstants.ACCOUNT_CONTACT_USER_DIALOGUE;
	}

	public String getRenewPrivilegesDialogue() {

		return PortalConstants.ACCOUNT_RENEW_PRIVILEGES_DIALOGUE;
	}

	public StreamResult saveEmailReportSetting() {
		logger.info("Status: " + emailReportStatus);

		Account accountWithEmailReport = accountManager.getAccountWithEmailReport(getAccount().getId());

		EmailReportType emailReportType = getCheckedEmailReportType();

		if (getEmailReportFrequency().equals("none")) {
			accountWithEmailReport.removeAccountEmailReportSetting(emailReportType);
		} else {
			AccountEmailReportSetting emailReportSetting = new AccountEmailReportSetting();

			AccountEmailReportFrequency emailReportFrequency = getCheckedEmailReportFrequency();
			emailReportSetting.setReportType(emailReportType);
			emailReportSetting.setFrequency(emailReportFrequency);
			emailReportSetting.setAccount(accountWithEmailReport);
			emailReportSetting.setAccountStatuses(emailReportStatus);

			accountWithEmailReport.updateAccountEmailReportSetting(emailReportSetting);

		}

		accountManager.saveAccount(accountWithEmailReport);

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public StreamResult contactUser() {

		List<AccountMessageTemplate> accountMsgs = new ArrayList<AccountMessageTemplate>();

		JSONArray jsonAccountMessage = new JSONArray(getAccountMessages());

		Map<Long, AccountMessageTemplate> accountTemplateIds = accountMessageTemplateManager
				.getAccountMessageTemplateListByIds(getAccountTemplateIds(jsonAccountMessage));

		for (int i = 0; i < jsonAccountMessage.length(); i++) {

			JSONObject accountMsgObj = jsonAccountMessage.getJSONObject(i);

			try {
				Long id = accountMsgObj.getLong("id");
				String msg = accountMsgObj.getString("userMsg");

				AccountMessageTemplate accountMsg = accountTemplateIds.get(id);

				if (accountMsg.getMessage().isEmpty()) {
					accountMsg.setMessage(msg);
				}

				accountMsgs.add(accountMsg);
			} catch (IllegalArgumentException e) {
				logger.error("Invalid account template arguments id and/or message : " + e);
			}
		}

		String orgName = modulesConstants.getModulesOrgName();
		String orgUrl = modulesConstants.getModulesAccountURL();
		String orgEmail = modulesConstants.getModulesOrgEmail(getDiseaseId());

		accountMessageTemplateManager.contactUser(getCurrentAccount(), accountMsgs, orgName, orgUrl, orgEmail);

		AccountHistory contactUserAccountHistory = new AccountHistory(getCurrentAccount(),
				AccountActionType.PRIVILEGE_RENEWAL_EMAIL, "", getAccountHistoryComment(accountMsgs), new Date(),
				getUser());

		accountHistoryManager.recordAccountHistory(getCurrentAccount(), contactUserAccountHistory);

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));

	}

	public StreamResult renewPrivileges() {

		Date expireDate = null;

		try {
			if (expirationDate != null && !expirationDate.trim().equals("")) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				expireDate = sdf.parse(expirationDate);
			}
		} catch (ParseException e) {
			logger.error("Invalid expiration date : " + e);
		}

		Set<AccountRole> expiringSoonRoles = getCurrentAccount().getExpiringSoonRoles();

		for (AccountRole accountRole : expiringSoonRoles) {

			// The date will not apply to the Accounts module
			if (accountRole.getRoleType() != RoleType.ROLE_USER)
				accountRole.setExpirationDate(expireDate);

			// If the date selected is more than 30 days away, the user account will be
			// changed to status “Active”
			if (expireDate == null || setAccountRoleActive(accountRole, expireDate)) {
				accountRole.setRoleStatus(RoleStatus.ACTIVE);
			}
		}

		String actionTypeArguments = expirationDate;

		AccountHistory renewRoleAccountHistory = new AccountHistory(getCurrentAccount(),
				AccountActionType.PRIVILEGE_RENEWAL_COMMENT, actionTypeArguments, getRenewPrivilegesComment(),
				new Date(), getUser());
		getCurrentAccount().setLastUpdatedDate(new Date());
		getCurrentAccount().addAccountHistory(renewRoleAccountHistory);

		accountManager.saveAccount(getCurrentAccount());

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public EmailReportType getCheckedEmailReportType() {

		try {
			return EmailReportType.valueOf(getEmailReportType());
		} catch (IllegalArgumentException e) {
			logger.error("Invalid account email report type : " + e);
		}

		return null;
	}

	public AccountEmailReportFrequency getCheckedEmailReportFrequency() {

		try {
			return AccountEmailReportFrequency.valueOf(getEmailReportFrequency());
		} catch (IllegalArgumentException e) {
			logger.error("Invalid account email report frequency : " + e);
		}

		return null;
	}

	private String getAccountHistoryComment(List<AccountMessageTemplate> accountMsgs) {

		List<String> comment = new ArrayList<String>();
		for (AccountMessageTemplate accountMsg : accountMsgs) {
			comment.add(accountMsg.getMessage());
		}

		return String.join(ServiceConstants.ACCOUNT_RENEWAL_COMMENT_DELIMITER, comment);
	}

	private boolean setAccountRoleActive(AccountRole accountRole, Date expirationDate) {

		if (expirationDate != null) {
			Date currentDate = new Date();
			int daysInterval = Days
					.daysBetween(new LocalDate(currentDate.getTime()), new LocalDate(expirationDate.getTime()))
					.getDays();

			if (daysInterval < ServiceConstants.EXPIRATION_SOON_DAYS) {
				return false;
			}
		}

		return true;
	}

	public String getEmailReportFrequency() {
		return emailReportFrequency;
	}

	public void setEmailReportFrequency(String emailReportFrequency) {
		this.emailReportFrequency = emailReportFrequency;
	}

	public String getEmailReportType() {
		return emailReportType;
	}

	public void setEmailReportType(String emailReportType) {
		this.emailReportType = emailReportType;
	}

	public AccountEmailReportSetting getCurrentEmailReportSetting() {
		return currentEmailReportSetting;
	}

	public void setCurrentEmailReportSetting(AccountEmailReportSetting currentEmailReportSetting) {
		this.currentEmailReportSetting = currentEmailReportSetting;
	}

	public List<AccountMessageTemplate> getAccountMessageTemplates() {
		return accountMessageTemplates;
	}

	public void setAccountMessageTemplates(List<AccountMessageTemplate> accountMessageTemplates) {
		this.accountMessageTemplates = accountMessageTemplates;
	}

	public String getAccountMessages() {
		return accountMessages;
	}

	public void setAccountMessages(String accountMessages) {
		this.accountMessages = accountMessages;
	}

	public String getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getRenewPrivilegesComment() {
		return renewPrivilegesComment;
	}

	public void setRenewPrivilegesComment(String renewPrivilegesComment) {
		this.renewPrivilegesComment = renewPrivilegesComment;
	}

	public String getFilterStartDate() {
		return filterStartDate;
	}

	public void setFilterStartDate(String reportStartDate) {
		this.filterStartDate = reportStartDate;
	}

	public String getFilterEndDate() {
		return filterEndDate;
	}

	public void setFilterEndDate(String reportEndDate) {
		this.filterEndDate = reportEndDate;
	}

	public String getFilterReportType() {
		if(filterReportType.isEmpty()) {
			return "All";
		}
		return filterReportType;
	}

	public void setFilterReportType(String reportType) {
		this.filterReportType = reportType;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String downloadReport() {

		fileName = modulesConstants.getModulesOrgName() + " " + ACCOUNT_REPORT + " " + filterStartDate + " - "
				+ filterEndDate + FILE_TYPE;
		contentType = ServiceConstants.XML_FILE;

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] file = generator.generateReport(getFilterReportType(), filterStartDate, filterEndDate,
					modulesConstants.getModulesOrgName(), getReportList(), false);
			baos.write(file);
			inputStream = new ByteArrayInputStream(baos.toByteArray());

		} catch (IOException ioe) {
			logger.error("Error creating account report!");
			ioe.printStackTrace();
		}

		return PortalConstants.ACTION_DOWNLOAD;
	}

	public List<AccountReportingLog> getReportList(){
		
		Date startDate = new Date();
		Date endDate = new Date();
		Calendar cal = Calendar.getInstance();
		
		
		try {
			startDate = dateFormat.parse(MIN_DATE);
			endDate = dateFormat.parse(MAX_DATE);
			
			if(filterStartDate != null && !filterStartDate.isEmpty()) {
				startDate = dateFormat.parse(filterStartDate);
			} 				
			if(filterEndDate != null && !filterEndDate.isEmpty()) {	
				endDate = dateFormat.parse(filterEndDate);
				endDate.setHours(23);
				endDate.setMinutes(59);
				endDate.setSeconds(59);
			}
		}
		catch (ParseException e) {
			logger.error("Error parsing date for Account Report");
		}
				
		return accountReportingManager.generateReport(AccountReportType.getByName(getFilterReportType()), startDate, endDate);
		
	}
}
