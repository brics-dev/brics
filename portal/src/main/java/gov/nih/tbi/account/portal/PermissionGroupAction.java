
package gov.nih.tbi.account.portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.AccountActionType;
import gov.nih.tbi.account.model.PermissionGroupForm;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.model.PermissionGroupStatus;

/**
 * This class is an action for the creation/edit of permission groups
 * 
 * @author Francis Chen
 */
public class PermissionGroupAction extends BaseAccountAction {

	private static final long serialVersionUID = -3835151519478930337L;

	PermissionGroup currentPermissionGroup;
	PermissionGroupForm permissionGroupForm;
	List<Account> availableAccounts;

	/**
	 * Gets a list of all accounts, minus the ones already a member in the permission group
	 * 
	 * @return - A list of all accounts, minus the ones already a member in the session permission group.
	 */
	public List<Account> getAvailableAccounts() {
		if (availableAccounts == null) {
			availableAccounts = accountManager.getActiveAccounts();
		}

		Set<PermissionGroupMember> memberSet = getSessionAccountEdit().getPermissionGroup().getMemberSet();

		if (memberSet != null) {
			for (PermissionGroupMember member : memberSet) {
				availableAccounts.remove(member.getAccount());
			}
		}

		return availableAccounts;
	}

	/**
	 * This method returns a list of members of the session permission group with the status of 'Requested'
	 * 
	 * @return - A list of members of the session permission group with the status of 'Requested'
	 */
	public List<PermissionGroupMember> getAccessRequests() {
		List<PermissionGroupMember> requestList = new ArrayList<PermissionGroupMember>();

		for (PermissionGroupMember member : getSessionAccountEdit().getPermissionGroup().getMemberSet()) {
			if (PermissionGroupStatus.PENDING.equals(member.getPermissionGroupStatus())) {
				requestList.add(member);
			}
		}

		// Sort the list
		if (!requestList.isEmpty()) {
			Collections.sort(requestList, new Comparator<PermissionGroupMember>() {
				@Override
				public int compare(PermissionGroupMember o1, PermissionGroupMember o2) {
					String o1Name = o1.getAccount().getUser().getFullName();
					String o2Name = o2.getAccount().getUser().getFullName();

					return o1Name.compareTo(o2Name);
				}
			});
		}

		return requestList;
	}

	/**
	 * This method initiates the create page of permission group
	 * 
	 * @return - The string 'input' for struts to direct user to the proper jsp
	 */
	public String create() {

		getSessionAccountEdit().setPermissionGroup(new PermissionGroup());
		saveSession();
		return PortalConstants.ACTION_INPUT;
	}

	/**
	 * This method gets the value of the 'permissionGroupId' parameter in the URL.
	 * 
	 * @return - The PermissionGroup with the ID of the 'permissionGroupId' parameter.
	 */
	public PermissionGroup getPermissionGroupFromParam() {

		String permissionGroupId = getRequest().getParameter(PortalConstants.PERMISSION_GROUP_ID);

		try {
			return accountManager.getPermissionGroupById(Long.valueOf(permissionGroupId));
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Illegal permission group ID");
		} catch (NullPointerException e) {
			throw new RuntimeException("Permission Group with ID = " + permissionGroupId + " not found.");
		}
	}

	/**
	 * This method initiates the edit permission group page.
	 * 
	 * @return - The string 'input' so struts can direct user to the proper jsp
	 */
	public String edit() {

		getSessionAccountEdit().setPermissionGroup(getPermissionGroupFromParam());
		saveSession();
		return PortalConstants.ACTION_INPUT;
	}

	/**
	 * This method initiates the list permission group page
	 * 
	 * @return - The string 'list' so struts can direct user to the proper jsp
	 */
	public String list() {

		return PortalConstants.ACTION_LIST;
	}

	/**
	 * Adapt data from the form to session permission group
	 */
	public void saveSession() {

		currentPermissionGroup = getSessionAccountEdit().getPermissionGroup();

		if (permissionGroupForm == null) {
			permissionGroupForm = new PermissionGroupForm(currentPermissionGroup);
		} else {
			permissionGroupForm.adapt(currentPermissionGroup);
		}

	}

	/**
	 * This method saves the permission group to database, and if the Account status has been changed save the session
	 * account
	 * 
	 * @return
	 */
	/**
	 * @return
	 */
	public String submit() {
		// Sync the current permission group object with the data from the user.
		saveSession();

		Set<PermissionGroupMember> oldMembers = null;

		if (currentPermissionGroup.getId() != null) {
			PermissionGroup oldGroup = accountManager.getPermissionGroupById(currentPermissionGroup.getId());
			oldMembers = new HashSet<>(oldGroup.getMemberSet());
		}

		for (PermissionGroupMember member : permissionGroupForm.getMemberRemoval()) {
			accountsLog.info(getAccount().getUserName() + ": Removed " + member.getAccount().getUserName() + " from "
					+ currentPermissionGroup.getGroupName() + " permission group.");
		}

		// Remove any permission group members from the database.
		accountManager.removePermissionGroupMembers(permissionGroupForm.getMemberRemoval());

		// Save changes to the permission group to the database.
		currentPermissionGroup = accountManager.savePermissionGroup(currentPermissionGroup);

		if (oldMembers != null) {
			Set<PermissionGroupMember> newMembers = currentPermissionGroup.getMemberSet();

			Iterator<PermissionGroupMember> newMemberIterator = newMembers.iterator();
			while (newMemberIterator.hasNext()) {
				PermissionGroupMember newMember = newMemberIterator.next();
				if (oldMembers.contains(newMember)) {
					newMemberIterator.remove();
				}
			}

			for (PermissionGroupMember member : newMembers) {
				accountsLog.info(getAccount().getUserName() + ": Added " + member.getAccount().getUserName() + " to "
						+ currentPermissionGroup.getGroupName() + " permission group.");
			}
		} else {
			for (PermissionGroupMember member : currentPermissionGroup.getMemberSet()) {
				accountsLog.info(getAccount().getUserName() + ": Added " + member.getAccount().getUserName() + " to "
						+ currentPermissionGroup.getGroupName() + " permission group.");
			}
		}

		for (PermissionGroupMember member : permissionGroupForm.getMemberAccountUpdateSet()) {
			// need to re-get the account from the database, or else it will cause duplicate groups to get created
			// *shrug*
			Account currentAccount = accountManager.getAccountById(member.getAccount().getId());
			currentAccount = accountManager.accountActivation(currentAccount);

			accountsLog.info(getAccount().getUserName() + ": Approved " + currentAccount.getUserName()
					+ " access to permission group " + currentPermissionGroup.getGroupName());

			AccountHistory accountHistory =
					new AccountHistory(currentAccount, AccountActionType.PERMISSION_GROUP_APPROVE,
							member.getPermissionGroup().getDisplayName(), "", new Date(), getUser());

			currentAccount.addAccountHistory(accountHistory);
			accountManager.saveAccount(currentAccount);
		}

		return PortalConstants.ACTION_LIST;
	}

	public Long getPermissionGroupId() {

		return getSessionAccountEdit().getPermissionGroup().getId();
	}

	public Boolean getIsCreate() {

		return getSessionAccountEdit().getPermissionGroup().getId() == null;
	}

	public PermissionGroup getCurrentPermissionGroup() {

		return getSessionAccountEdit().getPermissionGroup();
	}

	public void setCurrentPermissionGroup(PermissionGroup currentPermissionGroup) {

		this.currentPermissionGroup = currentPermissionGroup;
	}

	public PermissionGroupForm getPermissionGroupForm() {

		return permissionGroupForm;
	}

	public void setPermissionGroupForm(PermissionGroupForm permissionGroupForm) {

		this.permissionGroupForm = permissionGroupForm;
	}

	public List<PermissionGroup> getPermissionGroupList() {

		return accountManager.getPermissionGroupList();
	}

}
