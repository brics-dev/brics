package gov.nih.tbi.dictionary.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.service.EformManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.EformRestServiceModel.BasicEformList;
import gov.nih.tbi.dictionary.model.EformRestServiceModel.DataElementNameList;
import gov.nih.tbi.dictionary.model.EformRestServiceModel.EformList;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;
import gov.nih.tbi.portal.PortalUtils;

@Path("/dictionary/eforms/")
public class EformRestService extends AbstractRestService {
	private static Logger logger = Logger.getLogger(EformRestService.class);

	@Autowired
	EformManager eformManager;

	@Autowired
	RepositoryManager repositoryManager;
	
	@Context 
	private MessageContext context;

	@GET
	@Path("search")
	@Produces(MediaType.APPLICATION_XML)
	public Response getEformListByUserAccess() throws WebApplicationException {
		try {
			Account requestingAccount = getAuthenticatedAccount();
			checkUserIsLoggedIn(requestingAccount);
			
			// Check users permission to the given FS
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			Long accountId = null;
			String proxyTicket = null;
			if (requestingAccount != null) {
				accountId = requestingAccount.getId();
				proxyTicket = PortalUtils.getProxyTicket(accountUrl);
			}
			RestAccountProvider restProvider = new RestAccountProvider(accountUrl, proxyTicket);
			Set<Long> accessEformIds = restProvider.listUserAccess(accountId, EntityType.EFORM, PermissionType.READ, false);
			
			List<StatusType> publishedList = new ArrayList<StatusType>();
			publishedList.add(StatusType.PUBLISHED);
			
			List<BasicEform> eformList = eformManager.basicEformSearch(accessEformIds, publishedList, "", "", null);
			BasicEformList basicEformWrapper = new BasicEformList();
			basicEformWrapper.addAll(eformList);
			
			return Response.ok(basicEformWrapper).build();
		} catch (UnsupportedEncodingException e) {
			String msg = "There was an error authenticating the user.";
			logger.error(msg, e);
			throw new BadRequestException(msg, e);
		}
	}

	@GET
	@Path("{eformShortName}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getFullEformByShortName(@PathParam("eformShortName") String eformShortName)
			throws WebApplicationException {
		try {
			logger.debug("requesting eform details for eform " + eformShortName);
			Account requestingAccount = getAuthenticatedAccount();

			checkUserIsLoggedIn(requestingAccount);
			Eform eform = eformManager.getEformNoLazyLoad(eformShortName);
			EformList eformWrapper = new EformList();
			eformWrapper.add(eform);

			if (!eformManager.hasRole(requestingAccount, RoleType.ROLE_DICTIONARY_ADMIN)) {
				logger.debug("user is not a dictionary administrator");
				String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
				RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
				PermissionType permission = restProvider.getAccess(requestingAccount.getId(), EntityType.EFORM, eform.getId()).getPermission();
				// Null permisson means user doesn't have read, write or owner any
				// type of permsisson
				if (permission == null || ((!(PermissionType.compare(permission, PermissionType.READ) >= 0) && StatusType.DRAFT.equals(eform.getStatus())))) {
					Response errResponse = Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_XML).entity(ServiceConstants.READ_ACCESS_DENIED).build();
					throw new WebApplicationException(errResponse);
				}
			}
			return Response.ok(eformWrapper).build();

		} catch (UnsupportedEncodingException e) {
			String msg = "There was an error authenticating the user.";
			logger.error(msg, e);
			throw new BadRequestException(msg, e);
		} catch (UserAccessDeniedException e) {
			String msg = "Access denied while getting primissions for eForm (" + eformShortName + ").";
			logger.error(msg, e);
			throw new ForbiddenException(msg, e);
		}
	}

	@POST
	@Path("geteForms")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_XML)
	public Response getFullEformsByShortNameList(String eFormShortNames) throws WebApplicationException {
		try {
			logger.debug("requesting eform details for eform " + eFormShortNames);
			Account requestingAccount = getAuthenticatedAccount();

			checkUserIsLoggedIn(requestingAccount);
			// Verify that the parameter is not null or empty.
			validateParamStringNotBlank(eFormShortNames, "eFormShortNames");

			// Read in JSON array of eForm short names.
			JsonArray eFormShortNameArray = null;

			try {
				JsonParser parser = new JsonParser();
				eFormShortNameArray = parser.parse(eFormShortNames).getAsJsonArray();
			} catch (Exception e) {
				String msg = "Could not parse or process the given JSON array string: " + eFormShortNames;
				logger.error(msg, e);
				throw new BadRequestException(msg, e);
			}

			EformList eformWrapper = new EformList();
			List<Eform> eformList = eformManager.getEformListNoLazyLoad(eFormShortNameJsonArrayToSet(eFormShortNameArray));
			
			for(int i=0; i<eformList.size(); i++) {
				Eform eform = eformList.get(i);
				eformWrapper.add(eform);
			
				if (!eformManager.hasRole(requestingAccount, RoleType.ROLE_DICTIONARY_ADMIN)) {
					logger.debug("user is not a dictionary administrator");
					String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
					RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
					PermissionType permission = restProvider.getAccess(requestingAccount.getId(), EntityType.EFORM, eform.getId()).getPermission();
					// Null permisson means user doesn't have read, write or owner any
					// type of permsisson
					if (permission == null || ((!(PermissionType.compare(permission, PermissionType.READ) >= 0) && StatusType.DRAFT.equals(eform.getStatus())))) {
						Response errResponse = Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_XML).entity(ServiceConstants.READ_ACCESS_DENIED).build();
						throw new WebApplicationException(errResponse);
					}
				}
			}
			return Response.ok(eformWrapper).build();

		} catch (UnsupportedEncodingException e) {
			String msg = "There was an error authenticating the user.";
			logger.error(msg, e);
			throw new BadRequestException(msg, e);
		} catch (UserAccessDeniedException e) {
			String msg = "User access has been denied.";
			logger.error(msg, e);
			throw new ForbiddenException(msg, e);
		}
	}
	
	@GET
	@Path("question/{id}/document/{fileName}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getQuestionDocumentFileStream(@PathParam("id") long questionId,
			@PathParam("fileName") String questionDocumentFileName) throws WebApplicationException {
		Account requestingAccount = null;
		try {
			requestingAccount = getAuthenticatedAccount();
		} catch (UnsupportedEncodingException e) {
			String msg = "There was an error authenticating the user.";
			logger.error(msg, e);
			throw new BadRequestException(msg, e);
		}

		checkUserIsLoggedIn(requestingAccount);
		validateParamStringNotBlank(questionDocumentFileName, "fileName");

		Question documentQuestion = eformManager.getQuestion(questionId);

		QuestionDocument userFileDocument =
				eformManager.getQuestionDocument(documentQuestion, questionDocumentFileName);

		try {
			byte[] questionDocumentBytes = repositoryManager.getFileByteArray(userFileDocument.getUserFile());
			ResponseBuilder response = Response.ok(questionDocumentBytes);
			response.header("Content-Disposition", "inline; filename=" + userFileDocument.getUserFile().getName());
			return response.build();
		} catch (Exception e) {
			String errorMessage = "There was an error retrieving the image.";
			logger.error(errorMessage, e);
			throw new InternalServerErrorException(errorMessage, e);
		}
	}
	
	/**
	 * Validates the requesting account to ensure that the object exists and is not the anonymous user account.
	 * 
	 * @param requestingAccount - The requesting account to be validated.
	 * @throws WebApplicationException When the requesting account is not valid or logged in.
	 */
	private void checkUserIsLoggedIn(Account requestingAccount) throws WebApplicationException {
		if ((requestingAccount == null) || requestingAccount.getUserName().equals(ANONYMOUS_USER_NAME)) {
			String msg =
					"The user does not have an active session or was trying to request access without being logged in.";
			logger.error(msg);
			Response errResponse =
					Response.status(Response.Status.FORBIDDEN).type(MediaType.TEXT_PLAIN).entity(msg).build();

			throw new WebApplicationException(errResponse);
		}
	}

	private void validateParamStringNotBlank(String stringToCheck, String paramaterName) {
		if (StringUtils.isBlank(stringToCheck)) {
			String msg = "The " + paramaterName + " field is required.";
			logger.error(msg);

			throw new BadRequestException(msg);
		}
	}
	
	@GET
	@Path("dataElementNameList/byEformShortName/{eformShortName}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getDeNamesByEformShortName(@PathParam("eformShortName") String eformShortName)
			throws WebApplicationException {
		// Validate requesting account.
		try {
			Account requestingAccount = getAuthenticatedAccount();
			checkUserIsLoggedIn(requestingAccount);
			
			// Check users permission to the given FS
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			Long accountId = null;
			String proxyTicket = null;

			if (requestingAccount != null) {
				accountId = requestingAccount.getId();
				proxyTicket = PortalUtils.getProxyTicket(accountUrl);
			}

			RestAccountProvider restProvider = new RestAccountProvider(accountUrl, proxyTicket);
			Set<Long> accessEformIds = restProvider.listUserAccess(accountId, EntityType.EFORM, PermissionType.READ, false);

			// Check the access eForm ID set.
			if (!validateEformShortNameAccess(accessEformIds, eformShortName)) {
				throw new BadRequestException(
						"The account requesting pre-pop DE names does not have read access to eForm " + eformShortName
								+ ".");
			}

		} catch (WebApplicationException wae) {
			logger.error("Web service error occured while validating the authenicated account.", wae);
			throw wae;
		} catch (Exception e) {
			String msg = "Failure while getting or validating the authenticated account for this request.";
			logger.error(msg, e);
			throw new BadRequestException(msg, e);
		}

		List<String> returnDEList = new ArrayList<String>();

		// Get a list of data elements for the given eForm short name.
		try {
			Set<QuestionAttribute> qaSet = eformManager.getQuestionAttributeListByEformShortName(eformShortName);

			for (QuestionAttribute qa : qaSet) {
				String deName = qa.getDataElementName();

				if (deName.equals("VisitTypPDBP")) {
					returnDEList.add("VisitType");
				} else {
					returnDEList.add(deName);
				}
			}

		} catch (HibernateException he) {
			String msg = "A database error occurred while get a list of data element names for the eForm "
					+ eformShortName + ".";
			logger.error(msg, he);

			throw new InternalServerErrorException(msg, he);
		} catch (Exception e) {
			String msg = "Could not get a list of data element names for the eForm " + eformShortName + ".";
			logger.error(msg, e);

			throw new InternalServerErrorException(msg, e);
		}

		DataElementNameList deNamesWrapper = new DataElementNameList();
		deNamesWrapper.addAll(returnDEList);

		return Response.ok(deNamesWrapper).build();
	}

	@POST
	@Path("dataElementNameList/byEformShortNameJsonArray")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_XML)
	public Response getDeNamesByEformShortNameArray(String eFormShortNames) throws WebApplicationException {
		// Verify that the parameter is not null or empty.
		validateParamStringNotBlank(eFormShortNames, "eFormShortNames");

		// Read in JSON array of eForm short names.
		JsonArray eFormShortNameArray = null;

		try {
			JsonParser parser = new JsonParser();
			eFormShortNameArray = parser.parse(eFormShortNames).getAsJsonArray();
		} catch (Exception e) {
			String msg = "Could not parse or process the given JSON array string: " + eFormShortNames;
			logger.error(msg, e);
			throw new BadRequestException(msg, e);
		}

		// Validate the requesting account.
		try {
			Account requestingAccount = getAuthenticatedAccount();
			checkUserIsLoggedIn(requestingAccount);

			// Check users permission to the given FS
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			Long accountId = requestingAccount.getId();
			String proxyTicket = PortalUtils.getProxyTicket(accountUrl);
			RestAccountProvider restProvider = new RestAccountProvider(accountUrl, proxyTicket);
			Set<Long> accessEformIds =
					restProvider.listUserAccess(accountId, EntityType.EFORM, PermissionType.READ, false);

			// Check the access eForm ID set.
			if (!validateEformShortNameAccess(accessEformIds, eFormShortNameArray)) {
				throw new BadRequestException(
						"The account requesting pre-pop DE names does not have read access to any eForms.");
			}
		} catch (WebApplicationException wae) {
			logger.error("Web service error occured while validating the authenicated account.", wae);
			throw wae;
		} catch (Exception e) {
			String msg = "Failure while getting or validating the authenticated account for this request.";
			logger.error(msg, e);
			throw new BadRequestException(msg, e);
		}

		// Get the pre-population data element names.
		List<String> returnDEList = new ArrayList<String>();

		try {
			Set<QuestionAttribute> qaSet = eformManager.getQuestionAttributeListByEformShortNameCollection(
					eFormShortNameJsonArrayToSet(eFormShortNameArray));

			for (QuestionAttribute qa : qaSet) {
				if (qa.getDataElementName().equals("VisitTypPDBP")) {
					returnDEList.add("VisitType");
				} else {
					returnDEList.add(qa.getDataElementName());
				}
			}
		} catch (HibernateException he) {
			String msg = "Error occured while getting a set of question attribute objects from the database.";
			logger.error(msg, he);
			throw new InternalServerErrorException(msg, he);
		} catch (IllegalStateException ise) {
			String msg = "Error occured while converting the JSON array to a set.";
			logger.error(msg, ise);
			throw new BadRequestException(msg, ise);
		} catch (Exception e) {
			String msg = "Error occured while getting a listing of pre-pop data element names.";
			logger.error(msg, e);
			throw new InternalServerErrorException(msg, e);
		}

		// Construct the response body.
		DataElementNameList deNamesWrapper = new DataElementNameList();
		deNamesWrapper.addAll(returnDEList);

		return Response.ok(deNamesWrapper).build();
	}

	private boolean validateEformShortNameAccess(Set<Long> permittedEformIds, String eformShortName) {
		boolean found = false;
		List<BasicEform> primittedEforms = eformManager.getBasicEformSearchList(permittedEformIds);

		for (BasicEform eform : primittedEforms) {
			if (eform.getShortName().equals(eformShortName)) {
				found = true;
				break;
			}
		}

		return found;
	}

	/**
	 * A helper method that validates a JSON array of short names against a list of eForms that the current user has
	 * read access to. If any of the short names in the JSON array is not found in the list of permitted eForms, then
	 * this method will return false. If the reverse is true, then this method will return true.
	 * 
	 * @param permittedEformIds - A set of eForm IDs that the current user is permitted to have read access to.
	 * @param eFormShortNames - A JSON array of eForm short names to validate.
	 * @return True if and only if all eForm short names in the given JSON array are associated with the eForms that the
	 *         current user has read access to.
	 * @throws IllegalStateException When there is an error reading short names from the JSON array object.
	 */
	private boolean validateEformShortNameAccess(Set<Long> permittedEformIds, JsonArray eFormShortNames)
			throws IllegalStateException {
		boolean isValid = true;
		List<BasicEform> primittedEforms = eformManager.getBasicEformSearchList(permittedEformIds);

		// Validate each eForm short name in the JSON array.
		for (JsonElement elem : eFormShortNames) {
			String shortName = elem.getAsJsonPrimitive().getAsString();
			boolean found = false;

			for (BasicEform eform : primittedEforms) {
				if (eform.getShortName().equalsIgnoreCase(shortName)) {
					found = true;
					break;
				}
			}

			// Check if the short name was found.
			if (!found) {
				isValid = false;
				break;
			}
		}

		return isValid;
	}

	/**
	 * Converts a JSON array of eForm short names to a set.
	 * 
	 * @param eFormShortNames - The JSON array of eForm short names to convert.
	 * @return A set containing all of the eForm short names in the given JSON array.
	 * @throws IllegalStateException When there is a conversion error while reading data from the JSON array.
	 */
	private Set<String> eFormShortNameJsonArrayToSet(JsonArray eFormShortNames) throws IllegalStateException {
		Set<String> shortNameSet = new HashSet<String>(eFormShortNames.size());

		for (JsonElement elem : eFormShortNames) {
			shortNameSet.add(elem.getAsJsonPrimitive().getAsString());
		}

		return shortNameSet;
	}
}
