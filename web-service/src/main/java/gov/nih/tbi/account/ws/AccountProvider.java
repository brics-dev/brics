
package gov.nih.tbi.account.ws;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;

import org.apache.log4j.Logger;

/**
 * Provides a lightweight implemenation of the client side classes needed to utilize a JaxWS Service.
 * 
 * @author Michael Valeiras
 * 
 */
public class AccountProvider extends AuthenticationProvider
{

    /***************************************************************************************************/

    private static Logger logger = Logger.getLogger(AccountProvider.class);

    private static final String namespaceURI = "http://cxf.ws.account.tbi.nih.gov/";
    private static final String serviceName = "AccountWebServiceImplService";
    private static final String portName = "AccountWebServiceImplPort";
    private static final String wsdlExtension = "portal/ws/accountWebService?wsdl";

    private static URL WSDL_LOCATION;

    /***************************************************************************************************/

    private Service service;
    private AccountWebService accountWebService;

    /***************************************************************************************************/

    /**
     * Constructor that takes the web service WSDL URL, username, and a password
     * 
     * @param acocuntWsdlLocation
     * @throws MalformedURLException
     */
    public AccountProvider(String serverLocation, String userName, String password) throws MalformedURLException
    {

        super(serverLocation, userName, password);
        WSDL_LOCATION = new URL(AccountProvider.class.getResource("."), serverLocation + wsdlExtension);

        service = Service.create(WSDL_LOCATION, new QName(namespaceURI, serviceName));
        accountWebService = getAccountService();
    }

    public AccountProvider() throws MalformedURLException
    {

        super();
    }

    /**
     * Constructor that takes the web service WSDL URL
     * 
     * @param serverLocation
     * @throws MalformedURLException
     */
    public AccountProvider(String serverLocation) throws MalformedURLException
    {

        this(serverLocation, "anonymous", "");
    }

    /**
     * Creates a new connection to consume the web service. This should only need to be called once in a program.
     * 
     * @return
     */
    @WebEndpoint(name = "accountWebService")
    private AccountWebService getAccountService()
    {

        return service.getPort(new QName(namespaceURI, portName), AccountWebService.class);
    }

    /***************************************************************************************************/

    /**
     * Returns the details for the given account
     * 
     * @param id
     * @return
     */
    public Account getAccountById(Long id)
    {

        Account account = accountWebService.getAccountDetailsById(getUserLogin(), id);
        return account;
    }

    /**
     * Returns the details for the given account
     * 
     * @return
     */
    public Account getAccount()
    {

        Account account = accountWebService.getAccountDetails(getUserLogin());
        return account;
    }

    /**
     * Get a list of users with the specified role.
     * 
     * @param roleType
     * @return
     */
    public List<Account> getAccountsWithRole(String role)
    {

        return accountWebService.getAccountsWithRole(getUserLogin(), role);
    }

    /**
     * Returns the permission the given user has for the given entity. Includes permissionGroups
     * 
     * @param accountId
     * @param entityType
     * @param entityId
     * @return
     */
    public EntityMap getAccess(Long accountId, EntityType entityType, Long entityId)
    {

        return getAccess(accountId, entityType, entityId, true);
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
     */
    public EntityMap getAccess(Long accountId, EntityType entityType, Long entityId, boolean includePermissionGroups)
    {

        return accountWebService.getAccess(getUserLogin(), accountId, entityType, entityId, includePermissionGroups);
    }

    /**
     * Returns the account of the owner of a given entity
     * 
     * @param entityId
     * @param type
     * @return
     */
    public Account getEntityOwnerAccount(Long entityId, EntityType type)
    {

        return accountWebService.getEntityOwnerAccount(getUserLogin(), entityId, type);
    }

    /**
     * Calls the account Web Service to unregister an entity in User Management
     * 
     * @param entityId
     * @param type
     */
    public void unregisterEntity(Long entityId, EntityType type)
    {

        accountWebService.unregisterEntity(getUserLogin(), entityId, type);
    }

    /**
     * Calls the account Web Service to unregister an entity in user management based on id
     * 
     * @param id
     */
    public void unregisterEntity(Long id)
    {

        accountWebService.unregisterEntityById(getUserLogin(), id);
    }

    /**
     * Calls the account Web Service to unregister an entity in User Management
     * 
     * @param entityId
     * @param type
     */
    public void unregisterEntityList(List<EntityMap> removedEntityMapList)
    {

        for (EntityMap em : removedEntityMapList)
        {
            accountWebService.unregisterEntityById(getUserLogin(), em.getId());
        }
    }

    /**
     * Calls the account webservice to add the given entityMap into the entityMap table
     * 
     * @param entityMapList
     */
    public void registerEntity(List<EntityMap> entityMapList)
    {

        // accountWebService.registerEntity(getUserLogin(), entityMapList);
        for (EntityMap em : entityMapList)
        {
            Long accountId = null;
            Long pgId = null;
            if (em.getAccount() != null)
            {
                accountId = em.getAccount().getId();
            }
            if (em.getPermissionGroup() != null)
            {
                pgId = em.getPermissionGroup().getId();
            }

            registerEntity(accountId, em.getType(), em.getEntityId(), em.getPermission(), pgId);
        }

    }

    /**
     * 
     * @param accountId
     * @param type
     * @param entityId
     * @param permission
     * @param permissionGroupId
     */
    public void registerEntity(Long accountId, EntityType type, Long entityId, PermissionType permission,
            Long permissionGroupId)
    {

        accountWebService.registerEntity(getUserLogin(), accountId, type, entityId, permission, permissionGroupId);
    }
    

    /**
     * 
     * @param account
     * @param type
     * @param permission
     * @param onlyGranted
     * @return
     */
    public Set<Long> listUserAccess(Long accountId, EntityType type, PermissionType permission, Boolean onlyGranted)
    {

        return accountWebService.listUserAccess(getUserLogin(), accountId, type, permission, onlyGranted);
    }

    /**
     * 
     * @param name
     * @return
     */
    public PermissionGroup getPermissionGroup(String name)
    {

        return accountWebService.getPermissionGroup(getUserLogin(), name);
    }

    /**
     * This function is now secure and can be used in production. Users can request only their own username. Admin users
     * can request any username.
     * 
     * @param username
     * @return
     */
    public Account getByUserName(String username)
    {

        return accountWebService.getByUserName(getUserLogin(), username);
    }

    public List<EntityMap> listEntityAccess(Long entityId, EntityType type)
    {

        return accountWebService.listEntityAccess(getUserLogin(), entityId, type);
    }

    /**
     * A web service replacement for AccountManager.getPrivatePermissionGroups()
     * 
     * @return
     */
    public List<PermissionGroup> getPrivatePermissionGroups()
    {

        return accountWebService.getPrivatePermissionGroups(getUserLogin());
    }

    /**
     * A web service replacement for AccountManager.getPermissionGroupById(Long id)
     * 
     * @param id
     * @return
     */
    public PermissionGroup getPermissionGroupById(Long id)
    {

        return accountWebService.getPermissionGroupById(getUserLogin(), id);
    }
    
    public boolean hasRole(Account account, RoleType type)
    {
        return accountWebService.hasRole(getUserLogin(), account, type);
    }
}
