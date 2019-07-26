package gov.nih.tbi.guid.ws;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.guid.exception.InvalidJwtException;
import gov.nih.tbi.guid.model.GuidJwt;
import gov.nih.tbi.guid.ws.exception.AuthenticationException;

/**
 * Represents the SERVERs authentication to the Centralized GUID Server. Manages the login status, allows for reusing
 * authorized tokens, obtains USER JWTs (but does NOT store them!), etc.
 * 
 * Singleton scoped to allow the server to reuse the Home Server JWT as much as is needed until it expires.
 * 
 * @author jpark1
 *
 */
@Component
@Scope("singleton")
public class GuidServerAuthenticationUtil {
	private static final Logger logger = Logger.getLogger(GuidServerAuthenticationUtil.class);
	private static final String BEARER_BEARER = "Bearer";

	@Autowired
	private ModulesConstants modulesConstants;

	@Autowired
	protected AccountManager accountManager;

	private GuidJwt currentJwt;

	private String serverToken;
	private String homeServerLoginURL;
	private String getUserJwtURL;
	private String renewUrl;

	/**
	 * This class itself is autowired itself so autowired dependencies may not be available at constructor time.
	 * Therefore, we use the annotation here to perform actual initialization of the needed data.
	 */
	@PostConstruct
	public void init() {
		serverToken = modulesConstants.getModulesGuidServiceToken();
		homeServerLoginURL = modulesConstants.getModulesGTServiceURL() + "auth/homeServer";
		getUserJwtURL = modulesConstants.getModulesGTServiceURL() + "auth/user";
		renewUrl = modulesConstants.getModulesGTURL() + modulesConstants.getModulesPortalRoot()
				+ "/guid/guidAction!getJwt.action";
	}

	/**
	 * Answers the question "is the home server currently logged in to the GUID server?"
	 * 
	 * @return boolean true if logged in and not expired, otherwise false
	 */
	public boolean isLoggedIn() {
		return currentJwt != null && !isExpired();
	}

	public boolean isExpired() {
		if (currentJwt == null) {
			// safest to return true in this case
			return true;
		}
		return currentJwt.isExpired();
	}
	
	public boolean isAlmostExpired() {
		if (currentJwt == null) {
			// safest to return true in this case
			return true;
		}
		return currentJwt.isAlmostExpired();
	}

	public GuidJwt serverLogin()
			throws InvalidJwtException, JsonSyntaxException, WebApplicationException {
		if (isLoggedIn() && !isAlmostExpired()) {
			// don't need to do anything if we're already logged in. This is a shortcut that lets the developer
			// simply call this method without a check.
			return currentJwt;
		}

		logger.info("Logging into the GUID home server at " + homeServerLoginURL);

		WebClient client = WebClient.create(homeServerLoginURL);
		client.header("Authorization", BEARER_BEARER + " " + serverToken);

		// there is no body necessary for this particular call, it only needs the headers
		String jwtStr = client.post("", String.class);

		currentJwt = new GuidJwt(jwtStr);

		return currentJwt;
	}

	/**
	 * Obtains a user JWT which provides them authentication to the GUID server. If needed, this logs in the server as
	 * well. This method does NOT perform any permisssions checking. That should be done externally.
	 * 
	 * @param account - User account attempting to get GUID access
	 * @return GuidJwt JWT model object
	 * @throws JsonSyntaxException If the response from the server, inside the JWT payload, is not valid JSON.
	 * @throws InvalidJwtException If the JWT is not a headless or headed JWT with proper (dotted) separators.
	 * @throws WebApplicationExcepRtion If the GUID server responds with an error code.
	 */
	public GuidJwt getUserJwt(Account account)
			throws JsonSyntaxException, InvalidJwtException, WebApplicationException {
		// there is no is-logged-in check here because, if this is called, we should always request a new one

		boolean hasGuidAdminAccess = accountManager.hasRole(account, RoleType.ROLE_GUID_ADMIN) || accountManager.hasRole(account, RoleType.ROLE_ADMIN);

		AccountRole role = account.getAccountByRoleType(RoleType.ROLE_GUID);
		String status = "INACTIVE";
		if (role != null) {
			RoleStatus guidRoleStatus = role.getRoleStatus();

			// status must be either "ACTIVE", "INACTIVE", or "EXPIRED"
			if (guidRoleStatus.equals(RoleStatus.ACTIVE) || guidRoleStatus.equals(RoleStatus.EXPIRING_SOON)) {
				status = "ACTIVE";
			} else if (guidRoleStatus.equals(RoleStatus.EXPIRED)) {
				status = "EXPIRED";
			} else {
				status = "INACTIVE";
			}
		}

		logger.info("Getting GUID JWT for user: " + account.getUserName());

		serverLogin();

		WebClient client = WebClient.create(getUserJwtURL);
		client.header("Authorization", "JWT " + currentJwt.toString());
		client.type(MediaType.APPLICATION_JSON);
		JsonObject requestJson = new JsonObject();
		requestJson.add(GuidJwt.CLAIM_USERNAME, new JsonPrimitive(account.getUserName()));
		requestJson.add(GuidJwt.CLAIM_RENEW_URL, new JsonPrimitive(renewUrl));
		requestJson.add(GuidJwt.CLAIM_IS_HOME_ADMIN, new JsonPrimitive(hasGuidAdminAccess));
		requestJson.add(GuidJwt.CLAIM_ORGANIZATION, new JsonPrimitive(account.getAffiliatedInstitution()));
		requestJson.add(GuidJwt.CLAIM_FIRSTNAME, new JsonPrimitive(account.getUser().getFirstName()));
		requestJson.add(GuidJwt.CLAIM_LASTNAME, new JsonPrimitive(account.getUser().getLastName()));
		requestJson.add(GuidJwt.CLAIM_EMAIL, new JsonPrimitive(account.getUser().getEmail()));
		requestJson.add(GuidJwt.CLAIM_STATUS, new JsonPrimitive(status));

		if (role != null) {
			if (role.getExpirationDate() == null) {
				requestJson.add(GuidJwt.CLAIM_PERM_EXPIRATION, JsonNull.INSTANCE);
			} else {
				requestJson.add(GuidJwt.CLAIM_PERM_EXPIRATION, new JsonPrimitive(role.getExpirationDate().getTime()));
			}
		} else {
			// else, we shouldn't be generating a jwt at all and stuff is weird
			return null;
		}

		String jwtResponse = client.post(requestJson.toString(), String.class);

		return new GuidJwt(jwtResponse);
	}

	
	public GuidJwt getSystemUserJwt() throws JsonSyntaxException, InvalidJwtException, AuthenticationException {

		serverLogin();

		WebClient client = WebClient.create(getUserJwtURL);
		client.header("Authorization", "JWT " + currentJwt.toString());
		client.type(MediaType.APPLICATION_JSON);
		JsonObject requestJson = new JsonObject();
		requestJson.add(GuidJwt.CLAIM_USERNAME, new JsonPrimitive(modulesConstants.getModulesGuidServiceUsername()));
		requestJson.add(GuidJwt.CLAIM_RENEW_URL, new JsonPrimitive(renewUrl));
		// TODO: fill in
		requestJson.add(GuidJwt.CLAIM_IS_HOME_ADMIN, new JsonPrimitive(false));
		requestJson.add(GuidJwt.CLAIM_IS_ADMIN, new JsonPrimitive(true));
		requestJson.add(GuidJwt.CLAIM_ORGANIZATION, new JsonPrimitive(modulesConstants.getGuidSysUserOrg()));
		requestJson.add(GuidJwt.CLAIM_FIRSTNAME, new JsonPrimitive(modulesConstants.getGuidSysUserFirstname()));
		requestJson.add(GuidJwt.CLAIM_LASTNAME, new JsonPrimitive(modulesConstants.getGuidSysUserLastname()));
		requestJson.add(GuidJwt.CLAIM_EMAIL, new JsonPrimitive("REPLACED@nih.gov"));
		requestJson.add(GuidJwt.CLAIM_STATUS, new JsonPrimitive("ACTIVE"));
		requestJson.add(GuidJwt.CLAIM_PERM_EXPIRATION, JsonNull.INSTANCE);

		Response response = client.post(requestJson.toString());
		if (response.getStatus() != 200) {
			throw new AuthenticationException(
					"Unable to log in user because of an authentication error: " + response.getStatus());
		}

		String jwtResponse = response.readEntity(String.class);
		return new GuidJwt(jwtResponse);
	}
	
	public GuidJwt getJwt() {
		return currentJwt;
	}
}
