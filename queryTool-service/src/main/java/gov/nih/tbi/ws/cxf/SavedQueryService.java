package gov.nih.tbi.ws.cxf;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.util.SavedQueryUtil;
import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.service.QueryAccountManager;
import gov.nih.tbi.service.SavedQueryManager;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.QueryRestProviderUtils;
import gov.nih.tbi.ws.provider.RestQueryAccountProvider;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

@Path("/savedQueries")
public class SavedQueryService extends QueryBaseRestService {
	private static final Logger logger = Logger.getLogger(SavedQueryService.class);

	@Autowired
	DataCart dataCart;

	@Autowired
	PermissionModel permissionModel;

	@Autowired
	SavedQueryManager savedQueryManager;

	@Autowired
	ApplicationConstants constants;

	@Autowired
	QueryAccountManager queryAccountManager;

	/**
	 * Handler for POST request sent to the "https://<instance_domain>/query/service/savedQueries/save" URL. The saved
	 * query described by the JSON data in the request will be validated then finally saved in the system.
	 * 
	 * @param sqJSON
	 * @return A response object containing the updated saved query JSON object, whose data has been synced with the
	 *         database. (i.e. any temporay IDs will be replaced with their actual IDs.
	 * @throws WebApplicationException When there is an error while saving the saved query data.
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@POST
	@Path("/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response save(String sqJSON)
			throws WebApplicationException, UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		SavedQuery savedQuery = null;
		List<EntityMap> entityMapList = null;
		JsonObject jsonSavedQuery = null;

		// Parse the JSON string to a JSON object, then run validation tests.
		try {
			JsonParser parser = new JsonParser();

			logger.info("Parsing the saved query JSON sent in the POST request...");
			logger.debug("Saved query JSON data sent: " + sqJSON);

			jsonSavedQuery = (JsonObject) parser.parse(sqJSON);

			logger.info("Validating the saved query form fields...");

			// Validate the new/edited saved query.
			validateSavedQuery(jsonSavedQuery);
		} catch (JsonSyntaxException | IllegalStateException e) {
			String msg = "Error when parsing the JSON string or when accessing the JSON data.";
			logger.error(msg, e);
			throw new BadRequestException(msg, e);
		}

		// Convert the given JSON string to a SavedQuery object.
		try {
			savedQuery = SavedQueryUtil.jsonToSavedQueryWithNoQuery(jsonSavedQuery);
			entityMapList = jsonToEntityMapList(jsonSavedQuery.getAsJsonArray("linkedUsers"));
		} catch (WebApplicationException | UnsupportedEncodingException e) {
			String msg = "Couldn't get a list Account objects for the user permission convertions.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		} catch (Exception e) {
			String msg = "Couldn't convert the given JSON string to a SavedQuery object.";
			logger.error(msg, e);
			throw new BadRequestException(msg, e);
		}

		logger.info("Peristing the saved query data to the database...");

		// Save the query.
		savedQuery = savedQueryManager.saveSavedQuery(dataCart, savedQuery, entityMapList);

		// Convert the update SavedQuery and EntityMap list objects to a JSON object.
		logger.info("Converting the new/updated saved query data and permissions to JSON...");
		jsonSavedQuery = SavedQueryUtil.savedQueryToBackBoneModel(savedQuery, entityMapList);

		return Response.ok(jsonSavedQuery.toString(), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	// http://pdbp-portal-local.cit.nih.gov:8080/query/services/savedQueries/
	public Response getAll() {
		getAuthenticatedAccount();
		List<SavedQuery> savedQueries = savedQueryManager.getSavedQueries(permissionModel.getUserName());

		Collections.sort(savedQueries, new Comparator<SavedQuery>() {
			@Override
			public int compare(SavedQuery sq1, SavedQuery sq2) {
				if (sq1 != null && sq2 != null && sq1.getName() != null && sq2.getName() != null) {
					return sq1.getName().compareToIgnoreCase(sq2.getName());
				} else {
					return sq1.getId().compareTo(sq2.getId());
				}
			}
		});

		JsonArray jsonQueries = new JsonArray();
		for (SavedQuery query : savedQueries) {
			jsonQueries.add(SavedQueryUtil.savedQueryToSimpleJson(query));
		}

		return Response.ok(jsonQueries.toString()).build();
	}

	@GET
	@Path("/query")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQuery(@QueryParam("id") String id) {
		getAuthenticatedAccount();
		JsonObject output = new JsonObject();
		Long sqId = Long.parseLong(id);
		SavedQuery sq = savedQueryManager.getSavedQueryById(sqId);
		output = SavedQueryUtil.savedQueryToJsonWithQuery(sq);
		return Response.ok(output.toString()).build();
	}

	@GET
	@Path("/view")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQueryView(@QueryParam("id") String id) {
		getAuthenticatedAccount();
		JsonObject output = new JsonObject();
		Long sqId = Long.parseLong(id);
		SavedQuery sq = savedQueryManager.getSavedQueryById(sqId);
		RestQueryAccountProvider accountProvider = new RestQueryAccountProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()),
				ApplicationConstants.isWebservicesSecured());

		List<EntityMap> entityMap = accountProvider.listEntityAccess(sq.getId(), EntityType.SAVED_QUERY,
				permissionModel.getAccount(), constants.getListSavedQueryPermissionsWebServiceURL());
		output = SavedQueryUtil.toJsonViewWithPerms(sq, entityMap);
		//This was added to accommodate past saved queries that don't have the date values inside their queryData property
		Date sqLastUpdated = sq.getLastUpdated();
		if(sqLastUpdated != null) {
			output.addProperty("lastUpdated", sqLastUpdated.toString());
		}
		
		Date sqDateCreated = sq.getDateCreated();
		if(sqDateCreated != null) {
			output.addProperty("dateCreated", sqDateCreated.toString());
		}

		return Response.ok(output.toString()).build();
	}

	/**
	 * Converts the permissions in the saved query JSON to a list of EntityMap objects, which represents the user
	 * permissions for saved query. Note: The entity IDs of the generated entity maps are not set at this point. Those
	 * will be set during the save process.
	 * 
	 * @param linkedUsers - The linked users JSON array from the saved query JSON object.
	 * @return A list of EntityMap objects that corresponds to the user permissions for the given saved query.
	 * @throws UnsupportedEncodingException When there is an error with the proxy ticket of an account web service call.
	 * @throws WebApplicationException When there is a HTTP error response when getting a list of accounts.
	 * @throws IllegalStateException When there is an error while accessing data from the JSON array.
	 */
	private List<EntityMap> jsonToEntityMapList(JsonArray linkedUsers)
			throws UnsupportedEncodingException, WebApplicationException, IllegalStateException {
		// Build a list of account user names from the saved query linked users array.
		List<String> usernames = new ArrayList<String>(linkedUsers.size());

		for (JsonElement usrElem : linkedUsers) {
			JsonObject userJson = (JsonObject) usrElem;

			usernames.add(userJson.getAsJsonPrimitive("userName").getAsString());
		}

		// Get a map of Account objects by the user names for the linked users array.
		RestQueryAccountProvider accountProvider = new RestQueryAccountProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()),
				ApplicationConstants.isWebservicesSecured());

		Map<String, Account> accountMap =
				accountProvider.getUserAccountsByUserName(usernames, constants.getAccountMapByUserNameURL());

		// Convert the linked users JSON array into a list of EntityMap objects.
		List<EntityMap> entityList = SavedQueryUtil.jsonToEntityMapList(linkedUsers, accountMap);

		return entityList;
	}

	/**
	 * Run validation tests against the supplied saved query.
	 * 
	 * @param jo - The saved query JSON object to run validation tests against.
	 * @param errorMsgs - Used to store any error messages.
	 * @return True if and only if the given saved query passes all validation tests.
	 * @throws WebApplicationException When there was a HTTP error returned by the underlining web service call, a proxy
	 *         ticket couldn't be created for a web service call, or one or more of the validation tests fail.
	 * @throws IllegalStateException When there is an error accessing properties of the JSON object.
	 */
	private void validateSavedQuery(JsonObject jo) {
		JsonArray errorArray = new JsonArray();
		boolean isValid = true;
		String name = jo.getAsJsonPrimitive("name").getAsString().trim();

		jo.addProperty("name", name);

		// Validate the query name.
		if (!name.isEmpty()) {
			// Check for duplicate names in the system.
			if (jo.getAsJsonPrimitive("editMode").getAsString().equals("create")
					|| (jo.has("oldName") && !jo.getAsJsonPrimitive("oldName").getAsString().equals(name))) {
				if (!savedQueryManager.isQueryNameUnique(name)) {
					errorArray.add(new JsonPrimitive("The query name already exists in the system."));
					isValid = false;
				}
			}

			// Validate the length of the name.
			if (name.length() > 100) {
				errorArray.add(new JsonPrimitive("The query name cannot exceed 100 characters."));
				isValid = false;
			}

			// Verify that the saved query contains no illegal characters (\ / : * ? | < >).
			Pattern pattern = Pattern.compile("[\\\\/:\\*\\?\\|\\<\\>]");
			Matcher matcher = pattern.matcher(name);

			if (matcher.find()) {
				errorArray.add(new JsonPrimitive(
						"The query name cannot contain the following special characters: \\ / : * ? | < >"));
				isValid = false;
			}
		} else {
			errorArray.add(new JsonPrimitive("The name is required."));
			isValid = false;
		}

		String desc = jo.getAsJsonPrimitive("description").getAsString().trim();

		jo.addProperty("description", desc);

		// Count the number of owners for this saved query.
		int numOwner = 0;

		for (JsonElement userElm : jo.getAsJsonArray("linkedUsers")) {
			String permission = userElm.getAsJsonObject().getAsJsonObject("assignedPermission")
					.getAsJsonPrimitive("permission").getAsString();

			if (permission.equals(PermissionType.OWNER.getName())) {
				numOwner++;
			}
		}

		// Validate the owner count.
		if (numOwner > 1) {
			errorArray.add(new JsonPrimitive("There can only be one Owner."));
			isValid = false;
		} else if (numOwner == 0) {
			errorArray.add(new JsonPrimitive("An owner must be assigned to this defined query."));
			isValid = false;
		}

		// Verify that the "copyFlag" is false.
		if (jo.getAsJsonPrimitive("copyFlag").getAsBoolean()) {
			errorArray.add(new JsonPrimitive(
					"The Query Tool cannot save changes to defined queries that are registered to a meta study."));
			isValid = false;
		}

		// If any of the validation tests fail send an error response back to the client.
		if (!isValid) {
			throw new BadRequestException("The saved query is invalid");
		}
	}


	@GET
	@Path("/canEdit")
	@Produces(MediaType.TEXT_PLAIN)
	public Response canEditSavedQuery(@QueryParam("id") String id) {
		getAuthenticatedAccount();
		if (ValUtil.isBlank(id) || !ValUtil.isNumeric(id)) {
			throw new BadRequestException("Invalid parameter passed in.");
		}

		long sqId = Long.parseLong(id);
		boolean canEditSavedQuery = queryAccountManager.canEditSavedQuery(sqId, permissionModel.getAccount());

		return Response.ok(canEditSavedQuery, MediaType.TEXT_PLAIN).build();
	}


	@GET
	@Path("/canDelete")
	@Produces(MediaType.TEXT_PLAIN)
	public Response canDeleteSavedQuery(@QueryParam("id") String id)
			throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		if (ValUtil.isBlank(id) || !ValUtil.isNumeric(id)) {
			throw new BadRequestException("Invalid parameter passed in.");
		}

		long sqId = Long.parseLong(id);
		boolean canDeleteSavedQuery = queryAccountManager.canDeleteSavedQuery(sqId, permissionModel.getAccount());
		
		if (!canDeleteSavedQuery) {
			return Response.ok(canDeleteSavedQuery, MediaType.TEXT_PLAIN).build();
		} 
		
		boolean isLinkedToMetaStudy = savedQueryManager.isQueryLinkedToMetaStudy(sqId);
		if (isLinkedToMetaStudy) {
			return Response.ok("linked", MediaType.TEXT_PLAIN).build();
		}
		
		return Response.ok(canDeleteSavedQuery, MediaType.TEXT_PLAIN).build();
	}
}
