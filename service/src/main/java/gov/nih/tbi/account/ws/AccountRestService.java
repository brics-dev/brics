
package gov.nih.tbi.account.ws;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.dao.EntityMapDao;
import gov.nih.tbi.account.model.AccountRestServiceModel.AccountsMapByUsernameWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.AccountsWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.EntityMapsWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.LongListWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.PerGrpMemBool;
import gov.nih.tbi.account.model.AccountRestServiceModel.PermissionGroupMemberWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.PermissionGroupsWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.StringListWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.UserList;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.account.model.jaxb.EntityMapList;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.util.CommonUtils;
//import gov.nih.tbi.commons.ws.cxf.AbstractRestService;
//import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.semantic.model.QueryPermissions;

@Path("/account/")
public class AccountRestService extends AbstractRestService {

	private static final Logger logger = Logger.getLogger(AccountRestService.class);
	private static final Response DEFAULT_HEAD_RESPONSE = Response.noContent().header("Pragma", "no-cache")
			.header("Cache-Control", "no-store, no-cache, must-revalidate").header("X-Content-Type-Options", "nosniff")
			.header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials", "false")
			.header("Access-Control-Allow-Methods", "HEAD, GET, POST, DELETE")
			.header("Access-Control-Allow-Headers", "Content-Type, Content-Range, Content-Disposition")
			.header("Vary", "Accept").build();

	@Autowired
	private AccountManager accountManager;

	@Autowired
	private AccountDao accountDao;

	@Autowired
	private EntityMapDao entityMapDao;

	@Autowired
	protected SessionAccount sessionAccount;

	@Autowired
	protected ModulesConstants modulesConstants;

	@Resource
	private WebServiceContext wsContext;

	// private String userName = "administrator";

	private String hash1 = "ae29adeefda7a481f509783aaa8c835505602efec9ceaf93c1b3d14bc225d071";

	private String hash2 = "236e9aa353cd397d4739be546802c664229b90d333f7ba334ef544e3db9bfb7b";

	/***************************************************************************************************/

	//

	/**
	 * Responses to HTTP HEAD request made to the Account Rest Service's root path. Mostly used for situations where
	 * there is a need to transparently extend a users CAS session. A HEAD request offers a lower overhead than a
	 * regular response from Struts. The headers in the response also includes signals to networking equipment and
	 * browsers to not cache the response.
	 * 
	 * @return
	 */
	@HEAD
	public Response handleHeadRequests() {
		return DEFAULT_HEAD_RESPONSE;
	}

	/**
	 * When given an account number, the method will return an account in xml. If the account doesn't exist, a blank
	 * account will be returned This method should only be accessed by the admin as it shows all the information of a
	 * user.
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Path("get/{id}")
	// URI WORK
	@Produces("text/xml")
	public Account getAccountDetailsById(@PathParam("id") Long id) {

		try {

			Account requestingAccount = getAuthenticatedAccount();
			logger.debug(requestingAccount.getUserName() + " called getAccountDetailsById. id = " + id);

			Account account = accountManager.getAccountByUserName(requestingAccount.getUserName());

			// This type of security no longer works. In order to add permissions to all modules we need to return
			// account objects to anyone with a proper proxy ticket (all but anon user).
			// if (account != null && (accountManager.hasRole(account, RoleType.ROLE_ADMIN) ||
			// accountManager.hasRole(account, RoleType.ROLE_DICTIONARY) || account.getId().equals(id)))
			if (account != null && !ANONYMOUS_USER_NAME.equals(account.getUserName())) {
				Account returnedAccount = accountManager.getAccount(account.getUser(), id);
				if (returnedAccount != null) {
					logger.debug("Returning account: " + returnedAccount.getUserName());
					return returnedAccount;
				} else {
					Account fake = new Account();
					return fake;
				}
			}
			logger.debug("Requesting Account " + requestingAccount.getUserName()
					+ " does not have access to account with id: " + id);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * A user is able to access their own account information once signed in. NOTE: IN ORDER TO USE THIS METHOD SOMEONE
	 * MUST BE LOGGED IN!!!!!!!
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("get/")
	// URI WORK
	@Produces("text/xml")
	public Account getAccountDetails() throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getAccountDetails()");

		Account account = accountManager.getAccountByUserName(requestingAccount.getUserName());
		return account;
	}

	/**
	 * When given a role, a list is returned of all account that has this role. For security reasons, only admins should
	 * only have access to method. A 500 exception is thrown if a user enters the wrong address.
	 * 
	 * @param role
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("role/{role}")
	// URI WORK
	@Produces("text/xml")
	public AccountsWrapper getAccountsWithRole(@PathParam("role") String role) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.info(requestingAccount.getUserName() + " requesting accounts with role: " + role);

		AccountsWrapper acct = new AccountsWrapper();

		if (role == null) {
			throw new RuntimeException("Argument 'role' cannot be null");
		}
		RoleType roleType = RoleType.getByName(role);
		if (roleType == null) {
			throw new RuntimeException("Unrecognized role: " + role);
		}

		if (ANONYMOUS_USER_NAME.equals(requestingAccount.getUserName())) {
			throw new RuntimeException("Web Service can only be accessed by logged in users");
		}
		if (!accountManager.hasRole(requestingAccount, roleType)) {
			throw new RuntimeException(
					"User " + requestingAccount.getUserName() + " does not have access to " + role + " user list.");
		}

		List<Account> list = accountManager.getAccountListByRole(roleType, true);
		acct.addAll(list);
		return acct;

	}

	/**
	 * An Entity Map Getter. Providing an accountId of null, defaults to the anonymous user. An non-anuthenticated call
	 * also defaults to the anonymous user.
	 * 
	 * @param entityType
	 * @param entityId
	 * @param includePermissionGroups
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("entityMap/getAccess")
	// URI Works: 11/28
	@Produces("text/xml")
	public Response getAccess(@QueryParam("accountId") Long accountId, @QueryParam("entityType") String entityType,
			@QueryParam("entityId") Long entityId,
			@DefaultValue("true") @QueryParam("includePerGrps") boolean includePermissionGroups)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();

		Account account = accountManager.getAccount(requestingAccount.getUser(), accountId);
		logger.info(requestingAccount.getUserName() + " requesting access for entityType: " + entityType
				+ ", entityId: " + entityId + ", includePerGrps: " + includePermissionGroups + ", user: "
				+ account.getUserName());

		// Security checks
		if (entityType == null) {
			throw new RuntimeException("Argument 'type' cannot be null");
		}

		if (!accountManager.hasRole(requestingAccount, RoleType.ROLE_ADMIN)
				&& !requestingAccount.getId().equals(account.getId())) {
			logger.info("Authenticated user in context is User :" + account.getUserName() + " and account making rest call is :: " + requestingAccount.getUserName());
			logger.info("Sending response 403 since the logged in user is no more in context, and context now has anonymous user");
			return Response.ok().status(403).build();
		}

		EntityType type = EntityType.valueOf(entityType);
		EntityMap map = accountManager.getAccess(account, type, entityId, includePermissionGroups);
		if (map == null) {
			logger.info("EM is null");
		} else {
			logger.info(map.toString());
		}
		
		return Response.ok(map, MediaType.APPLICATION_XML).build();
	}

	@GET
	@Path("getOwner")
	// URI Works 11/28
	@Produces("text/xml")
	public Account getEntityOwnerAccount(@QueryParam("entityId") Long entityId, @QueryParam("entityType") String type)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getEntityOwnerAccount with parameters: entityId="
				+ entityId + ", entityType=" + type);

		// if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
		// {
		// throw new RuntimeException("Failed to validate Username.");
		// }
		EntityType entityType = EntityType.valueOf(type);
		EntityMap em = entityMapDao.getOwnerEntityMap(entityId, entityType);
		if (em != null) {
			Account account = accountDao.get(em.getAccount().getId());
			return account;
		}
		Account account = new Account();
		return account;
	}

	/**
	 * Unlike unregisterEntityById the EntityMaps are deleted using their EntityType and EntityID instead of purely
	 * their id in the database. Calling this method could result in multiple entries in the database being deleted.
	 * 
	 * @param id
	 * @param type
	 * @throws UnsupportedEncodingException
	 */
	@DELETE
	@Path("unregister/{entityType}/{id}")
	public void unregisterEntity(@PathParam("id") Long id, @PathParam("entityType") String type)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " calling unregisterEntity with id: " + id + ", and entityType: "
				+ type);

		// if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
		// {
		// throw new RuntimeException("Failed to validate Username.");
		// }
		EntityType entityType = EntityType.valueOf(type);

		accountManager.unregisterEntity(entityType, id);

	}

	/**
	 * EntityMaps are unregistered based on the id in the database. If the id passed in doesn't exist, then nothing is
	 * deleted and the response returned will be 204 - Not found
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@DELETE
	@Path("entityMap/unregister/{entityId}")
	public Response unregisterEntityById(@PathParam("entityId") Long id) throws UnsupportedEncodingException {

		// @QueryParam("id") Long id
		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " calling unregisterEntityBy is with id: " + id);

		// if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
		// {
		// throw new RuntimeException("Failed to validate Username.");
		// }

		// Account requestingAccount = accountManager.getAccountByUserName(userLogin.getUserName());
		// EntityMap em = entityMapDao.get(id);
		// if (!accountManager.getAccess(requestingAccount, em.getType(), em.getEntityId(), PermissionType.ADMIN))
		// {
		// throw new RuntimeException("User does not have privileges to unregister entities for :"
		// + em.getType().getName() + em.getEntityId());
		// }
		// for (Long id : ids)
		// {

		if (entityMapDao.exists(id)) {

			entityMapDao.remove(id);

			return Response.ok().build();
		}

		return Response.ok().status(204).build();

		//
	}

    /**
	 * EntityMaps are unregistered based on the IDs passed in the POST body. If any of the ID passed in doesn't exist,
	 * it will be ignored.
	 * 
	 * @param ids - A list of entity map IDs to be deleted from the database
	 * @return No content response.
	 * @throws WebApplicationException When an error response needs to be sent back to the client.
	 */
	@POST
    @Path("entityMap/unregister/list")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response unregisterEntityById(@FormParam("ids") List<Long> ids) throws WebApplicationException {
		try {
			Account requestingAccount = getAuthenticatedAccount();
			logger.debug(requestingAccount.getUserName() + " calling unregisterEntityById on a list of ids");

			if ((ids != null) && !ids.isEmpty()) {
				logger.info("Attempting to remove " + ids.size() + " entity map(s) from the database.");
				accountManager.unregisterEnity(ids);
			}
		} catch (UnsupportedEncodingException uee) {
			String msg = "Account is not authorized to unregister entities.";
			logger.error(msg, uee);
			throw new ForbiddenException(msg, uee);
		} catch (HibernateException he) {
			String msg = "Couldn't delete listed entities (" + ids.toString() + ") because of a database error.";
			logger.error(msg, he);
			throw new InternalServerErrorException(msg, he);
		} catch (Exception e) {
			String msg = "Couldn't delete listed entities because of an error.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}


		return Response.noContent().build();
	}
	
	/**
	 * EntityMaps are unregistered based on the id in the database. If the id passed in doesn't exist, then nothing is
	 * deleted.
	 * 
	 * @deprecated Use the POST version of this method ({@link #unregisterEntityById(List)}). This way we don't have to
	 *             worry about URL length limits, and there is better information hiding with POST over TLS.
	 * @param id - The list of entity map IDs to be deleted.
	 * @return No content response.
	 * @throws WebApplicationException When there are any errors, or any error response that need to be sent back.
	 */
	@DELETE
	@Path("entityMap/unregister/list")
	public Response unregisterEntityByIdInURL(@QueryParam("id") List<Long> ids) throws WebApplicationException {
		return unregisterEntityById(ids);
	}

	/**
	 * This method need to be investigates as the EntityMap doesnt follow the rules for an object being passed
	 * 
	 * @param userLogin
	 * @param removedEntityMapList
	 * @throws UnsupportedEncodingException
	 */
	@DELETE
	@Path("EntityList/{List}")
	public void unregisterEntityList(@QueryParam("entityMapId") List<Long> mapList)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " calling unregisterEntityList");
		List<EntityMap> removedEntityMapList = new ArrayList<EntityMap>();
		for (Long mapId : mapList) {
			if (entityMapDao.exists(mapId))
				removedEntityMapList.add(entityMapDao.get(mapId));
		}
		logger.debug("unregisterEntityList -- ");

		// if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
		// {
		// throw new RuntimeException("Failed to validate Username.");
		// }

		accountManager.unregisterEntity(removedEntityMapList);
	}

	@DELETE
	@Path("entityMap/unregister/fromGroup")
	public void unregisterEntityFromGroup(@QueryParam("entityType") String type, @QueryParam("entityId") Long entityId,
			@QueryParam("permissionGroupId") String permissionGroupId) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " calling registerEntity().");

		String variables[] = {"entityType", "entityId", "permissionGroupId"};
		String parameters[] = {type, entityId.toString(), permissionGroupId.toString()};
		logger.debug("registerEntity: " + buildlLogger(variables, parameters));

		Long pgId = 0L;

		if (CommonUtils.isNumeric(permissionGroupId)) {
			pgId = Long.valueOf(permissionGroupId);
		}

		EntityType entityType = EntityType.valueOf(type);

		accountManager.unregisterEntityFromGrop(entityType, entityId, pgId);

	}

	@GET
	@Path("EntityMap/Owned")
	public EntityMapsWrapper getEntitiesOwned(@QueryParam("ids") List<Long> ids,
			@QueryParam("entityType") EntityType type) throws UnsupportedEncodingException {
		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " calling unregisterEntityList");

		EntityMapsWrapper emw = new EntityMapsWrapper();

		List<EntityMap> ownerEntityList = accountManager.getOwnerEntityMaps(ids, type);
		emw.addAll(ownerEntityList);

		return emw;
	}
	
	/**
	 * @deprecated The conversion of this method from its PUT variant was done very poorly (that's putting it nicely)
	 *             First off this method is configured to receive an XML file but, yet doesn't require one?!?!? What is
	 *             worst is that all the info is passed via URL parameters, which is a waste of a POST request. It is
	 *             also potentially insecure since all of the permission data is stored in the clear in request header.
	 *             Please use the {@link #registerEntity(EntityMap)} method instead, or re-write this method to use form
	 *             parameters instead.
	 * @param accountId
	 * @param type
	 * @param entityId
	 * @param permission
	 * @param permissionGroupId
	 * @throws UnsupportedEncodingException
	 */
	@POST
	@Path("entityMap/register")
	@Consumes("application/xml")
	public void registerEntity(@DefaultValue("noVal") @QueryParam("accountId") String accountId,
			@QueryParam("entityType") String type, @QueryParam("entityId") Long entityId,
			@QueryParam("permission") String permission,
			@DefaultValue("noVal") @QueryParam("permissionGroupId") String permissionGroupId)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " calling registerEntity().");

		String variables[] = {"accountId", "entityType", "entityId", "permission", "permissionGroupId"};
		String parameters[] = {accountId, type, entityId.toString(), permission, permissionGroupId.toString()};
		logger.debug("registerEntity: " + buildlLogger(variables, parameters));

		Long acctId = 0L;
		Long pgId = 0L;

		if (CommonUtils.isNumeric(accountId)) {
			acctId = Long.valueOf(accountId);
		}

		if (CommonUtils.isNumeric(permissionGroupId)) {
			pgId = Long.valueOf(permissionGroupId);
		}

		EntityType entityType = EntityType.valueOf(type);
		PermissionType permissionType = PermissionType.valueOf(permission);

		if (acctId != 0L && accountManager.getAccount(requestingAccount.getUser(), acctId) != null) {

			accountManager.registerEntity(accountManager.getAccount(requestingAccount.getUser(), acctId), entityType,
					entityId, permissionType);
		}
		if (pgId != 0L) {
			accountManager.registerEntity(accountManager.getPermissionGroupById(pgId), entityType, entityId,
					permissionType);
		}

	}

	@GET
	@Path("EntityMap/MyAccess")
	public EntityMapsWrapper listUserAccessEntities(@QueryParam("entityType") EntityType type,
			@QueryParam("permission") PermissionType permissionType, @QueryParam("onlyGranted") boolean onlyGranted)
			throws UnsupportedEncodingException {
		Account requestingAccount = getAuthenticatedAccount();
		logger.info(requestingAccount.getUserName() + " calling unregisterEntityList");

		EntityMapsWrapper emw = new EntityMapsWrapper();

		logger.error(" service informationw Account: " + requestingAccount.getUserName() + " Permission: "
				+ permissionType.getName());

		List<EntityMap> ownerEntityList =
				accountManager.listUserAccessEntities(requestingAccount, type, permissionType, onlyGranted);
		emw.addAll(ownerEntityList);

		return emw;
	}

	/**
	 * Saves changes or creates a new entity map.
	 * 
	 * @param em - The entity map object to save.
	 * @return A No Content Response object (HTTP code 204).
	 * @throws WebApplicationException When there is an error saving the changes for the given entity map.
	 */
	@POST
	@Path("entityMap/add")
	@Consumes(MediaType.APPLICATION_XML)
	public Response registerEntity(EntityMap em) {
		try {
			Account requestingAccount = getAuthenticatedAccount();

			logger.debug(requestingAccount.getUserName() + " calling registerEntity().");
			logger.info("Registering the following entity: " + em.toString());

			// Save the entity map changes.
			accountManager.registerEnitity(em);
		} catch (UnsupportedEncodingException uee) {
			String msg = "Couldn't authenticate the user requesting the entity map registration.";
			logger.error(msg, uee);
			throw new ForbiddenException(msg, uee);
		} catch (HibernateException he) {
			String msg = "Couldn't saved the entity map to the database.";
			logger.error(msg, he);
			throw new InternalServerErrorException(msg, he);
		} catch (Exception e) {
			String msg = "Could not register the entity map.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		return Response.noContent().build();
	}

	/**
	 * Saves any changes for the listed entity maps.
	 * 
	 * @param emw - EntityMap list wrapper object.
	 * @return A no content Response object (HTTP code 204).
	 * @throws WebApplicationException When there is an error while saving the list of entity maps.
	 */
	@POST
	@Path("entityMap/register/list")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response registerEntity(EntityMapsWrapper emw) throws WebApplicationException {
		List<EntityMap> SyncedEntites = null;

		try {
			List<EntityMap> entityList = emw.getEntityMapsList();
			Account requestingAccount = getAuthenticatedAccount();

			logger.debug(requestingAccount.getUserName() + " calling registerEntity().");
			logger.info("Attempting to register " + entityList.size() + " entities.");

			// Save the entity list to the system.
			SyncedEntites = accountManager.saveUpdateEntity(entityList);
		} catch (UnsupportedEncodingException uee) {
			String msg = "Couldn't authenticate the user requesting the entity map list registration.";
			logger.error(msg, uee);
			throw new ForbiddenException(msg, uee);
		} catch (HibernateException he) {
			String msg = "Database error occured while saving changes for one of the listed entity maps.";
			logger.error(msg, he);
			throw new InternalServerErrorException(msg, he);
		} catch (Exception e) {
			String msg = "Could not save changes for all listed entity maps.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		// Applied the synced entities to the list wrapper.
		EntityMapsWrapper responseWrapper = new EntityMapsWrapper();
		responseWrapper.addAll(SyncedEntites);

		return Response.ok(responseWrapper, MediaType.APPLICATION_XML).build();
	}

	@GET
	@Path("entityMap/list")
	// URI Works
	// @Produces("text/xml")
	public LongListWrapper listUserAccess(@QueryParam("id") Long accountId, @QueryParam("entityType") String type,
			@QueryParam("permissionType") String permission, @QueryParam("isGranted") Boolean onlyGranted)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " calling listUserAccess");

		String variables[] = {"id", "entityType", "permission", "isGranted"};
		String parameters[] = {accountId.toString(), type, permission, onlyGranted.toString()};

		logger.debug("registerEntity: " + buildlLogger(variables, parameters));

		LongListWrapper wrapper = new LongListWrapper();
		logger.debug("listUserAccess -- ");

		EntityType entityType = EntityType.valueOf(type);
		PermissionType permissionType = PermissionType.valueOf(permission);

		Account account = accountManager.getAccount(requestingAccount.getUser(), accountId);

		wrapper.addAll(accountManager.listUserAccess(account, entityType, permissionType, onlyGranted));
		return wrapper;
		// return list;
	}

    /**
	 * Given a entityId and a entityType, a list of entityMaps are returned that follows the criteria.
	 * 
	 * @param entityId
	 * @param entityType
	 * @return
	 * @throws WebApplicationException
	 */
    @GET
    @Path("entityMap/listEntityAccess")
	@Produces(MediaType.TEXT_XML)
	public Response listEntityAccess(@QueryParam("entityId") Long entityId, @QueryParam("entityType") EntityType type)
			throws WebApplicationException {
		EntityMapsWrapper emw = new EntityMapsWrapper();

		try {
			Account requestingAccount = getAuthenticatedAccount();
			logger.info(requestingAccount.getUserName() + " calling listEntityAccess.");
			logger.debug("listEntityAccess: id= " + entityId + System.getProperty("line.separator") + "entityType= "
					+ type.getName());

			List<EntityMap> list = accountManager.listEntityAccess(entityId, type);
			emw.addAll(list);
		} catch (UnsupportedEncodingException uee) {
			String msg = "User not authorized to get a list of entities.";
			logger.error(msg, uee);
			throw new ForbiddenException(msg, uee);
		}
		catch (HibernateException he) {
			String msg = "Couldn't get a list of entities because of a database error.";
			logger.error(msg, he);
			throw new InternalServerErrorException(msg, he);
		} catch (Exception e) {
			String msg = "An error occurred while getting a list of entities.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		return Response.ok(emw).build();
    }

	/**
	 * Given a user name, using the accountManager, an Account is returned. In order to use this method the user must
	 * have the admin role
	 * 
	 * @param username
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("user/{username}")
	@Produces("text/xml")
	public Account getByUserName(@PathParam("username") String username) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(
				requestingAccount.getUserName() + " calling getByUserName and requesting the account of " + username);

		// // Make sure this guy is who he says he is.
		// if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
		// {
		// throw new RuntimeException("Failed to validate Username.");
		// }
		Account account = accountDao.getByUserName(username);
		// Anyone can get their own information.
		if (requestingAccount.getUserName().equals(username)) {
			return account;
		}

		// Admins can get any account
		if (accountManager.hasRole(requestingAccount, RoleType.ROLE_ADMIN)
				|| accountManager.hasRole(requestingAccount, RoleType.ROLE_DICTIONARY)) {
			return account;
		}
		throw new RuntimeException("Failed to validate ROLE_ADMIN for " + requestingAccount.getUserName());

	}
	
	@POST
	@Path("get/map/byUserName")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response getAccountsByUserNameList(StringListWrapper slw) throws WebApplicationException {
		AccountsMapByUsernameWrapper mapWrapper = new AccountsMapByUsernameWrapper();
		List<String> usernames = slw.getStringList();

		try {
			// Validate the user that is requesting access.
			Account requestingAccount = getAuthenticatedAccount();

			if (!(accountManager.hasRole(requestingAccount, RoleType.ROLE_ADMIN)
					|| accountManager.hasRole(requestingAccount, RoleType.ROLE_DICTIONARY)
					|| accountManager.hasRole(requestingAccount, RoleType.ROLE_QUERY))) {
				throw new IllegalAccessException("The user, " + requestingAccount.getUserName()
						+ ", doesn't have access to get a list of accounts.");
			}

			List<Account> al = accountDao.getByUserNameList(usernames);

			// Populate the map wrapper.
			for (Account a : al) {
				mapWrapper.put(a.getUserName(), a);
			}
		} catch (UnsupportedEncodingException | IllegalAccessException e) {
			String msg = "Couldn't get a list of accounts due to a permissions violation.";
			logger.error(msg, e);
			throw new ForbiddenException(msg, e);
		} catch (HibernateException he) {
			String msg = "A database error occurred while getting a map of user accounts.";
			logger.error(msg, he);
			throw new InternalServerErrorException(msg, he);
		} catch (Exception e) {
			String msg = "An error occurred while getting a map of user accounts.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		return Response.ok(mapWrapper, MediaType.APPLICATION_XML).build();
	}

	/**
	 * Method to check if the user has GUID admin role or not
	 * 
	 * @param username
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("guid/{username}")
	@Produces("text/plain")
	public Boolean isUserGuidAdmin(@PathParam("username") String username) throws UnsupportedEncodingException {
		Account account = accountDao.getByUserName(username);
		Set<AccountRole> accountRoleList = account.getAccountRoleList();
		for (AccountRole accountRole : accountRoleList) {

			if ((RoleType.ROLE_GUID_ADMIN.equals(accountRole.getRoleType())
					&& accountRole.getIsActive())
					|| (RoleType.ROLE_ADMIN.equals(accountRole.getRoleType())
							&& accountRole.getIsActive())) {
				return true;
			}
		}

		return false;

	}

	/**
	 * Returns true if the user has query tool admin role, false otherwise
	 * 
	 * @param username
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("queryTool/isAdmin")
	@Produces(MediaType.TEXT_PLAIN)
	public boolean getQTisAdmin(@QueryParam("username") String username) throws UnsupportedEncodingException {

		Set<AccountRole> accountRoleList;

		// if not secured use, username passed in
		if (!ModulesConstants.getWsSecured()) {

			Account account = accountDao.getByUserName(username);

			logger.debug("Calling UNSECURED getQTisAdmin with username of " + username);

			accountRoleList = account.getAccountRoleList();
		}
		// if secured use user that is authenticated
		else {
			Account requestingAccount = getAuthenticatedAccount();

			logger.debug("Calling SECURED getQTPermissions with account of " + requestingAccount.getId() + ":"
					+ requestingAccount.getUserName());

			accountRoleList = requestingAccount.getAccountRoleList();
		}

		if (accountRoleList == null) {
			return false;
		} else {
			for (AccountRole accountRole : accountRoleList) {
				if (RoleType.ROLE_QUERY_ADMIN.equals(accountRole.getRoleType())
						&& accountRole.getIsActive()) {
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * Gets the Study and Forms this user has access to. Used by the Query Tool. If this service is being called where
	 * wsSecured=false, it will take in the username and get the permissions for that username
	 * 
	 * 
	 * @param username
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("queryTool/permissions")
	@Produces("text/xml")
	public QueryPermissions getQTPermissions(@QueryParam("username") String username)
			throws UnsupportedEncodingException {

		// if not secured use, username passed in
		if (!ModulesConstants.getWsSecured()) {

			Account account = accountDao.getByUserName(username);

			logger.debug("Calling UNSECURED getQTPermissions with username of " + username);

			return accountManager.getQTPermissionsMap(account);
		}
		// if secured use user that is authenticated
		else {
			Account requestingAccount = getAuthenticatedAccount();

			logger.debug("Calling SECURED getQTPermissions with account of " + requestingAccount.getId() + ":"
					+ requestingAccount.getUserName());

			// Add entity maps and Roles to qtPermissions
			QueryPermissions qtPermissions = accountManager.getQTPermissionsMap(requestingAccount);

			return qtPermissions;
		}

	}

	/**
	 * Retrieves all PrivatePermissionGroups using the accountManager
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("PermissionGroups/private")
	// URI Works: 11/28
	@Produces("text/xml")
	public PermissionGroupsWrapper getPrivatePermissionGroups() throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " calling getPrivatePermissionGroups).");

		PermissionGroupsWrapper pgw = new PermissionGroupsWrapper();

		// if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
		// {
		// throw new RuntimeException("Failed to validate Username.");
		// }
		pgw.addAll(accountManager.getPrivatePermissionGroups());
		return pgw;
	}

	/**
	 * Given an Id, using the accountManager, a PermissionGroup is returned.
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("PermissionGroups/id")
	// URI Works: 11/28
	@Produces("text/xml")
	public PermissionGroup getPermissionGroupById(@QueryParam("id") Long id) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " calling getPermissionGroupById with id: " + id);

		// if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
		// {
		// throw new RuntimeException("Failed to validate Username.");
		// }

		return accountManager.getPermissionGroupById(id);

	}

	@GET
	@Path("PermissionGroups/name")
	// URI Works: 11/28
	@Produces("text/xml")
	public PermissionGroup getPermissionGroup(@QueryParam("name") String name) throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " calling getPermissionGroup with name: " + name);
		name = name.replace('+', ' ');

		// if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
		// {
		// throw new RuntimeException("Failed to validate Username.");
		// }

		return accountManager.getPermissionGroup(name);
	}

	/**
	 * 
	 * 
	 * Permission Groups Methods
	 * 
	 **/

	/**
	 * 
	 * List of all the members (accounts) attached to a permission group that group /PermissionGroups/XXX/getMembers XXX
	 * is for the Group Name
	 * 
	 * @param groupName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("PermissionGroups/name/{groupName}/getMembers")
	@Produces("text/xml")
	public PermissionGroupMemberWrapper getPermissionGroupMembers(@PathParam("groupName") String groupName)
			throws UnsupportedEncodingException {

		// Using the group name, the group will have its members returned
		Account requestingAccount = getAuthenticatedAccount();

		logger.debug(requestingAccount.getUserName() + " calling getPermissionGroupMembers with name: " + groupName);

		PermissionGroupMemberWrapper pgmw = new PermissionGroupMemberWrapper();

		if (accountManager.getPermissionGroup(groupName) != null)
			pgmw.addAll(accountManager.getPermissionGroup(groupName).getMemberSet());

		return pgmw;

	}

	/**
	 * 
	 * List of all the members (accounts) attached to a permission group that group /PermissionGroups/XXX/getMembers XXX
	 * is for the Group Number
	 * 
	 * @param groupID
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("PermissionGroups/id/{groupID}/getMembers")
	@Produces("text/xml")
	public PermissionGroupMemberWrapper getPermissionGroupMembers(@PathParam("groupID") Long groupID)
			throws UnsupportedEncodingException {

		// Using the group name, the group will have its members returned

		Account requestingAccount = getAuthenticatedAccount();

		logger.debug(requestingAccount.getUserName() + " calling getPermissionGroupMembers with name: " + groupID);

		PermissionGroupMemberWrapper pgmw = new PermissionGroupMemberWrapper();

		if (accountManager.getPermissionGroupById(groupID) != null)
			pgmw.addAll(accountManager.getPermissionGroupById(groupID).getMemberSet());

		return pgmw;

	}

	/**
	 * 
	 * Using an accountID, a user is able to determine whether or not this user is apart of the group
	 * 
	 * @param groupName
	 * @param accountID
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("PermissionGroups/{groupName}/isMember")
	@Produces("text/xml")
	@Transactional
	public PerGrpMemBool isGroupMember(@PathParam("groupName") String groupName, @QueryParam("id") Long accountID)
			throws UnsupportedEncodingException {

		// Using the group name, the group will have its members returned

		Account requestingAccount = getAuthenticatedAccount();

		logger.debug(requestingAccount.getUserName() + " calling isGroupMember in Permission Group with: " + groupName
				+ " using accountID: " + accountID);

		Set<PermissionGroupMember> pgmSet = null;
		PerGrpMemBool pgmb = new PerGrpMemBool();
		pgmb.setWorking(false);

		String decoded = URLDecoder.decode(groupName, "UTF-8");

		if (accountManager.getPermissionGroup(decoded) != null)
			pgmSet = accountManager.getPermissionGroup(decoded).getMemberSet();
		else
			throw new NullPointerException("Group Name: " + decoded + " is not Permission Group");

		if (pgmSet.isEmpty())
			pgmb.setWorking(false);

		for (PermissionGroupMember pgm : pgmSet) {
			if (pgm.getAccount().getId().equals(accountID))
				pgmb.setWorking(true);
		}

		return pgmb;

	}

	/**
	 * This is used to build logging statements in an efficient manner to not wasted memory.
	 * 
	 * @param variables
	 * @param parameters
	 * @param ticketProperty
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String buildlLogger(String[] variables, String[] parameters) throws UnsupportedEncodingException {

		StringBuilder builder = new StringBuilder();

		if (variables.length != parameters.length) {
			throw new RuntimeException("Number of variables and parameters must be the same!");
		}

		// counts the number of parameters
		for (int i = 0; i < variables.length; i++) {
			if (parameters[i] == null || ServiceConstants.EMPTY_STRING.equals(parameters[i])) {
				continue;
			}

			// variable=parameter
			if (i < variables.length - 1) {
				builder.append(variables[i]).append("=").append(parameters[i])
						.append(System.getProperty("line.separator")).append("\t");
			} else {
				builder.append(variables[i]).append("=").append(parameters[i]);
			}
		}

		return builder.toString();
	}

	/**
	 * Method to keep session alive by getting WS request set/get proxy ticket
	 * 
	 * @return
	 */
	@GET
	@Path("keepAlive")
	@Produces("text/xml")
	public Response keepSessionAlive() {

		try {
			Account requestingAccount = getAuthenticatedAccount();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.error("error in keepSessionAlive" + Response.ok().status(200).build());
			e.printStackTrace();
		}



		return Response.ok().status(200).build();
		// TODO Auto-generated catch block

	}

	/**
	 * This called needs to be a POST because they could be
	 * enough entity IDs passed in that goes beyond the maximum of parameters allowed for a GET call.
	 * @param entityIds
	 * @param entityTypeName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@POST
	@Path("EntityMap/{entityType}/getByEntityIds")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEntityMapByEntityIds(@FormParam("entityIds") Set<Long> entityIds,
			@PathParam("entityType") String entityTypeName) throws UnsupportedEncodingException {
		EntityType entityType = EntityType.nameOf(entityTypeName);
		Account requestingAccount = getAuthenticatedAccount();
		List<EntityMap> entityMaps = accountManager.getEntityMapsByEntityIds(requestingAccount, entityIds, entityType);
		EntityMapList entityMapList = new EntityMapList(entityMaps);

		return Response.ok(entityMapList).build();
	}
	
	@GET
    @Path("entityMap/listEntityAccessByAccount")
	@Produces(MediaType.TEXT_XML)
	public EntityMapsWrapper getEntityMapByAccountAndType(@QueryParam("accountId") Long accountId,
			@QueryParam("entityType") EntityType type)  {
		
		EntityMapsWrapper emw = new EntityMapsWrapper();
		
		List<EntityMap> entityMaps = accountManager.listEntityAccessByAccount(accountId, type);
		emw.addAll(entityMaps);

		return emw;
	}
	
	@GET
	@Path("User/getDetailsByIds")
	@Produces("text/xml")
	public UserList getUserDetailsByIds(@QueryParam("userId") Set<Long> userIds)
			throws UnsupportedEncodingException {

		Account requestingAccount = getAuthenticatedAccount();
		logger.debug(requestingAccount.getUserName() + " called getUserDetailsByIds().");
		
		UserList userList = new UserList();
		
		userList.addAll(accountManager.getUserDetailsByIds(userIds));

		return userList;
	}
	
}
