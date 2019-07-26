package gov.nih.tbi.taglib.datatableDecorators;

import java.text.SimpleDateFormat;
import java.util.Date;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.model.PermissionGroupStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class AccountRoleListIdtDecorator extends IdtDecorator {

	private boolean onlyReviewer;
	private boolean isAdmin;


	public AccountRoleListIdtDecorator() {
		super();
	}

	public AccountRoleListIdtDecorator(boolean onlyReviewer, boolean isAdmin) {
		super();
		this.onlyReviewer = onlyReviewer;
		this.isAdmin = isAdmin;
	
	}

	public String getDecoratedPrivilege() {

		AccountRole accountRole = (AccountRole) this.getObject();


		return accountRole.getRoleType().getTitle();

	}


	// this method should only be called for user that is not in requested or pending...therefore they will just see
	// their existing privileges and can not take any action on them
	public String getDecoratedStatus() {

		AccountRole accountRole = (AccountRole) this.getObject();
		if (accountRole.isExpired()) {
			return "Expired";
		}

		return accountRole.getRoleStatus().getName();
	}

	public String getDecoratedStatusRequest() {
		AccountRole accountRole = (AccountRole) this.getObject();
		RoleStatus status = accountRole.getRoleStatus();
		RoleType type = accountRole.getRoleType();
		String output = "";

		if (accountRole.isExpired()) {
			return "Expired";
		}


		if (onlyReviewer || RoleType.ROLE_USER == accountRole.getRoleType()) {
			output = status.getName();
			// just need hidden inputs
			output += "<input type=\"hidden\" name=\"accountPrivilegesForm.accountRoleList\" value=\""
					+ accountRole.getRoleType().getId() + ", " + status.getId() + "\" />";
			output += "<input type=\"hidden\" name=\"accountPrivilegesForm.accountRoleExpiration\" value=\""
					+ accountRole.getRoleType().getId() + ", " + accountRole.getExpirationString() + "\" />";
		} else {
			if (status == RoleStatus.PENDING) {
				output = status.getName();
				output += "  <a href=\"#\" class=\"cancel-request\" data-id=\"" + accountRole.getId()
						+ "\">Cancel Request</a>";
			} else if (status == RoleStatus.NOT_REQUESTED) {
				output = status.getName();
				output +=
						"  <a href=\"#\" class=\"request-priv\" data-id=\"" + type.getId() + "\">Request Privilege</a>";

			} else {
				output = status.getName();
				// just need hidden inputs
				output += "<input type=\"hidden\" name=\"accountPrivilegesForm.accountRoleList\" value=\""
						+ accountRole.getRoleType().getId() + ", " + status.getId() + "\" />";
				//output += "<input type=\"hidden\" name=\"accountPrivilegesForm.accountRoleExpiration\" value=\""
					//	+ accountRole.getRoleType().getId() + ", " + accountRole.getExpirationString() + "\" />";
			}


		}



		return output;
	}

	public String getDecoratedExpirationDate() {

		AccountRole accountRole = (AccountRole) this.getObject();
		if (accountRole.getExpirationString().equals("-")) {
			return "No Expiration Date";
		}
		
		
		return accountRole.getExpirationString();

	}

	public String getDecoratedExpirationDateRequest() {
		AccountRole accountRole = (AccountRole) this.getObject();
		RoleStatus status = accountRole.getRoleStatus();
		
		
		String expirationString = accountRole.getExpirationString();
		if (expirationString.equals("-")) {
			if (accountRole.getRoleType().equals(RoleType.ROLE_USER)) {
				return "No Expiration Date";
			} else if (status == RoleStatus.PENDING) {
				return "Pending";
			} else if (status == RoleStatus.NOT_REQUESTED) {
				return "Not Requested";
			} else {
				return "No Expiration Date";
			}
		}
		return expirationString;
	}
	
	public String getDecoratedExpirationDateRequestWithDatePicker() {
		AccountRole accountRole = (AccountRole) this.getObject();
		RoleStatus status = accountRole.getRoleStatus();
		String output;
		if(this.isAdmin) { 
			output = "<input type=\"hidden\" id=\"role_"+accountRole.getRoleType().getId()+"\" class=\"roleHidden\"  name=\"accountPrivilegesForm.accountRoleList\" value=\""+accountRole.getRoleType().getId()+", "+accountRole.getRoleStatus().getId()+"\" />";
			output += "<input type=\"text\" id=\"roleExpirationTable_"+accountRole.getRoleType().getId()+"\" maxlength=\"10\" class=\"date-picker no-chkbox small textfield\" value=\""+accountRole.getExpirationString()+"\" />";
		} else {
			output = accountRole.getExpirationString();
		}
		
		String expirationString = accountRole.getExpirationString();
		if (expirationString.equals("-")) {
			if (accountRole.getRoleType().equals(RoleType.ROLE_USER)) {
				return "No Expiration Date";
			} else if (status == RoleStatus.PENDING) {
				return "Pending";
			} else if (status == RoleStatus.NOT_REQUESTED) {
				return "Not Requested";
			} else {
				return "No Expiration Date";
			}
		}
		return output;
	}

	public String getDecoratedPermissionGroupName() {

		PermissionGroupMember permissionGroupMember = (PermissionGroupMember) this.getObject();
		return permissionGroupMember.getPermissionGroup().getGroupName();

	}

	public String getDecoratedPermissionGroupStatus() {

		PermissionGroupMember permissionGroupMember = (PermissionGroupMember) this.getObject();

		String groupName = permissionGroupMember.getPermissionGroup().getGroupName();



		if (onlyReviewer) {
			return permissionGroupMember.getPermissionGroupStatus().getName();
		} else {
			if (permissionGroupMember.getPermissionGroupStatus() == PermissionGroupStatus.PENDING) {
				return permissionGroupMember.getPermissionGroupStatus().getName()
						+ "&nbsp;&nbsp;&nbsp;<a href=\"cancelRequestedGroupAction!cancelRequestedGroup.action?groupName="
						+ groupName + "\" onclick=\"return confirm('Are you sure you want to cancel your request?')\">Cancel Request</a>";
			} else {
				return permissionGroupMember.getPermissionGroupStatus().getName();
			}

		}



	}

	public String getDecoratedPermissionGroupNoExpirationDate() {
		return "No Expiration Date";
	}


	public String getDecoratedPermissionGroupPending() {
		return "Pending";
	}

	public String getDecoratedPermissionGroupExprationDate() {

		PermissionGroupMember permissionGroupMember = (PermissionGroupMember) this.getObject();

		Date expirationDate = permissionGroupMember.getExpirationDate();

		if (expirationDate == null) {
			if (permissionGroupMember.getPermissionGroupStatus() == PermissionGroupStatus.ACTIVE) {
				return "No Expiration Date";
			} else {
				return "Pending";
			}


		} else {
			SimpleDateFormat df = new SimpleDateFormat(ModelConstants.UNIVERSAL_DATE_FORMAT);
			return df.format(expirationDate);

		}

	}
}
