package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;

public class AccountRoleListDecorator extends Decorator {

	public String getDecoratedPrivilege() {

		AccountRole accountRole = (AccountRole) this.getObject();
	

		return accountRole.getRoleType().getTitle();

	}

	public String getDecoratedStatus() {

		AccountRole accountRole = (AccountRole) this.getObject();
		if (accountRole.isExpired()) {
			return "Expired";
		}
		return accountRole.getRoleStatus().getName();

	}

	public String getDecoratedExpirationDate() {

		AccountRole accountRole = (AccountRole) this.getObject();
		if (accountRole.getExpirationString().equals("-")) {
			return "No Expiration Date";
		}
		return accountRole.getExpirationString();

	}

	public String getDecoratedPermissionGroupName() {

		PermissionGroupMember permissionGroupMember = (PermissionGroupMember) this
				.getObject();
		return permissionGroupMember.getPermissionGroup().getGroupName();

	}

	public String getDecoratedPermissionGroupStatus() {

		PermissionGroupMember permissionGroupMember = (PermissionGroupMember) this
				.getObject();
		return permissionGroupMember.getPermissionGroupStatus().getName();

	}

	public String getDecoratedPermissionGroupNoExpirationDate() {
		return "No Expiration Date";
	}

}
