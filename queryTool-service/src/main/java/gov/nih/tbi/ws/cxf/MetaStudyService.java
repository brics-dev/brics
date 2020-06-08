package gov.nih.tbi.ws.cxf;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.util.ValUtil;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.query.model.hibernate.SavedQuery;
import gov.nih.tbi.service.DownloadManager;
import gov.nih.tbi.service.MetaStudyManager;
import gov.nih.tbi.service.SavedQueryManager;
import gov.nih.tbi.service.cache.InstancedDataCache;
import gov.nih.tbi.service.model.DataCart;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.DownloadUtil;
import gov.nih.tbi.util.QueryRestProviderUtils;
import gov.nih.tbi.ws.provider.RestQueryAccountProvider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.jasig.cas.client.validation.Assertion;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@Path("/metaStudies")
public class MetaStudyService extends QueryBaseRestService {
	private static final Logger logger = Logger.getLogger(MetaStudyService.class);

	@Autowired
	ApplicationConstants constants;

	@Autowired
	SavedQueryManager savedQueryManager;

	@Autowired
	MetaStudyManager metaStudyManager;

	@Autowired
	DataCart dataCart;

	@Autowired
	DownloadManager downloadManager;

	@Autowired
	PermissionModel permissionModel;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMetaStudies() {
		// call services to get meta study list
		List<MetaStudy> convertedMetaStudyList = new ArrayList<MetaStudy>(savedQueryManager.getMetaStudies().values());

		JsonArray output = new JsonArray();
		for (MetaStudy study : convertedMetaStudyList) {
			output.add(study.toJsonLight());
		}

		return Response.ok(output.toString()).build();
	}


	@POST
	@Path("/validateLinkSavedQueryData")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateLinkSavedQueryData(@FormParam("sendQueryFilters") String sendQueryFilters,
			@FormParam("queryName") String queryName, @FormParam("sendData") String sendData,
			@FormParam("dataName") String dataName, @FormParam("metaStudyId") long metaStudyId)
			throws UnauthorizedException, IOException, JAXBException {

		getAuthenticatedAccount();

		JsonObject output = new JsonObject();

		if (sendQueryFilters.equals("yes") && !isSavedQueryNameUniquePerMetaStudy(queryName, metaStudyId)) {
			output.add("queryNameUnique", new JsonPrimitive(false));
		}

		if (sendQueryFilters.equals("yes") && !validateSavedQueryName(queryName)) {
			output.add("globalQueryNameUnique", new JsonPrimitive(false));
		}

		if (sendData.equals("yes") && !isSavedQueryFileNameUniquePerMetaStudy(dataName, metaStudyId)) {
			output.add("dataNameUnique", new JsonPrimitive(false));
		}

		return Response.ok(output.toString()).build();

	}

	@POST
	@Path("/linkSavedQueryData")
	@Produces(MediaType.APPLICATION_JSON)
	public Response linkMetaStudySavedQuery(@FormParam("sendQueryFilters") String sendQueryFilters,
			@FormParam("queryName") String queryName, @FormParam("queryDescription") String queryDescription,
			@FormParam("sendData") String sendData, @FormParam("dataName") String dataName,
			@FormParam("dataDescription") String dataDescription, @FormParam("metaStudyId") long metaStudyId,
			@FormParam("filterExpression") String booleanExpression,@FormParam("outputCode") String outputCode) {

		getAuthenticatedAccount();

		SavedQuery newSavedQuery = null;

		if (sendQueryFilters.equals("yes")) {

			if (!isSavedQueryNameUniquePerMetaStudy(queryName, metaStudyId)) {
				newSavedQuery = savedQueryManager.getSavedQueryByNameAndMetaStudy(queryName, metaStudyId);
				if (newSavedQuery != null) {
					newSavedQuery = savedQueryManager.saveSavedQuery(dataCart, newSavedQuery);
				}
			} else {

				if (!validateSavedQueryName(queryName)) {
					// error case: the name failed uniqueness checks

					Response errResponse = Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON)
							.entity("The query name is not unique.  Please choose a different query name").build();

					// A message must be included as a separate argument otherwise the unhandled exception doesn't get
					// printed to System.ERROR
					throw new BadRequestException("The query name is not unique", errResponse);
				}

				newSavedQuery = new SavedQuery();
				String userName = permissionModel.getUserName();
				// set date for created saved query
				newSavedQuery.setDateCreated(new Date());
				newSavedQuery.setLastUpdated(new Date());
				newSavedQuery.setName(queryName);
				newSavedQuery.setDescription(queryDescription);
				newSavedQuery.setOutputCode(outputCode);
				newSavedQuery =
						savedQueryManager.saveSavedQuery(dataCart, newSavedQuery, createPermissionMap(userName));
				savedQueryManager.linkSavedQueryMetaStudy(metaStudyId, newSavedQuery);
			}
		}

		if (sendData.equals("yes")) {

			final InstancedDataTable instancedDataTable = new InstancedDataTable(dataCart.getInstancedDataTable());
			final String dataFileName = dataName + ApplicationConstants.ZIP_EXTENSION;
			final List<FormResult> clonedForms = DownloadUtil.cloneFormResults(dataCart.getSelectedForms());
			final CodeMapping codeMapping = dataCart.getCodeMapping();
			final Account userAccount = permissionModel.getAccount();
			final long msId = metaStudyId;
			final String description = dataDescription;
			final Assertion casAssertion = QueryRestProviderUtils.getAssertionFromSecurityContext();
			final InstancedDataCache cache = dataCart.getInstancedDataCache();
			final boolean showAgeRange = (QueryToolConstants.PDBP_ORG_NAME.equals(constants.getOrgName()))
					&& !permissionModel.isQueryAdmin() && !permissionModel.isSysAdmin();

			Thread downloadThread = new Thread(new Runnable() {
				public void run() {
					try {
						downloadManager.downloadToMetaStudy(dataFileName, instancedDataTable, clonedForms, codeMapping,
								userAccount, msId, description, casAssertion, cache, booleanExpression, showAgeRange);
					} catch (FilterEvaluatorException e) {
						throw new InternalServerErrorException("Error occured when evaluating the filters", e);
					}
				}
			});

			downloadThread.setName("ThreadWithTimeStamp" + System.currentTimeMillis());
			downloadThread.start();
		}

		return Response.ok().build();
	}

	private List<EntityMap> createPermissionMap(String username) {
		getAuthenticatedAccount();
		List<EntityMap> entityList = new ArrayList<EntityMap>();
		List<String> usernames = new ArrayList<String>(1);
		usernames.add(username);
		RestQueryAccountProvider accountProvider = new RestQueryAccountProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()),
				ApplicationConstants.isWebservicesSecured());
		Map<String, Account> accountMap =
				accountProvider.getUserAccountsByUserName(usernames, constants.getAccountMapByUserNameURL());

		EntityMap em = new EntityMap();
		em.setAccount(accountMap.get(username));
		em.setType(EntityType.SAVED_QUERY);
		// Set the permission type. It will be owner for saved queries going to metastudy
		String permType = "Owner";
		em.setPermission(PermissionType.getByName(permType));

		entityList.add(em);
		return entityList;
	}

	/**
	 * Confirms that the passed query name is unique and not empty.
	 * 
	 * @return True if and only if the query name passes all of the validation tests.
	 * @throws JAXBException
	 * @throws IOException
	 */
	private boolean validateSavedQueryName(String name) {
		// Verify that the query name exists.
		if (ValUtil.isBlank(name)) {
			return false;
		}

		if (!savedQueryManager.isQueryNameUnique(name)) {
			return false;
		}

		return true;
	}

	private boolean isSavedQueryNameUniquePerMetaStudy(String queryName, Long metaStudyId) {

		if (!savedQueryManager.isQuerySavedNameUniquePerMetaStudy(queryName, metaStudyId)) {
			return false;
		}

		return true;
	}

	private boolean isSavedQueryFileNameUniquePerMetaStudy(String fileName, Long metaStudyId)
			throws UnsupportedEncodingException {

		if (!savedQueryManager.isQueryFileNameUniquePerMetaStudy(fileName, metaStudyId)) {
			return false;
		}

		return true;
	}



}
