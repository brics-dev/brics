
package gov.nih.tbi.account.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.account.model.AccountRestServiceModel.AccountsWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.EntityMapsWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.LongListWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.PerGrpMemBool;
import gov.nih.tbi.account.model.AccountRestServiceModel.PermissionGroupMemberWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.PermissionGroupsWrapper;
import gov.nih.tbi.account.model.AccountRestServiceModel.UserList;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.account.model.jaxb.EntityMapList;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.ServiceConstants;

/**
 * Provides an implementation for the secure and public Rest service calls of the BRICS account module.
 * 
 * Developer Note: All the rest service call functions defined in this class must call
 * RestAuthenticationProvider.ticketValid() and RestAuthenticationProvider.cleanup() at the beginning and end of the
 * function respectively to guarantee validity of the proxy ticket. Finally, the user must get the proxyTicket argument
 * (i.e. ticket=PT-3284792379842) and append it to the web service url if the user needs to be authenticated. Any ws
 * calls without a ticket will be considered anon. The user can get this formated property with
 * RestAuthenticationProvider.getTicketProperty().
 * 
 * @author Michael Valeiras
 * 
 */
public class RestAccountProvider extends RestAuthenticationProvider {

	/***************************************************************************************************/

	private static Logger logger = Logger.getLogger(RestAccountProvider.class);
	protected final String restServiceUrl = "portal/ws/account/account/";

	/***************************************************************************************************/

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
	public RestAccountProvider(String serverLocation, String proxyTicket) {

		super(serverLocation, proxyTicket);
	}

	/**
	 * FOR QUERY TOOL USE ONLY. MUST PASS ApplicationConstants.isWebservicesSecured() INTO THE THIRD ARG.
	 * 
	 * @param serverLocation
	 * @param proxyTicket
	 * @param wsSecure
	 */
	public RestAccountProvider(String serverLocation, String proxyTicket, boolean wsSecure) {
		super(serverLocation, proxyTicket, wsSecure);
	}

	/***************************************************************************************************/

	/**
	 * Returns the details for the given account
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Account getAccountById(Long id) throws UnsupportedEncodingException {

		ticketValid();

		WebClient client =
				WebClient.create(serverUrl + restServiceUrl + "get/" + id.toString() + "?" + getTicketProperty());
		Account account = client.accept("text/xml").get(Account.class);

		cleanup();

		return account;
	}

	/*****
	 * ( "http://foobar/somepage?arg1={0}&arg2={1} <http://foobar/somepage?arg1=%7b0%7d&arg2=%7b1%7d> ",
	 * Uri.EscapeDataString("escape me"), Uri.EscapeDataString("& me !!")); string text; using (WebClient client = new
	 * WebClient()) { text = client.DownloadString(address); }
	 */

	/**
	 * Returns the details for the given account
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Account getAccount() throws UnsupportedEncodingException {

		ticketValid();

		WebClient client = WebClient.create(serverUrl + restServiceUrl + "get?" + getTicketProperty());
		Account account = client.accept("text/xml").get(Account.class);

		cleanup();

		return account;
	}

	/**
	 * Get a list of users with the specified role.
	 * 
	 * @param roleType
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public List<Account> getAccountsWithRole(String role) throws UnsupportedEncodingException {

		ticketValid();
		String text = serverUrl + restServiceUrl + "role/" + role + "?" + getTicketProperty();
		WebClient client = WebClient.create(text);
		logger.info(text);
		AccountsWrapper accWrp = client.accept("text/xml").get(AccountsWrapper.class);
		List<Account> accounts = accWrp.getAccountList();

		cleanup();

		return accounts;
	}

	/**
	 * Returns the permission the given user has for the given entity. Includes permissionGroups
	 * 
	 * @param accountId
	 * @param entityType
	 * @param entityId
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public EntityMap getAccess(Long accountId, EntityType entityType, Long entityId)
			throws UnsupportedEncodingException, UserAccessDeniedException {

		ticketValid();

		String url = serverUrl + restServiceUrl + "entityMap/getAccess?entityType=" + entityType.toString();
		if (entityId != null) {
			url = url + "&entityId=" + entityId;
		}
		if (accountId != null) {
			url = url + "&accountId=" + accountId;
		}
		if (getTicketProperty() != ServiceConstants.EMPTY_STRING) {
			url = url + "&" + getTicketProperty();
		}

		
		Response response = WebClient.create(url).accept("text/xml").get();
		
		
		if(response.getStatus() == HttpStatus.FORBIDDEN.value()) {
			logger.info("RestAccountProvider -> getAccess :: User session invalidated");
			throw new UserAccessDeniedException();
		}
		
		EntityMap em = (EntityMap)response.readEntity(EntityMap.class);
		
		cleanup();
		return em;
	}

	/**
	 * Returns the permission the user has for the given entity. The last argument determines if permission groups are
	 * also searched.
	 * 
	 * @param accountId
	 * @param entityType
	 * @param entityId
	 * @param includePermissionGroups
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public EntityMap getAccess(Long accountId, EntityType entityType, Long entityId, boolean includePermissionGroups)
			throws UnsupportedEncodingException {

		ticketValid();

		WebClient client = WebClient.create(serverUrl + restServiceUrl + "entityMap/getAccess?entityType=" + entityType
				+ "&entityId=" + entityId + "&accountId=" + accountId + "&includePerGrps=" + includePermissionGroups
				+ "&" + getTicketProperty());

		// WebClient client = WebClient.create(serverUrl + restServiceUrl + "entityMap/getAccess");
		// client.query("entityType", entityType).query("entityId", entityId)
		// .query("includePerGrps", includePermissionGroups).path("&" + getTicketProperty())
		EntityMap em = client.accept("text/xml").get(EntityMap.class);

		cleanup();

		return em;
	}

	/**
	 * Returns the account of the owner of a given entity
	 * 
	 * @param entityId
	 * @param type
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Account getEntityOwnerAccount(Long entityId, EntityType type) throws UnsupportedEncodingException {

		ticketValid();

		WebClient client = WebClient.create(serverUrl + restServiceUrl + "getOwner?entityType=" + type + "&entityId="
				+ entityId + "&" + getTicketProperty());
		Account em = client.accept("text/xml").get(Account.class);

		cleanup();

		return em;
	}

	/**
	 * Calls the account Web Service to unregister an entity in User Management
	 * 
	 * @param entityId
	 * @param type
	 * @throws IOException
	 * @throws HttpException
	 */
	public void unregisterEntity(Long entityId, EntityType type) throws HttpException, IOException {

		ticketValid();

		DeleteMethod deleteMethod = new DeleteMethod(
				serverUrl + restServiceUrl + "unregister/" + type + "/" + entityId + "?" + getTicketProperty());
		try {
			client.executeMethod(deleteMethod);

		} finally {
			deleteMethod.releaseConnection();
		}

		cleanup();

	}

	public void unregisterEntityFromGroup(Long entityId, EntityType type, Long pgId) throws HttpException, IOException {

		ticketValid();

		DeleteMethod deleteMethod = null;
		if (getTicketProperty() != ServiceConstants.EMPTY_STRING) {
			deleteMethod = new DeleteMethod(serverUrl + restServiceUrl + "entityMap/unregister/fromGroup?entityType="
					+ type + "&entityId=" + entityId + "&permissionGroupId=" + pgId + "&" + getTicketProperty());
		} else {
			deleteMethod = new DeleteMethod(serverUrl + restServiceUrl + "entityMap/unregister/fromGroup?entityType="
					+ type + "&entityId=" + entityId + "&permissionGroupId=" + pgId);
		}

		try {
			client.executeMethod(deleteMethod);

		} finally {
			deleteMethod.releaseConnection();
		}

		cleanup();

	}

	/**
	 * Calls the account Web Service to unregister an entity in user management based on id
	 * 
	 * @param id
	 * @throws IOException
	 * @throws HttpException
	 */
	public void unregisterEntity(Long id) throws HttpException, IOException {

		ticketValid();

		DeleteMethod deleteMethod =
				new DeleteMethod(serverUrl + restServiceUrl + "entityMap/unregister/" + id + "?" + getTicketProperty());
		try {
			client.executeMethod(deleteMethod);
		} finally {
			deleteMethod.releaseConnection();
		}

		cleanup();

	}

	/**
	 * Calls the account Web Service to unregister an entity in User Management
	 * 
	 * @param entityId
	 * @param type
	 * @throws IOException
	 * @throws HttpException
	 */
	public void unregisterEntityList(List<EntityMap> removedEntityMapList) throws HttpException, IOException {

		ticketValid();

		List<Long> idList = new ArrayList<Long>();
		String urlList = "";

		for (EntityMap em : removedEntityMapList) {
			urlList += "id=" + em.getId() + "&";
		}

		String url = urlList.substring(0, urlList.length() - 1);

		DeleteMethod deleteMethod = new DeleteMethod(
				serverUrl + restServiceUrl + "entityMap/unregister/list?" + url + "&" + getTicketProperty());
		try {
			client.executeMethod(deleteMethod);

		} finally {
			deleteMethod.releaseConnection();
		}

		cleanup();

	}

	/**
	 * Calls the account webservice to add the given entityMap into the entityMap table
	 * 
	 * @param entityMapList
	 * @throws IOException
	 * @throws HttpException
	 */
	public void registerEntity(List<EntityMap> entityMapList) throws HttpException, IOException {

		ticketValid();

		// accountWebService.registerEntity(getUserLogin(), entityMapList);
		for (EntityMap em : entityMapList) {
			Long accountId = null;
			Long pgId = null;
			if (em.getAccount() != null) {
				accountId = em.getAccount().getId();
			}
			if (em.getPermissionGroup() != null) {
				pgId = em.getPermissionGroup().getId();
			}

			registerEntity(accountId, em.getType(), em.getEntityId(), em.getPermission(), pgId);
		}

		cleanup();

	}

	/**
	 * 
	 * @param accountId
	 * @param type
	 * @param entityId
	 * @param permission
	 * @param permissionGroupId
	 * @throws IOException
	 * @throws HttpException
	 */
	public void registerEntity(Long accountId, EntityType type, Long entityId, PermissionType permission,
			Long permissionGroupId) throws HttpException, IOException {

		String permGroupVal;
		String accountVal;
		ticketValid();
		if (accountId == null)
			accountVal = "noVal";
		else
			accountVal = accountId.toString();

		if (permissionGroupId == null)
			permGroupVal = "noVal";
		else
			permGroupVal = permissionGroupId.toString();

		PostMethod postMethod;
		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {
			
			logger.debug("Entity Map to be registered on "+ serverUrl+ " accountId: "+ accountVal+" entityId: "+
					entityId+" permission: "+permission);
			
			postMethod = new PostMethod(serverUrl + restServiceUrl + "entityMap/register?accountId=" + accountVal
					+ "&entityType=" + type + "&entityId=" + entityId + "&permission=" + permission
					+ "&permissionGroupId=" + permGroupVal + "&" + getTicketProperty());
		} else {
			
			logger.debug("Entity Map to be registered on "+ serverUrl+ " accountId: "+ accountVal+" entityId: "+
					entityId+" permission: "+permission);
			
			postMethod = new PostMethod(serverUrl + restServiceUrl + "entityMap/register?accountId=" + accountVal
					+ "&entityType=" + type + "&entityId=" + entityId + "&permission=" + permission
					+ "&permissionGroupId=" + permGroupVal);
		}
		try {
			client.executeMethod(postMethod);
		} finally {
			postMethod.releaseConnection();
		}

		cleanup();

	}

	/**
	 * 
	 * @param account
	 * @param type
	 * @param permission
	 * @param onlyGranted
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Set<Long> listUserAccess(Long accountId, EntityType type, PermissionType permission, Boolean onlyGranted)
			throws UnsupportedEncodingException {

		ticketValid();
		LongListWrapper llw;

		// WebClient client = WebClient.create(serverUrl + "/portal/ws/account/account/" + "entityMap/");

		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {
			WebClient client = WebClient.create(serverUrl + restServiceUrl + "entityMap/list?id=" + accountId
					+ "&entityType=" + type + "&permissionType=" + permission + "&isGranted=" + onlyGranted + "&"
					+ getTicketProperty());
			llw = client.accept("text/xml").get(LongListWrapper.class);

		} else {
			WebClient client = WebClient.create(serverUrl + restServiceUrl);
			llw = client.path("entityMap").path("list").query("id=" + accountId).query("entityType=" + type)
					.query("permissionType=" + permission).query("isGranted=" + onlyGranted).accept("text/xml")
					.get(LongListWrapper.class);
		}

		cleanup();
		return llw.getList();
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public PermissionGroup getPermissionGroup(String name) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client;
		if (!isProxyTicketNull()) {

			client = WebClient.create(serverUrl + restServiceUrl + "PermissionGroups/name?name="
					+ URLEncoder.encode(name, "UTF-8") + "&" + getTicketProperty());
		} else {
			client = WebClient.create(
					serverUrl + restServiceUrl + "PermissionGroups/name?name=" + URLEncoder.encode(name, "UTF-8"));
		}

		PermissionGroup pg = client.accept("text/xml").get(PermissionGroup.class);
		cleanup();

		return pg;
	}

	/**
	 * This function is now secure and can be used in production. Users can request only their own username. Admin users
	 * can request any username.
	 * 
	 * @param username
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Account getByUserName(String username) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client;
		logger.info("get account from url: " + serverUrl + restServiceUrl + "user for user: " + username);
		if (!getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {
			client = WebClient.create(serverUrl + restServiceUrl + "user/" + username + "?" + getTicketProperty());
		} else {
			client = WebClient.create(serverUrl + restServiceUrl + "user");
			client.path(username);
		}

		Account account = client.accept("text/xml").get(Account.class);
		cleanup();
		return account;
	}

	public List<EntityMap> listEntityAccess(Long entityId, EntityType type) throws UnsupportedEncodingException {

		ticketValid();
		EntityMapsWrapper emw;

		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {

			WebClient client = WebClient.create(serverUrl + restServiceUrl + "entityMap/listEntityAccess?entityId="
					+ entityId + "&entityType=" + type + "&" + getTicketProperty());
			emw = client.accept("text/xml").get(EntityMapsWrapper.class);
		} else {
			WebClient client = WebClient.create(serverUrl + restServiceUrl + "entityMap/listEntityAccess?entityId="
					+ entityId + "&entityType=" + type);
			emw = client.accept("text/xml").get(EntityMapsWrapper.class);
		}

		// WebClient client = WebClient.create(serverUrl + restServiceUrl + "entityMap/listEntityAccess");
		// EntityMapsWrapper emw = client.query("entityId", entityId).query("entityType", type)
		// .path("&" + getTicketProperty()).accept("text/xml").get(EntityMapsWrapper.class);
		cleanup();

		return emw.getEntityMapsList();
	}

	/**
	 * A web service replacement for AccountManager.getPrivatePermissionGroups()
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public List<PermissionGroup> getPrivatePermissionGroups() throws UnsupportedEncodingException {

		ticketValid();

		PermissionGroupsWrapper pgw;
		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {

			WebClient client =
					WebClient.create(serverUrl + restServiceUrl + "PermissionGroups/private?" + getTicketProperty());
			pgw = client.accept("text/xml").get(PermissionGroupsWrapper.class);

		} else {
			WebClient client =
					WebClient.create(serverUrl + restServiceUrl + "PermissionGroups/private?" + getTicketProperty());
			pgw = client.accept("text/xml").get(PermissionGroupsWrapper.class);
		}
		cleanup();

		return pgw.getPermissionGroupsList();
	}

	/**
	 * A web service replacement for AccountManager.getPermissionGroupById(Long id)
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public PermissionGroup getPermissionGroupById(Long id) throws UnsupportedEncodingException {

		ticketValid();

		WebClient client;
		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {

			client = WebClient
					.create(serverUrl + restServiceUrl + "PermissionGroups/id?id=" + id + "&" + getTicketProperty());
		} else {
			client = WebClient.create(serverUrl + restServiceUrl + "PermissionGroups/id?id=" + id);
		}

		cleanup();

		return client.accept("text/xml").get(PermissionGroup.class);
	}

	/**
	 * 
	 * List of all the members (accounts) attached to a permission group
	 * 
	 * @param groupName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public List<PermissionGroupMember> getPermissionGroupMembers(String groupName) throws UnsupportedEncodingException {

		ticketValid();

		WebClient client;
		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {
			client = WebClient.create(serverUrl + restServiceUrl + "PermissionGroups/name/" + encodeUrlParam(groupName)
					+ "/getMember?" + getTicketProperty());
		} else {
			client = WebClient.create(
					serverUrl + restServiceUrl + "PermissionGroups/name/" + encodeUrlParam(groupName) + "/getMember");
		}

		cleanup();

		return client.accept("text/xml").get(PermissionGroupMemberWrapper.class).getPermissionGroupMembersList();
	}

	/**
	 * 
	 * List of all the members (accounts) attached to a permission group
	 * 
	 * @param groupID
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public List<PermissionGroupMember> getPermissionGroupMembers(Long groupID) throws UnsupportedEncodingException {

		ticketValid();

		WebClient client;
		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {
			client = WebClient.create(serverUrl + restServiceUrl + "PermissionGroups/id/" + groupID + "/getMember?"
					+ getTicketProperty());
		} else {
			client = WebClient.create(serverUrl + restServiceUrl + "PermissionGroups/id/" + groupID + "/getMember");
		}

		cleanup();

		return client.accept("text/xml").get(PermissionGroupMemberWrapper.class).getPermissionGroupMembersList();
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
	public Boolean isGroupMember(String groupName, Long accountID) throws UnsupportedEncodingException {

		ticketValid();

		WebClient client;

		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {
			client = WebClient.create(serverUrl + restServiceUrl + "PermissionGroups/" + encodeUrlParam(groupName)
					+ "/isMember?id=" + accountID + "&" + getTicketProperty());
		} else {
			client = WebClient.create(serverUrl + restServiceUrl + "PermissionGroups/" + encodeUrlParam(groupName)
					+ "/isMember?id=" + accountID);
		}

		cleanup();

		return client.accept("text/xml").get(PerGrpMemBool.class).isWorking();
	}

	public List<EntityMap> getEntityOwner(Set<Long> entityIds, EntityType type) throws UnsupportedEncodingException {
		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/EntityMap/Owned");
		for (Long id : entityIds) {
			client.query("ids", id);
		}
		client.query("entityType", type);
		String ac = client.getCurrentURI().toString();
		WebClient.create(ac + "&" + getTicketProperty());
		EntityMapsWrapper entityMapWrapper = client.accept("text/xml").get(EntityMapsWrapper.class);
		cleanup();
		return entityMapWrapper.getEntityMapsList();
	}

	public List<EntityMap> listUserAccessEntities(EntityType type, PermissionType permission, boolean onlyGranted)
			throws UnsupportedEncodingException {
		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/EntityMap/MyAccess");

		client.query("entityType", type);
		client.query("permission", permission);
		client.query("onlyGranted", onlyGranted);

		String ac = client.getCurrentURI().toString();
		client = WebClient.create(ac + "&" + getTicketProperty());
		logger.info(client.getCurrentURI().toString());
		EntityMapsWrapper entityMapWrapper = client.accept("text/xml").get(EntityMapsWrapper.class);
		cleanup();
		return entityMapWrapper.getEntityMapsList();
	}

	/**
	 * Method to keep session alive
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Response keepSessionAlive() throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "keepAlive");
		Response response = client.get();
		cleanup();

		return response;
	}

	/**
	 * Returns a list of entity maps using the given entity IDs. This called needs to be a POST because they could be
	 * enough entity IDs passed in that goes beyond the maximum of parameters allowed for a GET call.
	 * 
	 * @param entityIds
	 * @param entityType
	 * @return
	 * @throws IOException
	 * @throws HttpException
	 * @throws UnsupportedEncodingException
	 */
	public List<EntityMap> getEntityMapsByEntityIds(List<Long> entityIds, EntityType entityType)
			throws UnsupportedEncodingException {
		ticketValid();
		PostMethod postMethod = null;
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(serverUrl).append(restServiceUrl).append("EntityMap/").append(entityType.name())
				.append("/getByEntityIds");

		// append the proxy ticket
		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {
			urlBuilder.append("?").append(getTicketProperty());
		}

		logger.info("POST URL: " + urlBuilder.toString());

		EntityMapList entityMapList = null;

		try {
			postMethod = new PostMethod(urlBuilder.toString());

			// add all the entity IDs as a form param
			for (Long entityId : entityIds) {
				postMethod.addParameter("entityIds", entityId.toString());
			}

			getClient().executeMethod(postMethod);

			// unmarshall the entity map list response
			byte[] responseBody = postMethod.getResponseBody();
			JAXBContext jc = JAXBContext.newInstance(EntityMapList.class);
			entityMapList = (EntityMapList) jc.createUnmarshaller().unmarshal(new ByteArrayInputStream(responseBody));
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
			cleanup();
		}
		
		return entityMapList.getEntityMapList();
	}
	
	
	/**
	 * Returns entities the user has permission to based on the entity type
	 * @param entityId
	 * @param type
	 * @return List<EntityMap>
	 * @throws UnsupportedEncodingException
	 */
	public List<EntityMap> listEntityAccessByAccount(Long accountId, EntityType type) throws UnsupportedEncodingException {

		ticketValid();
		EntityMapsWrapper emw;

		if (!isProxyTicketNull() && !getTicketProperty().equals(ServiceConstants.EMPTY_STRING)) {

			WebClient client = WebClient.create(serverUrl + restServiceUrl + "entityMap/listEntityAccessByAccount?accountId="
					+ accountId + "&entityType=" + type + "&" + getTicketProperty());
			emw = client.accept("text/xml").get(EntityMapsWrapper.class);
		} else {
			WebClient client = WebClient.create(serverUrl + restServiceUrl + "entityMap/listEntityAccessByAccount?accountId="
					+ accountId + "&entityType=" + type);
			emw = client.accept("text/xml").get(EntityMapsWrapper.class);
		}

		cleanup();

		return emw.getEntityMapsList();
	}

	public List<User> getUserDetailsByIds(Set<Long> userIds) throws UnsupportedEncodingException {

		ticketValid();
		WebClient client = WebClient.create(serverUrl + restServiceUrl + "/User/getDetailsByIds");

		for (Long userId : userIds) {
			client.query("userId", userId);
		}

		String ac = client.getCurrentURI().toString();
		WebClient.create(ac + "&" + getTicketProperty());
		UserList userLists = client.accept("text/xml").get(UserList.class);

		cleanup();

		return userLists.getUsers();

	}

	/***************************************************************************************************/

}
