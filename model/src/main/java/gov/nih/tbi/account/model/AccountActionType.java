package gov.nih.tbi.account.model;

public enum AccountActionType {
	CREATE("Account created by "),
	REQUEST("Account requested by "),
	PROFILE_EDIT("Profile information edited by "),
	PRIVILEGE_REQUEST("Privilege to %s requested by "),
	PRIVILEGE_WITHDRAWAL("Request to %s withdrawn by "),
	PERMISSION_GROUP_REQUEST("Access to %s requested by "),
	PERMISSION_GROUP_WITHDRAWAL("Request to %s withdrawn by "),
	STATUS_CHANGE("Account status changed to %s by "),
	PARTIAL_APPROVE("Partial approval by "),
	FILE_DELETE("File %s deleted by "),
	FILE_RENAME("File Name changed from %s to %s by "),
	FILETYPE_RELABEL("File type of %s changed from %s to %s by "),
	REJECT("Account request denied by "),
	PRIVILEGE_RENEWAL_EMAIL("Renewal: User contacted regarding account by "),
	ADD_DOCUMENTATION("\"%s\"  uploaded by "),
	FINAL_APPROVAL("Account request approved by "),
	TEMP_REJECT("Temporary rejection by "),
	PRIVILEGE_RENEWAL_COMMENT("All expiring privileges renewed till %s by "),
	PERMISSION_GROUP_APPROVE("Requested access to \"%s\" approved by "),
	DEACTIVATE("Account was deactivated by "),
	REINSTATE_REQUEST("Account request reactivated by "),
	EXPIRATION_CHANGE("Module expiration date was changed by "),
	ADMIN_REMOVE_PERMISSION("Access to %s removed by "),
	ADMIN_ADD_PERMISSION("Access to %s given by "),
	PASSWORD_RESET("Password reset by "),
	PASSWORD_RESET_REQUEST_BY_USER("Password reset requested by "),
	PASSWORD_RESET_REQUESTED_BY_ADMIN("Password reset requested by ");

	private String stringFormat;

	AccountActionType(String stringFormat) {
		this.stringFormat = stringFormat;
	}

	public String getStringFormat() {
		return stringFormat;
	}

	public void setStringFormat(String stringFormat) {
		this.stringFormat = stringFormat;
	}
}
