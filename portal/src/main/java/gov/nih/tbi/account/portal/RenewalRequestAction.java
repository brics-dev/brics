package gov.nih.tbi.account.portal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.AccountActionType;
import gov.nih.tbi.account.model.AccountType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.BricsInstanceType;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.taglib.datatableDecorators.AccountRoleListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.ExistingFileListIdtDecorator;

public class RenewalRequestAction extends BaseAccountAction {

	private static final long serialVersionUID = 6611004938531229549L;
	private static final Logger logger = Logger.getLogger(RenewalRequestAction.class);

	public static final String DAR_FILE_TYPE = "Data Access Request";
	public static final String DSR_FILE_TYPE = "Data Submission Request";
	public static final String BIO_FILE_TYPE = "CV/Biographical Sketch";

	public static final int FILE_UPDATED_DAY_LIMIT = 30;

	private Account currentAccount;

	private File upload;
	private String uploadContentType;
	private String uploadFileName;
	private String uploadDescription;

	public String view() {
		getSessionAccountEdit().clearAll();

		currentAccount = accountManager.getAccount(getUser(), getAccount().getId());
		getSessionAccountEdit().setAccount(currentAccount);

		return SUCCESS;
	}

	public StreamResult submitRenewal() {

		currentAccount = getCurrentAccount();
		
		if (getNeedTemplates()) {
			List<UserFile> userFiles = repositoryManager.getAdminFiles(getCurrentAccount().getUser().getId());
			AccountType accountType = currentAccount.getAccountReportingType();
			
			Set<String> missingFiles = RenewalRequestAction.checkMissingFiles(accountType, userFiles);
			if (missingFiles != null && !missingFiles.isEmpty()) {
				return new StreamResult(new ByteArrayInputStream((String.join(", ", missingFiles)).getBytes()));
			}
		}
		
		String renewPrivileges = "";
		
		for (AccountRole accountRole : currentAccount.getAccountRoleList()) {
			if (accountRole.getRoleStatus() == RoleStatus.EXPIRED
					|| accountRole.getRoleStatus() == RoleStatus.EXPIRING_SOON) {
				accountRole.setRoleStatus(RoleStatus.PENDING);
				
				if (!StringUtils.isEmpty(renewPrivileges)) {
					renewPrivileges += ", ";
				}
				renewPrivileges += accountRole.getRoleType().getTitle();
			}
		}

		if (!StringUtils.isEmpty(renewPrivileges)) {
			currentAccount.setAccountStatus(AccountStatus.RENEWAL_REQUESTED);
			currentAccount.setRequestSubmitDate(new Date());

			AccountHistory renewalRequestHistory = new AccountHistory(currentAccount, AccountActionType.RENEWAL_REQUEST,
					renewPrivileges, "", new Date(), getUser());
			currentAccount.addAccountHistory(renewalRequestHistory);

			currentAccount = accountManager.saveAccount(currentAccount);
			getSessionAccountEdit().setAccount(currentAccount);
		}
		
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	/**
	 * This method will check if there exist the required user files for the given account type. User files 
	 * that were uploaded earlier than 30 days ago are treated as not the latest and do not count. 
	 * 
	 * Account Type		Required File Types
	 * ------------    ----------------------------------------------------------------------
	 * DSR				Data Submission Request
	 * DAR				Data Access Request, CV/Biographical Sketch
	 * DSR + DAR		Data Submission Request, Data Access Request, CV/Biographical Sketch
	 * OTHER			None
	 *  
	 * @param accountType - account type 
	 * @param userFiles - list of userFiles 
	 * @return set of the file type names that are missing in the given userFiles list.
	 */
	public static Set<String> checkMissingFiles(AccountType accountType, List<UserFile> userFiles) {

		Set<String> missingFiles = new HashSet<String>();
		boolean dsrExists = false, darExists = false, bioExists = false;

		long nowInMs = (new Date()).getTime();

		for (UserFile userFile : userFiles) {
			Date uploadDate = userFile.getUploadedDate();
			long dayDiff = TimeUnit.DAYS.convert(nowInMs - uploadDate.getTime(), TimeUnit.MILLISECONDS);

			// If the file was uploaded more than 30 days ago, it's not the latest
			if (dayDiff > FILE_UPDATED_DAY_LIMIT) {
				continue;
			}

			if (DSR_FILE_TYPE.equals(userFile.getDescription())) {
				dsrExists = true;
			}
			if (DAR_FILE_TYPE.equals(userFile.getDescription())) {
				darExists = true;
			}
			if (BIO_FILE_TYPE.equals(userFile.getDescription())) {
				bioExists = true;
			}
		}

		if (accountType == AccountType.DSR) {
			if (!dsrExists) {
				missingFiles.add(DSR_FILE_TYPE);
			}
		} else if (accountType == AccountType.DAR) {
			if (!darExists) {
				missingFiles.add(DAR_FILE_TYPE);
			}
			if (!bioExists) {
				missingFiles.add(BIO_FILE_TYPE);
			}
		} else if (accountType == AccountType.DSRDAR) {
			if (!dsrExists) {
				missingFiles.add(DSR_FILE_TYPE);
			}
			if (!darExists) {
				missingFiles.add(DAR_FILE_TYPE);
			}
			if (!bioExists) {
				missingFiles.add(BIO_FILE_TYPE);
			}
		}

		return missingFiles;
	}

	
	public String addFileLightbox() {
		return PortalConstants.ADD_FILE_LIGHTBOX;
	}

	public StreamResult uploadFile() {
		currentAccount = getCurrentAccount();
		if (currentAccount == null) {
			return new StreamResult(new ByteArrayInputStream((PortalConstants.ACTION_LANDING).getBytes()));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Uploading file: [ " + uploadFileName + ", " + uploadDescription + " ]");
		}

		try {
			repositoryManager.uploadFile(currentAccount.getUser().getId(), upload, uploadFileName, uploadDescription,
					ServiceConstants.FILE_TYPE_ACCOUNT, new Date());
		} catch (IOException | JSchException e) {
			e.printStackTrace();
		}

		AccountHistory accountHistory = new AccountHistory(getCurrentAccount(), AccountActionType.ADD_DOCUMENTATION,
				uploadFileName, "", new Date(), getUser());
		currentAccount.addAccountHistory(accountHistory);
		currentAccount.setLastUpdatedDate(new Date());

		currentAccount = accountManager.saveAccount(currentAccount);
		getSessionAccountEdit().setAccount(currentAccount);

		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public String getExistingPrivileges() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<AccountRole> outputList = new ArrayList<AccountRole>(getCurrentAccount().getAccountRoleList());
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

	public String getExistingFiles() {

		List<UserFile> userAdminFiles = repositoryManager.getAdminFiles(getCurrentAccount().getUser().getId());

		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<UserFile> outputList = new ArrayList<UserFile>(userAdminFiles);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ExistingFileListIdtDecorator(false, false));
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	public List<UserFile> getUserAdminFiles() {
		return repositoryManager.getAdminFiles(getCurrentAccount().getUser().getId());
	}

	public boolean getNeedTemplates() {
		BricsInstanceType instanceType = this.getInstanceType();
		if (instanceType == BricsInstanceType.FITBIR || instanceType == BricsInstanceType.NEI_BRICS
				|| instanceType == BricsInstanceType.NINR) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the current account requires renewal, false otherwise.
	 * 
	 * @return if the current account is active and has roles that is expired or
	 *         expiring soon.
	 */
	public boolean getNeedRenewal() {

		currentAccount = getCurrentAccount();
		AccountStatus accountStatus = currentAccount.getAccountStatus();

		if (accountStatus == AccountStatus.ACTIVE || accountStatus == AccountStatus.CHANGE_REQUESTED) {
			for (AccountRole accountRole : currentAccount.getAccountRoleList()) {
				if (accountRole.getRoleStatus() == RoleStatus.EXPIRED
						|| accountRole.getRoleStatus() == RoleStatus.EXPIRING_SOON) {
					return true;
				}
			}
		}

		return false;
	}

	public String getAccountType() {
		return getCurrentAccount().getAccountReportingType().getDescription();
	}

	public List<FileType> getAdminFileTypes() {
		List<FileType> allAdminFileTypes = staticManager.getAdminFileTypeList();

		if (this.getNeedTemplates()) {
			List<FileType> requiredFileTypes = new ArrayList<FileType>();

			for (FileType fileType : allAdminFileTypes) {
				String typeName = fileType.getName();
				if (typeName.equals(DAR_FILE_TYPE) || typeName.equals(DSR_FILE_TYPE)
						|| typeName.equals(BIO_FILE_TYPE)) {
					requiredFileTypes.add(fileType);
				}
			}
			return requiredFileTypes;
		}

		return allAdminFileTypes;
	}

	public Long getUserId() {
		return getCurrentAccount().getUserId();
	}

	public boolean getIsApproved() {
		return true;
	}

	public Account getCurrentAccount() {
		return getSessionAccountEdit().getAccount();
	}

	public void setCurrentAccount(Account currentAccount) {
		this.currentAccount = currentAccount;
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

	public String getUploadDescription() {
		return uploadDescription;
	}

	public void setUploadDescription(String uploadDescription) {
		if (!StringUtils.isEmpty(uploadDescription)) {
			for (FileType adminFileType : staticManager.getAdminFileTypeList()) {
				if (Long.valueOf(uploadDescription).equals(adminFileType.getId())) {
					this.uploadDescription = adminFileType.getName();
				}
			}
		}
	}

}
