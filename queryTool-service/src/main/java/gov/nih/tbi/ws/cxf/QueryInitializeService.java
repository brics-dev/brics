package gov.nih.tbi.ws.cxf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.openrdf.http.protocol.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.pojo.ResultType;
import gov.nih.tbi.semantic.model.QueryPermissions;
import gov.nih.tbi.semantic.model.QueryPermissions.FormResultPermission;
import gov.nih.tbi.semantic.model.QueryPermissions.StudyResultPermission;
import gov.nih.tbi.service.QueryAccountManager;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.model.MetaDataCache;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.QueryRestProviderUtils;
import gov.nih.tbi.util.SearchResultUtil;
import gov.nih.tbi.ws.provider.RestQueryAccountProvider;

public class QueryInitializeService extends QueryBaseRestService {

	private final static Logger logger = Logger.getLogger(QueryInitializeService.class);

	@Autowired
	ResultManager resultManager;

	@Autowired
	PermissionModel permissionModel;

	@Autowired
	QueryAccountManager queryAccountManager;


	@Autowired
	ApplicationConstants constants;
	
	@Autowired
	MetaDataCache metaDataCache;

	/**
	 * Web service method to be called when launching QT, it populates MetaDataCache if necessary and also initializes
	 * the permission model for the login user, url: http://hostname:8080/query/service/initialize
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("initialize/")
	@Produces(MediaType.APPLICATION_XML)
	public Response initialize(@QueryParam("savedQueryId") String savedQueryId)
			throws UnsupportedEncodingException, UnauthorizedException {

		logger.info("Initializing PermissionModel ...");
		Account account = getAuthenticatedAccount();

		if (account == null || ANONYMOUS_USER_NAME.equals(account.getUserName())) {
			URI uri = UriBuilder.fromUri("../cas/logout").build();
			return Response.temporaryRedirect(uri).build();
		}

		if (permissionModel == null || permissionModel.getAccount() == null) {

			initializePermission(account);
		}

		logger.info("Initializing MetaDataCache ...");

		if (metaDataCache.isResultCacheEmpty(ResultType.STUDY)) {
			SearchResultUtil.cacheStudyResults(metaDataCache, resultManager);
		}
		if (metaDataCache.isResultCacheEmpty(ResultType.FORM_STRUCTURE)) {
			SearchResultUtil.cacheFormResults(metaDataCache, resultManager);
		}

		String savedQueryUriPart = "";
		if (savedQueryId != null) {
			savedQueryUriPart = "?savedQueryId=" + savedQueryId;
		}
		URI uri = UriBuilder.fromUri("../queryTool.jsp" + savedQueryUriPart).build();
		return Response.temporaryRedirect(uri).build();
	}


	/**
	 * Web service method that clears the meta data cache. Url: http://hostname:8080/query/service/clearCache
	 * 
	 * @return response with OK status if cache is cleared successfully.
	 * @throws UnsupportedEncodingException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("clearCache")
	@Produces("application/xml")
	public Response clearCache() throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();
		if (!permissionModel.isQueryAdmin()) {
			String msg = "Only QT admin can clear Meta Data Cache ...";
			logger.error(msg);
			Response errResponse = Response.status(Status.FORBIDDEN).entity(msg).build();
			throw new ForbiddenException(errResponse);
		}

		logger.info("Clearing MetaDataCache ...");
		metaDataCache.clearCache();

		return Response.ok().build();
	}


	private void initializePermission(Account account)
			throws UnsupportedEncodingException, WebApplicationException, UnauthorizedException {
		getAuthenticatedAccount();
		permissionModel.setAccount(account);

		if (queryAccountManager.hasRole(account, RoleType.ROLE_ADMIN)) {
			permissionModel.setSysAdmin(true);
		}
		if (queryAccountManager.hasRole(account, RoleType.ROLE_QUERY_ADMIN)) {
			permissionModel.setQueryAdmin(true);
		}

		String accUrl = applicationConstants.getModulesAccountURL();
		logger.info("This is the accUrl var: " + accUrl);
		RestQueryAccountProvider accountProvider =
				new RestQueryAccountProvider(accUrl, QueryRestProviderUtils.getProxyTicket(accUrl));

		QueryPermissions queryPermissions =
				accountProvider.getQueryPermission(account, applicationConstants.getPermissionsWebserviceURL());

		for (StudyResultPermission srp : queryPermissions.getStudyResultPermissions()) {
			permissionModel.addStudyResultPermission(srp);
		}

		Map<Long, String> formIdNameMap = queryAccountManager.getFormIdNameMap();

		for (FormResultPermission frp : queryPermissions.getFormResultPermissions()) {
			Long formId = frp.getFormId();
			String shortName = formIdNameMap.get(formId);
			permissionModel.addFormResultPermission(shortName, frp);
		}

		queryAccountManager.updateGraphAccount(permissionModel);
	}


	@GET
	@Path("accountInfo/")
	@Produces("application/json")
	// http://hostname:8080/query/service/accountInfo
	public String getAccountInfo() throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();

		JsonObject accountJson = new JsonObject();
		Account account = permissionModel.getAccount();

		accountJson.addProperty("userName", account.getUserName());
		accountJson.addProperty("userFirstName", account.getUser().getFirstName());
		accountJson.addProperty("isSysAdmin", Boolean.toString(permissionModel.isSysAdmin()));
		accountJson.addProperty("isQTAdmin", Boolean.toString(permissionModel.isQueryAdmin()));

		getModulePermissionJson(accountJson);

		return accountJson.toString();
	}

	@GET
	@Path("accounts/")
	@Produces("application/json")
	// http://hostname:8080/query/service/accounts
	public Response qtAccounts() throws UnauthorizedException, UnsupportedEncodingException {
		logger.info("Retrieve list of Query Tool accessible accounts");
		getAuthenticatedAccount();

		String accUrl = applicationConstants.getModulesAccountURL();
		String proxyTicket = QueryRestProviderUtils.getProxyTicket(accUrl);
		if (proxyTicket == null) {
			throw new WebApplicationException(400);
		}
		// portal/ws/account/account/role/ROLE_QUERY
		String restServiceUrl = "/ws/account/account/role/ROLE_QUERY";
		WebClient client = WebClient.create(accUrl + restServiceUrl);

		HTTPConduit http = WebClient.getConfig(client).getHttpConduit();
		HTTPClientPolicy policy = http.getClient();
		policy.setConnectionTimeout(1000000);
		policy.setReceiveTimeout(1000000);

		client.query("ticket", proxyTicket);
		String output = client.get(String.class);

		return Response.ok(output, MediaType.APPLICATION_JSON).build();
	}


	/**
	 * This web service will keep both portal and dictionary session alive.
	 * @return
	 * @throws WebApplicationException
	 * @throws UnauthorizedException
	 */
	@GET
	@Path("keepAlive")
	public Response keepAlive() throws UnsupportedEncodingException, UnauthorizedException {
		logger.debug("Keep Portal session alive!!!");

		String accUrl = applicationConstants.getModulesAccountURL();
		String proxyTicket = QueryRestProviderUtils.getProxyTicket(accUrl);
		if (proxyTicket == null) {
			logger.error("The user's session has expired");
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		String restServiceUrl = "/ws/account/account/";
		WebClient client = WebClient.create(accUrl + restServiceUrl + "keepAlive");
		Response response = client.get();
		
		logger.debug("Keep Dictionary session alive!!!");
		
		String dictUrl = applicationConstants.getModulesDDTURL();
		String dictProxyTicket = QueryRestProviderUtils.getProxyTicket(dictUrl);
		if (dictProxyTicket == null) {
			logger.error("The user's dictionary session has expired");
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		String dictRestServiceUrl = "/ws/ddt/dictionary/";
		WebClient dictClient = WebClient.create(dictUrl + dictRestServiceUrl + "keepDictAlive");
		Response dictResp = dictClient.get();
		
		if (response.getStatus() == 200 && dictResp.getStatus() == 200) {
			return response;
		} else {
			String msg = "error in QT keep session alive " + response.getStatus() + " " + dictResp.getStatus();
			logger.error(msg);
			Response errRes = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
			throw new InternalServerErrorException(errRes);
		}
	}


	private void getModulePermissionJson(JsonObject json) throws UnauthorizedException, UnsupportedEncodingException {
		getAuthenticatedAccount();

		Account account = permissionModel.getAccount();

		Boolean hasAccessToAccount = false;
		Boolean hasAccessToGUID = false;
		Boolean hasAccessToProforms = false;
		Boolean hasAccessToRepository = false;
		Boolean hasAccessToWorkspace = false;
		Boolean hasAccessToDictionary = false;
		Boolean hasAccessToQuery = false;
		Boolean hasAccessToMetaStudy = false;
		Boolean hasAccessToReporting = false;

		if (queryAccountManager.hasRole(account, RoleType.ROLE_ADMIN)) {
			// Admin user has all permissions
			hasAccessToAccount = true;
			hasAccessToGUID = true;
			hasAccessToProforms = true;
			hasAccessToRepository = true;
			hasAccessToWorkspace = true;
			hasAccessToDictionary = true;
			hasAccessToQuery = true;
			hasAccessToMetaStudy = true;
			hasAccessToReporting = true;

		} else {
			if (queryAccountManager.hasRole(account, RoleType.ROLE_USER)) {
				hasAccessToWorkspace = true;
				hasAccessToAccount = true;
			}

			if (queryAccountManager.hasRole(account, RoleType.ROLE_PROFORMS)
					|| queryAccountManager.hasRole(account, RoleType.ROLE_PROFORMS_ADMIN)) {
				hasAccessToProforms = true;
			}

			if (queryAccountManager.hasRole(account, RoleType.ROLE_GUID)
					|| queryAccountManager.hasRole(account, RoleType.ROLE_GUID_ADMIN)) {
				hasAccessToGUID = true;
			}

			if (queryAccountManager.hasRole(account, RoleType.ROLE_DICTIONARY)
					|| queryAccountManager.hasRole(account, RoleType.ROLE_DICTIONARY_ADMIN)) {
				hasAccessToDictionary = true;
			}

			if (queryAccountManager.hasRole(account, RoleType.ROLE_STUDY)
					|| queryAccountManager.hasRole(account, RoleType.ROLE_STUDY_ADMIN)) {
				hasAccessToRepository = true;
			}

			if (queryAccountManager.hasRole(account, RoleType.ROLE_QUERY)
					|| queryAccountManager.hasRole(account, RoleType.ROLE_QUERY_ADMIN)) {
				hasAccessToQuery = true;
			}

			if (queryAccountManager.hasRole(account, RoleType.ROLE_METASTUDY)
					|| queryAccountManager.hasRole(account, RoleType.ROLE_METASTUDY_ADMIN)) {
				hasAccessToMetaStudy = true;
			}

			if (queryAccountManager.hasRole(account, RoleType.ROLE_REPORTING_ADMIN)) {
				hasAccessToReporting = true;
			}
		}

		json.addProperty("hasAccessToAccount", hasAccessToAccount.toString());
		json.addProperty("hasAccessToGUID", hasAccessToGUID.toString());
		json.addProperty("hasAccessToProforms", hasAccessToProforms.toString());
		json.addProperty("hasAccessToRepository", hasAccessToRepository.toString());
		json.addProperty("hasAccessToWorkspace", hasAccessToWorkspace.toString());
		json.addProperty("hasAccessToDictionary", hasAccessToDictionary.toString());
		json.addProperty("hasAccessToQuery", hasAccessToQuery.toString());
		json.addProperty("hasAccessToMetaStudy", hasAccessToMetaStudy.toString());
		json.addProperty("hasAccessToReporting", hasAccessToReporting.toString());
	}

	@POST
	@Path("stateless/clearCache/")
	@Produces(MediaType.TEXT_HTML)
	public Response clearMetaCache() {
		metaDataCache.clearCache();
		return Response.noContent().build();
	}

	// http://hostname:8080/query/service/deploymentVersion
	@GET
	@Path("deploymentVersion/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getDeploymentVersion(@Context HttpServletRequest request) throws IOException {
		String deploymentVersion = null;
		if (deploymentVersion == null) {
			ServletContext aContext = request.getSession().getServletContext();
			InputStream fis = aContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			try {
				Properties p = new Properties();
				p.load(fis);
				deploymentVersion = p.getProperty("Implementation-Build");

			} finally {
				fis.close();
			}
		}

		return deploymentVersion;
	}

	@GET
	@Path("repositoryId/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getRepositoryId(@Context HttpServletRequest request) throws IOException {
		String repositoryId = null;
		if (repositoryId == null) {
			ServletContext aContext = request.getSession().getServletContext();
			InputStream fis = aContext.getResourceAsStream("/META-INF/MANIFEST.MF");
			try {
				Properties p = new Properties();
				p.load(fis);
				repositoryId = p.getProperty("Repo-Id");

			} finally {
				fis.close();
			}
		}

		return repositoryId;
	}
}
