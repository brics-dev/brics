
package gov.nih.tbi.commons.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.ws.AccountProvider;
import gov.nih.tbi.account.ws.AuthenticationProvider;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.dictionary.model.UpdateFormStructureReferencePayload;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.ws.DictionaryProvider;
import gov.nih.tbi.repository.ws.RepositoryProvider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.httpclient.HttpException;

public interface WebServiceManager
{

    public AccountProvider getAccountProvider(Account account, Long diseaseId);

    public DictionaryProvider getDictionaryProvider(Account account, Long diseaseId);

    public RepositoryProvider getRepositoryProvider(Account account, Long diseaseId);

    public AuthenticationProvider getAuthenticationProvider(Account account, Long diseaseId);

    /**
     * Searches all the account modules for the owner of the given account.
     * 
     * @param account
     * @param entityId
     * @param type
     * @return
     * @throws MalformedURLException
     */
    // public Account getEntityOwnerAccount(Account account, Long entityId, EntityType type) throws
    // MalformedURLException;

    /**
     * Compiles a list of entityMaps for the given entity by querying all diseases and merging the results
     * 
     * @param account
     * @param entityId
     * @param type
     * @return
     * @throws MalformedURLException
     */
    // public List<EntityMap> listEntityAccess(Account account, Long entityId, EntityType type)
    // throws MalformedURLException;

    /**
     * Set the DataStoreInfo Archive flag for every repository this data structure belongs to.
     * 
     * @param account
     * @param dataStructureId
     * @param isArchived
     */
    void setDataStoreInfoArchive(Account account, FormStructure structure, boolean isArchived);

    /**
     * Calls RepositoryProvider.createTableFromDataStructure for the ST URL for every disease this form structure
     * belongs in (that exists). If there is only 1 ST URL than that URL is used and if the structure contains the
     * disease "General" then every ST URL is called.
     * 
     * @param account
     * @param datastructure
     * @throws Exception
     * @throws SQLException
     */
    public boolean createTableFromDataStructure(Account account, StructuralFormStructure datastructure) throws SQLException,
            Exception;

    /**
     * Calls the AccountProvider.unregisterEntityList for every accountProvider, passing in the removedEntityMapList.
     * EntityMaps not on the list are ignored by the dao when removing entries from the EntityMap table.
     * 
     * @param account
     * @param removedEntityMapList
     * @throws MalformedURLException
     */
    // public void unregisterEntityList(Account account, List<EntityMap> removedEntityMapList)
    // throws MalformedURLException;

    /**
     * Calls the AccountProvider.registerEntity(List<EntityMap>) for every accountProvider.
     * 
     * @param account
     * @param entityList
     * @throws MalformedURLException
     */
    // public void registerEntityList(Account account, List<EntityMap> entityList) throws MalformedURLException;

    /**
     * Calls the AccountProvider.getAccountListByRole web service all for every accountProvider and combines the results
     * into 1. This function also tags all the accounts returned with a disease tag.
     * 
     * @param account
     * @param role
     * @return
     * @throws MalformedURLException
     */
    // public List<Account> getAccountListByRole(Account account, RoleType role) throws MalformedURLException;

    /**
     * Calls the AccountProvider.getPrivatePermssionGroups() web service call for every accountProvider and combines the
     * results into 1 list. This function also tags all the accounts returned with a disease tag.
     * 
     * @param account
     * @return
     * @throws MalformedURLException
     */
    // public List<PermissionGroup> getPrivatePermssionGroups(Account account) throws MalformedURLException;

    /**
     * Adds the entity to the permission group for all diseases This will only work if the administrator credentials in
     * modules.properties are the same for all diseases of an environment
     * 
     * @param account
     * @param groupName
     * @param type
     * @param id
     * @param permission
     * @throws MalformedURLException
     */
    public void addToPermissionGroup(Account account, String groupName, EntityType type, Long id,
            PermissionType permission) throws MalformedURLException;

    /************************************************************************/

    /**
     * Searches all the account modules for the owner of the given account.
     * 
     * @param account
     * @param entityId
     * @param type
     * @return
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    public Account getEntityOwnerAccountRestful(Account account, Long entityId, EntityType type)
            throws MalformedURLException, UnsupportedEncodingException;

    /**
     * Compiles a list of entityMaps for the given entity by querying all diseases and merging the results
     * 
     * @param account
     * @param entityId
     * @param type
     * @return
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    public List<EntityMap> listEntityAccessRestful(Account account, Long entityId, EntityType type)
            throws MalformedURLException, UnsupportedEncodingException;

    /**
     * Calls the AccountProvider.unregisterEntityList for every accountProvider, passing in the removedEntityMapList.
     * EntityMaps not on the list are ignored by the dao when removing entries from the EntityMap table.
     * 
     * @param account
     * @param removedEntityMapList
     * @throws MalformedURLException
     * @throws IOException
     * @throws HttpException
     */
    public void unregisterEntityListRestful(Account account, List<EntityMap> removedEntityMapList)
            throws MalformedURLException, HttpException, IOException;

    /**
     * Calls the AccountProvider.registerEntity(List<EntityMap>) for every accountProvider.
     * 
     * @param account
     * @param entityList
     * @throws MalformedURLException
     * @throws IOException
     * @throws HttpException
     */
    public void registerEntityListRestful(Account account, List<EntityMap> entityList, String[] proxyTicketArr)
            throws MalformedURLException, HttpException, IOException;

    /**
     * Calls the AccountProvider.getAccountListByRole web service all for every accountProvider and combines the results
     * into 1. This function also tags all the accounts returned with a disease tag.
     * 
     * @param account
     * @param role
     * @return
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    public List<Account> getAccountListByRoleRestful(Account account, RoleType role) throws MalformedURLException,
            UnsupportedEncodingException;

    /**
     * Calls the AccountProvider.getPrivatePermssionGroups() web service call for every accountProvider and combines the
     * results into 1 list. This function also tags all the accounts returned with a disease tag.
     * 
     * @param account
     * @return
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    public List<PermissionGroup> getPrivatePermssionGroupsRestful(Account account) throws MalformedURLException,
            UnsupportedEncodingException;

    /**
     * Adds the entity to the permission group for all diseases This will only work if the administrator credentials in
     * modules.properties are the same for all diseases of an environment
     * 
     * @param account
     *            : The account doing the registering (Must be admin)
     * @param groupName
     * @param type
     * @param id
     * @param permission
     * @throws IOException
     * @throws HttpException
     * @throws MalformedURLException
     */
    public void registerEntityToPermissionGroup(Account account, String groupName, EntityType type, Long id,
            PermissionType permission) throws HttpException, IOException;

    /**
     * Remove the entity to the permission group for all diseases
     * 
     * 
     * @param groupName
     * @param type
     * @param id
     * @param permission
     * @throws HttpException
     * @throws IOException
     */
    public void unregisterEntityToPermissionGroup(String groupName, EntityType type, Long id, PermissionType permission)
            throws HttpException, IOException;
    
    /**
     * Update the ID references in each environment after a form structure has been versioned
     * @param account
     * @param payload
     * @return
     * @throws SQLException 
     */
    public boolean updateFormStructureReferences(Account account, UpdateFormStructureReferencePayload payload) throws SQLException;
}
