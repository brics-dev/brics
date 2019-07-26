
package gov.nih.tbi.commons.service;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.service.complex.BaseManagerImpl;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.account.ws.AuthenticationProvider;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.dictionary.model.UpdateFormStructureReferencePayload;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.ws.DictionaryProvider;
import gov.nih.tbi.repository.ws.RepositoryProvider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Creates, maintains and retrieves the appropriate provider for the user based on the disease. A mapping of Providers
 * is kept for each type of provider, and a user must provide a diseaseId to specify which provider
 * 
 * NOTE: A User can provide a disease type of -1 to use an unspecified provider
 * 
 */
@Service
@Scope("session")
public class WebServiceManagerImpl extends BaseManagerImpl implements WebServiceManager {

	private static final long serialVersionUID = 3793926168999800836L;
	static Logger logger = Logger.getLogger(WebServiceManagerImpl.class);

	@Autowired
	ModulesConstants modulesConstants;

	private Map<Long, AccountProvider> accountProvider = new LinkedHashMap<Long, AccountProvider>();
	private Map<Long, AuthenticationProvider> authenticationProvider =
			new LinkedHashMap<Long, AuthenticationProvider>();
	private Map<Long, DictionaryProvider> dictionaryProvider = new LinkedHashMap<Long, DictionaryProvider>();
	private Map<Long, RepositoryProvider> repositoryProvider = new LinkedHashMap<Long, RepositoryProvider>();
	private Account account;

	public AccountProvider getAccountProvider(Account account, Long diseaseId) {

		// Check to see if the passed in account is different than what this provider was initially created with
		boolean differentAccount = false;

		if (!account.equals(this.account)) {
			differentAccount = true;
		}

		if (accountProvider.get(diseaseId) == null || differentAccount) {
			try {
				logger.debug("Creating new Account Provider!");

				accountProvider.put(diseaseId, new AccountProvider(modulesConstants.getModulesAccountURL(diseaseId),
						account.getUserName(), getHash2(account)));
				this.account = account;

				return accountProvider.get(diseaseId);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			logger.debug("Reusing Account Provider!");

			return accountProvider.get(diseaseId);
		}
	}

	public AuthenticationProvider getAuthenticationProvider(Account account, Long diseaseId) {

		// Check to see if the passed in account is different than what this provider was initially created with
		boolean differentAccount = false;

		if (!account.equals(this.account)) {
			differentAccount = true;
		}

		if (authenticationProvider.get(diseaseId) == null || differentAccount) {
			try {
				logger.debug("Creating new Authentication Provider!");

				authenticationProvider.put(diseaseId, new AccountProvider(
						modulesConstants.getModulesAccountURL(diseaseId), account.getUserName(), getHash2(account)));

				this.account = account;

				return authenticationProvider.get(diseaseId);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			logger.debug("Reusing Authentication Provider!");

			return authenticationProvider.get(diseaseId);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void addToPermissionGroup(Account account, String groupName, EntityType type, Long id,
			PermissionType permission) throws MalformedURLException {

		Set<Long> ddtKeys = modulesConstants.getModulesDDTMap().keySet();

		for (Long diseaseId : ddtKeys) {
			if (!modulesConstants.isDuplicateModule(diseaseId)) {
				AccountProvider accountProvider = new AccountProvider(modulesConstants.getModulesAccountURL(diseaseId),
						modulesConstants.getAdministratorUsername(),
						HashMethods.getServerHash(modulesConstants.getAdministratorUsername(),
								modulesConstants.getAdministratorPassword()));

				if (accountProvider != null && dictionaryProvider != null) {
					PermissionGroup group = accountProvider.getPermissionGroup(groupName);
					if (group != null) {
						accountProvider.registerEntity(null, type, id, PermissionType.READ, group.getId());
					}
				}
			}
		}
	}

	public DictionaryProvider getDictionaryProvider(Account account, Long diseaseId) {

		// Check to see if the passed in account is different than what this provider was initially created with
		boolean differentAccount = false;

		if (!account.equals(this.account)) {
			differentAccount = true;
		}

		if (dictionaryProvider.get(diseaseId) == null || differentAccount) {
			try {
				logger.debug("Creating new Dictionary Provider!");

				dictionaryProvider.put(diseaseId, new DictionaryProvider(modulesConstants.getModulesDDTURL(diseaseId),
						modulesConstants.getModulesAccountURL(diseaseId), account.getUserName(), getHash2(account)));

				this.account = account;

				return dictionaryProvider.get(diseaseId);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			logger.debug("Reusing Dictionary Provider!");

			return dictionaryProvider.get(diseaseId);
		}
	}

	public RepositoryProvider getRepositoryProvider(Account account, Long diseaseId) {

		// Check to see if the passed in account is different than what this provider was initially created with
		boolean differentAccount = false;

		if (!account.equals(this.account)) {
			differentAccount = true;
		}

		if (repositoryProvider.get(diseaseId) == null || differentAccount) {
			try {
				logger.debug("Creating new Repository Provider!");
				logger.debug("Disease ID: " + diseaseId);
				logger.debug("URL: " + modulesConstants.getModulesAccountURL(diseaseId));
				logger.debug("Password: " + modulesConstants.getSaltedAdministratorPassword(diseaseId));
				
				repositoryProvider.put(diseaseId,
						new RepositoryProvider(modulesConstants.getModulesAccountURL(diseaseId),
								modulesConstants.getAdministratorUsername(),
								HashMethods.getServerHash(modulesConstants.getAdministratorUsername(),
										modulesConstants.getSaltedAdministratorPassword(diseaseId))));

				this.account = account;

				return repositoryProvider.get(diseaseId);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			logger.debug("Reusing Repository Provider!");

			return repositoryProvider.get(diseaseId);
		}
	}

	/****************************** COMBINED WEB SERVICE CALLS (SOAP) ***************************************/

	// /**
	// * @throws MalformedURLException
	// * @inheritDoc
	// */
	// @Override
	// public Account getEntityOwnerAccount(Account account, Long entityId, EntityType type) throws
	// MalformedURLException
	// {
	//
	// Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();
	//
	// // Case: There is only 1 account module listed
	// if (keys.size() == 1)
	// {
	// return getAccountProvider(account, ServiceConstants.DEFAULT_PROVIDER).getEntityOwnerAccount(entityId, type);
	// }
	//
	// Account owner = null;
	//
	// for (Long k : keys)
	// {
	// AccountProvider anonAccountProvider = new AccountProvider(modulesConstants.getModulesAccountURL(k));
	// owner = anonAccountProvider.getEntityOwnerAccount(entityId, type);
	// if (owner != null)
	// {
	// break;
	// }
	// }
	//
	// return owner;
	//
	// }
	//
	// /**
	// * @throws MalformedURLException
	// * @inheritDoc
	// */
	// @Override
	// public List<EntityMap> listEntityAccess(Account account, Long entityId, EntityType type)
	// throws MalformedURLException
	// {
	//
	// Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();
	//
	// // Case: There is only 1 account module listed (Listed at -1)
	// if (keys.size() == 1)
	// {
	// List<EntityMap> list = getAccountProvider(account, ServiceConstants.DEFAULT_PROVIDER).listEntityAccess(
	// entityId, type);
	// for (EntityMap em : list)
	// {
	// if (em.getAccount() != null)
	// {
	// em.getAccount().setDiseaseKey(ServiceConstants.DEFAULT_PROVIDER.toString());
	// }
	// if (em.getPermissionGroup() != null)
	// {
	// em.getPermissionGroup().setDiseaseKey(ServiceConstants.DEFAULT_PROVIDER.toString());
	// }
	// }
	// return list;
	// }
	//
	// List<EntityMap> list = new ArrayList<EntityMap>();
	//
	// for (Long k : keys)
	// {
	// // This is a case for more than one account modules so -1 should be a duplicate (the default key)
	// if (!k.equals(ServiceConstants.DEFAULT_PROVIDER))
	// {
	// AccountProvider anonAccountProvider = new AccountProvider(modulesConstants.getModulesAccountURL(k));
	// List<EntityMap> toAdd = anonAccountProvider.listEntityAccess(entityId, type);
	// if (toAdd != null)
	// {
	// for (EntityMap em : toAdd)
	// {
	// if (em.getAccount() != null)
	// {
	// em.getAccount().setDiseaseKey(k.toString());
	// }
	// if (em.getPermissionGroup() != null)
	// {
	// em.getPermissionGroup().setDiseaseKey(k.toString());
	// }
	// }
	// list.addAll(toAdd);
	// }
	// }
	// }
	//
	// return list;
	// }

	/**
	 * @inheritDoc
	 */
	@Override
	public void setDataStoreInfoArchive(Account account, FormStructure structure, boolean isArchived) {

		Set<Long> keys = modulesConstants.getModulesSTMap().keySet();

		// Case: There is only 1 account modules listed in modules.properties (Listed at -1)
		if (keys.size() == 1) {
			getRepositoryProvider(account, ServiceConstants.DEFAULT_PROVIDER).setDataStoreInfoArchive(structure.getId(),
					isArchived);
			return;
		}
		// Case: Write to all ST URLs

		for (Long k : keys) {
			if (!modulesConstants.isDuplicateModule(k) && !k.equals(ServiceConstants.DEFAULT_PROVIDER)) {
				getRepositoryProvider(account, k).setDataStoreInfoArchive(structure.getId(), isArchived);
			}
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean createTableFromDataStructure(Account account, StructuralFormStructure datastructure)
			throws SQLException, Exception {

		Set<Long> keys = modulesConstants.getModulesSTMap().keySet();
		if (logger.isDebugEnabled()) {
			logger.debug(
					"Creating data structure tables for " + datastructure.getTitle() + " at the following locations: ");
			for (Long k : keys) {
				logger.debug(k + ": " + modulesConstants.getModulesSTURL(k));
			}
		}

		// Case: There is only 1 account modules listed in modules.properties (Listed at -1)
		if (keys.size() == 1) {
			boolean success = getRepositoryProvider(account, ServiceConstants.DEFAULT_PROVIDER)
					.createTableFromDataStructure(datastructure);
			logger.debug("(Single) Repository " + modulesConstants.getModulesSTURL(ServiceConstants.DEFAULT_PROVIDER)
					+ " reported " + success);
			return success;
		}

		boolean returnValue = true;
		// Case: There is more than one ST modules so we want to publish to all of them.
		for (Long k : keys) {

			if (!modulesConstants.isDuplicateModule(k) && !k.equals(ServiceConstants.DEFAULT_PROVIDER)) {
				boolean success = getRepositoryProvider(account, k).createTableFromDataStructure(datastructure);
				logger.debug("Repository " + modulesConstants.getModulesSTURL(k) + " reported " + success);
				if (!success) {
					returnValue = success;
				}
			}
		}
		return returnValue;
	}

	public boolean updateFormStructureReferences(Account account, UpdateFormStructureReferencePayload payload)
			throws SQLException {

		Set<Long> keys = modulesConstants.getModulesSTMap().keySet();
		if (logger.isDebugEnabled()) {
			logger.debug("Updating form structure references in the repository for " + payload.getOldFormStructureId()
					+ " at the following locations: ");
			for (Long k : keys) {
				logger.debug(k + ": " + modulesConstants.getModulesSTURL(k));
			}
		}

		// Case: There is only 1 account modules listed in modules.properties (Listed at -1)
		if (keys.size() == 1) {
			boolean success = getRepositoryProvider(account, ServiceConstants.DEFAULT_PROVIDER)
					.updateFormStructureReferences(payload);
			logger.debug("(Single) Repository " + modulesConstants.getModulesSTURL(ServiceConstants.DEFAULT_PROVIDER)
					+ " reported " + success);
			return success;
		}

		boolean returnValue = true;
		// Case: There is more than one ST modules so we want to publish to all of them.
		for (Long k : keys) {

			if (!modulesConstants.isDuplicateModule(k) && !k.equals(ServiceConstants.DEFAULT_PROVIDER)) {
				boolean success = getRepositoryProvider(account, k).updateFormStructureReferences(payload);
				logger.debug("Repository " + modulesConstants.getModulesSTURL(k) + " reported " + success);
				if (!success) {
					returnValue = success;
				}
			}
		}
		return returnValue;
	}

	/****************************** COMBINED WEB SERVICE CALLS (RESTFUL) ***************************************/

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	@Override
	public Account getEntityOwnerAccountRestful(Account account, Long entityId, EntityType type)
			throws MalformedURLException, UnsupportedEncodingException {

		Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();

		// Case: There is only 1 account module listed
		if (keys.size() == 1) {
			RestAccountProvider anonAccountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER), null);
			return anonAccountProvider.getEntityOwnerAccount(entityId, type);
		}

		Account owner = null;

		for (Long k : keys) {
			RestAccountProvider anonAccountProvider =
					new RestAccountProvider(modulesConstants.getModulesAccountURL(k), null);
			owner = anonAccountProvider.getEntityOwnerAccount(entityId, type);
			if (owner != null && owner.getId() != null) {
				break;
			}
		}

		return owner;

	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	@Override
	public List<EntityMap> listEntityAccessRestful(Account account, Long entityId, EntityType type)
			throws MalformedURLException, UnsupportedEncodingException {

		Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();

		// Case: There is only 1 account module listed (Listed at -1)
		if (keys.size() == 1) {
			RestAccountProvider anonAccountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER), null);
			List<EntityMap> list = anonAccountProvider.listEntityAccess(entityId, type);
			for (EntityMap em : list) {
				if (em.getAccount() != null) {
					em.getAccount().setDiseaseKey(ServiceConstants.DEFAULT_PROVIDER.toString());
				}
				if (em.getPermissionGroup() != null) {
					em.getPermissionGroup().setDiseaseKey(ServiceConstants.DEFAULT_PROVIDER.toString());
				}
			}
			return list;
		}

		List<EntityMap> list = new ArrayList<EntityMap>();

		for (Long k : keys) {
			// This is a case for more than one account modules so -1 should be a duplicate (the default key)
			if (!modulesConstants.isDuplicateModule(k) && !k.equals(ServiceConstants.DEFAULT_PROVIDER)) {
				RestAccountProvider anonAccountProvider =
						new RestAccountProvider(modulesConstants.getModulesAccountURL(k), null);
				List<EntityMap> toAdd = anonAccountProvider.listEntityAccess(entityId, type);
				if (toAdd != null) {
					for (EntityMap em : toAdd) {
						if (em.getAccount() != null) {
							em.getAccount().setDiseaseKey(k.toString());
						}
						if (em.getPermissionGroup() != null) {
							em.getPermissionGroup().setDiseaseKey(k.toString());
						}
					}
					list.addAll(toAdd);
				}
			}
		}

		return list;
	}

	/**
	 * @throws IOException
	 * @throws HttpException
	 * @inheritDoc
	 */
	@Override
	public void unregisterEntityListRestful(Account account, List<EntityMap> removedEntityMapList)
			throws HttpException, IOException {

		Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();

		// Case: There is only 1 account module listed (Listed at -1)
		if (keys.size() == 1) {
			RestAccountProvider anonAccountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER), null);
			anonAccountProvider.unregisterEntityList(removedEntityMapList);
			return;
		}

		for (Long k : keys) {
			for (EntityMap em : removedEntityMapList) {
				if (k.equals(em.getDiseaseKey())) {
					RestAccountProvider anonAccountProvider =
							new RestAccountProvider(modulesConstants.getModulesAccountURL(k), null);
					anonAccountProvider.unregisterEntity(em.getId());
				}
			}
		}

	}

	/**
	 * @throws IOException
	 * @throws HttpException
	 * @inheritDoc
	 */
	@Override
	public void registerEntityListRestful(Account account, List<EntityMap> entityList, String[] proxyTicketArr)
			throws HttpException, IOException {

		Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();
		int index = 0;
		// Case: There is only 1 account module listed (Listed at -1)
		if (keys.size() == 1) {
			for (EntityMap em : entityList) {
				RestAccountProvider anonAccountProvider = new RestAccountProvider(
						modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER),
						proxyTicketArr[index]);
				if (em.getAccount() != null) {
					anonAccountProvider.registerEntity(em.getAccount().getId(), em.getType(), em.getEntityId(),
							em.getPermission(), null);
				} else if (em.getPermissionGroup() != null) {
					anonAccountProvider.registerEntity(null, em.getType(), em.getEntityId(), em.getPermission(),
							em.getPermissionGroup().getId());
				} else if (em.getPermissionGroup() != null) {
					anonAccountProvider.registerEntity(null, em.getType(), em.getEntityId(), em.getPermission(),
							em.getPermissionGroup().getId());
				}
				index++;
			}
			return;
		}

		for (Long k : keys) {
			// Only send the keys with a matching disease key
			// diseaseList: The list of entities to be written to a given disease.
			List<EntityMap> diseaseList = new ArrayList<EntityMap>();
			for (EntityMap em : entityList) {
				if (k.equals(em.getDiseaseKey())) {
					diseaseList.add(em);
				}
			}

			if (!diseaseList.isEmpty()) {

				for (EntityMap em : diseaseList) {
					RestAccountProvider anonAccountProvider =
							new RestAccountProvider(modulesConstants.getModulesAccountURL(k), proxyTicketArr[index]);
					if (em.getAccount() != null) {
						anonAccountProvider.registerEntity(em.getAccount().getId(), em.getType(), em.getEntityId(),
								em.getPermission(), null);
					} else if (em.getPermissionGroup() != null) {
						anonAccountProvider.registerEntity(null, em.getType(), em.getEntityId(), em.getPermission(),
								em.getPermissionGroup().getId());
					} else if (em.getPermissionGroup() != null) {
						anonAccountProvider.registerEntity(null, em.getType(), em.getEntityId(), em.getPermission(),
								em.getPermissionGroup().getId());
					}
					index++;
				}

			}
		}

	}

	/**
	 * NOTE: This function does not function properly. It pulls usernames from all account modules under the false
	 * assumption that it can do so anonymously.
	 * 
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	@Override
	public List<Account> getAccountListByRoleRestful(Account account, RoleType role)
			throws MalformedURLException, UnsupportedEncodingException {

		Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();

		// Case: There is only 1 account module listed (Listed at -1)
		if (keys.size() == 1) {

			RestAccountProvider anonAccountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER), null);
			List<Account> list = anonAccountProvider.getAccountsWithRole(role.getName());
			for (Account a : list) {
				a.setDiseaseKey(ServiceConstants.DEFAULT_PROVIDER.toString());
			}
			return list;

		}

		List<Account> list = new ArrayList<Account>();
		for (Long k : keys) {
			// This is a case for more than one account modules so -1 should be a duplicate (the default key)
			if (!modulesConstants.isDuplicateModule(k) && !k.equals(ServiceConstants.DEFAULT_PROVIDER)) {
				RestAccountProvider anonAccountProvider =
						new RestAccountProvider(modulesConstants.getModulesAccountURL(k), null);
				List<Account> toAdd = anonAccountProvider.getAccountsWithRole(role.getName());
				if (toAdd != null) {
					for (Account a : toAdd) {
						a.setDiseaseKey(k.toString());
					}
					list.addAll(toAdd);
				}
			}
		}

		return list;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @inheritDoc
	 */
	@Override
	public List<PermissionGroup> getPrivatePermssionGroupsRestful(Account account)
			throws MalformedURLException, UnsupportedEncodingException {

		Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();

		// Case: There is only 1 account module listed (Listed at -1)
		if (keys.size() == 1) {

			RestAccountProvider anonAccountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER), null);
			List<PermissionGroup> list = anonAccountProvider.getPrivatePermissionGroups();
			for (PermissionGroup pg : list) {
				pg.setDiseaseKey(ServiceConstants.DEFAULT_PROVIDER.toString());
			}
			return list;

		}

		List<PermissionGroup> list = new ArrayList<PermissionGroup>();
		for (Long k : keys) {
			// This is a case for more than one account modules so -1 should be a duplicate (the default key)
			if (!modulesConstants.isDuplicateModule(k) && !k.equals(ServiceConstants.DEFAULT_PROVIDER)) {
				RestAccountProvider anonAccountProvider =
						new RestAccountProvider(modulesConstants.getModulesAccountURL(k), null);
				List<PermissionGroup> toAdd = anonAccountProvider.getPrivatePermissionGroups();
				if (toAdd != null) {
					for (PermissionGroup pg : toAdd) {
						pg.setDiseaseKey(k.toString());
					}
					list.addAll(toAdd);
				}
			}
		}

		return list;
	}

	/**
	 * @throws IOException
	 * @throws HttpException
	 * @inheritDoc
	 */
	public void registerEntityToPermissionGroup(Account account, String groupName, EntityType type, Long id,
			PermissionType permission) throws HttpException, IOException {

		Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();

		// Case: There is only one accountMap
		if (keys.size() == 1) {
			RestAccountProvider anonAccountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER), null);
			PermissionGroup group = anonAccountProvider.getPermissionGroup(groupName);
			if (group != null) {
				anonAccountProvider = new RestAccountProvider(
						modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER), null);
				anonAccountProvider.registerEntity(null, type, id, PermissionType.READ, group.getId());
			}
		}

		for (Long k : keys) {
			// This is a case for more than one account modules so -1 should be a duplicate (the default key)
			if (!modulesConstants.isDuplicateModule(k) && !k.equals(ServiceConstants.DEFAULT_PROVIDER)) {
				RestAccountProvider anonAccountProvider =
						new RestAccountProvider(modulesConstants.getModulesAccountURL(k), null);
				PermissionGroup group = anonAccountProvider.getPermissionGroup(groupName);
				if (group != null) {
					anonAccountProvider = new RestAccountProvider(modulesConstants.getModulesAccountURL(k), null);
					anonAccountProvider.registerEntity(null, type, id, PermissionType.READ, group.getId());
				}
			}
		}
	}

	public void unregisterEntityToPermissionGroup(String groupName, EntityType type, Long id, PermissionType permission)
			throws HttpException, IOException {

		Set<Long> keys = modulesConstants.getModulesAccountMap().keySet();

		// Case: There is only one accountMap
		if (keys.size() == 1) {
			RestAccountProvider anonAccountProvider = new RestAccountProvider(
					modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER), null);
			PermissionGroup group = anonAccountProvider.getPermissionGroup(groupName);
			if (group != null) {
				anonAccountProvider = new RestAccountProvider(
						modulesConstants.getModulesAccountURL(ServiceConstants.DEFAULT_PROVIDER), null);
				anonAccountProvider.unregisterEntityFromGroup(id, type, group.getId());
			}
		}

		for (Long k : keys) {
			// This is a case for more than one account modules so -1 should be a duplicate (the default key)
			if (!modulesConstants.isDuplicateModule(k) && !k.equals(ServiceConstants.DEFAULT_PROVIDER)) {
				RestAccountProvider anonAccountProvider =
						new RestAccountProvider(modulesConstants.getModulesAccountURL(k), null);
				PermissionGroup group = anonAccountProvider.getPermissionGroup(groupName);
				if (group != null) {
					anonAccountProvider = new RestAccountProvider(modulesConstants.getModulesAccountURL(k), null);
					anonAccountProvider.unregisterEntityFromGroup(id, type, group.getId());
				}
			}
		}
	}
}
