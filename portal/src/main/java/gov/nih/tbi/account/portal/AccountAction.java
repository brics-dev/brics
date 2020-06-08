
package gov.nih.tbi.account.portal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonSyntaxException;
import com.jcraft.jsch.JSchException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.AccountActionType;
import gov.nih.tbi.account.model.AccountDetailsForm;
import gov.nih.tbi.account.model.AccountMessageTemplateType;
import gov.nih.tbi.account.model.AccountPrivilegesForm;
import gov.nih.tbi.account.model.AccountReportType;
import gov.nih.tbi.account.model.AccountSignatureForm;
import gov.nih.tbi.account.model.AccountType;
import gov.nih.tbi.account.model.AccountUserDetails;
import gov.nih.tbi.account.model.SessionAccountMessageTemplates;
import gov.nih.tbi.account.model.SignatureType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountAdministrativeNote;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.account.model.hibernate.AccountMessageTemplate;
import gov.nih.tbi.account.model.hibernate.AccountReportingLog;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.BasicAccount;
import gov.nih.tbi.account.model.hibernate.ElectronicSignature;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.account.service.hibernate.AccountReportingManager;
import gov.nih.tbi.account.service.util.AccountServiceUtil;
import gov.nih.tbi.account.util.ESignDocGenerator;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.PermissionGroupStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.Country;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.model.hibernate.State;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.AccountMessageTemplateManager;
import gov.nih.tbi.commons.service.HibernateManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.guid.exception.InvalidJwtException;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.taglib.datatableDecorators.AccountAdministrativeNotesListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.AccountHistoryListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.AccountListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.AccountRoleListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.ExistingFileListIdtDecorator;

/**
 * Action for account request/edit
 * 
 * @author Francis Chen
 */
public class AccountAction extends BaseAccountAction {

	private static final long serialVersionUID = 1721607734875667152L;
	private static Logger logger = Logger.getLogger(AccountAction.class);

	/******************************************************************/

	private AccountDetailsForm accountDetailsForm;
	private AccountPrivilegesForm accountPrivilegesForm;
	private AccountSignatureForm accountSignatureForm;
	

	private Account currentAccount;

	private String availability;

	private List<Account> accountList;

	private List<BasicAccount> searchAccountList;

	private String userName;

	private String key;

	// PaginationData
	private Integer numSearchResults;
	private Integer page;
	private Integer pageSize;
	private Boolean ascending;
	private String sort;

	private Long statusId;
	/************************ Change Password ***************************/

	String password;
	String newPassword;
	String confirmPassword;

	/*********************** File Upload Stuff **************************/

	private Map<UserFile, byte[]> uploadedFilesMap;
	private File upload;
	private String uploadContentType;
	private String uploadFileName;
	private String uploadDescription;
	private InputStream inputStream;
	private boolean removeDuplicate;
	private Date uploadedDate;

	Set<AccountUserDetails> activeUserList;
	Set<AccountRole> accountRoleList;
	Set<PermissionGroupMember> permissionGroupMemberList;

	private String jsonResponse;

	private long userFileId;
	private String changedFileName;

	private String redirect;

	public String partialApprovalComments;
	public String finalApprovalComments;
	public String finalApprovalExpireDate;

	public List<AccountMessageTemplate> accountMessageTemplates;

	/**** account message template props ****/
	@Autowired
	protected AccountMessageTemplateManager accountMessageTemplateManager;

	@Autowired
	protected SessionAccountMessageTemplates sessionAccountGuidanceEmails;

	@Autowired
	protected HibernateManager hibernateManager;
	
	@Autowired
	AccountReportingManager accountReportingManager;

	private String rejectionReason;

	private String accountAdministrativeNoteText;

	private AccountHistory accountHistory;
	


	public static final String PASSWORD_RESET = "Password reset";


	public List<AccountMessageTemplate> getAccountMessageTemplates() {
		return accountMessageTemplates;
	}

	protected List<Long> getAccountTemplateIds(JSONArray jsonAccountMessage) {

		List<Long> accountTemplateIds = new ArrayList<Long>();

		for (int i = 0; i < jsonAccountMessage.length(); i++) {

			JSONObject accountMsgObj = jsonAccountMessage.getJSONObject(i);

			try {
				Long id = accountMsgObj.getLong("id");
				accountTemplateIds.add(id);
			} catch (IllegalArgumentException e) {
				logger.error("Invalid account template id : " + e);
			}

		}

		return accountTemplateIds;
	}

	public void setAccountMessageTemplates(String accountMessageTemplates) {
		List<AccountMessageTemplate> accountMsgs = new ArrayList<>();
		JSONArray jsonAccountMessage = new JSONArray(accountMessageTemplates);

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

		this.accountMessageTemplates = accountMsgs;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public String getJsonResponse() {
		return jsonResponse;
	}

	public void setJsonResponse(String jsonResponse) {
		this.jsonResponse = jsonResponse;
	}

	public boolean getIsApproved() {
		return !getIsRequest() && getCurrentAccount().getAccountStatus() != AccountStatus.REQUESTED;
	}

	public boolean getIsRequest() {

		if (getCurrentAccount().getId() == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns true if namespace is 'accounts' or 'publicAccounts'
	 */
	public boolean getInAccounts() {

		return (PortalConstants.NAMESPACE_ACCOUNTS.equals(getNameSpace())
				|| PortalConstants.NAMESPACE_PUBLICACCOUNTS.equals(getNameSpace()));
	}

	/**
	 * @return true if in namespace /sso or jsp/sso
	 */
	public boolean getInSSO() {

		return (PortalConstants.NAMESPACE_SSO.equals(getNameSpace())
				|| PortalConstants.NAMESPACE_JSP_SSO.equals(getNameSpace()));
	}

	/**
	 * Returns true if namespace is 'acountAdmin'
	 */
	public boolean getInAccountAdmin() {

		return PortalConstants.NAMESPACE_ACCOUNT_ADMIN.equals(getNameSpace());
	}

	public boolean getOnlyReviewer() {
		Account loggedInAccount = getAccount();

		return accountManager.hasRole(loggedInAccount, RoleType.ROLE_ACCOUNT_REVIEWER)
				&& !accountManager.hasRole(loggedInAccount, RoleType.ROLE_ADMIN)
				&& !accountManager.hasRole(loggedInAccount, RoleType.ROLE_ACCOUNT_ADMIN);
	}


	/**
	 * Returns true if namespace is 'acountReviewer'
	 */
	public boolean getInAccountReviewer() {

		return PortalConstants.NAMESPACE_ACCOUNT_REVIEWER.equals(getNameSpace());
	}

	public boolean getHasAdminPrivilege() {

		Set<AccountRole> userRoles = getCurrentAccount().getAccountRoleList();

		for (AccountRole accountRole : userRoles) {
			if (accountRole.getRoleType().getIsAdmin()) {
				return true;
			}
		}

		return false;
	}

	public boolean getIsAccountAdmin() {
		Account loggedInAccount = getAccount();

		return (accountManager.hasRole(loggedInAccount, RoleType.ROLE_ADMIN)
				|| accountManager.hasRole(loggedInAccount, RoleType.ROLE_ACCOUNT_ADMIN));

	}

	public boolean getIsAccountReviewer() {
		Account loggedInAccount = getAccount();

		return (accountManager.hasRole(loggedInAccount, RoleType.ROLE_ACCOUNT_REVIEWER));

	}

	public boolean getIsAccountActive() {

		return (getCurrentAccount().getAccountStatus() == AccountStatus.ACTIVE);
	}

	public boolean getIsAccountRejected() {

		AccountStatus accountStatus = getCurrentAccount().getAccountStatus();
		return (accountStatus == AccountStatus.DENIED || accountStatus == AccountStatus.INACTIVE);
	}

	public RoleType[] getRoleList() {

		List<RoleType> rtnList = new ArrayList<RoleType>();
		// These should be removed when other non-NTRR instances get the reporting module
		if (modulesConstants.getModulesOrgName().equals(ModulesConstants.NTRR)) {
			rtnList = Arrays.asList(RoleType.getNTRRRoleTypes());
		} else if (modulesConstants.getModulesOrgName().equals(ModulesConstants.CISTAR)) {
			rtnList = Arrays.asList(RoleType.getCISTARRoleTypes());
		} else {
			rtnList = Arrays.asList(RoleType.getRoleTypes());
		}
		
		Collections.sort(rtnList, new Comparator<RoleType>() {
			@Override
			public int compare(RoleType rt1, RoleType rt2) {

				return rt1.getTitle().compareToIgnoreCase(rt2.getTitle());
			}
		});
		RoleType[] rtn = new RoleType[rtnList.size()];
		rtn = rtnList.toArray(rtn);
		return rtn;
	}

	public RoleStatus[] getRoleStatus() {
		return RoleStatus.values();
	}

	public String getAffiliatedInstitutionList() {

		List<String> affiliatedInstitutions = accountManager.getAffiliatedInstitutions();
		StringBuilder builder = new StringBuilder("[ ");

		for (int i = 0; i < affiliatedInstitutions.size(); i++) {
			String currentAffiliatedInstitution = affiliatedInstitutions.get(i);

			if (currentAffiliatedInstitution != null) {
				if (i != 0) {
					builder.append(PortalConstants.COMMA + PortalConstants.WHITESPACE);
				}
				builder.append(PortalConstants.QUOTE);

				// we need to escape backslashes or it will break the javascript on account request
				currentAffiliatedInstitution = currentAffiliatedInstitution.replaceAll("\\\\", "\\\\\\\\");

				// we need to escape quotes or it will break the javascript on account request
				currentAffiliatedInstitution = currentAffiliatedInstitution.replaceAll("\\\"", "\\\\\"");
				builder.append(currentAffiliatedInstitution);
				builder.append(PortalConstants.QUOTE);
			}
		}

		builder.append(" ]");

		return builder.toString();
	}

	public List<State> getStateList() {

		return staticManager.getStateList();
	}

	public List<Country> getCountryList() {

		return staticManager.getCountryList();
	}

	/**
	 * Returns a set of all user files in session
	 */
	public Set<UserFile> getUploadedFiles() {
		if(getSessionAccountEdit().getUploadedFilesMap() == null) {
			return null;
		}
		
		return getSessionAccountEdit().getUploadedFilesMap().keySet();
	}

	public List<String> getSessionFileNames() {
		if (getSessionAccountEdit().getUploadedFilesMap() == null) {
			return new ArrayList<String>();
		}

		return getSessionAccountEdit().getUploadedFilesMap().keySet().stream().map(UserFile::getName)
				.collect(Collectors.toList());
	}

	public List<FileType> getAdminFileTypes() {

		return staticManager.getAdminFileTypeList();
	}

	/**
	 * gets a list of all permission groups
	 */
	public List<PermissionGroup> getPermissionGroupList() {

		return accountManager.getPrivatePermissionGroups();
	}

	public List<UserFile> getCurrentUserFiles() {

		return repositoryManager.getAdminFiles(getCurrentAccount().getUser().getId());
	}

	public String getExistingFileTypes() {
		String existingFileTypes = "";
		Set<UserFile> existingFiles = getUploadedFiles();
		
		if(existingFiles == null) {
			//sigh...why do these methods randomly switch between Lists and Sets
			existingFiles = new HashSet<UserFile> (getCurrentUserFiles());
		}
		
		for (UserFile existingFile : existingFiles) {
			existingFileTypes += existingFile.getDescription() + ";";
		}

		if (!existingFileTypes.isEmpty()) {
			existingFileTypes = existingFileTypes.substring(0, existingFileTypes.length() - 1);
		}
		
		return existingFileTypes;
	}
	
	public StreamResult existingFileTypesAjax() {
		return new StreamResult(new ByteArrayInputStream(getExistingFileTypes().getBytes()));
	}

	/***************************************************************************/

	/**
	 * A helper function to serve the total number of results pages to the jsp
	 * 
	 * @return
	 */
	public Integer getNumPages() {

		return (int) Math.ceil(((double) numSearchResults) / ((double) pageSize));
	}

	/*******************************************************************/

	public SessionAccountMessageTemplates getSessionAccountGuidanceEmails() {
		if (sessionAccountGuidanceEmails == null) {
			sessionAccountGuidanceEmails = new SessionAccountMessageTemplates();
		}
		return sessionAccountGuidanceEmails;
	}

	public List<AccountMessageTemplate> getAccountTemporaryRejectionList() {

		return getSessionAccountGuidanceEmails().getAccountTemporaryRejectionList();
	}


	/**
	 * method that brings up lightbox for user to enter reason as to why they are withdrawing requested account
	 * 
	 * @return
	 */
	public String withdrawAccountRequestLightbox() {

		return PortalConstants.ACTION_WITHDRAW_REQUEST_LIGHBOX;
	}



	/**
	 * method that brings up lightbox for user to enter reason as to why they are withdrawing requested account
	 * 
	 * @return
	 */
	public String changeFileTypeLightbox() {

		return PortalConstants.CHANGE_FILE_TYPE_LIGHBOX;
	}



	public StreamResult changeFileType() {

		UserFile userfile = repositoryManager.getFileById(userFileId);
		String fileName = userfile.getName();
		String oldFileTypeDescription = userfile.getDescription();
		userfile.setDescription(uploadDescription);
		repositoryManager.saveUserFile(userfile);

		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return new StreamResult(new ByteArrayInputStream((PortalConstants.ACTION_LANDING).getBytes()));
		}

		recordChangeFileType(fileName, oldFileTypeDescription, uploadDescription);

		currentAccount = accountManager.saveAccount(currentAccount);
		getSessionAccountEdit().setAccount(currentAccount);

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}


	/**
	 * method that brings up lightbox for user to enter reason as to why they are withdrawing requested account
	 * 
	 * @return
	 */
	public String changeFileNameLightbox() {

		return PortalConstants.CHANGE_FILE_NAME_LIGHBOX;
	}


	public String changeFileName() throws JSchException {

		UserFile userfile = repositoryManager.getFileById(userFileId);
		String currentFileName = userfile.getName();
		userfile.setName(changedFileName);
		String filePath = userfile.getPath();

		boolean isValid = repositoryManager.moveAsNewFileName(filePath, currentFileName, changedFileName);
		if (isValid) {
			repositoryManager.saveUserFile(userfile);

			currentAccount = getCurrentAccount();
			if (currentAccount == null) {
				return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
			}
			
			recordChangeFileName(currentFileName, changedFileName);
			currentAccount = accountManager.saveAccount(currentAccount);
			getSessionAccountEdit().setAccount(currentAccount);

			return null;
		} else {
			return "error";
		}
	}



	/**
	 * method that calls the code for withdrawing of the requested account after user enters reason
	 * 
	 * @return
	 */
	public String withdrawAccountRequest() {
		
		if (getCurrentAccount() == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}
		
		doWithdraw();
		String withdrawReason = getRequest().getParameter("withdrawReason");

		currentAccount = getCurrentAccount();

		AccountHistory accountHistory = new AccountHistory();

		accountHistory.setAccount(currentAccount);
		accountHistory.setAccountActionType(AccountActionType.STATUS_CHANGE);
		accountHistory.setActionTypeArguments(AccountStatus.WITHDRAWN.getName());
		accountHistory.setComment(withdrawReason);
		accountHistory.setCreatedDate(new Date());
		accountHistory.setChangedByUser(getUser());

		currentAccount.addAccountHistory(accountHistory);
		currentAccount.setLastUpdatedDate(new Date());

		currentAccount = accountManager.saveAccount(currentAccount);

		this.setJsonResponse("{}");

		return SUCCESS;
	}



	/**
	 * method that brings up lightbox for user to enter reason as to why they partiall approving
	 * 
	 * @return
	 */
	public String partialApprovalLightbox() {

		return PortalConstants.ACTION_PARTIAL_APPROVAL_LIGHBOX;
	}

	/**
	 * method that does the partial approval
	 * 
	 * @return
	 */
	public String partialApproval() {
		// String partialApprovalComments = getRequest().getParameter("partialApprovalComments");

		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}
		
		AccountHistory accountHistory = new AccountHistory();

		accountHistory.setAccount(currentAccount);
		accountHistory.setAccountActionType(AccountActionType.PARTIAL_APPROVE);
		accountHistory.setActionTypeArguments("");
		accountHistory.setComment(partialApprovalComments);
		accountHistory.setCreatedDate(new Date());
		accountHistory.setChangedByUser(getUser());

		currentAccount.addAccountHistory(accountHistory);
		currentAccount.setLastUpdatedDate(new Date());
		
		currentAccount.addAccountReportingLog(new AccountReportingLog(currentAccount,currentAccount.getAccountReportingType(), getRequestedAccountReportingType(), AccountReportType.PENDING, new Date()));

		currentAccount = accountManager.saveAccount(currentAccount);

		return null;
	}



	/**
	 * method that brings up lightbox for user to enter reason as to why they are temporarily rejecting
	 * 
	 * @return
	 */
	public String temporaryRejectionLightbox() {

		return PortalConstants.ACTION_TEMPORARY_REJECTION_LIGHTBOX;
	}

	/**
	 * method that does the temporary rejection
	 * 
	 * @return
	 */
	public StreamResult temporaryRejection() {
		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return new StreamResult(new ByteArrayInputStream((PortalConstants.ACTION_LANDING).getBytes()));
		}

		if (AccountStatus.REQUESTED == currentAccount.getAccountStatus()) {
			currentAccount.setAccountStatus(AccountStatus.PENDING);
		}

		currentAccount.setLastUpdatedDate(new Date());
		
		
		currentAccount.addAccountReportingLog(new AccountReportingLog(currentAccount, currentAccount.getAccountReportingType(), getRequestedAccountReportingType(), AccountReportType.AWAITING, new Date()));


		currentAccount = accountManager.saveAccount(currentAccount);

		StringBuffer rolesRequestedBuffer = new StringBuffer("<ul>");

		for (AccountRole role : currentAccount.getAccountRoleList()) {
			if (RoleStatus.PENDING == role.getRoleStatus()) {
				rolesRequestedBuffer.append("<li>");
				rolesRequestedBuffer.append(role.getRoleType().getTitle());
				rolesRequestedBuffer.append("</li>");
			}
		}
		rolesRequestedBuffer.append("</ul>");

		String rolesRequestedBody = rolesRequestedBuffer.toString();

		StringBuffer automatedMessageBuffer = new StringBuffer();


		for (AccountMessageTemplate messageTemplate : accountMessageTemplates) {
			String messageLabel = messageTemplate.getCheckboxText();
			String message = messageTemplate.getEmailMessage();

			Boolean simpleFormat = false;
			List<AccountMessageTemplate> emptyMsgTemplateList = accountMessageTemplateManager
					.getgAccountMessageTemplateEmptyMsgListByType(AccountMessageTemplateType.TEMPORARY_REJECTION);
			for (AccountMessageTemplate emptyMsgTemplate : emptyMsgTemplateList) {
				if (messageLabel.equalsIgnoreCase(emptyMsgTemplate.getCheckboxText())) {
					simpleFormat = true;
					break;
				}
			}
			if (simpleFormat) {
				automatedMessageBuffer
						.append(String.format(PortalConstants.AUTOMATED_TEMP_REJECT_MESSAGE_SIMPLE_FORMAT, message));
			} else {
				automatedMessageBuffer.append(
						String.format(PortalConstants.AUTOMATED_TEMP_REJECT_MESSAGE_FORMAT, messageLabel, message));
			}

		}

		String automatedMessageBody = automatedMessageBuffer.toString();
		
		// This is for eyegene with orgName as "NEI_BRICS", we don't want the OrgName to contain underscore in the email. 
		String orgName = modulesConstants.getModulesOrgName();
		if(orgName.contains("_")) {
			orgName = orgName.replaceAll("_", " ");
		}
		
		String subjectText =
				this.getText(PortalConstants.MAIL_RESOURCE_TEMP_REJECT + PortalConstants.MAIL_RESOURCE_SUBJECT,
						Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId())));

		String bodyText = this.getText(PortalConstants.MAIL_RESOURCE_TEMP_REJECT + PortalConstants.MAIL_RESOURCE_BODY,
				Arrays.asList(orgName, rolesRequestedBody, automatedMessageBody,
						modulesConstants.getModulesAccountURL(), modulesConstants.getModulesOrgEmail()));

		try {
			sendAccountEmail(currentAccount, subjectText, bodyText, false);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		recordTempRejectHistory();
		
		

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	private void recordTempRejectHistory() {
		StringBuffer historyCommentBuffer = new StringBuffer();
		List<AccountMessageTemplate> emptyMsgTemplateList = accountMessageTemplateManager
				.getgAccountMessageTemplateEmptyMsgListByType(AccountMessageTemplateType.TEMPORARY_REJECTION);

		Boolean isMessage = false;
		for (AccountMessageTemplate messageTemplate : accountMessageTemplates) {
			for (AccountMessageTemplate emptyMsgTemplate : emptyMsgTemplateList) {
				if (emptyMsgTemplate.getCheckboxText().equalsIgnoreCase(messageTemplate.getCheckboxText())) {
					isMessage = true;
					break;
				}
			}
			if (isMessage) {
				historyCommentBuffer.append(messageTemplate.getEmailMessage());
			} else {
				historyCommentBuffer.append(messageTemplate.getCheckboxText());
			}
			historyCommentBuffer.append("/");
		}

		historyCommentBuffer =
				historyCommentBuffer.replace(historyCommentBuffer.length() - 1, historyCommentBuffer.length(), "");

		AccountHistory accountHistory = new AccountHistory(currentAccount, AccountActionType.TEMP_REJECT, "",
				historyCommentBuffer.toString(), new Date(), getUser());

		if (logger.isDebugEnabled()) {
			logger.debug("Recording account history: " + accountHistory.toString());
		}

		currentAccount.addAccountHistory(accountHistory);

		accountManager.saveAccount(currentAccount);
	}



	/**
	 * method that brings up lightbox for user to enter reason as to why they partiall approving
	 * 
	 * @return
	 */
	public String finalApprovalLightbox() {

		return PortalConstants.ACTION_FINAL_APPROVAL_LIGHBOX;
	}

	public String finalApproval() throws ParseException {
		if (currentAccount == null) {
			currentAccount = getCurrentAccount();
		}
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		AccountHistory accountHistory = new AccountHistory(currentAccount, AccountActionType.FINAL_APPROVAL,
				PortalConstants.EMPTY_STRING, this.getFinalApprovalComments(), new Date(), getUser());

		currentAccount.addAccountHistory(accountHistory);
		currentAccount.setAccountStatus(AccountStatus.ACTIVE);
		currentAccount.setIsActive(true);

		Date expireDate = null;
		boolean expireDateExpiringSoon = false;
		if (finalApprovalExpireDate != null && !finalApprovalExpireDate.trim().equals("")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			expireDate = sdf.parse(finalApprovalExpireDate);
			Date date = new Date();

			long diff = expireDate.getTime() - date.getTime();
			int days = Math.round(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
			if (days >= 0 && days <= ServiceConstants.EXPIRATION_SOON_DAYS) {
				expireDateExpiringSoon = true;
			}
		}

		for (PermissionGroupMember member : currentAccount.getPermissionGroupMemberList()) {
			if (member.getPermissionGroupStatus() == PermissionGroupStatus.PENDING) {
				member.setPermissionGroupStatus(PermissionGroupStatus.ACTIVE);
				// no expiration date for permission group
				member.setExpirationDate(null);
			}
		}

		for (AccountRole role : currentAccount.getAccountRoleList()) {
			if (role.getRoleStatus() == RoleStatus.PENDING) {
				if (expireDateExpiringSoon) {
					role.setRoleStatus(RoleStatus.EXPIRING_SOON);
				} else {
					role.setRoleStatus(RoleStatus.ACTIVE);
				}
				role.setExpirationDate(expireDate);
			}
		}

		currentAccount.setAccountReportingType(getLatestRequestedAccountReportingType());
		currentAccount.addAccountReportingLog(
				new AccountReportingLog(currentAccount, currentAccount.getAccountReportingType(),
						getRequestedAccountReportingType(), AccountReportType.APPROVED, new Date()));

		currentAccount.setLastUpdatedDate(new Date());
		getSessionAccountEdit().setAccount(currentAccount);
		currentAccount = accountManager.saveAccount(currentAccount);		
		
		updateGuidAccount(currentAccount);

		// send email to user
		try {
			String url = getRequest().getRequestURL().toString();
			if (!url.contains("local")) {
				String bodyText = this.getText(
						PortalConstants.MAIL_RESOURCE_ACCEPTED_ACCOUNT + PortalConstants.MAIL_RESOURCE_BODY,
						Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
								modulesConstants.getModulesOrgEmail(getDiseaseId()),
								currentAccount.getUser().getFullName(), getSessionAccountEdit().getReason()))
						+ this.pdMessage();


				sendAccountEmail(currentAccount, PortalConstants.MAIL_RESOURCE_ACCEPTED_ACCOUNT, bodyText, true);

			}
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return SUCCESS;


	}

	public String list() {

		if (getInAccounts()) {
			return PortalConstants.ACTION_REDIRECT;

		} else if (getInAccountAdmin()) {

			getSessionAccountEdit().clearAll();
			return PortalConstants.ACTION_LIST;

		} else {
			return null;
		}
	}

	public String edit() {

		adminLoadUser();
		return PortalConstants.ACTION_EDIT;
	}

	public String viewUserAccount() {

		adminLoadUser();
		return PortalConstants.ACTION_VIEW_ACCOUNT;
	}

	public String viewAccountRequest() {

		adminLoadUser();

		// get guidance message lists
		getSessionAccountGuidanceEmails().clearAll();

		List<AccountMessageTemplate> accountTemporaryRejectionList =
				accountMessageTemplateManager.getAccountTemporaryRejectionList();

		getSessionAccountGuidanceEmails().setAccountTemporaryRejectionList(accountTemporaryRejectionList);

		return PortalConstants.ACTION_VIEW_ACCOUNT;
	}

	protected void adminLoadUser() {

		// Let's clear everything out first
		getSessionAccountEdit().clearAll();

		// If we have a parameter, then lets get it and get the user
		String accountId = getRequest().getParameter(PortalConstants.ACCOUNT_ID);

		if (StringUtils.isEmpty(accountId)) {
			throw new RuntimeException("Account ID is required to view/edit a user account.");
		}

		// If we're not in accountAdmin, then we should be allowed to access a user
		if (!(getInAccountAdmin() || getInAccountReviewer())) {
			throw new RuntimeException(
					"Cannot access user account because call is not from the account administator or reviewer namespace.");
		}

		try {
			currentAccount = accountManager.getAccount(getUser(), Long.valueOf(accountId));
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Illegal account ID");
		}

		// make shallow copy of the account into originalAccount
		Account originalAccount = shallowCopyAccount(currentAccount);

		getSessionAccountEdit().setOriginalAccount(originalAccount);

		// Save the stuff to session
		getSessionAccountEdit().setAccount(currentAccount);
		saveSession();
	}

	private Account shallowCopyAccount(Account account) {
		Account originalAccount = null;
		try {
			originalAccount = (Account) BeanUtils.cloneBean(currentAccount);
			originalAccount.setUser((User) BeanUtils.cloneBean(currentAccount.getUser()));

			Set<AccountRole> originalAccountRoles = new HashSet<AccountRole>();

			for (AccountRole accountRole : currentAccount.getAccountRoleList()) {
				AccountRole roleCopy = (AccountRole) BeanUtils.cloneBean(accountRole);
				originalAccountRoles.add(roleCopy);
			}

			originalAccount.setAccountRoleList(originalAccountRoles);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		return originalAccount;
	}

	/**
	 * Loads account and user information into session. If we are not creating a new session then the account_id URL
	 * will be missing. Should only be used for the current user
	 * 
	 * Called for viewProfile!view.action (View My Profile) Called for editProfile!view.action (load up Edit My Profile)
	 * 
	 * @return
	 */
	public String view() {

		// Let's clear everything out first
		getSessionAccountEdit().clearAll();

		// The only User Account a person can get is their own, so lets load that instead
		currentAccount = accountManager.getAccount(getUser(), getAccount().getId());

		// Save the stuff to session
		getSessionAccountEdit().setAccount(currentAccount);

		// make shallow copy of the account into originalAccount
		Account originalAccount = shallowCopyAccount(currentAccount);

		getSessionAccountEdit().setOriginalAccount(originalAccount);
		saveSession();

		return PortalConstants.ACTION_VIEW;
	}

	public String duc() {
		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		saveSession();

		checkForRequest();

		getSessionAccountEdit().setAccount(currentAccount);
		saveToFtp();
		return PortalConstants.ACTION_DUC;
	}

	/**
	 * This method is used for loading up the Create User page. Used both in the Admin Create Account and also the New
	 * User Create Account
	 */
	public String create() {

		// Remove anything from session. We want to start fresh
		getSessionAccountEdit().clearAll();

		// Current account is a new User Account
		currentAccount = new Account();

		setCurrentAccountInfo();

		saveSession();

		return PortalConstants.ACTION_INPUT;
	}

	private void setCurrentAccountInfo() {
		// Set the defaults for the new User Account
		currentAccount.setIsLocked(false);
		currentAccount.setAccountStatus(AccountStatus.REQUESTED);

		Date currentDate = new Date();
		currentAccount.setApplicationDate(currentDate);
		currentAccount.setLastUpdatedDate(currentDate);

		// Set the admin note field to the current domain (for determining if account was created from fitbir or cnrm)
		currentAccount.setAdminNote(PortalUtils.stripToDomain(getCurrentUrl()));
	}



	public String createDetails() {
		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		setCurrentAccountInfo();

		saveSession();

		return PortalConstants.ACTION_INPUT;
	}


	/**
	 * Loads the privileges request page for the Account Request
	 * 
	 * @return
	 */
	public String accountRequestPrivileges() {

		if (getCurrentAccount() == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		saveSession();
		// Get existing account that's in session.
		currentAccount = getCurrentAccount();
		
		return PortalConstants.ACTION_SUCCESS;
	}

	/**
	 * Save account form data into session.
	 */
	public void saveSession() {
		if (currentAccount == null) {
			currentAccount = getCurrentAccount();
		}

		if (accountDetailsForm != null) {
			accountDetailsForm.adapt(currentAccount, getEnforceStaticFields(), getNameSpace());
		} else {
			accountDetailsForm = new AccountDetailsForm(currentAccount);
		}

		if (accountPrivilegesForm != null) {
			accountPrivilegesForm.adapt(currentAccount, getEnforceStaticFields());
		} else {
			accountPrivilegesForm = new AccountPrivilegesForm(currentAccount);
		}
		
		if (accountSignatureForm != null) {
			accountSignatureForm.adapt(currentAccount);
		}
		
		getSessionAccountEdit().setAccount(currentAccount);
	}
	
	public String uploadDocumentation() {

		return PortalConstants.ACTION_SUCCESS;
	}

	public String addAdministrativeNote() {

		return PortalConstants.ACTION_SUCCESS;
	}

	public StreamResult uploadFile() {
		Account currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return new StreamResult(new ByteArrayInputStream((PortalConstants.ACTION_LANDING).getBytes()));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Uploading file: [ " + uploadFileName + ", " + uploadDescription + " ]");
		}

		try {
			repositoryManager.uploadFile(currentAccount.getUser().getId(), upload, uploadFileName,
					uploadDescription, ServiceConstants.FILE_TYPE_ACCOUNT, new Date());
		} catch (IOException | JSchException e) {
			e.printStackTrace();
		}

		AccountHistory accountHistory = new AccountHistory(getCurrentAccount(), AccountActionType.ADD_DOCUMENTATION,
				uploadFileName, "", new Date(), getUser());
		currentAccount.addAccountHistory(accountHistory);
		currentAccount.setLastUpdatedDate(new Date());
		
		currentAccount = accountManager.saveAccount(currentAccount);
		getSessionAccountEdit().setAccount(currentAccount);
		
		// Send the notification email to OPS when user uploads a new documentation (on View My Profiles page)
		if (currentAccount.getUserId() != null && currentAccount.getUserId().equals(getUser().getId())) {
			this.notifyDocUpload();
		}

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}


	private void notifyDocUpload() {

		logger.debug("Send notification email to admin on user uploading new documentation.");
		currentAccount = getCurrentAccount();
		User user = getUser();

		String subject = this.getText(PortalConstants.MAIL_RESOURCE_DOC_UPLOAD + PortalConstants.MAIL_RESOURCE_SUBJECT,
				Arrays.asList(user.getLastName(), user.getFirstName(), modulesConstants.getModulesOrgName()));

		String userNameLink = modulesConstants.getModulesAccountURL() + PortalConstants.VIEW_USER_DETAILS_URL
				+ getCurrentAccount().getId();

		String bodyText = this.getText(PortalConstants.MAIL_RESOURCE_DOC_UPLOAD + PortalConstants.MAIL_RESOURCE_BODY,
				Arrays.asList(user.getLastName(), user.getFirstName(), userNameLink, getCurrentAccount().getUserName(),
						modulesConstants.getModulesOrgName(), uploadFileName, uploadDescription,
						BRICSTimeDateUtil.formatDate(new Date())));

		try {
			accountManager.sendMail(subject, bodyText, user.getEmail(), modulesConstants.getModulesOrgEmail());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The goal of this method is to add a record in account history for editedAccount when there is a regular field
	 * edit being made. This is done by comparing the fields in question from editAccount with originalAccount.
	 * 
	 * @param originalAccount
	 * @param editedAccount
	 */
	private void recordFieldEditHistory(Account originalAccount, Account editedAccount) {
		if (originalAccount == null || editedAccount == null) {
			return;
		}

		AccountHistory accountHistory =
				new AccountHistory(editedAccount, AccountActionType.PROFILE_EDIT, "", null, new Date(), getUser());

		String comment = PortalConstants.EMPTY_STRING;

		// ERA ID
		comment = comment.concat(
				generateFieldEditComment("NIH Federal Identity", originalAccount.getEraId(), editedAccount.getEraId()));

		// First Name
		comment = comment.concat(generateFieldEditComment("First Name", originalAccount.getUser().getFirstName(),
				editedAccount.getUser().getFirstName()));

		// Middle Name
		comment = comment.concat(generateFieldEditComment("Middle Name", originalAccount.getUser().getMiddleName(),
				editedAccount.getUser().getMiddleName()));

		// Last Name
		comment = comment.concat(generateFieldEditComment("Last Name", originalAccount.getUser().getLastName(),
				editedAccount.getUser().getLastName()));

		// Email
		comment = comment.concat(generateFieldEditComment("E-Mail", originalAccount.getUser().getEmail(),
				editedAccount.getUser().getEmail()));

		// Institution
		comment = comment.concat(generateFieldEditComment("Institution", originalAccount.getAffiliatedInstitution(),
				editedAccount.getAffiliatedInstitution()));

		// Address 1
		comment = comment.concat(
				generateFieldEditComment("Street Line 1", originalAccount.getAddress1(), editedAccount.getAddress1()));

		// Address 2
		comment = comment.concat(
				generateFieldEditComment("Street Line 2", originalAccount.getAddress2(), editedAccount.getAddress2()));

		// City
		comment = comment.concat(generateFieldEditComment("City", originalAccount.getCity(), editedAccount.getCity()));

		// Zip
		comment = comment.concat(generateFieldEditComment("Postal Code", originalAccount.getPostalCode(),
				editedAccount.getPostalCode()));

		// State
		comment = comment.concat(generateFieldEditComment("State",
				originalAccount.getState() != null ? originalAccount.getState().getName() : "",
				editedAccount.getState() != null ? editedAccount.getState().getName() : ""));

		// Phone
		comment =
				comment.concat(generateFieldEditComment("Phone", originalAccount.getPhone(), editedAccount.getPhone()));

		// Administrative notes
		//comment = comment.concat(generateFieldEditComment("Administrative Notes", originalAccount.getAdminNote(),
			//	editedAccount.getAdminNote()));


		// if no comments were added, then that means no field edits were made.
		if (!comment.isEmpty()) {
			comment = comment.replaceFirst("\\R", (String) PortalConstants.EMPTY_STRING); // remove the first new line
			accountHistory.setComment(comment);
			logger.info("Recording Account History: " + accountHistory);
			editedAccount.addAccountHistory(accountHistory);
		}
	}

	/**
	 * Generates a line of field edit comment with the given changes. Will also prepend the comment with a new line.
	 * 
	 * @param fieldName
	 * @param originalValue
	 * @param newValue
	 * @return
	 */
	private String generateFieldEditComment(String fieldName, String originalValue, String newValue) {
		String comment = (String) PortalConstants.EMPTY_STRING;

		if (originalValue == null) {
			originalValue = (String) PortalConstants.EMPTY_STRING;
		}

		if (newValue == null) {
			newValue = (String) PortalConstants.EMPTY_STRING;
		}

		if (!originalValue.equals(newValue)) {
			comment = comment.concat(PortalConstants.NEW_LINE);
			comment = comment
					.concat(String.format(PortalConstants.FIELD_EDIT_FORMAT, fieldName, originalValue, newValue));
		}

		return comment;
	}

	/**
	 * This method submits the request by saving data in session onto the database
	 * 
	 * @return
	 */
	public String submit() throws UserAccessDeniedException{

		boolean isNewAccount = false;

		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		if (currentAccount.getId() == null) {
			isNewAccount = true;
		}

		if (currentAccount.getId() != null && updateExists()) {
			currentAccount.setLastUpdatedDate(new Date());
		}

		if (alternateWorkflow() && isNewAccount && !currentAccount.hasDucESignature()) {
			this.createDucESignature();
		}

		saveSession();
		checkForRequest();

		if (StringUtils.isEmpty(currentAccount.getUserName())) {
			return PortalConstants.ACTION_REDIRECT;
		}


		if (isNewAccount) {
			currentAccount.setInitialRequestDate(new Date());

			AccountActionType aat = AccountActionType.REQUEST;
			User requesterUser = currentAccount.getUser();

			AccountHistory accountRequestHistory = new AccountHistory(currentAccount, aat, "",
					currentAccount.getInterestInTbi(), new Date(), requesterUser);
			currentAccount.addAccountHistory(accountRequestHistory);
			currentAccount.setRequestSubmitDate(new Date());
					
			AccountType accountType = getRequestedAccountReportingType();
			
			// If user didn't choose any role type, the default is OTHER
			if (accountType == null) {
				accountType = AccountType.OTHER;
			}
			currentAccount.setAccountReportingType(accountType);
			currentAccount.addAccountReportingLog(new AccountReportingLog(currentAccount, accountType, accountType,
					AccountReportType.NEW, new Date()));

			String bodyText = this.getText(PortalConstants.MAIL_RESOURCE_REQUESTED + PortalConstants.MAIL_RESOURCE_BODY,
					Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()), // {0}
							modulesConstants.getModulesOrgName(getDiseaseId()), // {1}
							modulesConstants.getModulesOrgEmail(getDiseaseId()), // {2}
							modulesConstants.getModulesOrgName(getDiseaseId()))); // {3}));

			StringBuilder bodyTextBuilder = new StringBuilder();
			bodyTextBuilder.append(PortalConstants.MAIL_ADMIN_RESOURCE_REQUESTED);
			bodyTextBuilder.append(PortalConstants.MAIL_RESOURCE_SUBJECT);
			
			
			String adminMailBodyStr =bodyTextBuilder.toString();
			String orgName = modulesConstants.getModulesOrgName(getDiseaseId());
			
			String adminMailSubject = this.getText(adminMailBodyStr,
					Arrays.asList(orgName));

			String adminReviewLink =modulesConstants.getModulesAccountURL()+"portal/accountReviewer/accountReportsAction!requestList.action";
		
			String adminMailbodyText = this.getText(PortalConstants.MAIL_ADMIN_RESOURCE_REQUESTED + PortalConstants.MAIL_RESOURCE_BODY,
					Arrays.asList(orgName, // {0}
							currentAccount.getUserName(),//{1}
							currentAccount.getUser().getFullName(),//{2}
							adminReviewLink //{3}
						));
				
			String adminEmailId = modulesConstants.getOpsEmailID();
			
			try {
				sendAccountEmail(currentAccount, PortalConstants.MAIL_RESOURCE_REQUESTED, bodyText, false);
				
				sendAdminEmail(adminEmailId, adminMailSubject, adminMailbodyText);
	
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		} else {
			Account originalAccount = getSessionAccountEdit().getOriginalAccount();
			recordFieldEditHistory(originalAccount, currentAccount);
			recordExpirationDateChange();
			recordPrivilegeChange();
			recordPermissionGroupChange();
		}
		
		currentAccount = accountManager.saveAccount(currentAccount);
		
		// If it's a new account request, generate and upload the electronic signature file.
		if (isNewAccount && !currentAccount.hasBricsESignature()) {
			this.generateESignatureDoc();
		}

		// updates user information in the GUID server
		updateGuidAccount(currentAccount);

		getSessionAccountEdit().setAccount(currentAccount);
		saveToFtp();

		return PortalConstants.ACTION_SUCCESS;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/accountAdmin/accountAction!getAccountLists.action
	public String getAccountLists() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			List<Account> outputList = accountManager.getAccountList();
			
			// get the map of accountId to the latest adminNote and set it at each account
			Map<Long, String> latestAdminNoteMap = accountManager.getLatestAdminNoteForAccounts(outputList);
			for (Account account : outputList) {
				if (latestAdminNoteMap.containsKey(account.getId())) {
					account.setAdminNote(latestAdminNoteMap.get(account.getId()));
				} else {
					account.setAdminNote("");
				}
			}

			idt.setList((ArrayList<Account>) outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/accounts/viewProfile!getExistingFiles.action
	public String getExistingFiles() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<UserFile> outputList = new ArrayList<UserFile>(getCurrentUserFiles());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ExistingFileListIdtDecorator(getInAccountReviewer(), getOnlyReviewer()));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/accounts/viewProfile!getPermissionGroupMembers.action
	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/accountAdmin/viewProfile!getPermissionGroupMembers.action
	public String getPermissionGroupMembers() {

		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<PermissionGroupMember> outputList =
					new ArrayList<PermissionGroupMember>(getPermissionGroupMemberList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountRoleListIdtDecorator(getOnlyReviewer(), getIsAccountAdmin()));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/accounts/viewProfile!getAccountRoleLists.action
	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/accountAdmin/viewProfile!getAccountRoleLists.action
	// getting exisiting privs
	public String getExistingAccountRoleLists() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<AccountRole> outputList = new ArrayList<AccountRole>(getAccountRoleList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountRoleListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}



	public String getAccountRoleLists() {
		/*
		 * try { IdtInterface idt = new Struts2IdtInterface(); ArrayList<AccountRole> outputList = new
		 * ArrayList<AccountRole>(getAccountRoleList()); idt.setList(outputList);
		 * idt.setTotalRecordCount(outputList.size()); idt.setFilteredRecordCount(outputList.size()); idt.decorate(new
		 * AccountRoleListIdtDecorator(getInAccountReviewer())); idt.output(); } catch (InvalidColumnException e) {
		 * logger.error("invalid column: " + e); e.printStackTrace(); }
		 */
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<AccountRole> outputList = getAlwaysShownAccountRoles();
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountRoleListIdtDecorator(getOnlyReviewer(), getIsAccountAdmin()));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}



	public ArrayList<AccountRole> getAlwaysShownAccountRoles() {
		ArrayList<AccountRole> rolesToShow = new ArrayList<AccountRole>(getAccountRoleList());
		ArrayList<RoleType> alwaysShownRoleTypes =
				new ArrayList<RoleType>(Arrays.asList(RoleType.getAlwaysShownRoles()));
		for (RoleType currentRoleType : alwaysShownRoleTypes) {
			boolean alreadyInRoleList =
					rolesToShow.stream().map(AccountRole::getRoleType).anyMatch(currentRoleType::equals);

			if (!alreadyInRoleList) {
				AccountRole nrAr = new AccountRole();
				nrAr.setRoleType(currentRoleType);
				nrAr.setRoleStatus(RoleStatus.NOT_REQUESTED);
				rolesToShow.add(nrAr);
			}
		}
		return rolesToShow;
	}



	// url: http://fitbir-portal-local-cit.nih.gov:8080/portal/accounts/viewProfile!getAccountHistory.action
	// url: http://fitbir-portal-local-cit.nih.gov:8080/portal/accountAdmin/viewProfile!getAccountHistory.action
	public String getAccountHistory() {
		
		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}
		
		try {
			List<AccountHistory> accountHistory = accountManager.getAccountHistory(currentAccount);

			// current user is not an admin, remove partial approval entry from the account history
			if (!getIsAccountAdmin() && !getIsAccountReviewer()) {
				List<AccountHistory> toRemoveList = accountHistory.stream()
						.filter(x -> x.getAccountActionType() == AccountActionType.PARTIAL_APPROVE)
						.collect(Collectors.toList());
				for (AccountHistory toRemove : toRemoveList) {
					accountHistory.remove(toRemove);
				}
			}

			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<AccountHistory> outputList = new ArrayList<AccountHistory>(accountHistory);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountHistoryListIdtDecorator(getCurrentAccount()));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	public String getAccountAdministrativeNotes() {
		
		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}
		
		try {
			// current user is not an admin, don't show anything in the table
			if (!getIsAccountAdmin() && !getIsAccountReviewer()) {
				return null;
			}

			Set<AccountAdministrativeNote> accountAdminNotesSet = currentAccount.getAccountAdministrativeNotes();
			List<AccountAdministrativeNote> accountAdminNotesDisplayList = new ArrayList<AccountAdministrativeNote>();
			if (accountAdminNotesSet != null) {
				accountAdminNotesDisplayList.addAll(accountAdminNotesSet);
			}

			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<AccountAdministrativeNote> outputList =
					new ArrayList<AccountAdministrativeNote>(accountAdminNotesDisplayList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new AccountAdministrativeNotesListIdtDecorator(getCurrentAccount()));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	

	/**
	 * Handles saving the privilege changes from the user to the session and the database.
	 * 
	 * @return The success result string.
	 */
	public String submitPrivilegeRequest() {
		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		if (currentAccount.getId() != null) {
			currentAccount.setLastUpdatedDate(new Date());
		}

		// Process privilege changes from the user.
		saveSession();
		checkForRequest();
		recordPrivilegeChange();
		recordExpirationDateChange();
		recordPermissionGroupChange();
		
		AccountReportType type;
		
		if(currentAccount.getAccountStatus().equals(AccountStatus.REQUESTED)) {
			type = AccountReportType.NEW;
		}
		else{
			type = AccountReportType.CHANGE_REQUESTED;
		}
		
		currentAccount.addAccountReportingLog(new AccountReportingLog(currentAccount,
				currentAccount.getAccountReportingType(), getRequestedAccountReportingType(), type, new Date()));		

		// Save changes to the database and session.
		currentAccount = accountManager.saveAccount(currentAccount);

		// we just committed the changes, so reset the original account
		getSessionAccountEdit().setAccount(currentAccount);
		getSessionAccountEdit().setOriginalAccount(shallowCopyAccount(currentAccount));

		return PortalConstants.ACTION_SUCCESS;
	}

	/**
	 * If account is active, check for any new requests, if so, set account to 'pending.' Does nothing otherwise
	 */
	public void checkForRequest() {
		boolean newRequest = false;

		if (currentAccount.getAccountStatus() == AccountStatus.ACTIVE) {
			// Check for any requested roles, and set the account status to pending if any are found.
			for (AccountRole role : currentAccount.getAccountRoleList()) {
				if (role.getRoleStatus() == RoleStatus.PENDING) {
					newRequest = true;
					break;
				}
			}
		} else if (currentAccount.getAccountStatus() == AccountStatus.RENEWAL_REQUESTED) {
			// If the account is renewal requested, we can use expiration date to distinguish newly requested role from
			// the pending role that was in expired/expiring soon status.
			for (AccountRole role : currentAccount.getAccountRoleList()) {
				if (role.getRoleStatus() == RoleStatus.PENDING && role.getExpirationDate() == null) {
					newRequest = true;
					break;
				}
			}
		}

		// Check permission group changes if no new request found
		if (!newRequest && currentAccount.getAccountStatus() == AccountStatus.ACTIVE) {
			for (PermissionGroupMember member : currentAccount.getPermissionGroupMemberList()) {
				if (member.getPermissionGroupStatus() == PermissionGroupStatus.PENDING) {
					newRequest = true;
					break;
				}
			}
		}

		// if there is a new request applied, then set a new request submit date
		if (newRequest) {
			currentAccount.setAccountStatus(AccountStatus.CHANGE_REQUESTED);
			currentAccount.setRequestSubmitDate(new Date());
		}
	}

	public void recordPrivilegeChange() {

		Account originalAccount = getSessionAccountEdit().getOriginalAccount();

		// no need to track privilege change if the account is rejected
		if (!accountRejected(getCurrentAccount())) {

			for (AccountRole role : currentAccount.getAccountRoleList()) {
				AccountRole originalRole = originalAccount.getAccountByRoleType(role.getRoleType());

				// add an account history if the role was originally not requested but now is.
				if (role.getRoleStatus() == RoleStatus.PENDING
						&& (originalRole == null || originalRole != null && !originalRole.getIsActive())) {
					String roleTitle = role.getRoleType().getTitle();
					String actionTypeArguments = roleTitle;
					AccountHistory privilegeRequestHistory = new AccountHistory(currentAccount,
							AccountActionType.PRIVILEGE_REQUEST, actionTypeArguments, "", new Date(), getUser());
					currentAccount.addAccountHistory(privilegeRequestHistory);
				}

				// record account history if the role is now active but was originally not active
				if (role.getIsActive() && originalRole != null && !originalRole.getIsActive()
						|| role.getIsActive() && originalRole == null) {
					AccountHistory newAccountHistory =
							new AccountHistory(currentAccount, AccountActionType.ADMIN_ADD_PERMISSION,
									role.getRoleType().getTitle(), "", new Date(), getUser());
					currentAccount.addAccountHistory(newAccountHistory);
				}

				// record account history if the role was originally active and now role is inactive
				if (originalRole != null && originalRole.getIsActive() && !role.getIsActive()) {
					AccountHistory newAccountHistory =
							new AccountHistory(currentAccount, AccountActionType.ADMIN_REMOVE_PERMISSION,
									role.getRoleType().getTitle(), "", new Date(), getUser());
					currentAccount.addAccountHistory(newAccountHistory);

				}
			}
		}
	}

	private void recordExpirationDateChange() {
		Account originalAccount = getSessionAccountEdit().getOriginalAccount();
		StringBuilder expirationDateCommentStringBuilder = new StringBuilder();

		for (AccountRole role : currentAccount.getAccountRoleList()) {
			AccountRole originalRole = originalAccount.getAccountByRoleType(role.getRoleType());

			String expirationComment = null;

			if (originalRole != null && originalRole.getIsActive() && role.getIsActive()) {
				expirationComment = generateExpirationComment(originalRole, role);
			}

			if (expirationComment != null) {
				expirationDateCommentStringBuilder.append(expirationComment).append(PortalConstants.NEW_LINE);
			}
		}

		if (expirationDateCommentStringBuilder.length() > 0) {
			AccountHistory newAccountHistory = new AccountHistory(currentAccount, AccountActionType.EXPIRATION_CHANGE,
					"", expirationDateCommentStringBuilder.toString(), new Date(), getUser());
			currentAccount.addAccountHistory(newAccountHistory);
		}
	}

	private String generateExpirationComment(AccountRole originalRole, AccountRole newRole) {
		if (originalRole == null || newRole == null) {
			return null;
		}

		String originalDateString = originalRole.getExpirationDateISOFormat();
		String newDateString = newRole.getExpirationDateISOFormat();

		if (!originalDateString.equals(newDateString)) {
			return String.format(PortalConstants.EXPIRATION_CHANGE_COMMENT_FORMAT, newRole.getRoleType().getTitle(),
					originalDateString, newDateString);
		} else {
			return null;
		}
	}

	@Deprecated
	public void recordPrivilegeRequests() {

		for (AccountRole role : currentAccount.getAccountRoleList()) {

			if (role.getRoleStatus() == RoleStatus.PENDING && !privilegeRequestsRecorded(role)) {

				String roleTitle = role.getRoleType().getTitle();
				String actionTypeArguments = roleTitle;
				AccountHistory privilegeRequestHistory = new AccountHistory(currentAccount,
						AccountActionType.PRIVILEGE_REQUEST, actionTypeArguments, "", new Date(), getUser());
				currentAccount.addAccountHistory(privilegeRequestHistory);
			}
		}
	}

	public boolean privilegeRequestsRecorded(AccountRole role) {

		Account originalAccount = getSessionAccountEdit().getOriginalAccount();

		for (AccountRole accountRole : originalAccount.getAccountRoleList()) {
			if (role.getRoleType().equals(accountRole.getRoleType())
					&& accountRole.getRoleStatus() == RoleStatus.PENDING) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Same as the normal submit action, except approves the account and all its account roles before saving.
	 * 
	 * @throws MessagingException
	 */
	public String adminSubmit() throws MessagingException, UserAccessDeniedException {

		if (currentAccount == null) {
			currentAccount = getCurrentAccount();
		}
		
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		if (currentAccount.getId() != null) {
			// Determine if there needs to be a new message
			Map<RoleType, String> changeMap = accountManager.determineChanges(currentAccount, accountPrivilegesForm);

			// do not send email about changes if the account is withdrawn.
			// it will allow an admin to change the request without the user knowing to avoid confusion
			if (!changeMap.isEmpty() && currentAccount.getAccountStatus() != AccountStatus.WITHDRAWN) {

				String url = getRequest().getRequestURL().toString();
				if (!url.contains("local")) {

					// Build a list of changes
					StringBuffer adminMessages = new StringBuffer();

					for (Entry<RoleType, String> changeEntry : changeMap.entrySet()) {
						RoleType roleType = changeEntry.getKey();
						String message = changeEntry.getValue().trim();

						if (roleType != RoleType.ROLE_USER) {
							adminMessages.append("\t - The " + roleType.getTitle() + " " + message + ".\n");
						}
					}

					String bodyMsgText = this.getText(
							PortalConstants.MAIL_RESOURCE_PENDING_ACCOUNT + PortalConstants.MAIL_RESOURCE_BODY,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()), // {0}
									modulesConstants.getModulesOrgEmail(getDiseaseId()), // {1}
									currentAccount.getUser().getFullName(), // {2}
									adminMessages.toString(), // {3}
									modulesConstants.getModulesOrgPhone(getDiseaseId())));

					sendAccountEmail(currentAccount, PortalConstants.MAIL_RESOURCE_PENDING_ACCOUNT, bodyMsgText, true);
				}
			}

		} else {
			saveSession();

			// do not send email about changes if the account is withdrawn.
			// it will allow an admin to change the request without the user knowing to avoid confusion
			if (currentAccount.getAccountStatus() != AccountStatus.WITHDRAWN) {

				String url = getRequest().getRequestURL().toString();

				// if not in a developer environment, send the email.
				if (!url.contains("local")) {
					String messageText = this.getText(
							PortalConstants.MAIL_RESOURCE_ADMIN_ACCOUNT_CREATE + PortalConstants.MAIL_RESOURCE_BODY,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()), // {0}
									currentAccount.getUserName(), // {1}
									modulesConstants.getModulesOrgEmail(getDiseaseId()), // {2}
									currentAccount.getUser().getFullName(), // {3}
									modulesConstants.getModulesOrgPhone(getDiseaseId()))); // {4}
					sendAccountEmail(currentAccount, PortalConstants.MAIL_RESOURCE_ADMIN_ACCOUNT_CREATE, messageText,
							false);
				}
			}
			
		}

		// only set the account to active if the account is not withdrawn
		if (currentAccount.getAccountStatus() != AccountStatus.WITHDRAWN) {
			currentAccount.setAccountStatus(AccountStatus.ACTIVE);
			currentAccount.setIsActive(true);
			currentAccount.setAccountReportingType(getRequestedAccountReportingType());
		}

		getSessionAccountEdit().setAccount(currentAccount);

		return submit();
	}

	/**
	 * An update would include a change to email, address, reason for access, or newly uploaded document.
	 * 
	 * @return true if an update is found.
	 */
	private boolean updateExists() {
		Account originalAccount = getCurrentAccount();

		// Email
		String oldEmail = originalAccount.getUser().getEmail();
		String newEmail = accountDetailsForm.getEmail();
		if (!oldEmail.trim().equalsIgnoreCase(newEmail.trim())) {
			return true;
		}

		// Address
		String oldAddress1 = originalAccount.getAddress1();
		String newAddress1 = accountDetailsForm.getAddress1();
		if (!oldAddress1.trim().equalsIgnoreCase(newAddress1.trim())) {
			return true;
		}

		// Address2 can be empty so null check is needed.
		String oldAddress2 = originalAccount.getAddress2();
		String newAddress2 = accountDetailsForm.getAddress2();
		if ((oldAddress2 == null && newAddress2 != null) || (oldAddress2 != null && newAddress2 == null)
				|| (oldAddress2 != null && newAddress2 != null
						&& !oldAddress2.trim().equalsIgnoreCase(newAddress2.trim()))) {
			return true;
		}

		if (!originalAccount.getCity().trim().equalsIgnoreCase(accountDetailsForm.getCity().trim())) {
			return true;
		}

		if (originalAccount.getCountry().getId().equals(accountDetailsForm.getCountry().getId())) {
			return true;
		}

		if (!originalAccount.getPostalCode().trim().equalsIgnoreCase(accountDetailsForm.getPostalCode().trim())) {
			return true;
		}

		// Reason for access
		String oldReason = originalAccount.getInterestInTbi();
		String newReason = accountDetailsForm.getInterestInTbi();
		if (!oldReason.trim().equalsIgnoreCase(newReason.trim())) {
			return true;
		}

		// newly uploaded document
		uploadedFilesMap = getSessionAccountEdit().getUploadedFilesMap();
		if (uploadedFilesMap != null && !uploadedFilesMap.isEmpty()) {
			return true;
		}

		return false;
	}


	/**
	 * Uploads all the files in session to sftp server.
	 */
	public void saveToFtp() {

		uploadedFilesMap = getSessionAccountEdit().getUploadedFilesMap();

		if (uploadedFilesMap != null) {
			for (UserFile userFile : uploadedFilesMap.keySet()) {
				byte[] currentFile = uploadedFilesMap.get(userFile);
				if (currentFile != null) {
					try {
						repositoryManager.uploadFile(getCurrentAccount().getUser().getId(), currentFile,
								userFile.getName(), userFile.getDescription(), ServiceConstants.FILE_TYPE_ACCOUNT,
								userFile.getUploadedDate());

						User requesterUser = new User();
						Long uid = getAccount().getUser().getId();
						if (uid != null) {
							requesterUser = getAccount().getUser();
						} else {
							requesterUser = currentAccount.getUser();
						}
						// record in history upload of new file.
						AccountHistory accountHistory = new AccountHistory(getCurrentAccount(),
								AccountActionType.ADD_DOCUMENTATION, userFile.getName(), "", new Date(), requesterUser);
						currentAccount.addAccountHistory(accountHistory);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			uploadedFilesMap.clear();

		}
	}


	public String cancelRequestedGroup() {
		hibernateManager.clearMetaHibernateCache();
		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		String paramGroupName = getRequest().getParameter("groupName");

		Set<PermissionGroupMember> groupMemberList = currentAccount.getPermissionGroupMemberList();

		PermissionGroupMember toRemove = groupMemberList.parallelStream()
				.filter(x -> paramGroupName.equals(x.getPermissionGroup().getGroupName())).findFirst().get();

		accountManager.removePermissionGroupMember(toRemove);

		currentAccount = accountManager.getAccountById(currentAccount.getId());

		getSessionAccountEdit().setAccount(currentAccount);


		if (!areThereAnyRequests() && AccountStatus.CHANGE_REQUESTED == currentAccount.getAccountStatus()) {
			currentAccount.setAccountStatus(AccountStatus.ACTIVE);
			currentAccount = accountManager.saveAccount(currentAccount);
		}

		AccountHistory newAccountHistory =
				new AccountHistory(currentAccount, AccountActionType.PERMISSION_GROUP_WITHDRAWAL,
						toRemove.getPermissionGroup().getDisplayName(), "", new Date(), getUser());
		currentAccount.addAccountHistory(newAccountHistory);
		accountManager.saveAccount(currentAccount);

		getSessionAccountEdit().setAccount(currentAccount);

		return SUCCESS;
	}

	/**
	 * Approve a requested account and take the user to the edit page.
	 * 
	 * @return
	 */
	public String approve() throws MessagingException, UserAccessDeniedException {

		if (getCurrentAccount() == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}
		
		saveSession();
		currentAccount = getCurrentAccount();

		Account enabledAccount = accountManager.approveAccount(currentAccount);
		if (enabledAccount == null) {
			throw new NullPointerException("Account ID cannot be null");
		}

		currentAccount.setLastUpdatedDate(new Date());
		
		currentAccount.setAccountReportingType(getLatestRequestedAccountReportingType());
		
		currentAccount.addAccountReportingLog(new AccountReportingLog(currentAccount,currentAccount.getAccountReportingType(), getRequestedAccountReportingType(), AccountReportType.APPROVED, new Date()));

		submit();

		// send email to user
		String url = getRequest().getRequestURL().toString();
		if (!url.contains("local")) {
			String bodyText =
					this.getText(PortalConstants.MAIL_RESOURCE_ACCEPTED_ACCOUNT + PortalConstants.MAIL_RESOURCE_BODY,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgEmail(getDiseaseId()),
									currentAccount.getUser().getFullName(), getSessionAccountEdit().getReason()))
							+ this.pdMessage();

			sendAccountEmail(currentAccount, PortalConstants.MAIL_RESOURCE_ACCEPTED_ACCOUNT, bodyText, true);
		}

		return PortalConstants.ACTION_SUCCESS;
	}

	/**
	 * method that brings up lightbox for user to enter reason as to why they are rejecting an account
	 * 
	 * @return
	 */
	public String rejectionLightbox() {

		return PortalConstants.ACTION_REJECTION_LIGHTBOX;
	}

	/**
	 * Reject a requested account and take the user to the edit page.
	 * 
	 * @return
	 */
	public String reject() throws MessagingException , UserAccessDeniedException{

		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		// send email to user
		String bodyText =
				this.getText(PortalConstants.MAIL_RESOURCE_REJECTED_ACCOUNT + PortalConstants.MAIL_RESOURCE_BODY,
						Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
								modulesConstants.getModulesOrgEmail()));

		sendAccountEmail(currentAccount, PortalConstants.MAIL_RESOURCE_REJECTED_ACCOUNT, bodyText, false);

		// Null out the ERa Id because currently there is no way to modify this
		currentAccount.setEraId(null);
		Account enabledAccount = accountManager.rejectAccount(currentAccount);
		if (enabledAccount == null) {
			throw new NullPointerException("Account ID cannot be null");
		}

		// Because we call saveSession in this function and again in submit(), we have to
		// create a new accountDetailsForm where the username field has been timestamped.
		accountDetailsForm = new AccountDetailsForm(currentAccount);

		String reason = rejectionReason;

		AccountHistory accountRejectHistory =
				new AccountHistory(currentAccount, AccountActionType.REJECT, "", reason, new Date(), getUser());

		currentAccount.addAccountHistory(accountRejectHistory);

		currentAccount.setLastUpdatedDate(new Date());
		
		currentAccount.addAccountReportingLog(new AccountReportingLog(currentAccount,currentAccount.getAccountReportingType(), getRequestedAccountReportingType(), AccountReportType.REJECTED, new Date()));


		submit();

		return PortalConstants.ACTION_SUCCESS;
	}


	/**
	 * Used to submit an administrator approving privileges without approving or rejecting an account.
	 * 
	 * @return
	 * @throws MessagingException
	 */
	public String approvePrivileges() throws MessagingException {

		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		String adminMessageText = getSessionAccountEdit().getReason();
		Map<RoleType, String> changeMap = accountManager.determineChanges(currentAccount, accountPrivilegesForm);

		saveSession();

		if (currentAccount.getId() != null) {
			currentAccount.setLastUpdatedDate(new Date());
		}

		// Check if the account can be set to active.
		currentAccount = accountManager.accountActivation(currentAccount);

		// Save changes to the database and session.
		currentAccount = accountManager.saveAccount(currentAccount);
		getSessionAccountEdit().setAccount(currentAccount);

		// Build and send email
		String url = getRequest().getRequestURL().toString();
		if (!url.contains("local")) {
			StringBuilder adminMsgSB = new StringBuilder();
			adminMsgSB.append(adminMessageText).append("\n\n");

			for (RoleType roleType : changeMap.keySet()) {
				if (!RoleType.ROLE_ADMIN.equals(roleType) && !RoleType.ROLE_USER.equals(roleType)) {
					adminMsgSB.append("\t - The ").append(roleType.getTitle()).append(" ")
							.append(changeMap.get(roleType).trim()).append(".\n");
				}
			}

			String messageText =
					this.getText(PortalConstants.MAIL_RESOURCE_PENDING_ACCOUNT + PortalConstants.MAIL_RESOURCE_BODY,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()), // {0}
									modulesConstants.getModulesOrgEmail(getDiseaseId()), // {1}
									currentAccount.getUser().getFullName(), // {2}
									adminMsgSB.toString(), // {3}
									modulesConstants.getModulesOrgPhone(getDiseaseId()))); // {4}

			logger.debug(messageText);
			sendAccountEmail(currentAccount, PortalConstants.MAIL_RESOURCE_PENDING_ACCOUNT, messageText, false);
		}

		return PortalConstants.ACTION_SUCCESS;
	}

	public Boolean pendingAccountGroups() {

		Set<PermissionGroupMember> acctGrpList = currentAccount.getPermissionGroupMemberList();
		for (PermissionGroupMember pgm : acctGrpList) {
			if (pgm.getPermissionGroupStatus().equals(PermissionGroupStatus.PENDING)) {
				return true;
			}
		}
		return false;
	}

	public String addFileLightbox() {
		return PortalConstants.ADD_FILE_LIGHTBOX;
	}

	public String addPermissionLightbox() {
		return PortalConstants.ADD_PERMISSION_LIGHTBOX;
	}

	/**
	 * This method handles uploading of files by storing a list of uploaded files onto session
	 * 
	 * @return
	 */
	public String upload() {
		if (uploadFileName != null) {
			// get the map of uploaded files in session
			uploadedFilesMap = getSessionAccountEdit().getUploadedFilesMap();

			// initialize map if there are no files that have been uploaded
			if (uploadedFilesMap == null) {
				uploadedFilesMap = new HashMap<UserFile, byte[]>();
			}

			// Set to true if the user wishes to re-upload a file
			if (removeDuplicate) {
				removeKey();
			}

			// set the user file information from the request
			UserFile userFile = new UserFile();
			userFile.setName(uploadFileName);
			userFile.setDescription(uploadDescription);
			userFile.setUploadedDate(new Date());

			try {
				// load the file into stream and save the byte array and userfile into session
				InputStream in = new FileInputStream(upload);
				byte[] bytes = new byte[(int) upload.length()];

				try {
					// TODO It is really not a good idea to write the whole contains of a file to memory, especially
					// when the default max file upload in struts is 250 MB. A better alternative is to save opened
					// input streams to the map, that way struts will not delete the temp files until the input stream
					// is closed.
					in.read(bytes);
					uploadedFilesMap.put(userFile, bytes);
					getSessionAccountEdit().setUploadedFilesMap(uploadedFilesMap);
				} finally {
					if (in != null) {
						in.close();
					}
				}
			} catch (FileNotFoundException e) {
				logger.error("Could not find the " + uploadFileName + " file.", e);
			} catch (IOException e) {
				logger.error("Could not read from " + uploadFileName + ".", e);
			}
		}

		if (getCurrentAccount() == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}
		
		if (PortalConstants.NAMESPACE_PUBLICACCOUNTS.equals(getNameSpace()) || getIsRequest()) {
			saveSession();

		} else if (getInAccounts()) {
			// This only applies on Edit user's own account page. File is saved upon uploading as there is
			// no Save/Submit button available. For all other scenarios uploaded file won't be saved to the database
			// until user clicks Save/Submit/Update button at the end of the work flow.
			saveToFtp();

			// set lastUpdatedDate and save current account
			currentAccount = getCurrentAccount();
			currentAccount.setLastUpdatedDate(new Date());
			accountManager.saveAccount(currentAccount);

		} else {
			// Admin edits user details
			saveSession();
		}

		return PortalConstants.ACTION_SUCCESS;
	}

	/**
	 * 
	 * @return
	 */
	public String removeFile() {

		// get the map of uploaded files in session
		uploadedFilesMap = getSessionAccountEdit().getUploadedFilesMap();
		removeKey();

		return "success";
	}

	/**
	 * This method will take the file name from uploadFileName and remove that file from the map It is meant to remove
	 * duplicate files from the map if the user wishes to
	 */
	private void removeKey() {

		UserFile removeKey = new UserFile();

		for (UserFile userFile : uploadedFilesMap.keySet()) {
			// file name has to be unique. if the file names match remove the file from the map
			if (userFile.getName().equalsIgnoreCase(uploadFileName)) {
				removeKey = userFile;
				break;
			}
		}

		uploadedFilesMap.remove(removeKey);
	}

	public String moveToRequest() {

		if (getCurrentAccount() == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}
		
		saveSession();
		saveToFtp();

		currentAccount = accountManager.saveAccount(currentAccount);
		getSessionAccountEdit().setAccount(currentAccount);


		return PortalConstants.ACTION_REDIRECT;
	}

	/**
	 * Action for changing the users password.
	 * 
	 * @return
	 */
	public String changePassword() {

		Account updatedAccount = accountManager.changePassword(getAccount(), newPassword);

		// Record Password when general user reset his password in account history
		accountHistory = new AccountHistory(updatedAccount, AccountActionType.PASSWORD_RESET,
				PortalConstants.EMPTY_STRING, PASSWORD_RESET, new Date(), getAccount().getUser());
		updatedAccount.addAccountHistory(accountHistory);

		// Save changes to the database
		accountManager.saveAccount(updatedAccount);

		return PortalConstants.ACTION_SUCCESS;
	}

	/**
	 * returns true is username is availible, false otherwise
	 * 
	 * @return
	 */
	public StreamResult checkUserName() {

		if (getCurrentAccount() == null) {
			return new StreamResult(new ByteArrayInputStream((PortalConstants.ACTION_LANDING).getBytes()));
		}

		if (!accountManager.validateUserName(userName)) {
			return new StreamResult(new ByteArrayInputStream(PortalConstants.ILLEGAL_USERNAME.getBytes()));
		} else if (accountManager.checkUserNameAvailability(userName, getCurrentAccount().getId())) {
			return new StreamResult(new ByteArrayInputStream(PortalConstants.USERNAME_AVAILABLE.getBytes()));
		} else {
			return new StreamResult(new ByteArrayInputStream(PortalConstants.USERNAME_NOT_AVAILABLE.getBytes()));
		}
	}

	/**
	 * Deactivates the current account and returns the user to the edit page.
	 */
	public String deactivate() {

		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		AccountHistory accountHistory =
				new AccountHistory(currentAccount, AccountActionType.DEACTIVATE, "", "", new Date(), getUser());

		currentAccount.addAccountHistory(accountHistory);

		Account disabledAccount = accountManager.deactivateAccount(currentAccount);

		currentAccount.setUserName(disabledAccount.getUserName());
		currentAccount.setAccountStatus(disabledAccount.getAccountStatus());
		currentAccount.setIsActive(disabledAccount.getIsActive());
		return PortalConstants.ACTION_VIEW_ACCOUNT;
	}

	private void doWithdraw() {
		// get the account in session
		currentAccount = getCurrentAccount();
		currentAccount = accountManager.withdrawAccount(currentAccount.getId());
		getSessionAccountEdit().setAccount(currentAccount);
	}

	/**
	 * Withdraw the current account and returns the user to the edit page. -- this case happens when a user requests an
	 * account but wishes to withdraw the request from the system. (PS-828)
	 */
	public String withdraw() {
		if (getCurrentAccount() == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}
		
		doWithdraw();
		return PortalConstants.ACTION_VIEW_ACCOUNT;
	}


	/**
	 * Reactivates a disabled account and returns the user to the edit page.
	 * 
	 * @throws MessagingException
	 */
	public StreamResult reactivate() throws MessagingException {

		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return new StreamResult(new ByteArrayInputStream((PortalConstants.ACTION_LANDING).getBytes()));
		}

		if (!accountManager.validateReactivateAccount(currentAccount.getId()) && userName == null) {
			return new StreamResult(new ByteArrayInputStream((PortalConstants.ACTION_INVALID).getBytes()));
		}

		AccountHistory accountHistory = new AccountHistory(currentAccount, AccountActionType.STATUS_CHANGE,
				AccountStatus.ACTIVE.getName(), "", new Date(), getUser());
		currentAccount.addAccountHistory(accountHistory);

		Account enabledAccount = accountManager.reactivateAccount(currentAccount, userName);
		if (enabledAccount == null) {
			throw new RuntimeException("Failed to activate account.");
		}

		currentAccount.setUserName(enabledAccount.getUserName());
		currentAccount.setAccountStatus(enabledAccount.getAccountStatus());
		currentAccount.setIsActive(enabledAccount.getIsActive());

		// construct subject line and message text to send through email
		if (!getCurrentUrl().contains("local") && userName != null) {

			String messageText = this.getText(
					PortalConstants.MAIL_RESOURCE_ACCOUNT_REACTIVATION + PortalConstants.MAIL_RESOURCE_BODY,
					Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
							modulesConstants.getModulesOrgPhone(getDiseaseId()), currentAccount.getUser().getFullName(),
							userName));

			sendAccountEmail(currentAccount, PortalConstants.MAIL_RESOURCE_ACCOUNT_REACTIVATION, messageText, true);
		}

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	/**
	 * Reactivates a withdrawn account request and returns the user to the edit page.
	 * 
	 * @throws MessagingException
	 */
	public StreamResult reinstateRequest() throws MessagingException {

		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return new StreamResult(new ByteArrayInputStream((PortalConstants.ACTION_LANDING).getBytes()));
		}

		// this makes sure the username isn't in use while the withdrawn account was renamed
		if (!accountManager.validateReactivateAccount(currentAccount.getId()) && userName == null) {
			return new StreamResult(new ByteArrayInputStream((PortalConstants.ACTION_INVALID).getBytes()));
		}

		AccountHistory accountHistory = new AccountHistory(currentAccount, AccountActionType.REINSTATE_REQUEST,
				PortalConstants.EMPTY_STRING, PortalConstants.EMPTY_STRING, new Date(), getUser());

		currentAccount.addAccountHistory(accountHistory);
		currentAccount.setLastUpdatedDate(new Date());

		Account enabledAccount = accountManager.reinstateAccountRequest(currentAccount, userName);

		if (enabledAccount == null) {
			throw new RuntimeException("Failed to activate account.");
		}

		currentAccount.setUserName(enabledAccount.getUserName());
		currentAccount.setAccountStatus(enabledAccount.getAccountStatus());

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	/**
	 * TODO: Is this still used? Search though account records and return a single page or results.
	 * 
	 * @return
	 */
	public String search() {

		PaginationData pageData = new PaginationData(page, pageSize, ascending, sort);

		// Escape the search key
		key = accountManager.escapeForILike(key);

		setSearchAccountList(accountManager.searchAccounts(key, statusId, pageData));
		numSearchResults = pageData.getNumSearchResults();

		return PortalConstants.ACTION_SEARCH;
	}

	public String reactivationLightbox() {

		return PortalConstants.ACTION_REACTIVATE_LIGHTBOX;
	}

	/**
	 * This loads the user with the given ID into session and returns a lightbox display. Because any user has
	 * permission to use this it should NOT be used to display anything other than accountLightbox!
	 * 
	 * @return sturts result "lightbox"
	 * @throws UserPermissionException
	 */
	public String accountLightbox() throws UserPermissionException {

		String accountId = getRequest().getParameter(PortalConstants.ACCOUNT_ID);

		if (StringUtils.isEmpty(accountId)) {
			throw new RuntimeException("Account ID is required to view a user account.");
		}

		if (getIsGuest()) {
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		}

		currentAccount = accountManager.getAccount(getUser(), Long.valueOf(accountId));
		getSessionAccountEdit().setAccount(currentAccount);

		return PortalConstants.ACTION_LIGHTBOX;
	}

	/**
	 * This method returns a list of account roles that are marked always shown. It will first load all of the account
	 * roles for the account, then add the roles that are marked always shown but not add the roles that is already part
	 * of the account.
	 * 
	 * @return
	 */
	public Set<AccountRole> getAlwaysShownRoles() {
		Set<AccountRole> currentRoles = getCurrentAccount().getAccountRoleList();
		List<RoleType> currentRoleTypes = new ArrayList<RoleType>();
		Set<AccountRole> requestedRoles = new HashSet<AccountRole>();

		for (AccountRole currentRole : currentRoles) {
			currentRoleTypes.add(currentRole.getRoleType());
			requestedRoles.add(currentRole);
		}

		RoleType[] alwaysShownRoleTypes = RoleType.getAlwaysShownRoles();

		for (RoleType alwaysShownRoleType : alwaysShownRoleTypes) {
			if (!currentRoleTypes.contains(alwaysShownRoleType)) {
				AccountRole newRole = new AccountRole();
				newRole.setRoleStatus(RoleStatus.NOT_REQUESTED);
				newRole.setRoleType(alwaysShownRoleType);
				requestedRoles.add(newRole);
			}
		}

		return requestedRoles;
	}

	/**
	 * Method to generate a random string that will be used as a hidden password for users with an SSO account
	 * 
	 * @return semi randomly generate password
	 */
	public StreamResult generateRandomPassword() {

		String password = "";

		password = password + RandomStringUtils.randomAlphanumeric(10);
		// to ensure there is some numbers
		password = password + RandomStringUtils.randomNumeric(3);
		// to ensure there is some special chars
		password = password + RandomStringUtils.random(2, "!@#$%");
		password = password + RandomStringUtils.randomAlphanumeric(10);
		return new StreamResult(new ByteArrayInputStream(password.getBytes()));
	}

	/**
	 * Used primarily for struts logic, the method determines whether or not an Account has any requested BRICs system
	 * privileges. The number of privilege requests is not important thats why the method returns true as soon as a
	 * requested privilege is found
	 * 
	 * @return
	 */
	public boolean getRequestedSystemPrivileges() {

		for (AccountRole role : getCurrentAccount().getAccountRoleList()) {
			// check to see if the account or account roles have requested status
			if (role.getRoleStatus().equals(RoleStatus.PENDING)
					|| currentAccount.getAccountStatus().getName().equalsIgnoreCase(RoleStatus.PENDING.name())) {
				return true;
			}
		}
		return false;
	}

	public boolean getRequestedAccountGroupPrivileges() {

		for (PermissionGroupMember pgm : getCurrentAccount().getPermissionGroupMemberList()) {
			if (pgm.getPermissionGroupStatus().equals(PermissionGroupStatus.PENDING)) {
				return true;
			}
		}
		return false;
	}

	private String pdMessage() {
		if (alternateWorkflow()) {
			return this.getText(PortalConstants.MAIL_RESOURCE_ACCEPTED_ACCOUNT + PortalConstants.MAIL_RESOURCE_PD,
					Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
							modulesConstants.getModulesOrgEmail(getDiseaseId()), currentAccount.getUser().getFullName(),
							getSessionAccountEdit().getReason()));
		} else {
			return "";
		}
	}

	/**
	 * Calls the GUID Introduction webservice (updates or creates user information). This can be called as many times as
	 * needed - it won't hurt anything. If it fails for some reason, it does so only to the console.
	 * 
	 * @param currentAccount Account to introduce to the GUID server
	 */
	private void updateGuidAccount(Account currentAccount) {
		try {
			guidServerAuthUtil.getUserJwt(currentAccount);
			// all we need to do is the introduction so no other call needed
		} catch (JsonSyntaxException | InvalidJwtException e) {
			logger.error("Failed to introduce user " + currentAccount.getUserName() + " to the GUID server.", e);
		}
	}


	/************************ Getters/Setters *****************************/

	public Set<PermissionGroupMember> getPermissionGroupMemberList() {
		return getCurrentAccount().getPermissionGroupMemberList();
	}

	public Date getUploadedDate() {
		return uploadedDate;
	}

	public void setUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
	}

	public Integer getNumSearchResults() {
		return numSearchResults;
	}

	public void getNumSearchResults(Integer numSearchResults) {
		this.numSearchResults = numSearchResults;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Long getStatusId() {
		return statusId;
	}

	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}

	public Long getAccountId() {
		return getCurrentAccount().getId();
	}

	public Long getUserId() {
		return getCurrentAccount().getUserId();
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Set<AccountUserDetails> getActiveUserList() {
		return activeUserList;
	}

	public void setActiveUserList(Set<AccountUserDetails> activeUserList) {
		this.activeUserList = activeUserList;
	}

	public Set<AccountRole> getAccountRoleList() {
		return getCurrentAccount().getAccountRoleList();
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Boolean getAscending() {
		return ascending;
	}

	public void setAscending(Boolean ascending) {
		this.ascending = ascending;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getAvailability() {
		return availability;
	}

	public List<Account> getAccountList() {
		return accountList;
	}

	public void setAccountList(List<Account> accountList) {
		this.accountList = accountList;
	}

	public List<BasicAccount> getSearchAccountList() {
		return searchAccountList;
	}

	public void setSearchAccountList(List<BasicAccount> searchAccountList) {
		this.searchAccountList = searchAccountList;
	}

	public void setAvailability(String availability) {
		this.availability = availability;
	}

	public Map<UserFile, byte[]> getUploadedFilesMap() {
		return uploadedFilesMap;
	}

	public void setUploadedFilesMap(Map<UserFile, byte[]> uploadedFilesMap) {
		this.uploadedFilesMap = uploadedFilesMap;
	}

	public File getUpload() {
		return upload;
	}

	public void setUpload(File upload) {
		this.upload = upload;
	}

	public String getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}

	public String getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	public void setRemoveDuplicate(boolean removeDuplicate) {
		this.removeDuplicate = removeDuplicate;
	}

	public boolean getRemoveDuplicate() {
		return removeDuplicate;
	}

	public String getUploadDescription() {
		return uploadDescription;
	}

	// sets the upload description
	public void setUploadDescription(String uploadDescription) {

		if (!StringUtils.isEmpty(uploadDescription)) {
			for (FileType adminFileType : staticManager.getAdminFileTypeList()) {
				if (Long.valueOf(uploadDescription).equals(adminFileType.getId())) {
					this.uploadDescription = adminFileType.getName();
				}
			}
		}
	}

	public Account getCurrentAccount() {
		return getSessionAccountEdit().getAccount();
	}

	public void setCurrentAccount(Account currentAccount) {
		this.currentAccount = currentAccount;
	}

	public AccountDetailsForm getAccountDetailsForm() {
		return accountDetailsForm;
	}

	public void setAccountDetailsForm(AccountDetailsForm accountDetailsForm) {
		this.accountDetailsForm = accountDetailsForm;
	}

	public AccountPrivilegesForm getAccountPrivilegesForm() {
		return accountPrivilegesForm;
	}

	public void setAccountPrivilegesForm(AccountPrivilegesForm accountPrivilegesForm) {
		this.accountPrivilegesForm = accountPrivilegesForm;
	}

	public AccountSignatureForm getAccountSignatureForm() {
		return accountSignatureForm;
	}

	public void setAccountSignatureForm(AccountSignatureForm accountSignatureForm) {
		this.accountSignatureForm = accountSignatureForm;
	}

	public boolean getEnforceStaticFields() {
		return false;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public long getUserFileId() {
		return userFileId;
	}

	public void setUserFileId(long userFileId) {
		this.userFileId = userFileId;
	}

	public String getChangedFileName() {
		return changedFileName;
	}

	public void setChangedFileName(String changedFileName) {
		this.changedFileName = changedFileName;
	}



	public String getPartialApprovalComments() {
		return partialApprovalComments;
	}

	public void setPartialApprovalComments(String partialApprovalComments) {
		this.partialApprovalComments = partialApprovalComments;
	}

	public String getFinalApprovalComments() {
		return finalApprovalComments;
	}

	public void setFinalApprovalComments(String finalApprovalComments) {
		this.finalApprovalComments = finalApprovalComments;
	}

	public String getFinalApprovalExpireDate() {
		return finalApprovalExpireDate;
	}

	public void setFinalApprovalExpireDate(String finalApprovalExpireDate) {
		this.finalApprovalExpireDate = finalApprovalExpireDate;
	}

	public boolean areThereAnyRequests() {
		currentAccount = getCurrentAccount();

		for (AccountRole accountRole : currentAccount.getAccountRoleList()) {
			if (RoleStatus.PENDING == accountRole.getRoleStatus()) {
				return true;
			}
		}

		for (PermissionGroupMember groupMember : currentAccount.getPermissionGroupMemberList()) {
			if (PermissionGroupStatus.PENDING == groupMember.getPermissionGroupStatus()) {
				return true;
			}
		}

		return false;
	}

	public void cancelPrivilegeRequest() {
		
		currentAccount = getCurrentAccount();
		Long paramPrivilegeId = Long.valueOf(getRequest().getParameter("privilegeId"));
		Set<AccountRole> accountRoleSet = currentAccount.getAccountRoleList();

		// remove cancelled privilege, should this be set to a new status
		Iterator<AccountRole> iter = accountRoleSet.iterator();
		while (iter.hasNext()) {
			AccountRole role = (AccountRole) iter.next();
			Long roleId = role.getId();
			if (roleId.equals(paramPrivilegeId)) {
				Date expirationDate = role.getExpirationDate();
				
				// If it is to cancel a privilege renewal request, set its
				// status back to the status calculated from expiration date
				if (currentAccount.getAccountStatus() == AccountStatus.RENEWAL_REQUESTED && expirationDate != null) {
					role.setRoleStatus(AccountServiceUtil.getRoleStatusFromExpDate(expirationDate));
				} else {
					iter.remove();
				}

				recordPrivilegeWithdrawal(role);
				break;
			}
		}

		if (!areThereAnyRequests() && (AccountStatus.CHANGE_REQUESTED == currentAccount.getAccountStatus()
				|| AccountStatus.RENEWAL_REQUESTED == currentAccount.getAccountStatus())) {
			currentAccount.setAccountStatus(AccountStatus.ACTIVE);
		}

		currentAccount = accountManager.saveAccount(currentAccount);
		getSessionAccountEdit().setAccount(currentAccount);
	}


	public void requestPriv() {
		// get current account
		currentAccount = getCurrentAccount();
		// get requested privilege
		Long paramPrivilegeId = Long.valueOf(getRequest().getParameter("privilegeId"));

		RoleType roleType = RoleType.getById(paramPrivilegeId);

		AccountRole ar = new AccountRole(currentAccount, roleType, RoleStatus.PENDING, null);

		currentAccount.getAccountRoleList().add(ar);


		recordPrivilegeRequest(ar);

		currentAccount = accountManager.saveAccount(currentAccount);
		getSessionAccountEdit().setAccount(currentAccount);


	}



	public void recordPrivilegeRequest(AccountRole role) {

		String roleTitle = role.getRoleType().getTitle();
		String actionTypeArguments = roleTitle;

		AccountHistory privilegeRequestHistory = new AccountHistory(currentAccount, AccountActionType.PRIVILEGE_REQUEST,
				actionTypeArguments, "", new Date(), getUser());
		currentAccount.addAccountHistory(privilegeRequestHistory);

	}



	public void recordPrivilegeWithdrawal(AccountRole role) {

		String roleTitle = role.getRoleType().getTitle();
		String actionTypeArguments = roleTitle;

		AccountHistory privilegeWithdrawalHistory = new AccountHistory(currentAccount,
				AccountActionType.PRIVILEGE_WITHDRAWAL, actionTypeArguments, "", new Date(), getUser());
		currentAccount.addAccountHistory(privilegeWithdrawalHistory);

	}

	public void recordPermissionGroupWithdrawal(String paramGroupName) {

		String actionTypeArguments = paramGroupName;

		AccountHistory groupWithdrawalHistory = new AccountHistory(currentAccount,
				AccountActionType.PERMISSION_GROUP_WITHDRAWAL, actionTypeArguments, "", new Date(), getUser());
		currentAccount.addAccountHistory(groupWithdrawalHistory);

	}


	public void recordChangeFileType(String paramFileName, String paramOldFileType, String changedFileType) {

		String actionTypeArguments =
				paramFileName.concat("|").concat(paramOldFileType).concat("|").concat(changedFileType);

		AccountHistory changeFileTypeHistory = new AccountHistory(currentAccount, AccountActionType.FILETYPE_RELABEL,
				actionTypeArguments, "", new Date(), getUser());
		currentAccount.addAccountHistory(changeFileTypeHistory);

	}

	public void recordChangeFileName(String paramOldFileName, String changedFileName) {

		String actionTypeArguments = paramOldFileName.concat("|").concat(changedFileName);

		AccountHistory changeFileNameHistory = new AccountHistory(currentAccount, AccountActionType.FILE_RENAME,
				actionTypeArguments, "", new Date(), getUser());
		currentAccount.addAccountHistory(changeFileNameHistory);

	}

	public void recordPermissionGroupChange() {

		Account originalAccount = getSessionAccountEdit().getOriginalAccount();

		if (!accountRejected(getCurrentAccount())) {

			for (PermissionGroupMember permissionGroup : currentAccount.getPermissionGroupMemberList()) {
				Set<PermissionGroupMember> existingPermissionGroup = originalAccount.getPermissionGroupMemberList();

				if (!existingPermissionGroup.contains(permissionGroup)) {

					AccountHistory newAccountHistory =
							new AccountHistory(currentAccount, AccountActionType.PERMISSION_GROUP_REQUEST,
									permissionGroup.getPermissionGroup().getDisplayName(), "", new Date(), getUser());
					currentAccount.addAccountHistory(newAccountHistory);
				}
			}
		}
	}

	public boolean accountRejected(Account account) {

		if (account.getAccountStatus() == AccountStatus.DENIED) {
			return true;
		}

		return false;
	}

	public String getAccountAdministrativeNoteText() {
		return accountAdministrativeNoteText;
	}

	public void setAccountAdministrativeNoteText(String accountAdministrativeNoteText) {

		this.accountAdministrativeNoteText = accountAdministrativeNoteText;
	}


	public String addAccountAdmininistrativeNote() {
		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return PortalConstants.ACTION_LANDING;	// Session account is null, redirect back to login page
		}

		currentAccount.addAccountAdministrativeNote(
				new AccountAdministrativeNote(accountAdministrativeNoteText, currentAccount, getAccount(), new Date()));
		
		if(currentAccount.getId() != null) {
			currentAccount = accountManager.saveAccount(currentAccount);
		}
		
		getSessionAccountEdit().setAccount(currentAccount);
		
		return PortalConstants.ACTION_SUCCESS;
	}
	
	
	private void createDucESignature() {
		UserFile ducUserfile = repositoryManager.getFileById(new Long(PortalConstants.DUC_FILE));
		
		ElectronicSignature es = new ElectronicSignature();
		es.setFirstName(currentAccount.getUser().getFirstName());
		es.setMiddleName(currentAccount.getUser().getMiddleName());
		es.setLastName(currentAccount.getUser().getLastName());
		es.setSignatureDate(currentAccount.getApplicationDate());
		es.setSignatureType(SignatureType.PDBP_DMR_DUC);
		es.setSignatureFile(ducUserfile);
		es.setAccount(currentAccount);
		currentAccount.addElectronicSignature(es);
	}

	
	private void generateESignatureDoc() {
		ElectronicSignature es = currentAccount.getBricsESignature();
		ESignDocGenerator generator = new ESignDocGenerator(es);
		UserFile esigUserFile = null;
		
		try {
			byte[] fileBytes = generator.generateESignature(this.getOrgName().toLowerCase());

			if (fileBytes != null) {
				esigUserFile = new UserFile();
				String dateStr = BRICSTimeDateUtil.formatDate(es.getSignatureDate());
				String fileName = currentAccount.getUserName() + "_esig_" + dateStr + ".pdf";
				
				esigUserFile = repositoryManager.uploadFile(currentAccount.getUser().getId(), fileBytes, fileName, "",
						ServiceConstants.FILE_TYPE_ELECTRONIC_SIGNATURE, es.getSignatureDate());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (JSchException je) {
			je.printStackTrace();
		}
		
		if (esigUserFile != null) {
			es.setSignatureFile(esigUserFile);
			accountManager.saveElectronicSignature(es);
		}
	} 
	
	private AccountType getRequestedAccountReportingType() {
		
		if (accountPrivilegesForm == null || StringUtils.isEmpty(accountPrivilegesForm.getAccountType())) {
			return currentAccount.getAccountReportingType();
		}
		
		return AccountType.getByName(accountPrivilegesForm.getAccountType());
		
	}
	
	private AccountType getLatestRequestedAccountReportingType() {
		AccountType accountType = accountReportingManager.getLatestAccountTypeRequest(currentAccount);
		if(accountType == null) {
			return currentAccount.getAccountReportingType();
		}
		return accountType;
	}
	
}
