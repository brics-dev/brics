package gov.nih.tbi.commons.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import gov.nih.tbi.account.model.AccountPrivilegesForm;
import gov.nih.tbi.account.model.EmailReportType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountAdministrativeNote;
import gov.nih.tbi.account.model.hibernate.AccountEmailReportSetting;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.account.model.hibernate.BasicAccount;
import gov.nih.tbi.account.model.hibernate.ElectronicSignature;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.repository.model.AbstractDataset;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.semantic.model.QueryPermissions;

/**
 * This interface is used to abstract the internal workings of the account manager module.
 * 
 * IMPORTANT: Calls to account manager should be made through the AccountWebService. Any access to UM through BRICS or
 * DDT should be made through a web service call. (UM functionality such as request account/privileges can access the
 * accountManager directly). Exception: Calls to the accountManger that do not hit the database are alright.
 * 
 * @author Andrew Johnson
 * 
 * 
 */
public interface AccountManager extends BaseManager {

	/**
	 * Returns the unique account record with the given email address or null if no record exists.
	 * 
	 * @param email
	 * @return
	 */
	public Account getAccountByEmail(String email);

	/**
	 * Returns the unique account record with the given username or null if no record exists
	 * 
	 * @param userName
	 * @return
	 */
	public Account getAccountByUserName(String userName);

	/**
	 * Computes a hash token and returns it to the user. Stores the date the token was created with the account.
	 * 
	 * @param account
	 * @return
	 */
	public String addRecoveryTokenForAccount(Account account);

	/**
	 * Checks the token against the username and the recovery time of the account
	 * 
	 * @param account
	 * @param token
	 * @return
	 */
	public Boolean validateRecoveryTokenForAccount(Account account, String token);

	/**
	 * Send an email containing the recovery URL to the specified account email address
	 * 
	 * @param account
	 * @param Action path including token key, e.g.: publicAccounts/passwordRecoveryAction!recover.action?token=a0ce
	 * @param url URL NAME + SERVER ROOT, e.g.: http://REPLACED/portal/
	 * @param subject
	 * @param messagePattern
	 * 
	 * @throws MessagingException
	 */
	public void sendPasswordRecoveryEmail(Account account, String token, String url, String subject,
			String messagePattern) throws MessagingException;

	/**
	 * Send password recovery success email
	 * 
	 * @param account
	 * @param subject
	 * @throws MessagingException
	 */
	public void sendPasswordRecoverySuccessEmail(Account account, String subject, String message)
			throws MessagingException;

	/**
	 * This checks the password versus the old passwords in the database
	 * 
	 * @param account - account of user
	 * @param password - new requested password
	 * @return true if it has been used in the last 25 passwords
	 */
	public boolean checkPassword(Account account, String password);

	/**
	 * Hashes the the new password and saves it to the account
	 * 
	 * @param account
	 * @param password
	 * @return 
	 */
	public Account changePassword(Account account, String password);

	/**
	 * This method saves the account and related permissions
	 * 
	 * @param account
	 * @return
	 */
	public Account saveAccount(Account account);

	/**
	 * Gets a list of permission groups
	 * 
	 * @return
	 */
	public List<PermissionGroup> getPermissionGroupList();

	/**
	 * Validates if a username is valid
	 * 
	 * @param fieldValue
	 * @return
	 */
	public Boolean validateUserName(String userName);

	/**
	 * Validates if a username is available
	 * 
	 * @param fieldValue
	 * @return
	 */
	public Boolean checkUserNameAvailability(String userName, Long id);

	/**
	 * Validates if a era id is available
	 * 
	 * @param fieldValue
	 * @return
	 */
	public Boolean checkEraIdAvailability(String eraId, Long id);

	/**
	 * Hashes the password into a byte array
	 * 
	 * @param password
	 * @return
	 */
	public byte[] hashPassword(String password);

	/**
	 * Gets the account by ID, also checks user permission.
	 * 
	 * @param user
	 * @param valueOf
	 * @return
	 */
	public Account getAccount(User curUser, Long accountId);

	/**
	 * Gets the account by user
	 * 
	 * @param user
	 * @return
	 */
	public Account getAccountByUser(User user);

	/**
	 * Validates email for uniqueness. ID is the ID of the user to not check
	 * 
	 * @param email , id
	 * @return
	 */
	public boolean validateEmail(String email, Long id);

	/**
	 * Validates the format of password. One uppercase, one number, one special character
	 * 
	 * @param fieldValue
	 * @return
	 */
	public boolean validatePasswordFormat(String fieldValue);

	/**
	 * Get permission group by name
	 * 
	 * @param string
	 * @return
	 */
	public PermissionGroup getPermissionGroup(String groupName);

	/**
	 * Gets permission group by ID
	 * 
	 * @param selectedId
	 * @return
	 */
	public PermissionGroup getPermissionGroupById(Long selectedId);

	/**
	 * Gets a list of all the accounts
	 * 
	 * @return
	 */
	public List<Account> getAccountList();

	/**
	 * Gets the user with the specified ID
	 * 
	 * @param id
	 * @return
	 */
	public User getUserById(Long id);

	/**
	 * Return a list of accounts based on the search and pagination criteria. Passing a filter value > 0 will search for
	 * all status types.
	 * 
	 * @param key
	 * @param statusId
	 * @param pageData
	 * @return
	 */
	public List<BasicAccount> searchAccounts(String key, Long statusId, PaginationData pageData);

	/**
	 * Deactivated the account specified. Pending requests are left as is.
	 * 
	 * @param account
	 * @return a copy of the deactivated account.
	 */
	public Account deactivateAccount(Account account);

	/**
	 * Accounts that are withdrawn are requested accounts the user no longer wishes to have. Instead of disabling the
	 * account, the account will be marked as withdrawn
	 * 
	 * @param id
	 * @return a copy of the withdrawm account.
	 */
	public Account withdrawAccount(Long id);

	/**
	 * Changes an account from inactive to active or pending. The account will be changed to pending if there are any
	 * requested roles.
	 * 
	 * @param id
	 * @param newUsername : If provided, the accout will get the new username. If null or empty string, then timestamp
	 *        will be removed.
	 * @return a copy of the activated account
	 */
	public Account reactivateAccount(Account account, String newUsername);

	/**
	 * Changes an account from withdrawn to requested.
	 * 
	 * @param account
	 * @param newUsername : If provided, the accout will get the new username. If null or empty string, then timestamp
	 *        will be removed.
	 * @return a copy of the requested account
	 */
	public Account reinstateAccountRequest(Account account, String newUsername);

	/**
	 * Changes the given account to active or pending status, depending on if there are any outstanding change requests.
	 * 
	 * @param account
	 * @return The account object passed as a param
	 */
	public Account approveAccount(Account account);

	/**
	 * Changes the given account from requested to denied. Disables the account, sets the account status to denied and
	 * adds a timestamp to the username.
	 * 
	 * @param The account to make inactive
	 * @return The account object that is saved to the db.
	 */
	public Account rejectAccount(Account account);

	/**
	 * Gets a list of all active Accounts that have the role provided.
	 * 
	 * @param role
	 * @param onlyActive : if this is true, then only active accounts will be returned.
	 * @return
	 */
	public List<Account> getAccountListByRole(RoleType role, boolean onlyActive);

	/**
	 * Get the account of the current owner of the entity with and id matching entityId
	 * 
	 * @param entityId entityId to match against the id of the entities
	 * @param type of entity to search for
	 * @return
	 */
	public Account getEntityOwnerAccount(Long entityId, EntityType type);

	/**
	 * Gets a list of all public permission groups
	 * 
	 * @return
	 */
	public List<PermissionGroup> getPublicPermissionGroups();

	/**
	 * Gets a list of all non-public permission groups
	 * 
	 * @return
	 */
	public List<PermissionGroup> getPrivatePermissionGroups();

	/**
	 * Gets the number of accounts with a specific status based on the id given
	 * 
	 * @param statusId
	 * @return
	 */
	public Long getNumAccountsWithStatus(Long statusId);

	/**
	 * Returns true if the given account is a member of the given permission group
	 * 
	 * @param account
	 * @param permissionGroup
	 * @return
	 */
	public boolean isAccountInPermissionGroup(Account account, PermissionGroup permissionGroup);

	/**
	 * Checks if a phone number is valid.
	 * 
	 * @param phone
	 * @return valid -> true; invalid -> false
	 */
	public boolean validatePhoneNumber(String phone);

	/**
	 * Removes the entity map from a permission group
	 * 
	 * @param account
	 * @param permissionGroupName
	 * @param type
	 * @param entityId
	 * @return
	 */
	public boolean removeEntityFromPermissionGroup(Account account, String permissionGroupName, EntityType type,
			Long entityId);

	/**
	 * Removes the study from public study permission group
	 * 
	 * @param account
	 * @param type
	 * @param entityId
	 */
	public boolean removeFromPublicStudy(Account account, Study study);

	/**
	 * Checks if reactivating account would pass validation
	 * 
	 * @param id
	 * @return
	 */
	public boolean validateReactivateAccount(Long id);

	/**
	 * Saves the permission group to the database
	 * 
	 * @param permissionGroup - The permission group to save to database.
	 * @return 
	 */
	public PermissionGroup savePermissionGroup(PermissionGroup permissionGroup);

	/**
	 * Checks if the permission group name is unique
	 * 
	 * @param permissionGroupId
	 * @param fieldValue
	 * @return
	 */
	public boolean validatePermissionGroupName(Long permissionGroupId, String fieldValue);

	/**
	 * This method determines the differences between the old account and what has changed in the form.
	 * 
	 * @param previousAccount (Account) - the old account with old permissions
	 * @param tempPrivilegesForm (AccountPrivilegesForm) - the new list of privileges
	 * @return map between role types and the changes made.
	 */
	public Map<RoleType, String> determineChanges(Account previousAccount, AccountPrivilegesForm tempPrivilegesForm);

	/**
	 * Validates that the email matches an email of an account in the system
	 * 
	 * @param email - email to validate
	 * @return true if email matches, false otherwise
	 */
	public boolean validateEmailMatch(String email);

	/**
	 * Gets a list of all affiliated institutions that have been used in the past.
	 */
	public List<String> getAffiliatedInstitutions();

	/**
	 * Checks an account for access to a specific entity. Throws an UserPermissionException if user does not have access
	 * 
	 * @param account
	 * @param entityType
	 * @param entityId
	 * @param permissionType
	 * @throws UserPermissionException
	 */
	public void checkEntityAccess(Account account, EntityType entityType, Long entityId, PermissionType permissionType)
			throws UserPermissionException;

	/**
	 * Returns all the accounts that are active.
	 * 
	 * @return
	 */
	public List<Account> getActiveAccounts();

	/**********************************
	 * 
	 * 
	 * Entity Manager Accesss Used to be it's own interface
	 * 
	 * 
	 * *********************************
	 */

	/**
	 * register a new entry into the account management system
	 * 
	 * @param userId
	 * @param type
	 * @param entityId
	 * @param permissions
	 */
	public void registerEntity(Account account, EntityType type, Long entityId, PermissionType permission);

	/**
	 * register a new entry into the account management system
	 * 
	 * @param group
	 * @param type
	 * @param entityId
	 * @param permissions
	 */
	public void registerEntity(PermissionGroup group, EntityType type, Long entityId, PermissionType permission);

	/**
	 * Saves the given entity map into the account management system.
	 * 
	 * @param entityMap - The entity map to be saved.
	 */
	public void registerEnitity(EntityMap entityMap);
	
	public void registerEntity(List<EntityMap> entityMapList);

	/**
	 * Saves all changes to listed entity maps
	 * 
	 * @param entityMapList - A list of entity maps to save or update.
	 * @return A list of entity maps whose data is synced with the database.
	 * @throws HibernateException When there are any errors while saving the entity maps to the database.
	 */
	public List<EntityMap> saveUpdateEntity(List<EntityMap> entityMapList);

	/**
	 * Removes a entity from a group
	 * 
	 * @param type
	 * @param entityId
	 * @param permissionGroupId
	 */
	public void unregisterEntityFromGrop(EntityType type, Long entityId, Long permissionGroupId);

	/**
	 * Unregister an entry from the account management system by the given entity type and entity ID. All changes will
	 * be saved atomically, as in any changes will be persisted only when all deletions where successful.
	 * 
	 * @param type - The entity type of the entity map(s) to be deleted.
	 * @param entityId - The entity ID of the entity map(s) to be deleted.
	 * @throws HibernateException When there is a database error while trying to delete the entity maps. Any changes
	 *         before the error will be rolled back.
	 */
	public void unregisterEntity(EntityType type, Long entityId);

	/**
	 * unregister a list of entities
	 * 
	 * @param removedEntityMapList
	 */
	public void unregisterEntity(List<EntityMap> removedEntityMapList);

	/**
	 * Unregister entities by the list of IDs.
	 * 
	 * @param ids - A list of entity IDs to delete.
	 * @throws HibernateException When there is a database error while deleting the entities from the database. Any
	 *         changes made before the error will be rolled back.
	 */
	public List<EntityMap> unregisterEnity(List<Long> ids);

	/**
	 * List all entities that the user has access to
	 * 
	 * @param userId
	 * @param type
	 * @param onlyGranted
	 * @return
	 */
	public List<EntityMap> listUserAccess(Account account, EntityType type, boolean onlyGranted);

	/**
	 * List the ids of the entities a user has the provided permission to.
	 * 
	 * @param account
	 * @param type
	 * @param permission
	 * @return
	 */
	public Set<Long> listUserAccess(Account account, EntityType type, PermissionType permission);

	/**
	 * List the ids of the entities a user has the provided permission to.
	 * 
	 * @param account
	 * @param type
	 * @param permission
	 * @param onlyGranted (Boolean) - indicates if the method should only return actually granted permissions (no
	 *        permission groups or admins)
	 * @return
	 */
	public Set<Long> listUserAccess(Account account, EntityType type, PermissionType permission, Boolean onlyGranted);

	/**
	 * List the ids of the entities and the permission a user has the provided permission to.
	 * 
	 * @param account
	 * @param type
	 * @param permission
	 * @param onlyGranted (Boolean) - indicates if the method should only return actually granted permissions (no
	 *        permission groups or admins)
	 * @return
	 */
	public HashMap<Long, PermissionType> listUserAccessMap(Account account, EntityType type, PermissionType permission, Boolean onlyGranted);
	/**
	 * Return a list of entity objects that the user has the provided permission to.
	 * 
	 */
	public List<EntityMap> listUserAccessEntities(Account account, EntityType type, PermissionType permission,
			Boolean onlyGranted);

	/**
	 * List all users and their access to this entity
	 * 
	 * @param entityId
	 * @param type
	 * @return
	 */
	public List<EntityMap> listEntityAccess(Long entityId, EntityType type);

	/**
	 * 
	 * Get access information for entity type, entity id, and user id [by default includes permission groups]
	 * 
	 * @param userId
	 * @param type
	 * @param entityId
	 * @return
	 */
	public EntityMap getAccess(Account account, EntityType type, Long entityId);

	/**
	 * Get access information for entity type, entity id, and user id
	 * 
	 * @param account
	 * @param type
	 * @param entityId
	 * @param includePermissionGroups
	 * @return
	 */
	public EntityMap getAccess(Account account, EntityType type, Long entityId, Boolean includePermissionGroups);

	/**
	 * Get access by type
	 * 
	 * @param account
	 * @param type
	 * @param entityId
	 * @param permissions
	 * @return
	 */
	public Boolean getAccess(Account account, EntityType type, Long entityId, PermissionType permission);

	/**
	 * 
	 * @param account
	 * @param userName Will only be not null when wsSecured = false
	 * @return type is a Map with key StringURI and value of ArrayList of FormURI
	 */
	public QueryPermissions getQTPermissionsMap(Account account);

	public Account accountActivation(Account acct);

	public boolean requestedAccountGroupCheck(Account acct);

	public boolean requestedAccountRoleCheck(Account acct);

	public void removePermissionGroupMember(PermissionGroupMember member);
	
	public void removePermissionGroupMembers(Set<PermissionGroupMember> members);

	/**
	 * Given a list of entity ids. Will return the entityMap objects for the owners of those entities. This is used for
	 * the admin study search.
	 * 
	 * @param ids
	 * @param type
	 * @return
	 */
	public List<EntityMap> getOwnerEntityMaps(List<Long> ids, EntityType type);

	public void deleteLoginPendingApprovedAccount();

	/**
	 * Returns true if user has access to the given dataset. This check will return true if the user has access to the
	 * study the dataset is in.
	 * 
	 * @param account
	 * @param dataset
	 * @param permissionType
	 * @return
	 */
	public boolean getDatasetAccess(Account account, AbstractDataset dataset, PermissionType permissionType);
	
	/**
	 * Datasets share entity permissions with studies. If a study is public, but the dataset is private you can't go on the studies public permissions.
	 * you need to find out if the user has explicit access to the study. You pass in the study entity type and don't include public perimissions.
	 */
	public EntityMap getExplicitDatasetAccess(Account account, EntityType type, Long entityId, Boolean isPrivateDataset);
	
	public boolean getDatasetAccess(Account account, String studyTitle, String datasetName,
			PermissionType permissionType);

	/**
	 * Returns a list of entity maps by entity IDs and the current account. Will also check for all the entity maps of
	 * permission groups the current user is in. The IDs are stored in a set because they should all be unique.
	 * 
	 * @param account
	 * @param entityIds
	 * @param entityType
	 * @return
	 */
	public List<EntityMap> getEntityMapsByEntityIds(Account account, Set<Long> entityIds, EntityType entityType);
	
	public List <EntityMap> listEntityAccessByAccount(Long accountId, EntityType type);

	/**
	 * Returns a user with the given first and last name. We are only returning one User object because first and last
	 * name should be unique within the system.
	 * 
	 * @param firstName
	 * @param lastName
	 * @return
	 */
	public User getUserByName(String firstName, String lastName);
	
	public List<User> getUserDetailsByIds(Set<Long> userIds);
	
	public List<AccountHistory> getAccountHistory(Account account);
	
	public List<AccountAdministrativeNote> getAccountAdministrativeNotes(Account account);
	
	/**
	 * Returns a map of the latest adminNotes for the given account list, key: accountId, value: latest adminNote 
	 * @param accounts - a HashMap, key: accountId, value: the latest adminNotes 
	 * @return
	 */
	public Map<Long, String> getLatestAdminNoteForAccounts(List<Account> accounts);
	
	public void updateAccountPrivilegesExpiration();
	
	public List<Account> getAccountsUpForRenewal();
	
	/**
	 * Return all of the report settings with the given report type
	 * @param reportType
	 * @return
	 */
	public List<AccountEmailReportSetting> getReportSettingsByType(EmailReportType reportType);
	
	public List<Account> getAccountByStatuses(AccountStatus...accountStatuses);
	
	public Account getAccountWithEmailReport(Long accountId);

	public boolean checkUniqueFileName(String fileName, Long userId);
	
	/**
	 * Returns true if the given account has one of the following roles: ROLE_GUID, ROLE_GUID_ADMIN, or ROLE_ADMIN
	 * @param account
	 * @return
	 */
	public boolean hasAccessToGuidTool(Account account);
	
	public Account getAccountById(Long accountId);
	
	public ElectronicSignature saveElectronicSignature(ElectronicSignature electronicSignature);
	
	public User saveUserChanges(User user);
}
