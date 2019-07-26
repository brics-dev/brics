
package gov.nih.tbi.account.model;

import java.io.Serializable;
import java.util.Map;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class SessionAccountEdit implements Serializable {

	private static final long serialVersionUID = -3968948296210562780L;

	/**********************************************************/

	private Account account;
	private Account originalAccount; // this is meant to store the account before it has been edited. mainly to be able
									 // to record audits in account history
	private PermissionGroup permissionGroup;
	private Map<UserFile, byte[]> uploadedFilesMap;

	/**
	 * 'reason' variable exists only in session. It it not a property in Account, AccountAction, or
	 * AccountPriviletesForm. The jsp page pulls the content directly from the session.
	 */
	private String reason;

	/*********************************************************/

	public SessionAccountEdit() {

		super();
		reason = "";
	}

	public Account getAccount() {

		return account;
	}

	public PermissionGroup getPermissionGroup() {

		return permissionGroup;
	}

	public void setPermissionGroup(PermissionGroup permissionGroup) {

		this.permissionGroup = permissionGroup;
	}

	public void setAccount(Account account) {

		this.account = account;
	}

	public Map<UserFile, byte[]> getUploadedFilesMap() {

		return uploadedFilesMap;
	}

	public void setUploadedFilesMap(Map<UserFile, byte[]> uploadedFilesMap) {

		this.uploadedFilesMap = uploadedFilesMap;
	}

	public User getUser() {

		if (account != null) {
			return account.getUser();
		} else {
			return null;
		}
	}

	public String getReason() {

		return reason;
	}

	public void setReason(String reason) {

		this.reason = reason;
	}

	/**
	 * Clears out account information, old account information, uploaded file streams, and the reason.
	 */
	public void clearAll() {

		account = null;
		uploadedFilesMap = null;
		reason = "";
		originalAccount = null;
	}

	public Account getOriginalAccount() {
		return originalAccount;
	}

	public void setOriginalAccount(Account originalAccount) {
		this.originalAccount = originalAccount;
	}
}
