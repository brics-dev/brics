package gov.nih.tbi.account.model;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.model.PermissionGroupStatus;
import gov.nih.tbi.commons.service.AccountManager;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * This form is for the permission group creation form
 * 
 * @author Francis Chen
 */
public class PermissionGroupForm {

	@Autowired
	AccountDao accountDao;

	@Autowired
	AccountManager accountManager;

	private Long id;
	private String groupName;
	private String groupDescription;
	private Boolean publicStatus;
	private Set<PermissionGroupMember> memberSet;
	private Set<PermissionGroupMember> memberApproval;
	private Set<PermissionGroupMember> memberAccountUpdateSet;

	public PermissionGroupForm() {
		groupName = "";
		groupDescription = "";
		publicStatus = Boolean.valueOf(false);
		memberSet = new HashSet<PermissionGroupMember>();
		memberApproval = new HashSet<PermissionGroupMember>();
		memberAccountUpdateSet = new HashSet<PermissionGroupMember>();
	}

	public PermissionGroupForm(PermissionGroup pg) {
		id = pg.getId();
		groupName = pg.getGroupName();
		groupDescription = pg.getGroupDescription();
		publicStatus = pg.getPublicStatus();
		memberApproval = new HashSet<PermissionGroupMember>();
		memberAccountUpdateSet = new HashSet<PermissionGroupMember>();

		if (pg.getMemberSet() != null) {
			memberSet = new HashSet<PermissionGroupMember>(pg.getMemberSet());
		} else {
			memberSet = new HashSet<PermissionGroupMember>();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupDescription() {
		return groupDescription;
	}

	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
	}

	public Boolean getPublicStatus() {
		return publicStatus;
	}

	public void setPublicStatus(Boolean publicStatus) {
		this.publicStatus = publicStatus;
	}

	public Set<PermissionGroupMember> getMemberSet() {
		return memberSet;
	}

	public Set<PermissionGroupMember> getMemberAccountUpdateSet() {
		return memberAccountUpdateSet;
	}

	/**
	 * Converts the JSON string from the JSPs to values to be used with the {@link #setMemberApproval(Long, String)}
	 * method.
	 * 
	 * @param json - The JSON array of member and status IDs.
	 * @throws JsonSyntaxException When the given JSON string is not valid.
	 * @throws NumberFormatException When the status is not a number.
	 */
	public void setMemberApprovalJSON(String json) throws JsonSyntaxException, NumberFormatException {
		JsonParser parser = new JsonParser();
		JsonArray memberArray = parser.parse(json).getAsJsonArray();

		// Add in the new members.
		for (JsonElement elm : memberArray) {
			JsonObject member = elm.getAsJsonObject();
			Long memberId = Long.valueOf(member.get("memberId").getAsLong());
			String status = member.get("status").getAsString();

			setMemberApproval(memberId, status);
		}
	}

	/**
	 * Handles the processing of values from one or more input fields. Each element of the given string array holds
	 * value pairs to be used in the {@link #setMemberApproval(Long, String)} method. Each value pair should be in the
	 * form of: "memberId, status."
	 * 
	 * @param valuePairs - An array of string value pairs in the form of "memberId, status".
	 * @throws NumberFormatException When either value pair or both is not a number.
	 */
	public void setMemberApproval(String[] valuePairs) throws NumberFormatException {
		for (String value : valuePairs) {
			String[] vals = value.split(",");
			Long memberId = Long.valueOf(vals[0]);
			String status = vals[1];

			setMemberApproval(memberId, status);
		}
	}

	/**
	 * Adds a permission group member to the approval list based on the given member ID and status ID.
	 * 
	 * @param memberId - The ID of the permission group member to search for.
	 * @param status - The string version of the status ID for the target permission group member.
	 * @throws NumberFormatException When the status is not a number.
	 */
	public void setMemberApproval(Long memberId, String status) throws NumberFormatException {
		Long statusId = Long.valueOf(status);

		for (PermissionGroupMember member : accountManager.getPermissionGroupById(id).getMemberSet()) {
			if (memberId.equals(member.getId())) {
				if (PermissionGroupStatus.ACTIVE.getId().equals(statusId)) {
					// if ID is active, set status to active
					member.setPermissionGroupStatus(PermissionGroupStatus.ACTIVE);
					memberApproval.add(member);
					memberAccountUpdateSet.add(member);
				} else if (PermissionGroupStatus.PENDING.getId().equals(statusId)) {
					// if no action is selected, the member will remain as requested
					member.setPermissionGroupStatus(PermissionGroupStatus.PENDING);
					memberApproval.add(member);
				} else {
					// The deny action was selected. Add the member to the removal list.
					member.getAccount().getPermissionGroupMemberList().remove(member);
					memberAccountUpdateSet.add(member);
				}

				break;
			}
		}
	}

	/**
	 * Sets the field according to the optiontransferselect on the form.
	 * 
	 * @param accountList - A comma delimited list of account ID's to become members of this permission group.
	 * @throws NumberFormatException When any of the elements in the accountList is not a number.
	 */
	public void setMemberSet(String accountList) throws NumberFormatException {
		if (!accountList.equals("empty")) {
			// Clear out the current list.
			memberSet.clear();

			// Convert the comma delimited list into an array
			String[] accountArray = accountList.split(CoreConstants.COMMA);
			PermissionGroup currentGroup = accountManager.getPermissionGroupById(id);


			Set<PermissionGroupMember> existingMembers = null;

			if (currentGroup != null) {
				existingMembers = currentGroup.getMemberSet();
			}

			// Create/Retrieve appropriate members and add them to the member set
			for (String accountId : accountArray) {
				Account account = accountDao.get(Long.valueOf(accountId.trim()));
				PermissionGroupMember member = null;

				if (existingMembers != null) {
					for (PermissionGroupMember pgm : existingMembers) {
						if (pgm.getAccount().equals(account)) {
							member = pgm;
							break;
						}
					}
				}

				// Check if a new member needs to be created.
				if (member == null) {
					member = new PermissionGroupMember();
					account.addPermissionGroupMember(member);
					member.setPermissionGroup(currentGroup);
					member.setPermissionGroupStatus(PermissionGroupStatus.ACTIVE);
				}

				memberSet.add(member);
			}
		}
	}


	/**
	 * Transfers the fields from the form to an actual permission group object. This method also ensures that the
	 * bidirectional relationship between the members and the permission group is correctly established. The
	 * relationship between the members and the linked accounts will also be updated as needed.
	 * 
	 * @param permissionGroup - The permissionGroup we want to transfer our fields into. This should be the permission
	 *        group that will be used to persist any changes to the database.
	 */
	public void adapt(PermissionGroup permissionGroup) {
		permissionGroup.setGroupName(groupName);
		permissionGroup.setGroupDescription(groupDescription);
		permissionGroup.setPublicStatus(publicStatus);

		if (!publicStatus) {
			// Merge the "memberSet" and "memberApproval" sets together.
			memberSet.addAll(memberApproval);
		}

		// Check for members that were removed by the user.
		for (PermissionGroupMember member : permissionGroup.getMemberSet()) {
			if (!memberSet.contains(member)) {
				member.getAccount().getPermissionGroupMemberList().remove(member);
			}
		}

		// Ensure that all of the members are referencing the given permission group object.
		for (PermissionGroupMember member : memberSet) {
			member.setPermissionGroup(permissionGroup);
		}


		// Save the new member set.
		permissionGroup.setMemberSet(memberSet);
	}
}
