
package gov.nih.tbi.account.dao;

import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;

public interface EntityMapDao extends GenericDao<EntityMap, Long> {

	/**
	 * Used to get singular account privileges
	 * 
	 * @param account
	 * @param type
	 * @param entityId
	 * @return
	 */
	public EntityMap get(Account account, EntityType type, Long entityId);

	/**
	 * Overloaded get, used to include rules for admin and permission groups
	 * 
	 * @param account
	 * @param currentGroups
	 * @param type
	 * @param entityId
	 * @param isAdmin
	 * @return
	 */
	public EntityMap get(Account account, Set<PermissionGroup> currentGroups, EntityType type, Long entityId,
			boolean isAdmin);

	public List<EntityMap> listUserAccess(Account account, Set<PermissionGroup> currentGroups, EntityType type,
			boolean isAdmin);

	/**
	 * Retrieves a listing of EnityMap objects that match the given entity ID and type. Any entity maps that are
	 * associated with deactivated accounts will not be included in the listing.
	 * 
	 * @param entityId
	 * @param type
	 * @return
	 */
	public List<EntityMap> listEntityAccess(Long entityId, EntityType type);

	/**
	 * Returns the EntityMap with PermissionType of Owner for the given entityId
	 * 
	 * @param entityId of the entity to search for the owner of
	 * @param type of the entity
	 * @return
	 */
	public EntityMap getOwnerEntityMap(Long entityId, EntityType type);

	/**
	 * Returns the set of entities of a given type owned by the given account
	 * 
	 * @param account the owner of the entities
	 * @param type the type of entities
	 * @return
	 */
	public Set<EntityMap> listOwnedEntities(Account account, EntityType type);

	public EntityMap get(PermissionGroup permissionGroup, EntityType type, Long entityId);

	public List<EntityMap> getEntityMapsForDatasetByDatasetStatus(DatasetStatus status);

	public List<EntityMap> get(Long entityId, EntityType type);

	public EntityMap get(PermissionGroup permissionGroup, EntityType type, Long entityId, boolean isFlag);

	public List<EntityMap> getEntityMapsOfOwners(List<Long> ids, EntityType type);

	public List<EntityMap> getEntityForAccount(Account account);

	/**
	 * Creates or updates the given list of entity maps to the database, then returns the synced values back to the
	 * caller. If there is an exception thrown while saving any of the listed entity maps, all changes will be rolled
	 * back. In other words, the changes will not persist to the database until all changes are successful.
	 * 
	 * @param entityMapList - A listing of updated or new entity maps to be saved to the database.
	 * @return A list of entity maps, whose data has been synced with the values from the database.
	 * @throws HibernateException When there is an error connecting to the database or when saving any of the entity
	 *         maps.
	 */
	public List<EntityMap> createUpdateEntities(List<EntityMap> entityMapList) throws HibernateException;

	/**
	 * Remove the entity map entries that are referenced by the given list of IDs from the database. The removed
	 * entities will then be returned. The deletions are done atomically. So if any of the deletions fail, all changes
	 * are rolled back.
	 * 
	 * @param ids - A list of entity map IDs to be removed.
	 * @return A list of EntityMap objects that were removed from the database.
	 * @throws HibernateException When there is a database error while removing the entity maps. Any changes before the
	 *         error will be rolled back.
	 */
	public List<EntityMap> removeEntityMaps(List<Long> ids) throws HibernateException;
	

	/**
	 * Returns a list of entity maps by entity IDs
	 * 
	 * @param account
	 * @param permissionGroups
	 * @param entityIds
	 * @param entityType
	 * @return
	 */
	public List<EntityMap> getByEntityIds(Account account, Set<PermissionGroup> permissionGroups, Set<Long> entityIds,
			EntityType entityType);

	/**
	 * Removes all entity maps from the database that corresponds to the given entity type and entity ID.
	 * 
	 * @param type - The entity type of the entity maps to be removed.
	 * @param entityId - The ID of the entity that will be used to remove the entity maps.
	 * @throws HibernateException When there is a database error while removing the entity maps. Any changes before the
	 *         error will be rolled back.
	 */
	public void unregisterEntity(EntityType type, Long entityId) throws HibernateException;
	
	public List<EntityMap> getByAccountAndType(Account account, EntityType type);
}
