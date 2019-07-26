
package gov.nih.tbi.ws.provider;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import gov.nih.tbi.account.model.AccountRestServiceModel.AccountsMapByUsernameWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.EntityMapsWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.StringListWrapper;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.semantic.model.QueryPermissions;

public class RestQueryAccountProvider extends RestAuthenticationProvider {

	private static final Logger log = LogManager.getLogger(RestQueryAccountProvider.class);

	/**
	 * A constructor for a rest provider in which the serverLocation provided is the domain of the web service call
	 * being made. In the case that any path information other than a domain (such as /portal), that information is
	 * stripped.
	 * 
	 * A proxyTicket provided can only be used one time. If RestAccountProvider attempts to use the proxy ticket
	 * multiple times, an exception is thrown. If a null or blank string is provided, then public calls may be made.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @throws MalformedURLException
	 */
	public RestQueryAccountProvider(String serverLocation, String proxyTicket) {

		super(serverLocation, proxyTicket);
	}

	/**
	 * FOR QUERY TOOL USE ONLY. MUST PASS ApplicationConstants.isWebservicesSecured() INTO THE THIRD ARG.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @param wsSecure
	 */
	public RestQueryAccountProvider(String serverLocation, String proxyTicket, boolean wsSecure) {
		super(serverLocation, proxyTicket, wsSecure);
	}

	/**
	 * Queries the account rest web service to determine if the supplied username is an admin.
	 * 
	 * @param userName - The username to search for.
	 * @param path - The full URL path to the web service
	 * @return True if and only if the account with the supplied username is an admin.
	 * @throws UnsupportedEncodingException When there is an error while encoding the proxy ticket.
	 * @throws WebApplicationException When the web service call results in a HTTP error response.
	 */
	public boolean isUserAdmin(String userName, String path /* constants.getIsAdminWebServiceURL() */)
			throws UnsupportedEncodingException, WebApplicationException {
		log.debug("entering Update Query Permissions");
		boolean isAdmin = false;

		WebClient client = createWebClient(path);

		// Apply query strings
		client.query("username", userName);
		client.query("ticket", getEncodedTicket());

		log.debug("Is Admin URL:" + client.getCurrentURI().toString());

		isAdmin = client.accept(MediaType.TEXT_PLAIN).get(Boolean.class);

		return isAdmin;
	}

	public Account getUserAccountByUserName(String userName, String path /* constants.getAccountWebServiceURL() */)
			throws UnsupportedEncodingException, WebApplicationException {
		log.debug("Request made to get user account by user name.");
		ticketValid();
		
		WebClient client = createWebClient(path + "/" + userName);

		client.query("ticket", getEncodedTicket());

		log.info("Web service call to " + client.getCurrentURI().toString());
		
		Account userAccount = client.accept(MediaType.TEXT_XML).get(Account.class);
		
		log.debug("Got user account " + userAccount.getId());
		cleanup();

		return userAccount;
	}

	/**
	 * Gets a map of Account objects from the given list of usernames. The key of the map is the account's username.
	 * 
	 * @param userNameList - A list of usernames to get the accounts by.
	 * @param path - From constants.getAccountMapByUserNameURL().
	 * @return A map of accounts keyed by its username.
	 * @throws UnsupportedEncodingException When there is an error URL encoding the proxy ticket.
	 * @throws WebApplicationException When the web service sends a HTTP error response.
	 */
	public Map<String, Account> getUserAccountsByUserName(List<String> userNameList,
			String path /* constants.getAccountMapByUserNameURL() */)
			throws UnsupportedEncodingException, WebApplicationException {
		StringListWrapper slw = new StringListWrapper(userNameList);
		WebClient client = createWebClient(path);

		client.query("ticket", getEncodedTicket());

		log.info("Web service call to " + client.getCurrentURI().toString());

		AccountsMapByUsernameWrapper amw =
				client.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML).post(slw,
						AccountsMapByUsernameWrapper.class);

		return amw.getAccountMap();
	}

	public EntityMap getSavedQueryEntityPermission(Account account, Long entityId,
			String path /* constants.getSavedQueryAccessWebServiceURL() */)
			throws UnsupportedEncodingException, WebApplicationException {
		WebClient client = createWebClient(path);
		
		// Apply the query parameters
		client.query("accountId", account.getId());
		client.query("entityId", entityId);
		client.query("entityType", EntityType.SAVED_QUERY);
		client.query("ticket", getEncodedTicket());

		Response response = client.accept("text/xml").get();
		
		if(response.getStatus() == HttpStatus.FORBIDDEN.value()) {
			log.info("RestQueryAccountProvider :: getSavedQueryEntityPermission " + response.getStatus() + " cannot access saved query, user access denied");
			throw new RuntimeException(account.getUserName() + " User denied access ");
		}
		
		EntityMap em = (EntityMap)response.readEntity(EntityMap.class);

		
		return em;
	}

	public QueryPermissions getQueryPermission(Account account,
			String path /* constants.getPermissionsWebserviceURL() */)
			throws UnsupportedEncodingException, WebApplicationException {
		QueryPermissions queryPermissions = null;

		WebClient client = createWebClient(path + "?ticket=" + getEncodedTicket());

		// Apply the query parameters.
		client.query("username", account.getUserName());
		//client.query("ticket", getEncodedTicket());

		log.error("Permissions URL:" + client.getCurrentURI().toString());
		
		
		
		queryPermissions = client.accept(MediaType.TEXT_XML).get(QueryPermissions.class);

		return queryPermissions;
	}

	public List<EntityMap> listEntityAccess(Long entityId, EntityType type, Account account, String path /*constants.listSavedQueryPermissionsWebServiceURL()*/) throws UnsupportedEncodingException {

		WebClient client = createWebClient(path);

		// Apply URL query strings.
		client.query("entityId", entityId);
		client.query("entityType", type);
		client.query("ticket", getEncodedTicket());

		EntityMapsWrapper emw = client.accept(MediaType.TEXT_XML).get(EntityMapsWrapper.class);

		return emw.getEntityMapsList();
	}

	/**
	 * Persist a list of new/changes permissions (entity maps) to the account web service.
	 * 
	 * @param entityList - A list of permissions to send to the account web service.
	 * @param path - The full URL path to the web service.
	 * @return A list of permissions whose data has been synced with the database (i.e. all enity map IDs for new
	 *         permissions should now have there real IDs).
	 * @throws UnsupportedEncodingException When there is an error URL encoding the proxy ticket.
	 * @throws WebApplicationException When the web service all produces a HTTP error response.
	 */
	public List<EntityMap> saveEntityList(List<EntityMap> entityList,
			String path /* constants.updateSavedQueryPermissionListWebServiceURL() */)
			throws UnsupportedEncodingException, WebApplicationException {
		WebClient client = createWebClient(path);

		client.query("ticket", getEncodedTicket());

		// Create post body.
		EntityMapsWrapper requestWrapper = new EntityMapsWrapper();
		requestWrapper.addAll(entityList);

		EntityMapsWrapper emw = client.accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML)
				.post(requestWrapper, EntityMapsWrapper.class);

		return emw.getEntityMapsList();
	}
}
