package gov.nih.tbi.queryTool.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceContext;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.QueryToolManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.dictionary.model.QueryToolRestServiceModel.SavedQueryList;
import gov.nih.tbi.query.model.hibernate.SavedQuery;

@Path("SavedQuery")
public class SavedQueryRestService extends AbstractRestService {

	private static Logger logger = Logger.getLogger(SavedQueryRestService.class);

	@Autowired
	protected QueryToolManager queryToolManager;

	@Autowired
	protected RepositoryManager repositoryManager;

	@Autowired
	private AccountManager accountManager;

	@Autowired
	protected SessionAccount sessionAccount;

	@Autowired
	protected ModulesConstants modulesConstants;

	@Resource
	private WebServiceContext wsContext;

	UriInfo uriInfo;

	/**
	 * Gets a saved query based on the saved query id.
	 * 
	 * 
	 * @param savedQueryId
	 * @return
	 * @throws UserPermissionException
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("get/{savedQueryId}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getSavedQueryBySavedQueryId(@PathParam("savedQueryId") Long savedQueryId)
			throws WebApplicationException {
		SavedQuery savedQuery = null;

		try {
			logger.debug("REST WS CALL: getSavedQueryBySavedQueryId ( " + savedQueryId + " ) by "
					+ getAuthenticatedAccount().getUserName());

			// convert saved query id to long value
			savedQuery = queryToolManager.getSavedQueryBySavedQueryId(getAuthenticatedAccount(), savedQueryId);

			// verify existence of study
			if (savedQuery == null) {
				throw new ObjectRetrievalFailureException("Saved Query: " + savedQueryId + " cannot be found.", null);
			}
		} catch (UnsupportedEncodingException | UserPermissionException e) {
			String msg = "User couldn't be authenticated or doesn't have permission to get the saved query.";
			logger.error(msg, e);
			throw new ForbiddenException(msg, e);
		} catch (HibernateException | ObjectRetrievalFailureException e) {
			String msg = "Database error occurred while getting the saved query.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		} catch (Exception e) {
			String msg = "Error occurred while getting the saved query.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		return Response.ok(savedQuery, MediaType.APPLICATION_XML).build();
	}

	/**
	 * Returns a list of saved queries that belongs to the specified user.
	 * 
	 * @param username - The username of the account to find saved queries for.
	 * @param permission - The permission filter for querying the list of saved queries.
	 * @param isCopy - The isCopy filter for querying the list of saved queries.
	 * @return A response containing a listing of saved queries belonging to the given user with any search filters
	 *         applied.
	 * @throws WebApplicationException When there is an error while fetching the list of saved queries.
	 */
	@GET
	@Path("get/list/{username}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getSavedQueryList(@PathParam("username") String username,
			@DefaultValue("READ") @QueryParam("permission") PermissionType permission,
			@DefaultValue("false") @QueryParam("isCopy") boolean isCopy) throws WebApplicationException {
		SavedQueryList dsList = new SavedQueryList();

		logger.info("SavedQuery/get/list with: username=" + username + " , permission=" + permission.toString());

		try {
			// Find which user you are getting studies for and if you have permission
			Account account = getAuthenticatedAccount();
			boolean isAdminUser = false;

			if (username != null && !account.getUserName().equals(username)) {
				logger.info("account.getUserName() is " + account.getUserName() + ". Set account to null.");
				account = null;

			} else {
				Set<AccountRole> roles = account.getAccountRoleList();
				for (AccountRole role : roles) {
					if (role.getRoleType() == RoleType.ROLE_ADMIN && role.getIsActive()) {
						isAdminUser = true;
						break;
					}
				}
				
				logger.info("isAdminUser is " + isAdminUser);
			}
			logger.info("Start retrieving saved query list.");

			List<SavedQuery> savedQueries = null;

			if (!isAdminUser) {
				// get the list of valid saved Query ids
				Set<Long> ids = accountManager.listUserAccess(account, EntityType.SAVED_QUERY, permission);

				savedQueries = queryToolManager.searchSavedQuery(ids, null, null, null, null, isCopy);
			} else {
				// System Admin will have full access of all saved queries.
				savedQueries = queryToolManager.searchSavedQuery(null, null, null, null, null, isCopy);
				logger.info("Admin user got savedQueries " + (savedQueries != null) + " " + savedQueries.size());
			}

			dsList.addAll(savedQueries);
		} catch (UnsupportedEncodingException uee) {
			String msg = "User couldn't be authenticated for getting a list of saved queries.";
			logger.error(msg, uee);
			throw new ForbiddenException(msg, uee);
		} catch (HibernateException | ObjectRetrievalFailureException e) {
			String msg = "Database error occurred while getting a list of saved queries for " + username + ".";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		} catch (Exception e) {
			String msg = "Error occurred while getting a list of saved queries for " + username + ".";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		return Response.ok(dsList, MediaType.APPLICATION_XML).build();
	}

	/**
	 * Returns a list of saved queries. If a userid is supplied AND the authenticated user is verified, then the list of
	 * saved queries for "username" will be supplied.
	 * 
	 * @deprecated Use the {@link #getSavedQueryList(String, PermissionType, boolean)} method instead. The URL path on
	 *             that method conforms better to best practices. Also the exception handling is improved a bit.
	 * @param username
	 * @param permission
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	@GET
	// This path makes userid optional (SavedQuery/list and SavedQuery/list/ are both valid)
	@Path("getSavedQueryList/{username}")
	@Produces(MediaType.TEXT_XML)
	public SavedQueryList listSavedQueriesByUserAndPermission(@PathParam("username") String username,
			@QueryParam("permission") PermissionType permission, @QueryParam("isCopy") boolean isCopy)
			throws UnsupportedEncodingException, UserPermissionException {
		
		String perm = (permission != null ? permission.getName() : "isNull");
		String un = (username != null ? username : "isNull");
		logger.info("SavedQuery/getSavedQueryList with: username=" + un + " , permission=" + perm);
		
		// Find which user you are getting studies for and if you have permission
		Account account = getAuthenticatedAccount();
		boolean isAdminUser = false;
		
		if (username != null && !account.getUserName().equals(username)) {
			account = null;

		} else {
			Set<AccountRole> roles = account.getAccountRoleList();
			for (AccountRole role : roles) {
				if (role.getRoleType() == RoleType.ROLE_ADMIN && role.getIsActive()) {
					isAdminUser = true;
					break;
				}
			}
		}

		List<SavedQuery> savedQueries = null;

		if (isAdminUser) {
			// System Admin will have full access of all saved queries.
			savedQueries = queryToolManager.searchSavedQuery(null,null,null,null,null,isCopy);

		} else {
			if (permission == null) {
				permission = PermissionType.READ;    // The default permission type is READ
			}

			// get the list of valid saved Query ids
			Set<Long> ids = accountManager.listUserAccess(account, EntityType.SAVED_QUERY, permission);
			
			savedQueries = queryToolManager.searchSavedQuery(ids, null, null, null, null, isCopy);
		}

		SavedQueryList dsList = new SavedQueryList();
		dsList.addAll(savedQueries);

		return dsList;

	}

	/**
	 * Returns true if the given savedQueryName does not exist in the database.
	 * 
	 * @deprecated Use the {@link #checkSavedQueryNameUnique(String)} method instead. The URL path better conforms to
	 *             best practices.
	 * @param savedQueryName
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UserPermissionException
	 */
	@GET
	@Path("isSavedQueryNameUnique/{savedQueryName}")
	@Produces(MediaType.TEXT_PLAIN)
	public boolean isSavedQueryNameUnique(@PathParam("savedQueryName") String savedQueryName)
			throws UnsupportedEncodingException {

		String name = savedQueryName.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
		name = URLDecoder.decode(name, "UTF-8");
		return queryToolManager.isSavedQueryNameUnique(name);
	}

	/**
	 * Returns true if the given savedQueryName does not exist in the database.
	 * 
	 * @param savedQueryName - The query name to test for uniqueness.
	 * @return Response of the uniqueness of the given query name.
	 * @throws WebApplicationException When there are any errors while testing the uniqueness of the given query name.
	 */
	@GET
	@Path("util/unique/name/{savedQueryName}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response checkSavedQueryNameUnique(@PathParam("savedQueryName") String savedQueryName)
			throws WebApplicationException {
		Boolean isUnique = Boolean.FALSE;

		try {
			// Decode the given query name.
			String name = savedQueryName.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
			name = URLDecoder.decode(name, "UTF-8");

			isUnique = Boolean.valueOf(queryToolManager.isSavedQueryNameUnique(name));
		} catch (UnsupportedEncodingException uee) {
			String msg = "Error occurred while decoding the given query name.";
			logger.error(msg, uee);
			throw new BadRequestException(msg, uee);
		} catch (HibernateException he) {
			String msg = "Database error occurred while checking the uniqueness of the query name.";
			logger.error(msg, he);
			throw new InternalServerErrorException(msg, he);
		} catch (Exception e) {
			String msg = "Error occurred while checking the uniqueness of the query name.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		return Response.ok(isUnique, MediaType.TEXT_PLAIN).build();
	}


	/**
	 * Creates table values in the database from a SavedQuery object being passed in.
	 * 
	 * @param savedQuery - The saved query object to save.
	 * @return A response containing saved query object data that is synced with the database.
	 * @throws WebApplicationException Error occurred while saving the given saved query.
	 */
	@POST
	@Path("save")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response saveDefinedQuery(SavedQuery savedQuery) throws WebApplicationException {
		SavedQuery dbSavedQuery = null;

		try {
			Account requestingAccount = getAuthenticatedAccount();

			logger.debug(requestingAccount.getUserName() + " calling saveDefinedQuery().");

			// Persist changes to the saved query to the database.
			dbSavedQuery = queryToolManager.saveDefinedQuery(savedQuery, requestingAccount);
		} catch (UnsupportedEncodingException | IllegalArgumentException e) {
			String msg = "User couldn't be authenticated or lacked permissions to create/save a query.";
			logger.error(msg, e);
			throw new ForbiddenException(msg, e);
		} catch (HibernateException he) {
			String msg = "Database error occured while saving a query.";
			logger.error(msg, he);
			throw new InternalServerErrorException(msg, he);
		} catch (Exception e) {
			String msg = "An error occured while saving a query.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		return Response.ok(dbSavedQuery).build();
	}

	/**
	 * Creates table values in the database from a DataCartManager Structure (XML) being passed in.
	 * 
	 * @deprecated Use the {@link #saveDefinedQuery(SavedQuery)} method instead. The URL pattern in that method conforms
	 *             better with best practices.
	 * @param savedQuery
	 * @return
	 * @throws WebApplicationException
	 */
	@POST
	@Path("createSavedQuery")
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	public Response saveSavedQuery(SavedQuery savedQuery) throws WebApplicationException {
		return saveDefinedQuery(savedQuery);
	}

	/**
	 * Remove a saved query based on the saved query id.
	 * 
	 * 
	 * @param savedQueryId
	 * @return
	 * @throws UserPermissionException
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("remove/{savedQueryId}")
	public Response removeSavedQueryBySavedQueryId(@PathParam("savedQueryId") Long savedQueryId)
			throws WebApplicationException {
		try {
			logger.debug("REST WS CALL: removeSavedQueryBySavedQueryId ( " + savedQueryId + " ) by "
					+ getAuthenticatedAccount().getUserName());

			logger.info("Removing saved query " + savedQueryId.toString() + " from the database...");
			queryToolManager.removeSavedQuery(savedQueryId);
		} catch (UnsupportedEncodingException uee) {
			String msg = "User couldn't be authenticated to delete a query.";
			logger.error(msg, uee);
			throw new ForbiddenException(msg, uee);
		} catch (HibernateException | ObjectRetrievalFailureException e) {
			String msg = "Database error occured while deleting a query (id:" + savedQueryId + ").";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		} catch (Exception e) {
			String msg = "An error occured while deleting a query (id:" + savedQueryId + ").";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		return Response.noContent().build();
	}
	
	/**
	 * Web service method that migrates all old Saved Query xml to json format, it won't delete the xml column but
	 * just extracts and converts the content to json format and save it as data column.
	 * 
	 * Calling Url: http://hostname:8080/portal/ws/savedQueryService/SavedQuery/migrate
	 * @return
	 * @throws WebApplicationException
	 */
	@GET
	@Path("migrate/")
	public Response migrateAllSavedQueries() throws WebApplicationException {
		
		try {
			logger.info("Start migrating all saved queries ...");
			queryToolManager.migrateAllSavedQueries();
		} catch (Exception e) {
			String msg = "An error occured while migrateAllSavedQueries";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		return Response.noContent().build();
	}
	
	@GET
	@Path("util/unique/fileName/{savedQueryFileName}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response checkSavedQueryFileNameUnique(@PathParam("savedQueryFileName") String savedQueryFileName, @QueryParam("metaStudyId") long metaStudyId)
			throws WebApplicationException {
		Boolean isUnique = Boolean.FALSE;

		try {
			// Decode the given query file name.
			String name = savedQueryFileName.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
			name = URLDecoder.decode(name, "UTF-8");

			isUnique = Boolean.valueOf(queryToolManager.isSavedQueryFileNameUniquePerMetaStudy(savedQueryFileName, metaStudyId));
		} catch (UnsupportedEncodingException uee) {
			String msg = "Error occurred while decoding the given query file name.";
			logger.error(msg, uee);
			throw new BadRequestException(msg, uee);
		} catch (HibernateException he) {
			String msg = "Database error occurred while checking the uniqueness of the query file name.";
			logger.error(msg, he);
			throw new InternalServerErrorException(msg, he);
		} catch (Exception e) {
			String msg = "Error occurred while checking the uniqueness of the query file name.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		return Response.ok(isUnique, MediaType.TEXT_PLAIN).build();
	}
	
	@GET
	@Path("util/unique/queryName/{savedQueryName}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response checkSavedQueryNameUniquePerMetaStudy(@PathParam("savedQueryName") String savedQueryName, @QueryParam("metaStudyId") long metaStudyId)
			throws WebApplicationException {
		Boolean isUnique = Boolean.FALSE;

		// Decode the given query file name.
		String name = savedQueryName.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
		try {
			name = URLDecoder.decode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String msg = "Error occurred while decoding the given query name.";
			logger.error(msg, e);
		}

		isUnique = Boolean.valueOf(queryToolManager.isSavedQueryUniquePerMetaStudy(name, metaStudyId));

		return Response.ok(isUnique, MediaType.TEXT_PLAIN).build();
	}
	
	@GET
	@Path("util/isLinkedToMetaStudy/{savedQueryId}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response isQueryLinkedToMetaStudy(@PathParam("savedQueryId") Long savedQueryId)
			throws WebApplicationException {
		
		boolean isLinked = queryToolManager.isQueryLinkedToMetaStudy(savedQueryId);
		return Response.ok(isLinked, MediaType.TEXT_PLAIN).build();
	}

	@GET
	@Path("util/savedQueryPerMetaStudy/{savedQueryName}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getSavedQueryByNameAndMetaStudy(@PathParam("savedQueryName") String savedQueryName,
			@QueryParam("metaStudyId") long metaStudyId) {

		// Decode the given query name.
		String name = savedQueryName.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
		try {
			name = URLDecoder.decode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String msg = "Error occurred while decoding the given query name.";
			logger.error(msg, e);
		}

		SavedQuery savedQuery = queryToolManager.getSavedQueryByNameAndMetaStudy(name, metaStudyId);

		return Response.ok(savedQuery, MediaType.APPLICATION_XML).build();
	}

}
