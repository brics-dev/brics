package gov.nih.tbi.account.service.complex;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.stereotype.Service;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.dao.AccountAdministrativeNotesDao;
import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.dao.AccountEmailReportSettingDao;
import gov.nih.tbi.account.dao.AccountHistoryDao;
import gov.nih.tbi.account.dao.ElectronicSignatureDao;
import gov.nih.tbi.account.dao.TwoFactorAuthenticationDao;
import gov.nih.tbi.account.dao.hibernate.TwoFactorAuthenticationDaoImpl;
import gov.nih.tbi.account.dao.EntityMapDao;
import gov.nih.tbi.account.dao.PermissionGroupDao;
import gov.nih.tbi.account.dao.PermissionGroupMemberDao;
import gov.nih.tbi.account.dao.PermissionMapDao;
import gov.nih.tbi.account.dao.PreviousPasswordDao;
import gov.nih.tbi.account.model.AccountPrivilegesForm;
import gov.nih.tbi.account.model.EmailReportType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountAdministrativeNote;
import gov.nih.tbi.account.model.hibernate.AccountEmailReportSetting;
import gov.nih.tbi.account.model.hibernate.AccountHistory;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.BasicAccount;
import gov.nih.tbi.account.model.hibernate.ElectronicSignature;
import gov.nih.tbi.account.model.hibernate.TwoFactorAuthentication;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.account.model.hibernate.PreviousPassword;
import gov.nih.tbi.account.model.hibernate.VisualizationEntityMap;
import gov.nih.tbi.commons.dao.UserDao;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionGroupStatus;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.repository.dao.BasicDatasetDao;
import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.dao.UserFileDao;
import gov.nih.tbi.account.dao.VisualizationEntityMapDao;
import gov.nih.tbi.repository.model.AbstractDataset;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.semantic.model.DatasetRDF;
import gov.nih.tbi.semantic.model.QueryPermissions;
import gov.nih.tbi.semantic.model.StudyRDF;

/**
 * This is the account manager implemenation that uses hibernate
 * 
 * @author Andrew Johnson
 * 
 */
@Service
@Scope("singleton")
public class AccountManagerImpl extends BaseManagerImpl implements AccountManager, Serializable {

	private static final long serialVersionUID = -3856535486237089289L;

	static Logger logger = Logger.getLogger(AccountManagerImpl.class);

	@Autowired
	EntityMapDao entityMapDao;

	@Autowired
	PermissionMapDao permissionMapDao;

	@Autowired
	AccountDao accountDao;

	@Autowired
	PreviousPasswordDao previousPasswordDao;

	@Autowired
	MailEngine mailEngine;

	@Autowired
	UserDao userDao;

	@Autowired
	PermissionGroupDao permissionGroupDao;

	@Autowired
	PermissionGroupMemberDao permissionGroupMemberDao;

	@Autowired
	StaticReferenceManager staticManager;

	@Autowired
	RoleHierarchy roleHierarchy;

	@Autowired
	protected ModulesConstants modulesConstants;

	@Autowired
	DatasetDao datasetDao;

	@Autowired
	BasicDatasetDao basicDatasetDao;

	@Autowired
	AccountHistoryDao accountHistoryDao;

	@Autowired
	AccountEmailReportSettingDao accountEmailReportSettingDao;

	@Autowired
	UserFileDao userFileDao;
	
	@Autowired
	AccountAdministrativeNotesDao accountAdministrativeNotesDao;
	
	@Autowired
	ElectronicSignatureDao electronicSignatureDao;
	
	@Autowired
	VisualizationEntityMapDao VisualizationEntityMapDao;


	@Autowired
	TwoFactorAuthenticationDao twoFactorAuthenticationDao;;


	/**
	 * @inheritDoc
	 */
	public void registerEntity(Account account, EntityType type, Long entityId, PermissionType permission) {

		EntityMap oldMap = entityMapDao.get(account, type, entityId);
		if (oldMap != null) {
			oldMap.setPermission(permission);
			entityMapDao.save(oldMap);
		} else {
			EntityMap entityMap = new EntityMap();
			entityMap.setEntityId(entityId);
			entityMap.setType(type);
			entityMap.setAccount(account);
			entityMap.setPermission(permission);
			entityMapDao.save(entityMap);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void registerEntity(PermissionGroup group, EntityType type, Long entityId, PermissionType permission) {

		EntityMap oldMap = entityMapDao.get(group, type, entityId);

		if (oldMap != null) {
			oldMap.setPermission(permission);
			entityMapDao.save(oldMap);
		} else {

			EntityMap entityMap = new EntityMap();
			entityMap.setEntityId(entityId);
			entityMap.setType(type);
			entityMap.setPermissionGroup(group);

			entityMap.setPermission(permission);

			entityMapDao.save(entityMap);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void registerEnitity(EntityMap entityMap) {

		EntityMap oldMap = null;

		// Try to get current version of the entity map from the database.
		if (entityMap.getAccount() == null) {
			oldMap = entityMapDao.get(entityMap.getPermissionGroup(), entityMap.getType(), entityMap.getEntityId());
		} else {
			oldMap = entityMapDao.get(entityMap.getAccount(), entityMap.getType(), entityMap.getEntityId());
		}

		// Save changes to the database.
		if (oldMap != null) {
			oldMap.setPermission(entityMap.getPermission());
			entityMapDao.save(oldMap);
		} else {
			entityMapDao.save(entityMap);
		}
	}

	public void registerEntity(List<EntityMap> entityMapList) {

		if (entityMapList != null) {
			for (EntityMap em : entityMapList) {
				EntityMap oldMap = null;
				if (em.getAccount() == null) {
					oldMap = entityMapDao.get(em.getPermissionGroup(), em.getType(), em.getEntityId());
				} else {
					oldMap = entityMapDao.get(em.getAccount(), em.getType(), em.getEntityId());
				}

				if (oldMap != null) {
					oldMap.setPermission(em.getPermission());
					entityMapDao.save(oldMap);
				} else {
					entityMapDao.save(em);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EntityMap> saveUpdateEntity(List<EntityMap> entityMapList) throws HibernateException {

		List<EntityMap> syncedEntities = null;

		if ((entityMapList != null) && !entityMapList.isEmpty()) {
			syncedEntities = entityMapDao.createUpdateEntities(entityMapList);
		} else {
			syncedEntities = new ArrayList<EntityMap>();
		}

		return syncedEntities;
	}

	/**
	 * @inheritDoc
	 */
	public void checkEntityAccess(Account account, EntityType entityType, Long entityId, PermissionType permissionType)
			throws UserPermissionException {

		if (PermissionType.READ.equals(permissionType) && !getAccess(account, entityType, entityId, permissionType)) {
			throw new UserPermissionException(ServiceConstants.READ_ACCESS_DENIED);
		} else if (PermissionType.WRITE.equals(permissionType)
				&& !getAccess(account, entityType, entityId, permissionType)) {
			throw new UserPermissionException(ServiceConstants.WRITE_ACCESS_DENIED);
		} else if (PermissionType.ADMIN.equals(permissionType)
				&& !getAccess(account, entityType, entityId, permissionType)) {
			throw new UserPermissionException(ServiceConstants.ADMIN_ACCESS_DENIED);
		}
	}

	public void unregisterEntityFromGrop(EntityType type, Long entityId, Long permissionGroupId) {

		PermissionGroup pg = permissionGroupDao.get(permissionGroupId);
		EntityMap emToRemove = entityMapDao.get(pg, type, entityId, false);
		if (emToRemove != null) {
			entityMapDao.remove(emToRemove.getId());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterEntity(EntityType type, Long entityId) throws HibernateException {

		entityMapDao.unregisterEntity(type, entityId);
	}

	/**
	 * @inheritDoc
	 */
	public void unregisterEntity(List<EntityMap> removedEntityMapList) {

		entityMapDao.removeAll(removedEntityMapList);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<EntityMap> unregisterEnity(List<Long> ids) throws HibernateException {

		return entityMapDao.removeEntityMaps(ids);
	}

	/**
	 * @inheritDoc
	 */
	public List<EntityMap> getEntityMapsByEntityIds(Account account, Set<Long> entityIds, EntityType entityType) {

		// we want to include public permission groups and all of the permission groups that the user is in
		Set<PermissionGroup> currentGroups = new HashSet<PermissionGroup>();
		currentGroups.addAll(permissionGroupDao.getPublicGroups());
		currentGroups.addAll(permissionGroupDao.getGrantedGroups(account));

		return entityMapDao.getByEntityIds(account, currentGroups, entityIds, entityType);
	}

	/**
	 * @inheritDoc
	 */
	public List<EntityMap> listUserAccess(Account account, EntityType type, boolean onlyGranted) {

		Set<PermissionGroup> currentGroups = new HashSet<PermissionGroup>();
		currentGroups.addAll(permissionGroupDao.getPublicGroups());
		currentGroups.addAll(permissionGroupDao.getGrantedGroups(account));

		List<EntityMap> emList;
		if (onlyGranted) {
			emList = entityMapDao.listUserAccess(account, currentGroups, type, false);
		} else {
			emList = entityMapDao.listUserAccess(account, currentGroups, type, hasRole(account, type.getAdminRole()));
		}
		return emList;
	}

	/**
	 * @inheritDoc
	 */
	public Set<Long> listUserAccess(Account account, EntityType type, PermissionType permission) {

		return listUserAccess(account, type, permission, false);
	}

	/**
	 * @inheritDoc
	 */
	public List<EntityMap> listUserAccessEntities(Account account, EntityType type, PermissionType permission,
			Boolean onlyGranted) {

		List<EntityMap> entityMapList = null;

		// Being an admin gets you admin to all entites, but not owner. Therefore, if the permission being sought is
		// OWNER, the admin acts like a normal user.
		boolean isAdmin = false;
		if (hasRole(account, type.getAdminRole()) && !PermissionType.OWNER.equals(permission)) {
			isAdmin = true;
		}

		// if not only granted, then get all the entities the user has access to, including entities granted from public
		// groups
		if (!onlyGranted) {
			Set<PermissionGroup> currentGroups = new HashSet<PermissionGroup>();
			currentGroups.addAll(permissionGroupDao.getPublicGroups());
			currentGroups.addAll(permissionGroupDao.getGrantedGroups(account));
			logger.debug(" Manager only Granted = false Account: " + account.getUserName() + " Permission: "
					+ permission.getName());
			entityMapList = entityMapDao.listUserAccess(account, currentGroups, type, isAdmin);

			// if we are only getting permission of granted entities, get all the eneities the user has access to except
			// entities from public permission groups
		} else {
			Set<PermissionGroup> grantedPermissionGroups =
					new HashSet<PermissionGroup>(permissionGroupDao.getGrantedGroups(account));
			entityMapList = entityMapDao.listUserAccess(account, grantedPermissionGroups, type, false);
			logger.debug(" Manager only Granted = true Account: " + account.getUserName() + " Permission: "
					+ permission.getName());
		}
		logger.debug("number of returned results " + entityMapList.size());
		return entityMapList;

	}
	
	/**
	 * @inheritDoc
	 */
	public List<VisualizationEntityMap> listUserGrantedEntities(Long accountId, EntityType type) {
		// if we are only getting permission of granted entities, get all the eneities the user has access to except
		// entities from public permission groups

		
		List<VisualizationEntityMap> entityMapList = null;
		
		Long typeId = type.getId();
		
		entityMapList =  VisualizationEntityMapDao.getUserGrantedEntities(accountId, typeId);
		
		return entityMapList;
	}

	/**
	 * @inheritDoc
	 */
	public Set<Long> listUserAccess(Account account, EntityType type, PermissionType permission, Boolean onlyGranted) {

		List<EntityMap> entityMapList = listUserAccessEntities(account, type, permission, onlyGranted);

		Set<Long> ids = new HashSet<Long>();

		for (EntityMap em : entityMapList) {

			if (em.getPermission().contains(permission)) {
				ids.add(em.getEntityId());
			}
		}
		return ids;
	}

	/**
	 * @inheritDoc
	 */
	public HashMap<Long, PermissionType> listUserAccessMap(Account account, EntityType type, PermissionType permission,
			Boolean onlyGranted) {

		List<EntityMap> entityMapList = listUserAccessEntities(account, type, permission, onlyGranted);

		HashMap<Long, PermissionType> ids = new HashMap<Long, PermissionType>();

		for (EntityMap em : entityMapList) {

			if (em.getPermission().contains(permission)) {
				ids.put(em.getEntityId(), em.getPermission());
			}
		}
		return ids;
	}
	
	/**
	 * @inheritDoc
	 */
	public HashMap<Long, PermissionType> visualizationListUserAccessMap(Long accountId, EntityType type, PermissionType permission,
			Boolean onlyGranted) {

		List<VisualizationEntityMap> entityMapList = listUserGrantedEntities(accountId, type);

		HashMap<Long, PermissionType> ids = new HashMap<Long, PermissionType>();

		for (VisualizationEntityMap em : entityMapList) {
			PermissionType r = PermissionType.values()[em.getPermissionTypeId()];
			if (r.contains(permission)) {
				ids.put((long) em.getEntityId(), r);
			}
		}
		return ids;
	}

	/**
	 * @inheritDoc
	 */
	public EntityMap getAccess(Account account, EntityType type, Long entityId) {

		return getAccess(account, type, entityId, true);
	}

	/**
	 * @inheritDoc
	 */
	public EntityMap getAccess(Account account, EntityType type, Long entityId, Boolean includePermissionGroups) {

		EntityMap em = null;

		if (includePermissionGroups) {
			Set<PermissionGroup> currentGroups = new HashSet<PermissionGroup>();
			currentGroups.addAll(permissionGroupDao.getPublicGroups());
			currentGroups.addAll(permissionGroupDao.getGrantedGroups(account));
			em = entityMapDao.get(account, currentGroups, type, entityId, hasRole(account, type.getAdminRole()));
		} else {
			if (type.equals(EntityType.DATASET)) {
				Set<PermissionGroup> currentGroups = new HashSet<PermissionGroup>();
				currentGroups.addAll(permissionGroupDao.getGrantedGroups(account));
				em = entityMapDao.get(account, currentGroups, type, entityId, hasRole(account, type.getAdminRole()));
			} else {
				em = entityMapDao.get(account, type, entityId);
			}
		}

		return em;
	}

	/**
	 * @inheritDoc
	 */
	public Boolean getAccess(Account account, EntityType type, Long entityId, PermissionType permission) {

		Set<PermissionGroup> currentGroups = new HashSet<PermissionGroup>();
		currentGroups.addAll(permissionGroupDao.getPublicGroups());
		currentGroups.addAll(permissionGroupDao.getGrantedGroups(account));

		EntityMap em = entityMapDao.get(account, currentGroups, type, entityId, hasRole(account, type.getAdminRole()));

		// The user has NO permissions
		if (em.getPermission() == null) {
			return false;
		}

		return em.getPermission().contains(permission);
	}

	/**
	 * @inheritDoc
	 */
	public List<EntityMap> listEntityAccess(Long entityId, EntityType type) {

		List<EntityMap> emList = entityMapDao.listEntityAccess(entityId, type);

		return emList;
	}

	/**
	 * @inheritDoc
	 */
	public Account saveAccount(Account account) {

		account = accountDao.save(account);

		return account;
	}

	/**
	 * @inheritDoc
	 */
	public List<PermissionGroup> getPermissionGroupList() {

		List<PermissionGroup> permissionGroupList = permissionGroupDao.getAll();

		Collections.sort(permissionGroupList, new Comparator<PermissionGroup>() {

			@Override
			public int compare(PermissionGroup pa1, PermissionGroup pa2) {

				if (pa1.getDisplayName() != null && pa2.getDisplayName() != null
						&& pa1.getDisplayName().compareTo(pa2.getDisplayName()) != 0) {
					return pa1.getGroupName().compareToIgnoreCase(pa2.getGroupName());
				} else {
					return pa1.getGroupDescription().compareToIgnoreCase(pa2.getGroupDescription());
				}
			}
		});
		
		for(PermissionGroup pg : permissionGroupList) {
			pg.setGroupDescription(escapeDoubleQuotes(pg.getGroupDescription()));
		}
		
		return permissionGroupList;
	}
	
	
	public static String escapeDoubleQuotes( String value )
	{
	    StringBuilder builder = new StringBuilder();
	    for( char c : value.toCharArray() )
	    {
	        if( c == '\'' )
	            builder.append( "\\'" );
	        else if ( c == '\"' )
	            builder.append( "\\\"" );
	        else if( c == '\r' )
	            builder.append( "\\r" );
	        else if( c == '\n' )
	            builder.append( "\\n" );
	        else if( c == '\t' )
	            builder.append( "\\t" );
	        else if( c < 32 || c >= 127 )
	            builder.append( String.format( "\\u%04x", (int)c ) );
	        else
	            builder.append( c );
	    }

	    return builder.toString();
	}

	/**
	 * @inheritDoc
	 */
	public void saveSitePermission(List<Long> siteList, Account account) {

		for (Long siteId : siteList) {
			PermissionGroup currentGroup = permissionGroupDao.get(siteId);
			if (currentGroup != null) {
				PermissionGroupMember groupMember = new PermissionGroupMember();
				groupMember.setPermissionGroup(currentGroup);
				groupMember.setAccount(account);
				permissionGroupMemberDao.save(groupMember);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	public Boolean validateUserName(String userName) {

		Pattern pattern = Pattern.compile(ServiceConstants.USERNAME__REGEX);
		Matcher matcher = pattern.matcher(userName);
		return matcher.matches();
	}

	/**
	 * @inheritDoc
	 */
	public Boolean checkUserNameAvailability(String userName, Long id) {

		Account account = accountDao.getByUserName(userName);

		if (account == null) {
			return true;
		} else if (account.getId().equals(id)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @inheritDoc
	 */
	public Boolean checkEraIdAvailability(String eraId, Long id) {

		boolean notContains = true;

		if (eraId != null && eraId.length() > 0) {
			List<BasicAccount> accountList = accountDao.getAccountsByEraId(eraId);

			for (BasicAccount account : accountList) {
				if (eraId.equalsIgnoreCase(account.getEraId()) && (id == null || !account.getId().equals(id))) {
					notContains = false;
				}
			}
		}

		return notContains;
	}

	/**
	 * @inheritDoc
	 */
	public Account getAccountByEmail(String email) {

		User user = userDao.getByEmail(email);
		if (user == null) {
			return null;
		}
		return getAccountByUser(user);
	}

	/**
	 * @inheritDoc
	 */
	public Account getAccountByUser(User user) {

		return accountDao.getByUser(user);
	}

	/**
	 * @inheritDoc
	 */
	public Account getAccountByUserName(String userName) {
		Account result = accountDao.getByUserName(userName);
		return result;
	}

	/**
	 * @inheritDoc
	 */
	public String addRecoveryTokenForAccount(Account account) {

		// Get the current time
		// SQL returns timestamp object. Create date with current time and convert to timestamp
		Date date = new Date();
		Date timestamp = new Timestamp(date.getTime());

		// Get the token for the user
		String token = hashRecoveryString(account.getUserName(), timestamp);

		// Save the date into the account record
		account.setRecoveryDate(date);
		accountDao.save(account);

		return token;
	}

	/**
	 * @inheritDoc
	 */
	public Boolean validateRecoveryTokenForAccount(Account account, String token) {

		return token.equals(hashRecoveryString(account.getUserName(), account.getRecoveryDate()));
	}

	/**
	 * A private helper method for creating a hash from a username and a Date
	 * 
	 * @param userName
	 * @param date
	 * @return
	 */
	private String hashRecoveryString(String userName, Date date) {

		byte[] raw = null;

		// Digest the String
		try {
			raw = MessageDigest.getInstance("SHA-256")
					.digest((userName + date.toString() + CoreConstants.SALT).getBytes());
		} catch (NoSuchAlgorithmException e) {
			logger.error("There was an exception in the accountManagerImpl hashRecoveryString(): "
					+ e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}

		// Convert to a hex String
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(CoreConstants.HEXES.charAt((b & 0xF0) >> 4)).append(CoreConstants.HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	/**
	 * @throws MessagingException
	 * @inheritDoc
	 */
	public void sendPasswordRecoveryEmail(Account account, String token, String url, String subject,
			String messagePattern) throws MessagingException {

		// Create an email message
		String message = null;

		if (messagePattern == null) {
			// TODO:NIMESH Add PORTAL ROOT instead of taking it from the URL from previous method
			message = CoreConstants.RECOVERY_EMAIL_BODY + url + CoreConstants.RECOVERY_EMAIL_URI + token;
		} else {
			// TODO:NIMESH Add PORTAL ROOT instead of taking it from the URL from previous method
			message = java.text.MessageFormat.format(messagePattern, account.getUser().getFullName(),
					url + CoreConstants.RECOVERY_EMAIL_URI + token);
		}

		User user = userDao.get(account.getUserId());
		try {
			mailEngine.sendMail(subject, message, null, user.getEmail());
		} catch (MessagingException e) {
			logger.error("There was an exception in the accountManagerImpl sendPasswordRecoveryEmail(): "
					+ e.getLocalizedMessage());
			e.printStackTrace();
			throw e;
		}
	}

	public void sendPasswordRecoverySuccessEmail(Account account, String subject, String message)
			throws MessagingException {
		// Create an password recovery success email message

		String messagePattern = message = java.text.MessageFormat.format(message, account.getUser().getFullName());
		User user = userDao.get(account.getUserId());
		try {
			mailEngine.sendMail(subject, messagePattern, null, user.getEmail());
		} catch (MessagingException e) {
			logger.error("There was an exception in the accountManagerImpl sendPasswordRecoverySuccessEmail(): "
					+ e.getLocalizedMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean checkPassword(Account account, String password) {

		// check if provided password is not the same as current password
		if (account != null && account.getPassword() != null) {
			if (Arrays.equals(account.getPassword(), hashPassword(account.getSalt() + password))) {
				return true;
			}
		}

		List<PreviousPassword> passwordList = previousPasswordDao.getLast(account, 25);
		// check if provided password is not the same as previous 25 passwords
		if (passwordList != null && passwordList.size() > 0) {

			for (PreviousPassword pass : passwordList) {

				if (Arrays.equals(pass.getPassword(), hashPassword(pass.getSalt() + password))) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @inheritDoc
	 */
	public Account changePassword(Account account, String password) {

		// double check the password before committing it!
		if (checkPassword(account, password)) {
			throw new RuntimeException(ServiceConstants.INVALID_PASSWORD);
		}

		// we don't want edited account info from session so
		// get account from database
		Account currentAccount = accountDao.get(account.getId());

		// add old password to used passwords
		PreviousPassword pass = new PreviousPassword();
		pass.setAccount(currentAccount);
		pass.setPassword(currentAccount.getPassword());
		pass.setSalt(currentAccount.getSalt());
		pass.setEventDate(new Date());

		previousPasswordDao.save(pass);

		String salt = HashMethods.getPasswordSalt();
		currentAccount.setSalt(salt);

		accountDao.save(currentAccount);

		return accountDao.changePassword(currentAccount.getId(), hashPassword(salt + password));
	}

	/**
	 * @inheritDoc
	 */
	public byte[] hashPassword(String password) {

		// Way to complicated method to fill a Byte array with the encrypted password
		byte[] raw = null;
		try {
			raw = MessageDigest.getInstance("SHA-256").digest(password.getBytes());
		} catch (NoSuchAlgorithmException e) {
			logger.error("There was an exception in the accountManagerImpl hashPassword(): " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		byte[] passwordArray = new byte[raw.length];
		for (int i = 0; i < raw.length; i++) {
			passwordArray[i] = raw[i];
		}
		return passwordArray;
	}

	public Account getAccountById(Long accountId) {
		return accountDao.get(accountId);
	}

	/**
	 * @inheritDoc
	 */
	public Account getAccount(User curUser, Long accountId) {

		// TODO: may need to add permission checks later
		if (curUser == null) {
			throw new RuntimeException("curUser cannot be null.");
		}

		// this should never happen
		if (accountId == null) {
			throw new RuntimeException("accountId cannot be null.");
		}

		return accountDao.get(accountId);
	}

	public PermissionGroup getPermissionGroup(String groupName) {

		return permissionGroupDao.getByName(groupName);
	}

	/**
	 * @inheritDoc
	 */
	public PermissionGroup getPermissionGroupById(Long selectedId) {

		return permissionGroupDao.get(selectedId);
	}

	/**
	 * @inheritDoc
	 */
	public boolean validateEmail(String email, Long userId) {

		List<User> userList = userDao.getAll();

		boolean notContains = true;

		for (User user : userList) {
			if (email.equalsIgnoreCase(user.getEmail()) && (userId == null || !userId.equals(user.getId()))) {
				notContains = false;
				break;
			}
		}

		return notContains;
	}

	/**
	 * @inheritDoc
	 */
	public Long getNumAccountsWithStatus(Long statusId) {

		if (statusId == null) {
			return null;
		}
		return accountDao.getStatusCount(AccountStatus.getById(statusId));
	}

	/**
	 * @inheritDoc
	 */
	public boolean validatePhoneNumber(String phone) {

		Pattern pattern = Pattern.compile(ServiceConstants.PHONE_NUMBER_PATTERN);
		Matcher matcher = pattern.matcher(phone);
		return matcher.matches();
	}

	/**
	 * @inheritDoc
	 */
	public boolean validatePasswordFormat(String password) {

		int counter = 0;

		if (password == null)
			return false;

		Pattern pattern = Pattern.compile(ServiceConstants.PASSWORD_DIGIT_PATTERN);
		Matcher matcher = pattern.matcher(password);
		if (matcher.matches()) {
			counter++;
		}

		pattern = Pattern.compile(ServiceConstants.PASSWORD_LOWER_PATTERN);
		matcher = pattern.matcher(password);
		if (matcher.matches()) {
			counter++;
		}

		pattern = Pattern.compile(ServiceConstants.PASSWORD_UPPER_PATTERN);
		matcher = pattern.matcher(password);
		if (matcher.matches()) {
			counter++;
		}

		pattern = Pattern.compile(ServiceConstants.PASSWORD_SPECIAL_PATTERN);
		matcher = pattern.matcher(password);
		if (matcher.matches()) {
			counter++;
		}

		if (counter >= 3 && password.length() <= ServiceConstants.PASSWORD_MAX_LENGTH
				&& password.length() >= ServiceConstants.PASSWORD_MIN_LENGTH) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @inheritDoc
	 */
	public List<Account> getAccountList() {

		return accountDao.getAccountList();
	}

	/**
	 * @inheritDoc
	 */
	public List<Account> getActiveAccounts() {

		return accountDao.getAllActive();
	}

	/**
	 * @inheritDoc
	 */
	public List<Account> getAccountListByRole(RoleType role, boolean onlyActive) {

		// List<Account> accountList = new ArrayList<Account>();
		//
		// List<Account> activeAccounts = this.getActiveAccounts();
		// for (Account account : activeAccounts)
		// {
		// if (this.hasRole(account, role))
		// {
		// accountList.add(account);
		// }
		// }
		//
		// return accountList;

		// ///////////////////////////////

		return accountDao.getAccountsWithRoles(staticManager.rolesThatImplyRole(role), onlyActive);

	}

	/**
	 * @inheritDoc
	 */
	public User getUserById(Long id) {

		return userDao.get(id);
	}

	/**
	 * @inheritDoc
	 */
	public List<BasicAccount> searchAccounts(String key, Long statusId, PaginationData pageData) {

		AccountStatus accountStatus;
		// Make sure statusType is not less than -1
		if (statusId < 0L) {
			accountStatus = null;
		} else {
			accountStatus = AccountStatus.getById(statusId);
		}

		return accountDao.search(key, accountStatus, pageData);
	}

	/**
	 * @inheritDoc
	 */
	public Account withdrawAccount(Long id) {

		Account account = accountDao.get(id);
		if (account == null || (account.getAccountStatus() != AccountStatus.REQUESTED
				&& account.getAccountStatus() != AccountStatus.PENDING)) {
			return null;
		}

		Set<AccountRole> accountRoleSet = account.getAccountRoleList();
		Iterator<AccountRole> iter = accountRoleSet.iterator();
		while (iter.hasNext()) {
			AccountRole role = (AccountRole) iter.next();
			if (role.getRoleType() == RoleType.ROLE_USER) {
				role.setRoleStatus(RoleStatus.WITHDRAWN);
				break;
			}
		}

		// set properties for the deactivated account
		account.setUserName(deactivatedUserAccountGenerator(account.getUserName()));
		account.setIsActive(false);
		account.setAccountStatus(AccountStatus.WITHDRAWN);
		accountDao.save(account);
		return account;
	}

	/**
	 * @inheritDoc
	 */
	public Account deactivateAccount(Account account) {

		switch (account.getAccountStatus()) {
			case REQUESTED:
			case PENDING:
			case CHANGE_REQUESTED:
			case ACTIVE:
				// set properties for the deactivated account
				account.setUserName(deactivatedUserAccountGenerator(account.getUserName()));
				account.setIsActive(false);
				account.setAccountStatus(AccountStatus.INACTIVE);
				return accountDao.save(account);
			default:
				throw new RuntimeException("Failed to deactivate account. (Wrong account status?)");
		}
	}

	/*
	 * This function appends a '-' and time stamp to the end of a username
	 */
	public String deactivatedUserAccountGenerator(String activeUserName) {

		// For this to work, '-' must remain an illegal character in a username
		Date date = new Date();
		String newUsername = activeUserName + '-' + date.getTime();
		if (newUsername.length() > 55) {
			newUsername = newUsername.substring(0, 55);
		}
		return newUsername;
	}

	/**
	 * @inheritDoc
	 */
	public boolean validateReactivateAccount(Long id) {

		Account account = accountDao.get(id);
		String newName = account.getUserName().split("-")[0];
		return checkUserNameAvailability(newName, id);
	}

	/**
	 * @inheritDoc
	 */
	public Account reactivateAccount(Account account, String newUsername) {

		if (account == null || account.getAccountStatus() != AccountStatus.INACTIVE) {
			return null;
		}
		// (Orig comment) For this to work, '-' must remain an illegal character in a username
		// (CRIT-1781) Removed a function that was used here and in reinstateAccountRequest()
		// in favor of the old logic because the new function did not work.
		if (newUsername == null) {
			// case: No new username was given.
			account.setUserName(account.getUserName().split("-")[0]);
		} else {
			// case: username is now a duplicate. New username provided.
			account.setUserName(newUsername);
		}
		account.setIsActive(true);
		account.setAccountStatus(AccountStatus.ACTIVE);
		for (AccountRole role : account.getAccountRoleList()) {
			if (role.getRoleStatus() == RoleStatus.PENDING) {
				account.setAccountStatus(AccountStatus.CHANGE_REQUESTED);
			}

			if (RoleType.ROLE_USER == role.getRoleType()) {
				role.setRoleStatus(RoleStatus.ACTIVE);
			}
		}

		accountDao.save(account);
		return account;
	}

	/**
	 * @inheritDoc
	 */
	public Account reinstateAccountRequest(Account account, String newUsername) {

		if (account == null || account.getAccountStatus() != AccountStatus.WITHDRAWN) {
			return null;
		}

		// we need to set the user role back to active so the user can log in again.
		for (AccountRole currentRole : account.getAccountRoleList()) {
			if (RoleType.ROLE_USER == currentRole.getRoleType()) {
				currentRole.setRoleStatus(RoleStatus.ACTIVE);
				break;
			}
		}

		account.setAccountStatus(AccountStatus.REQUESTED);
		// Copied the old username setting code after a new function used here and in
		// reactivateAccount did not work.
		if (newUsername == null) {
			// case: No new username was given.
			account.setUserName(account.getUserName().split("-")[0]);
		} else {
			// case: username is now a duplicate. New username provided.
			account.setUserName(newUsername);
		}

		accountDao.save(account);
		return account;
	}

	/**
	 * @inheritDoc
	 */
	public Account approveAccount(Account account) {

		if (account == null || account.getAccountStatus() != AccountStatus.REQUESTED) {
			logger.error(
					"The requested account cannot be approved because it does not exist or it does not have requested status.");
			return null;
		}

		// set next password expiration
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, CoreConstants.EXPIRE_ACCOUNT_AFTER_DAYS);

			logger.debug(account.getUserName() + "'s next password change must be before " + cal.getTime());

			account.setPasswordExpirationDate(cal.getTime());
		}

		account.setIsActive(true);
		account.setAccountStatus(AccountStatus.ACTIVE);
		for (AccountRole role : account.getAccountRoleList()) {
			if (role.getRoleStatus() == RoleStatus.PENDING) {
				account.setAccountStatus(AccountStatus.CHANGE_REQUESTED);
				break;
			}
		}
		return account;
	}

	/**
	 * @inheritDoc
	 */
	public Account rejectAccount(Account account) {
		account.setIsActive(false);
		account.setAccountStatus(AccountStatus.DENIED);

		// Add the timestamp onto the username
		Date date = new Date();
		String newUsername = account.getUserName() + '-' + date.getTime();
		if (newUsername.length() > 55) {
			newUsername = newUsername.substring(0, 55);
		}
		account.setUserName(newUsername);

		String newUserEmail = account.getUser().getEmail() + '-' + date.getTime();
		if (newUserEmail.length() > 55) {
			newUserEmail = newUserEmail.substring(0, 55);
		}

		account.getUser().setEmail(newUserEmail);

		return account;
	}

	/**
	 * @inheritDoc
	 */
	public Account getEntityOwnerAccount(Long entityId, EntityType type) {

		return entityMapDao.getOwnerEntityMap(entityId, type).getAccount();
	}

	/**
	 * @inheritDoc
	 */
	public List<PermissionGroup> getPublicPermissionGroups() {

		return permissionGroupDao.getPublicGroups();
	}

	/**
	 * @inheritDoc
	 */
	public boolean isAccountInPermissionGroup(Account account, PermissionGroup permissionGroup) {

		if (permissionGroup == null || account == null) {
			return false;
		}

		for (PermissionGroupMember p : account.getPermissionGroupMemberList()) {
			if (p.getPermissionGroup().equals(permissionGroup)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public boolean removeEntityFromPermissionGroup(Account account, String permissionGroupName, EntityType type,
			Long entityId) {

		PermissionGroup publicStudy = this.getPermissionGroup(permissionGroupName);

		if (publicStudy != null) {
			for (EntityMap entity : publicStudy.getEntityMapSet()) {
				if (type.equals(entity.getType()) && entityId.equals(entity.getEntityId())) {
					if (publicStudy.getEntityMapSet().remove(entity)) {
						permissionGroupDao.save(publicStudy);
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * @inheritDoc
	 */
	public boolean removeFromPublicStudy(Account account, Study study) {

		return removeEntityFromPermissionGroup(account, ServiceConstants.PUBLIC_STUDY, EntityType.STUDY, study.getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PermissionGroup> getPrivatePermissionGroups() {

		return permissionGroupDao.getPrivateGroups();
	}

	/**
	 * {@inheritDoc}
	 */
	public PermissionGroup savePermissionGroup(PermissionGroup permissionGroup) {

		return permissionGroupDao.save(permissionGroup);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean validatePermissionGroupName(Long permissionGroupId, String name) {

		for (PermissionGroup permissionGroup : permissionGroupDao.getAll()) {
			if ((permissionGroupId == null || !permissionGroupId.equals(permissionGroup.getId()))
					&& name.equals(permissionGroup.getGroupName())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<RoleType, String> determineChanges(Account previousAccount, AccountPrivilegesForm tempPrivilegesForm) {

		Map<RoleType, String> changeMap = new HashMap<RoleType, String>();
		Set<RoleType> foundList = new HashSet<RoleType>();

		// Loop through all previous roles to see if changes have been made to them
		for (AccountRole oldRole : previousAccount.getAccountRoleList()) {
			AccountRole newRole = null;

			// Find the corresponding role from the role list given by the JSP.
			for (AccountRole ar : tempPrivilegesForm.getAccountRoleList()) {
				if (ar.getRoleType().equals(oldRole.getRoleType())) {
					newRole = ar;
					foundList.add(oldRole.getRoleType());
					break;
				}
			}

			if (newRole != null) {
				// Check for any role status changes.
				if (!oldRole.getRoleStatus().equals(newRole.getRoleStatus())) {
					// Role status changed...
					if (oldRole.getRoleStatus().equals(RoleStatus.PENDING) && newRole.getIsActive()) {
						// Role was granted.
						changeMap.put(oldRole.getRoleType(), ServiceConstants.ACCOUNT_ROLE_GRANTED);
					}

					if (oldRole.getRoleStatus().equals(RoleStatus.PENDING)
							&& newRole.getRoleStatus().equals(RoleStatus.INACTIVE)) {
						// Role has been denied.
						changeMap.put(oldRole.getRoleType(), ServiceConstants.ACCOUNT_ROLE_DENIED);
					}

					if (oldRole.getIsActive() && newRole.getRoleStatus().equals(RoleStatus.INACTIVE)) {
						// Role was revoked.
						changeMap.put(oldRole.getRoleType(), ServiceConstants.ACCOUNT_ROLE_REVOKED);
					}
				}

				// Check the expiration dates.
				if (oldRole.getExpirationDate() != null && newRole.getExpirationDate() != null
						&& !newRole.getExpirationDate().equals(oldRole.getExpirationDate())) {
					// The expiration date has changed.
					if (changeMap.get(oldRole.getRoleType()) == null) {
						changeMap.put(oldRole.getRoleType(),
								ServiceConstants.ACCOUNT_DATE_CHANGED + newRole.getExpirationString());
					} else {
						changeMap.put(oldRole.getRoleType(),
								changeMap.get(oldRole.getRoleType()) + ServiceConstants.STRING_AND
										+ ServiceConstants.ACCOUNT_DATE_CHANGED + newRole.getExpirationString());
					}
				} else if (oldRole.getExpirationDate() == null && newRole.getExpirationDate() != null) {
					// New expiration date added.
					if (changeMap.get(oldRole.getRoleType()) == null) {
						changeMap.put(oldRole.getRoleType(),
								ServiceConstants.ACCOUNT_DATE_SET + newRole.getExpirationString());
					} else {
						changeMap.put(oldRole.getRoleType(),
								changeMap.get(oldRole.getRoleType()) + ServiceConstants.STRING_AND
										+ ServiceConstants.ACCOUNT_DATE_SET + newRole.getExpirationString());
					}
				} else if (oldRole.getExpirationDate() != null && newRole.getExpirationDate() == null) {
					// The expiration date was removed.
					if (changeMap.get(oldRole.getRoleType()) == null) {
						changeMap.put(oldRole.getRoleType(), ServiceConstants.ACCOUNT_DATE_REMOVED);
					} else {
						changeMap.put(oldRole.getRoleType(), changeMap.get(oldRole) + ServiceConstants.STRING_AND
								+ ServiceConstants.ACCOUNT_DATE_REMOVED);
					}
				}
			} else {
				// PRIVILEGE REMOVED COMPLETELY (BOX WAS UNCHECKED BY ADMIN)
				// In this case we still want the role there, but we are going to change the status to inactive (no
				// expiration)
				tempPrivilegesForm.getAccountRoleList()
						.add(new AccountRole(oldRole.getAccount(), oldRole.getRoleType(), RoleStatus.INACTIVE, null));

				// If the status was already inactive there is no need to update the changeMap
				if (!oldRole.getRoleStatus().equals(RoleStatus.INACTIVE)) {
					changeMap.put(oldRole.getRoleType(), ServiceConstants.ACCOUNT_ROLE_REVOKED);
				}
			}

		}

		// Check for new permissions
		for (AccountRole ar : tempPrivilegesForm.getAccountRoleList()) {
			if (!foundList.contains(ar.getRoleType())) {
				if (ar.getRoleStatus().equals(RoleStatus.PENDING)) {
					// Role Requested
					changeMap.put(ar.getRoleType(), ServiceConstants.ACCOUNT_ROLE_REQUESTED);
				}

				if (ar.getIsActive()) {
					// Role Granted
					changeMap.put(ar.getRoleType(), ServiceConstants.ACCOUNT_ROLE_GRANTED);
				}
			}
		}

		return changeMap;
	}

	/**
	 * @inheritDoc
	 */
	public boolean validateEmailMatch(String email) {

		Account account = accountDao.getByEmail(email);

		if (account == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<String> getAffiliatedInstitutions() {

		// caste it into linkedhashset to get unique results of the affiliated institutions
		LinkedHashSet<String> affiliatedInstitutionSet =
				new LinkedHashSet<String>(accountDao.getAffiliatedInstitutions());
		return new ArrayList<String>(affiliatedInstitutionSet);
	}

	/**
	 * Datasets share entity permissions with studies. If a study is public, but the dataset is private you can't go on
	 * the studies public permissions. you need to find out if the user has explicit access to the study. You pass in
	 * the study entity type and don't include public perimissions.
	 */
	public EntityMap getExplicitDatasetAccess(Account account, EntityType type, Long entityId,
			Boolean isSharedDataset) {

		Set<PermissionGroup> currentGroups = new HashSet<PermissionGroup>();
		if (isSharedDataset) {
			currentGroups.addAll(permissionGroupDao.getPublicGroups());
		}
		currentGroups.addAll(permissionGroupDao.getGrantedGroups(account));
		EntityMap em = entityMapDao.get(account, currentGroups, type, entityId, hasRole(account, type.getAdminRole()));
		return em;
	}

	/**
	 * Given an admin account, return all the datasets that the admin user has access to
	 * 
	 * @param account
	 * @return
	 */
	private List<Dataset> getAdminDatasetAccess(Account account) {

		// admin user has access to all private and shared datasets in the query tool
		Set<DatasetStatus> privateAndSharedStatuses = new HashSet<DatasetStatus>();
		privateAndSharedStatuses.add(DatasetStatus.PRIVATE);
		privateAndSharedStatuses.add(DatasetStatus.SHARED);

		List<Dataset> datasets = datasetDao.getByStatuses(privateAndSharedStatuses);

		return datasets;
	}

	/**
	 * Given a non-admin account, return all the datasets that the non admin user has access to
	 * 
	 * @param account
	 * @return
	 */
	private List<Dataset> getNonAdminDatasetAccess(Account account) {

		Set<Long> datasetIds = new HashSet<Long>(); // this will be the set of dataset Ids the user has access to

		// ---------------------------- GET LIST OF EXPLICITLY GRANTED STUDY ACCESS (Not through permission group)
		// -----------------------------
		List<EntityMap> grantedStudyEntityMaps =
				listUserAccessEntities(account, EntityType.STUDY, PermissionType.READ, true);
		Set<Long> studyIds = new HashSet<Long>();
		if (grantedStudyEntityMaps != null && !grantedStudyEntityMaps.isEmpty()) {
			for (EntityMap study : grantedStudyEntityMaps) {
				studyIds.add(study.getEntityId());
			}
		}
		// user with explicit study access should be able to access private and shared datasets
		Set<DatasetStatus> privateAndSharedStatuses = new HashSet<DatasetStatus>();
		privateAndSharedStatuses.add(DatasetStatus.PRIVATE);
		privateAndSharedStatuses.add(DatasetStatus.SHARED);

		// add all the dataset IDs from the studies the current user has access to
		datasetIds.addAll(datasetDao.getDatasetIdsByStudyIds(studyIds, privateAndSharedStatuses));

		// ------------------------------ GET LIST OF ALL STUDIES THE USER HAS ACCESS TO (Includes public studies)
		// ---------------

		// get all the studies the user has access to, including ones in the public group
		List<EntityMap> allStudyEntityMaps = listUserAccess(account, EntityType.STUDY, false);
		if (allStudyEntityMaps != null && !allStudyEntityMaps.isEmpty()) {
			for (EntityMap study : allStudyEntityMaps) {
				studyIds.add(study.getEntityId());
			}
		}

		Set<DatasetStatus> sharedStatus = new HashSet<DatasetStatus>();

		// all users should be able to see shared datasets in public studies
		sharedStatus.add(DatasetStatus.SHARED);

		// add all the dataset IDs from the studies the current user has access to
		datasetIds.addAll(datasetDao.getDatasetIdsByStudyIds(studyIds, sharedStatus));

		List<Dataset> datasets = datasetDao.getByIds(datasetIds);
		return datasets;
	}

	/**
	 * @inheritDoc
	 */
	public List<Dataset> getDatasetAccess(Account account) {

		if (hasRole(account, RoleType.ROLE_ADMIN)
				|| hasRole(account, RoleType.ROLE_QUERY_ADMIN) || hasRole(account, RoleType.ROLE_STUDY_ADMIN)) {
			return getAdminDatasetAccess(account);
		} else {
			return getNonAdminDatasetAccess(account);
		}
	}

	/**
	 * @inheritDoc
	 */
	public QueryPermissions getQTPermissionsMap(Account account) {

		QueryPermissions queryPermissions = new QueryPermissions();

		List<Dataset> datasets = getDatasetAccess(account);

		Set<Long> studyIds = new HashSet<Long>();

		for (Dataset ds : datasets) {
			Long studyId = ds.getStudy().getId();
			studyIds.add(studyId);
		}

		// Go through maps
		for (Dataset ds : datasets) {
			// If study is not null, has forms and is private or shared
			if (ds != null && ds.getStudy() != null && ds.getStudy().getTitle() != null
					&& !ds.getDatasetDataStructure().isEmpty()) {
				String studyURI = StudyRDF.createStudyResource(ds.getStudy()).getURI();
				String datasetURI = DatasetRDF.createDatasetResource(ds).getURI();

				// Get all forms for this dataset
				Set<Long> formIdList = new HashSet<Long>();
				for (DatasetDataStructure dsds : ds.getDatasetDataStructure()) {
					formIdList.add(dsds.getDataStructureId());

					// Add FormResultPermission to queryPermission
					QueryPermissions.FormResultPermission existingFormResultPermission =
							queryPermissions.getFormResultPermissionByFormId(dsds.getDataStructureId());

					// if list has form id
					if (existingFormResultPermission != null) {
						QueryPermissions.StudyDataset studyDataset = new QueryPermissions.StudyDataset();
						studyDataset.setDatasetId(ds.getId());
						studyDataset.setDatasetURI(datasetURI);
						studyDataset.setStudyURI(studyURI);
						existingFormResultPermission.getStudyDatasets().add(studyDataset);
					}
					// if list does not have form id
					else {
						QueryPermissions.StudyDataset studyDataset = new QueryPermissions.StudyDataset();
						studyDataset.setDatasetId(ds.getId());
						studyDataset.setDatasetURI(datasetURI);
						studyDataset.setStudyURI(studyURI);

						QueryPermissions.FormResultPermission formResultPermission =
								new QueryPermissions.FormResultPermission();
						formResultPermission.setFormId(dsds.getDataStructureId());
						formResultPermission.getStudyDatasets().add(studyDataset);
						queryPermissions.getFormResultPermissions().add(formResultPermission);
					}

				}

				// Add StudyResult to queryPermissions getStudyResultPermissionByStudyURI

				QueryPermissions.StudyResultPermission existingStudyResultPermission =
						queryPermissions.getStudyResultPermissionByStudyURI(studyURI);

				// If list already has the study add the form uri's
				if (studyIds.contains(ds.getStudy().getId())) { // check if the study is in the study list that
																 // this
																 // account has access
					if (existingStudyResultPermission != null) {
						existingStudyResultPermission.getFormIds().addAll(formIdList);
					}
					// If list does not have study uri
					else {
						QueryPermissions.StudyResultPermission studyResultPermission =
								new QueryPermissions.StudyResultPermission();
						studyResultPermission.setStudyURI(studyURI);
						studyResultPermission.setFormIds(formIdList);
						queryPermissions.getStudyResultPermissions().add(studyResultPermission);
					}
				}
			}
		}

		// Adds the Role string to the object
		populateAccountRoles(queryPermissions, account);

		return queryPermissions;
	}

	private void populateAccountRoles(QueryPermissions qtPermissions, Account account) {

		qtPermissions.setHasAccessToAccount(false);
		qtPermissions.setHasAccessToGUID(false);
		qtPermissions.setHasAccessToProforms(false);
		qtPermissions.setHasAccessToProforms(false);
		qtPermissions.setHasAccessToRepository(false);
		qtPermissions.setHasAccessToWorkspace(false);
		qtPermissions.setHasAccessToDictionary(false);
		qtPermissions.setHasAccessToQuery(false);
		qtPermissions.setHasAccessToMetaStudy(false);

		// if Admin give them all permissions
		if (hasRole(account, RoleType.ROLE_ADMIN)) {
			qtPermissions.setHasAccessToAccount(true);
			qtPermissions.setHasAccessToGUID(true);
			qtPermissions.setHasAccessToProforms(true);
			qtPermissions.setHasAccessToProforms(true);
			qtPermissions.setHasAccessToRepository(true);
			qtPermissions.setHasAccessToWorkspace(true);
			qtPermissions.setHasAccessToDictionary(true);
			qtPermissions.setHasAccessToQuery(true);
			qtPermissions.setHasAccessToMetaStudy(true);
		} else {
			if (hasRole(account, RoleType.ROLE_USER)) {
				qtPermissions.setHasAccessToWorkspace(true);
				qtPermissions.setHasAccessToAccount(true);
			}

			// Proforms Permissions
			if (hasRole(account, RoleType.ROLE_PROFORMS) || hasRole(account, RoleType.ROLE_PROFORMS_ADMIN)) {
				qtPermissions.setHasAccessToProforms(true);
			}

			// GUID
			if (hasRole(account, RoleType.ROLE_GUID) || hasRole(account, RoleType.ROLE_GUID_ADMIN)) {
				qtPermissions.setHasAccessToGUID(true);
			}

			// Dictionary
			if (hasRole(account, RoleType.ROLE_DICTIONARY) || hasRole(account, RoleType.ROLE_DICTIONARY_ADMIN)) {
				qtPermissions.setHasAccessToDictionary(true);
			}

			// Repository
			if (hasRole(account, RoleType.ROLE_STUDY) || hasRole(account, RoleType.ROLE_STUDY_ADMIN)) {
				qtPermissions.setHasAccessToRepository(true);
			}

			if (hasRole(account, RoleType.ROLE_QUERY) || hasRole(account, RoleType.ROLE_QUERY_ADMIN)) {
				qtPermissions.setHasAccessToQuery(true);
			}

			if (hasRole(account, RoleType.ROLE_METASTUDY) || hasRole(account, RoleType.ROLE_METASTUDY_ADMIN)) {
				qtPermissions.setHasAccessToMetaStudy(true);
			}
		}
	}

	public Account accountActivation(Account acct) {

		if (!requestedAccountRoleCheck(acct) && !requestedAccountGroupCheck(acct)) {
			acct.setAccountStatus(AccountStatus.ACTIVE);
		}
		return acct;
	}

	public boolean requestedAccountGroupCheck(Account acct) {

		Set<PermissionGroupMember> permissionGroupMemberList = acct.getPermissionGroupMemberList();
		for (PermissionGroupMember pgm : permissionGroupMemberList) {
			if (pgm.getPermissionGroupStatus().equals(PermissionGroupStatus.PENDING)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks to see if they are any outstanding Account Role requests still pending; which will return true if there
	 * are.
	 * 
	 * @return
	 */
	public boolean requestedAccountRoleCheck(Account acct) {

		Set<AccountRole> accountRoleList = acct.getAccountRoleList();
		for (AccountRole ar : accountRoleList) {
			if (ar.getRoleStatus().equals(RoleStatus.PENDING))
				return true;
		}
		return false;
	}

	public void removePermissionGroupMember(PermissionGroupMember member) {
		permissionGroupMemberDao.remove(member.getId());
	}

	public void removePermissionGroupMembers(Set<PermissionGroupMember> members) {

		if (!ValUtil.isCollectionEmpty(members)) {
			permissionGroupMemberDao.removeAll(new ArrayList<PermissionGroupMember>(members));
		}
	}

	/**
	 * @inheritDoc
	 */
	public List<EntityMap> getOwnerEntityMaps(List<Long> ids, EntityType type) {

		return entityMapDao.getEntityMapsOfOwners(ids, type);
	}

	@Override
	public void deleteLoginPendingApprovedAccount() {

		logger.info("AccountManagerImpl->deleteUnusedAccount");
		List<Account> accList = accountDao.getApprovedAndPendignAccounts();
		logger.info("Account Criteria list size" + accList.size());
		// No of busienss days we want to calculate in this case we are calculating two business days
		int noOfDays = 2;
		for (Account a : accList) {
			logger.info("Account user_name:\t" + a.getUserName());
			logger.info("Account email:\t" + a.getUser().getEmail());
			if (BRICSTimeDateUtil.calculateBusinessDays(a.getLastUpdatedDate(), noOfDays)) {
				Set<PermissionGroupMember> permissionGroupMemberList = a.getPermissionGroupMemberList();
				List<EntityMap> emList = entityMapDao.getEntityForAccount(a);
				for (PermissionGroupMember pgm : permissionGroupMemberList) {
					logger.info("pgm" + pgm.getId());
					permissionGroupMemberDao.remove(pgm.getId());
				}

				for (EntityMap em : emList) {
					logger.info("em" + em.getId());
					entityMapDao.remove(em.getId());
				}

				accountDao.remove(a.getId());
				userDao.remove(a.getUser().getId());
			}
		}

	}

	/**
	 * @inheritDoc
	 */
	public boolean getDatasetAccess(Account account, AbstractDataset dataset, PermissionType permissionType) {

		Study study = dataset.getStudy();

		if (hasRole(account, RoleType.ROLE_ADMIN)) {
			return true;
		}

		EntityMap em = getExplicitDatasetAccess(account, EntityType.STUDY, study.getId(),
				DatasetStatus.SHARED.equals(dataset.getDatasetStatus()));

		// if entity map is null, return false, otherwise just check if entity map contains the given permission type
		return em != null && em.getPermission() != null ? em.getPermission().contains(permissionType) : false;
	}

	/**
	 * @inheritDoc
	 */
	public boolean getDatasetAccess(Account account, String studyTitle, String datasetName,
			PermissionType permissionType) {

		Dataset dataset = datasetDao.getDatasetByName(studyTitle, datasetName);
		return getDatasetAccess(account, dataset, permissionType);
	}

	@Override
	public List<EntityMap> listEntityAccessByAccount(Long accountId, EntityType type) {

		Account account = accountDao.get(accountId);

		List<EntityMap> emList = entityMapDao.getByAccountAndType(account, type);

		return emList;
	}

	/**
	 * @inheritDoc
	 */
	public User getUserByName(String firstName, String lastName) {

		User currentUser = userDao.getByName(firstName, lastName);
		if (currentUser != null) {
			Account currAccount = getAccountByUser(currentUser);
			if (currAccount.getAccountStatus().equals(AccountStatus.DENIED)) {
				currentUser = null;
			}
		}
		return currentUser;
	}

	@Override
	public List<User> getUserDetailsByIds(Set<Long> userIds) {

		List<User> userList = userDao.getUserDetailsByIds(userIds);

		return userList;
	}

	@Override
	public List<AccountHistory> getAccountHistory(Account account) {

		return accountHistoryDao.getAccountHistory(account);
	}
	
	@Override
	public List<AccountAdministrativeNote> getAccountAdministrativeNotes(Account account) {
		
		return accountAdministrativeNotesDao.getAccountAdministrativeNotes(account);
	}
	
	public Map<Long, String> getLatestAdminNoteForAccounts(List<Account> accounts) {
		return accountAdministrativeNotesDao.getLatestAdminNoteForAccounts(accounts);
	}

	@Override
	public void updateAccountPrivilegesExpiration() {
		List<Account> accounts = accountDao.getAllActive();

		for (Account account : accounts) {
			Set<AccountRole> accountRoles = account.getAccountRoleList();
			boolean isChanged = false;
			for (AccountRole accountRole : accountRoles) {
				RoleStatus roleStatus = accountRole.getRoleStatus();
				Date expirationDate = accountRole.getExpirationDate();
				Date currentDate = new Date();

				if (expirationDate != null) {
					if (roleStatus == RoleStatus.ACTIVE) {
						long diff = expirationDate.getTime() - currentDate.getTime();
						int days = Math.round(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
						if (days >= 0 && days <= ServiceConstants.EXPIRATION_SOON_DAYS) {
							logger.info("Account Expiration Job: Setting a role for " + account.getUserName()
									+ " to expiring soon.");
							accountRole.setRoleStatus(RoleStatus.EXPIRING_SOON);
							isChanged = true;
						}
					}

					// Expiring the account if the expiration date is in the past
					if (roleStatus == RoleStatus.ACTIVE || roleStatus == RoleStatus.EXPIRING_SOON) {
						if (currentDate.getTime() > expirationDate.getTime()) {
							logger.info("Account Expiration Job: Setting a role for " + account.getUserName()
									+ " to expired.");
							accountRole.setRoleStatus(RoleStatus.EXPIRED);
							isChanged = true;
						}
					}
				}
			}

			if (isChanged) {
				accountDao.save(account);
			}
		}
	}

	@Override
	public List<Account> getAccountsUpForRenewal() {

		return accountDao.getAccountsUpForRenewal();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AccountEmailReportSetting> getReportSettingsByType(EmailReportType reportType) {
		return accountEmailReportSettingDao.getByReportType(reportType);
	}

	public List<Account> getAccountByStatuses(AccountStatus... accountStatuses) {
		return accountDao.getByStatuses(accountStatuses);
	}

	@Override
	public Account getAccountWithEmailReport(Long accountId) {
		return accountDao.getAccountWithEmailReport(accountId);
	}

	public boolean checkUniqueFileName(String fileName, Long userId) {
		FileType accountDocumentationType = staticManager.getAdminFileTypeByName(ServiceConstants.FILE_TYPE_ACCOUNT);
		List<UserFile> userFiles = userFileDao.getByUserId(userId, accountDocumentationType);
		boolean fileNameExists = userFiles.parallelStream().map(UserFile::getName).anyMatch(fileName::equals);
		return !fileNameExists;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean hasAccessToGuidTool(Account account) {
		for (AccountRole accountRole : account.getAccountRoleList()) {
			if (accountRole.getIsActive()) {
				switch (accountRole.getRoleType()) {
					case ROLE_GUID:
					case ROLE_GUID_ADMIN:
					case ROLE_ADMIN:
						return true;
					default:
						break;
				}
			}
		}

		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ElectronicSignature saveElectronicSignature(ElectronicSignature electronicSignature) {
		return electronicSignatureDao.save(electronicSignature);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public TwoFactorAuthentication saveTwoFactorAuthentication(TwoFactorAuthentication TwoFa) {
		return twoFactorAuthenticationDao.save(TwoFa);
	}
	
	public User saveUserChanges(User user) {
		return userDao.save(user);
	}
	
}
