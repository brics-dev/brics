

package gov.nih.tbi.account.ws;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.ws.model.UserLogin;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;

import java.util.List;
import java.util.Set;

import javax.jws.WebService;

/**
 * Interface that defines a web service contract
 * 
 * @author Michael Valeiras
 * 
 */
@WebService
public interface AccountWebService
{

    /**
     * Get a full account record for the user with the requested id. Note, non-admin users can only retrieve their own
     * record.
     * 
     * @param id
     * @return
     */
    public Account getAccountDetailsById(UserLogin userLogin, Long id);

    /**
     * Returns a list of users who have ROLE. The role heirachy is used to determine what users have what roles (A
     * dictionary_admin implicitly has the roles dictionary, and user etc.) NOTE: In order to use this function, a user
     * must be an admin of the role he/she is implying. Only a full admin user can request the full list of users.
     * 
     * @param role
     * @return
     */
    public List<Account> getAccountsWithRole(UserLogin userLogin, String role);

    /**
     * Searches the entityMap table for a record that matches all the given criteria. The returned entityMap object
     * contains the permission level that the given account has with the specified entity.
     * 
     * @param user
     * @param accountId
     * @param type
     * @param entityId
     * @param includePermissionGroups
     * @return
     */
    public EntityMap getAccess(UserLogin userLogin, Long accountId, EntityType type, Long entityId,
            boolean includePermissionGroups);

    /**
     * A web service that returns the owner of a given entity
     * 
     * @param user
     * @param entityId
     * @param type
     * @return
     */
    public Account getEntityOwnerAccount(UserLogin userLogin, Long entityId, EntityType type);

    /**
     * Uses a web service to tell the EM to unregister the given entity
     * 
     * @param user
     * @param entityId
     * @param type
     */
    public void unregisterEntity(UserLogin userLogin, Long entityId, EntityType type);

    /**
     * Remove a single map entity recored based on id
     * 
     * @param userLogin
     * @param id
     */
    void unregisterEntityById(UserLogin userLogin, Long id);

    public void unregisterEntityList(UserLogin userLogin, List<EntityMap> removedEntityMapList);

    /**
     * 
     * @param user
     * @param accountId
     * @param type
     * @param entityId
     * @param permission
     * @param permissionGroupId
     */
    public void registerEntity(UserLogin userLogin, Long accountId, EntityType type, Long entityId,
            PermissionType permission, Long permissionGroupId);

    /**
     * 
     * @param user
     * @param account
     * @param type
     * @param permission
     * @param onlyGranted
     * @return
     */
    public Set<Long> listUserAccess(UserLogin userLogin, Long accountId, EntityType type, PermissionType permission,
            Boolean onlyGranted);

    /**
     * List all users and their access to this entity
     * 
     * @param entityId
     * @param type
     * @return
     */
    public List<EntityMap> listEntityAccess(UserLogin userLogin, Long entityId, EntityType type);

    /**
     * 
     * @param user
     * @param name
     * @return
     */
    public PermissionGroup getPermissionGroup(UserLogin userLogin, String name);

    public Account getByUserName(UserLogin userLogin, String username);

    public Account getAccountDetails(UserLogin userLogin);

    public List<PermissionGroup> getPrivatePermissionGroups(UserLogin userLogin);

    public PermissionGroup getPermissionGroupById(UserLogin userLogin, Long id);

    public boolean hasRole(UserLogin userLogin, Account account, RoleType roleType);
}
