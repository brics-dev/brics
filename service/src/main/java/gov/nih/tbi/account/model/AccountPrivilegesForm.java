package gov.nih.tbi.account.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.nih.tbi.account.dao.PermissionGroupDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.account.service.util.AccountServiceUtil;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.PermissionGroupStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.service.ServiceConstants;

/**
 * This class sets the fields from the Account Privileges Page
 * 
 * @author Francis Chen
 */
public class AccountPrivilegesForm {
	private static Logger logger = Logger.getLogger(AccountPrivilegesForm.class);


	@Autowired
	private PermissionGroupDao permissionGroupDao;

	private Set<AccountRole> accountRoleList;
	private Set<PermissionGroupMember> permissionGroupMemberList;
	private String accountType;
	
	public AccountPrivilegesForm() {
		accountRoleList = new HashSet<AccountRole>();
		permissionGroupMemberList = new HashSet<PermissionGroupMember>();
	}

	public AccountPrivilegesForm(Account account) {
		if (account.getAccountRoleList() != null) {
			accountRoleList = new HashSet<AccountRole>(account.getAccountRoleList());
		} else {
			accountRoleList = new HashSet<AccountRole>();
		}
		
		if (account.getPermissionGroupMemberList() != null) {
			permissionGroupMemberList = new HashSet<PermissionGroupMember>(account.getPermissionGroupMemberList());
		} else {
			permissionGroupMemberList = new HashSet<PermissionGroupMember>();
		}
		
		if(account.getAccountReportingType() != null) {
			this.accountType = account.getAccountReportingType().getDescription();
			
		}
	}

	public Set<AccountRole> getAccountRoleList() {
		return accountRoleList;
	}

	/**
	 * Default method for check boxes and radio buttons. The entries in the given array are expected to be a comma
	 * separated list of row type and role status. In other words the value attribute of the check box and radio buttons
	 * should be in the form of: value="row_type, row_status". Each value pair will then be used as inputs to the
	 * {@link #setAccountRoleList(long, String)} method.
	 * 
	 * @param inputValues - A listing of value pairs from check boxes or radio buttons.
	 * @throws RuntimeException When there is an error when splitting the value pair string, or when there is an issue
	 *         with converting the individual values to longs.
	 */
	public void setAccountRoleList(String[] inputValues) throws RuntimeException {
		for (String value : inputValues) {
			String[] roleVals = value.split(",");
			long roleType = Long.parseLong(roleVals[0].trim());
			String roleStatus = roleVals[1].trim();

			setAccountRoleList(roleType, roleStatus);
		}
	}

	/**
	 * Searches for the AccountRole with an a role id == index and changes the status of that role to value. If the
	 * AccountRole does not exist, then one is created.
	 * 
	 * @param index - The id of the RoleType
	 * @param value - The Id of the RoleStatus
	 */
	public void setAccountRoleList(long index, String value) {
		logger.info("Calling setAccountRoleList");

		// Search for the account with the correct RoleType
		AccountRole currentRole = null;
		RoleType newRoleType = RoleType.getById(Long.valueOf(index));
		RoleStatus newRoleStatus = RoleStatus.getById(Long.valueOf(value));

		for (AccountRole role : accountRoleList) {
			if (role.getRoleType() == newRoleType) {
				currentRole = role;

				if (role.getExpirationDate() == null) {
					role.setRoleStatus(newRoleStatus);
				}

				break;
			}
		}

		// If currentRole is null, then we need to create the appropriate AccountRole entry
		if (currentRole == null) {
			currentRole = new AccountRole();
			currentRole.setRoleType(newRoleType);
			currentRole.setRoleStatus(newRoleStatus);
			accountRoleList.add(currentRole);
		}
	}

	/**
	 * Converts a JSON array of role type and role status values into AccountRole objects. The
	 * {@link #setAccountRoleList(long, String)} method will be used the create the objects.
	 * 
	 * @param json - A JSON array of role type and role status pairs.
	 * @throws RuntimeException When there is an error during parsing or accessing the JSON objects.
	 */
	public void setAccountRoleJson(String json) throws RuntimeException {
		JsonParser parser = new JsonParser();
		JsonArray roleArray = parser.parse(json).getAsJsonArray();

		// Loop through the JSON array and add the account role values to the accountRoleList.
		for (JsonElement elem : roleArray) {
			JsonObject role = elem.getAsJsonObject();
			long roleType = role.get("roleTypeId").getAsLong();
			String roleStatus = role.get("roleStatusId").getAsString();

			setAccountRoleList(roleType, roleStatus);
		}
	}

	/**
	 * Converts a JSON array of role expiration dates, and assigns them to the appropriate AccountRole object. The
	 * {@link #setAccountRoleExpiration(long, String)} method handles the AccountRole assignment.
	 * 
	 * @param json - A JSON array of role type and expiration date pairs.
	 * @throws RuntimeException When there is an error while parsing or accessing the JSON objects.
	 */
	public void setAccountRoleExpirationJson(String json) throws RuntimeException {
		JsonParser parser = new JsonParser();
		JsonArray expireDtArray = parser.parse(json).getAsJsonArray();

		// Add the new expiration dates to the needed roles.
		for (JsonElement elem : expireDtArray) {
			JsonObject exprDate = elem.getAsJsonObject();
			long roleId = exprDate.get("roleTypeId").getAsLong();
			String date = exprDate.get("expirationDate").getAsString();

			setAccountRoleExpiration(roleId, date);
		}
	}

	/**
	 * Handles date text from one or more hidden HTML inputs. The value attributes from the input tags are expected to
	 * be in the form of: value="role_type, expiration_date_string." The actual updating of the AccountRole objects will
	 * be handled by the {@link #setAccountRoleExpiration(long, String)} method.
	 * 
	 * @param valuePairs - A listing of value pairs from HTML input elements.
	 * @throws RuntimeException When role type value is not a number, or if the account role to date association fails.
	 */
	public void setAccountRoleExpiration(String[] valuePairs) throws RuntimeException {
		for (String pair : valuePairs) {
			String[] vals = pair.split(",");
			long index = Long.parseLong(vals[0].trim());
			String dateStr = vals[1].trim();

			setAccountRoleExpiration(index, dateStr);
		}
	}

	/**
	 * Searches for an AccountRole with a role matching index and sets the expiration date of that role. If the role
	 * currently does not exist, then one is created.
	 * 
	 * @param index - The id of the roleType
	 * @param value - The expiration date of this role type in string format
	 */
	public void setAccountRoleExpiration(long index, String value) {
		logger.info("Calling setAccountRoleExpiration");
		// Create an Date object from value.
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		DateFormat df2 = new SimpleDateFormat("dd-MMM-yyyy");
		Date expirationDate = null;

		try {
			expirationDate = df.parse(value);
		} catch (ParseException e) {
			try {
				expirationDate = df2.parse(value);
			} catch (ParseException e2) {
				return;
			}
		}

		// Search for the account with the correct RoleType and set the expiration date
		AccountRole currentRole = null;
		RoleType newRoleType = RoleType.getById(Long.valueOf(index));

		for (AccountRole role : accountRoleList) {
			if (role.getRoleType() == newRoleType) {
				currentRole = role;

				// Only set the expiration date if the account is active
				role.setExpirationDate(expirationDate);

				role.setRoleStatus(AccountServiceUtil.getRoleStatusFromExpDate(expirationDate));

				break;
			}
		}

		// If currentRole is null, then we need to create the appropriate AccountRole entry
		if (currentRole == null) {
			currentRole = new AccountRole();
			currentRole.setRoleType(newRoleType);
			currentRole.setExpirationDate(expirationDate);
			currentRole.setRoleStatus(AccountServiceUtil.getRoleStatusFromExpDate(expirationDate));
			accountRoleList.add(currentRole);
		}
	}

	public Set<PermissionGroupMember> getPermissionGroupMemberList() {
		return permissionGroupMemberList;
	}

	public void setPermissionGroupMemberList(String[] permissionGroupList) {
		for (String permissionGroupId : permissionGroupList) {
			PermissionGroupMember newGroupMember = new PermissionGroupMember();

			newGroupMember.setPermissionGroupStatus(PermissionGroupStatus.PENDING);
			newGroupMember.setPermissionGroup(permissionGroupDao.get(Long.valueOf(permissionGroupId)));
			permissionGroupMemberList.add(newGroupMember);
		}
	}

	public void adapt(Account account, Boolean enforceStaticFields) {
		// Check for altered roles.
		for (AccountRole role : accountRoleList) {
			boolean contains = false;
			role.setAccount(account);

			// Checks if role type already exists in the accountRoleList.
			for (AccountRole existingRole : account.getAccountRoleList()) {
				if (existingRole.getRoleType() == role.getRoleType()) {
					contains = true;
					existingRole.setExpirationDate(role.getExpirationDate());
					
					// For renewal requested account roles in pending status, we don't want its status to be
					// overwritten by the default role status calculated from the expiration date.
					if (!(account.getAccountStatus() == AccountStatus.RENEWAL_REQUESTED
							&& existingRole.getRoleStatus() == RoleStatus.PENDING
							&& existingRole.getExpirationDate() != null)) {
						existingRole.setRoleStatus(role.getRoleStatus());
					}
					break;
				}
			}

			// if role type does not already exist, add the new one
			if (!contains) {
				account.getAccountRoleList().add(role);
			}
		}

		Set<PermissionGroupMember> accountMembers = account.getPermissionGroupMemberList();

		for (PermissionGroupMember newMember : permissionGroupMemberList) {
			boolean contains = false;

			newMember.setAccount(account);

			// Checks if the permission group already exists in permissionGroupMemberList.
			for (PermissionGroupMember existingMember : accountMembers) {
				if (existingMember.equals(newMember)) {
					// if permission group status is not active, set to requested
					if (existingMember.getPermissionGroupStatus() != PermissionGroupStatus.ACTIVE) {
						existingMember.setPermissionGroupStatus(PermissionGroupStatus.PENDING);
					}

					contains = true;
					break;
				}
			}

			// if permission group does not already exist, add the new one to the set
			if (!contains) {
				accountMembers.add(newMember);
			}
		}
	}
	
	public boolean getHasProformsPrivilege() {
		for(AccountRole role: accountRoleList) {
			if(role.getRoleType().equals(RoleType.ROLE_PROFORMS) && role.getIsActive()) {
				return true;
			}
		}
		return false;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	
}
