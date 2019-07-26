
package gov.nih.tbi.account.ws.cxf;

import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.dao.EntityMapDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.ws.AccountWebService;
import gov.nih.tbi.account.ws.model.UserLogin;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.ws.HashMethods;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;

/**
 * CXF implementation of the Account Web Service
 * 
 * @author Michael Valeiras
 * 
 */
@WebService(endpointInterface = "gov.nih.tbi.account.ws.AccountWebService")
public class AccountWebServiceImpl implements AccountWebService
{

    /***************************************************************************************************/

    private static Logger logger = Logger.getLogger(AccountWebServiceImpl.class);

    /***************************************************************************************************/

    @Autowired
    private AccountManager accountManager;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private RoleHierarchy roleHierarchy;

    @Autowired
    private EntityMapDao entityMapDao;

    @Resource
    private WebServiceContext wsContext;

    /***************************************************************************************************/

    
    public boolean hasRole(UserLogin userLogin, Account account, RoleType roleType)
    {
        logger.debug("hasRole(" + userLogin.getUserName() + ", " + roleType.getTitle() + " ) -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }
        
        return accountManager.hasRole(account, roleType);
    }
    
    /**
     * Replaced with REST
     * 
     * @inheritDoc
     */
    @SuppressWarnings("unchecked")
    @Override
    public Account getAccountDetailsById(UserLogin userLogin, Long id)
    {

        logger.debug("getAccountDetails(" + userLogin.getUserName() + ", " + id.toString() + " ) -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        Account account = accountManager.getAccountByUserName(userLogin.getUserName());

        // if (account != null && (accountManager.hasRole(account, RoleType.ROLE_ADMIN) || account.getId().equals(id)))
        // {
        Account returnedAccount = accountManager.getAccount(account.getUser(), id);
        return returnedAccount;
        // }

        // throw new RuntimeException("User " + account.getUserName() + "is not authorized to get account with id: " +
        // id);

    }

    /**
     * Replaced with REST
     * 
     * @inheritDoc
     */
    @SuppressWarnings("unchecked")
    @Override
    public Account getAccountDetails(UserLogin userLogin)
    {

        logger.debug("getAccountDetails(" + userLogin.getUserName() + "-- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        Account account = accountManager.getAccountByUserName(userLogin.getUserName());
        return account;

        // TODO: NEED TO CHECK DB passwords
        // throw new RuntimeException("User " + account.getUserName() + "is not authorized to get account with id: " +
        // id);

    }

    /**
     * Replaced with REST
     * 
     * @inheritDoc
     */
    @Override
    public List<Account> getAccountsWithRole(UserLogin userLogin, String role)
    {

        logger.debug("getAccountsWithRole(" + userLogin.getUserName() + ", " + role + " ) -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }
        if (role == null)
        {
            throw new RuntimeException("Argument 'role' cannot be null");
        }
        RoleType roleType = RoleType.getByName(role);
        if (roleType == null)
        {
            throw new RuntimeException("Unrecognized role: " + role);
        }

        // Account account = accountManager.getAccountByUserName(userLogin.getUserName());
        //
        // if (!checkRoleAccess(account, roleType))
        // {
        // throw new RuntimeException("User " + userLogin.getUserName() + " does not have access to " + role
        // + " user list.");
        // }

        List<Account> list = accountManager.getAccountListByRole(roleType, true);
        return list;

    }

    /**
     * @inheritDoc
     */
    @Override
    public EntityMap getAccess(UserLogin userLogin, Long accountId, EntityType type, Long entityId,
            boolean includePermissionGroups)
    {

        logger.debug("getAccess -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        Account account = accountManager.getAccountByUserName(userLogin.getUserName());
        EntityMap map = accountManager.getAccess(account, type, entityId, includePermissionGroups);

        return map;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Account getEntityOwnerAccount(UserLogin userLogin, Long entityId, EntityType type)
    {

        logger.debug("getEntityOwnerAccount -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        EntityMap em = entityMapDao.getOwnerEntityMap(entityId, type);
        Account account = accountDao.get(em.getAccount().getId());
        return account;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void unregisterEntity(UserLogin userLogin, Long entityId, EntityType type)
    {

        logger.debug("unregisterEntity -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        accountManager.unregisterEntity(type, entityId);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void unregisterEntityList(UserLogin userLogin, List<EntityMap> removedEntityMapList)
    {

        logger.debug("unregisterEntityList -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        accountManager.unregisterEntity(removedEntityMapList);
    }

    /**
     * @ineritDoc
     */
    @Override
    public void unregisterEntityById(UserLogin userLogin, Long id)
    {

        logger.debug("unregisterEntity -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        // Account requestingAccount = accountManager.getAccountByUserName(userLogin.getUserName());
        // EntityMap em = entityMapDao.get(id);
        // if (!accountManager.getAccess(requestingAccount, em.getType(), em.getEntityId(), PermissionType.ADMIN))
        // {
        // throw new RuntimeException("User does not have privileges to unregister entities for :"
        // + em.getType().getName() + em.getEntityId());
        // }

        entityMapDao.remove(id);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void registerEntity(UserLogin userLogin, Long accountId, EntityType type, Long entityId,
            PermissionType permission, Long permissionGroupId)
    {

        logger.debug("registerEntity -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        Account requestingAccount = accountManager.getAccountByUserName(userLogin.getUserName());
        if (requestingAccount != null)
        {
            if (accountId != null && accountManager.getAccount(requestingAccount.getUser(), accountId) != null)
            {

                accountManager.registerEntity(accountManager.getAccount(requestingAccount.getUser(), accountId), type,
                        entityId, permission);
            }
            if (permissionGroupId != null)
            {
                accountManager.registerEntity(accountManager.getPermissionGroupById(permissionGroupId), type, entityId,
                        permission);
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set<Long> listUserAccess(UserLogin userLogin, Long accountId, EntityType type, PermissionType permission,
            Boolean onlyGranted)
    {

        logger.debug("listUserAccess -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        Account requestingAccount = accountManager.getAccountByUserName(userLogin.getUserName());
        Account account = accountManager.getAccount(requestingAccount.getUser(), accountId);

        return accountManager.listUserAccess(account, type, permission, onlyGranted);
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<EntityMap> listEntityAccess(UserLogin userLogin, Long entityId, EntityType type)
    {

        logger.debug("listEntityAccess -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        List<EntityMap> list = accountManager.listEntityAccess(entityId, type);

        for (EntityMap em : list)
        {
            if (em.getAccount() != null)
            {
                em.setAccount(accountManager.getAccount(em.getAccount().getUser(), em.getAccount().getId()));
            }
            else
                if (em.getPermissionGroup() != null)
                {
                    em.setPermissionGroup(accountManager.getPermissionGroup(em.getPermissionGroup().getGroupName()));
                }
        }
        return list;
    }

    /**
     * @inheritDoc
     */
    @Override
    public PermissionGroup getPermissionGroup(UserLogin userLogin, String name)
    {

        logger.debug("getPermissionGroup -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        return accountManager.getPermissionGroup(name);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Account getByUserName(UserLogin userLogin, String username)
    {

        logger.debug("getByUserName -- ");

        // Make sure this guy is who he says he is.
        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }
        Account requestingAccount = accountDao.getByUserName(userLogin.getUserName());
        Account account = accountDao.getByUserName(username);
        // Anyone can get their own information.
        if (userLogin.getUserName().equals(username))
        {
            return account;
        }

        // Admins can get any account
        if (accountManager.hasRole(requestingAccount, RoleType.ROLE_ADMIN))
        {
            return account;
        }
        throw new RuntimeException("Failed to validate ROLE_ADMIN.");
    }

    @Override
    public List<PermissionGroup> getPrivatePermissionGroups(UserLogin userLogin)
    {

        logger.debug("getPrivatePermissionGroups -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        return accountManager.getPrivatePermissionGroups();
    }

    @Override
    public PermissionGroup getPermissionGroupById(UserLogin userLogin, Long id)
    {

        logger.debug("getPrivatePermissionGroups -- ");

        if (!HashMethods.getClientHash(userLogin.getUserName()).equals(userLogin.getHash1()))
        {
            throw new RuntimeException("Failed to validate Username.");
        }

        return accountManager.getPermissionGroupById(id);

    }

}
