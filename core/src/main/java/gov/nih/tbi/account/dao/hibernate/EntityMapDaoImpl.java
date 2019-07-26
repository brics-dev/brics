package gov.nih.tbi.account.dao.hibernate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.EntityMapDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;

/**
 * This is an EntityMapDao
 * 
 * @author Andrew Johnson
 * 
 */
@Transactional("metaTransactionManager")
@Repository
public class EntityMapDaoImpl extends GenericDaoImpl<EntityMap, Long> implements EntityMapDao {

	@Autowired
	public EntityMapDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(EntityMap.class, sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityMap get(Account account, EntityType type, Long entityId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);
		Root<EntityMap> root = query.from(EntityMap.class);

		query.where(cb.and(cb.equal(root.get(EntityMap.ACCOUNT), account)), cb.equal(root.get("type"), type),
				cb.equal(root.get("entityId"), entityId));

		return getUniqueResult(query);
	}

	/**
	 * {@inheritDoc}
	 */
	// TODO:NIMESH Change to Webservice
	public EntityMap get(Account account, Set<PermissionGroup> currentGroups, EntityType type, Long entityId,
			boolean isAdmin) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);

		Root<EntityMap> root = query.from(EntityMap.class);
		Predicate predicate = cb.conjunction();
		
		predicate = cb.and(predicate, cb.equal(root.get("type"), type));
		predicate = cb.and(predicate, cb.equal(root.get("entityId"), entityId));

		if (!currentGroups.isEmpty()) {
			predicate = cb.and(predicate, cb.or(root.get(EntityMap.PERMISSION_GROUP).in(currentGroups),
					cb.equal(root.get(EntityMap.ACCOUNT), account)));			
		} else {
			predicate = cb.and(predicate, cb.equal(root.get(EntityMap.ACCOUNT), account));			
		}

		EntityMap outEntity = new EntityMap();
		outEntity.setAccount(account);
		outEntity.setType(type);
		outEntity.setEntityId(entityId);
		outEntity.setPermission(null);

		if (isAdmin) {
			outEntity.setPermission(PermissionType.ADMIN);
		}

		query.where(predicate);
		List<EntityMap> entityMapList = createQuery(query).getResultList();

		for (EntityMap em : entityMapList) {
			if (outEntity.getPermission() == null) {
				outEntity.setPermission(em.getPermission());
			} else if (em.getPermission().contains(outEntity.getPermission())) {
				outEntity.setPermission(em.getPermission());
			}
		}

		return outEntity;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<EntityMap> listUserAccess(Account account, Set<PermissionGroup> currentGroups, EntityType type,
			boolean isAdmin) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);

		Root<EntityMap> root = query.from(EntityMap.class);
		Predicate predicate = cb.conjunction();

		// if any permission groups has been passed in, we need to get any of the entities are are associated with them
		if (!isAdmin) {
			if (!currentGroups.isEmpty()) {
				predicate = cb.and(predicate, cb.or(root.get(EntityMap.PERMISSION_GROUP).in(currentGroups),
						cb.equal(root.get(EntityMap.ACCOUNT), account)));
			} else {
				predicate = cb.and(predicate, cb.equal(root.get(EntityMap.ACCOUNT), account));
			}
		}

		predicate = cb.and(predicate, cb.equal(root.get("type"), type));
		query.where(predicate);

		List<EntityMap> out = createQuery(query).getResultList();
		return out;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<EntityMap> listEntityAccess(Long entityId, EntityType type) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);

		Root<EntityMap> root = query.from(EntityMap.class);
		root.fetch("account", JoinType.LEFT);
		root.fetch("permissionGroup", JoinType.LEFT);
		query.where(cb.and(cb.equal(root.get("type"), type), cb.equal(root.get("entityId"), entityId)));
		
		query.orderBy(cb.asc(root.get("entityId"))).distinct(true);
		
		TypedQuery<EntityMap> q = createQuery(query);
		List<EntityMap> out = new ArrayList<EntityMap>();

		// Add entity maps to the list, while filtering out ones with deactivated accounts.
		for (EntityMap em : q.getResultList()) {
			Account a = em.getAccount();

			if (a != null) {
				// Only add the entity map if the associated account is active.
				if (a.getIsActive()) {
					out.add(em);
				}
			} else {
				out.add(em);
			}
		}

		return out;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityMap getOwnerEntityMap(Long entityId, EntityType type) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);

		Root<EntityMap> root = query.from(EntityMap.class);
		query.where(cb.and(cb.equal(root.get("entityId"), entityId), cb.equal(root.get("type"), type),
				cb.equal(root.get("permission"), PermissionType.OWNER)));

		EntityMap em = getUniqueResult(query);
		if (em != null) {
			if (em.getAccount() != null) {
				em.getAccount().toString();
			}

			if (em.getPermissionGroup() != null) {
				em.getPermissionGroup().toString();
			}
		}

		return em;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<EntityMap> listOwnedEntities(Account account, EntityType type) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);
		Root<EntityMap> root = query.from(EntityMap.class);
		
		query.where(cb.and(cb.equal(root.get("account"), account), cb.equal(root.get("type"), type),
				cb.equal(root.get("permission"), PermissionType.OWNER)));
		
		TypedQuery<EntityMap> q = createQuery(query);
		return new HashSet<EntityMap>(q.getResultList());
	}

	@Override
	public EntityMap get(PermissionGroup permissionGroup, EntityType type, Long entityId, boolean isFlag) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);
		Root<EntityMap> root = query.from(EntityMap.class);
		
		query.where(cb.and(cb.equal(root.get("entityId"), entityId), cb.equal(root.get("type"), type),
				cb.equal(root.get("permissionGroup"), permissionGroup)));

		return getUniqueResult(query);
	}

	@Override
	public EntityMap get(PermissionGroup permissionGroup, EntityType type, Long entityId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);
		Root<EntityMap> root = query.from(EntityMap.class);
		
		query.where(cb.and(cb.equal(root.get("entityId"), entityId), cb.equal(root.get("type"), type),
				cb.equal(root.get("permissionGroup"), permissionGroup)));

		return getUniqueResult(query);
	}

	
	public List<Long> getDatasetIdsByStatus(DatasetStatus status) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);

		Root<EntityMap> root = query.from(EntityMap.class);
		query.select(root.get("id")).distinct(true);
		query.where(cb.equal(root.get("datasetStatus"), status));
		
		return createQuery(query).getResultList();
	}

	
	public List<EntityMap> getEntityMapsForDatasetByDatasetStatus(DatasetStatus status) {

		List<Long> datasetIds = getDatasetIdsByStatus(status);

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);
		Root<EntityMap> root = query.from(EntityMap.class);
		
		query.where(cb.and(root.get("entityId").in(datasetIds), cb.equal(root.get("type"), EntityType.DATASET)))
				.distinct(true);

		return createQuery(query).getResultList();
	}
	

	public List<EntityMap> get(Long entityId, EntityType type) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);

		Root<EntityMap> root = query.from(EntityMap.class);
		query.where(cb.and(cb.equal(root.get("entityId"), entityId), cb.equal(root.get("type"), type)));

		return createQuery(query.distinct(true)).getResultList();
	}
	

	public List<EntityMap> getEntityMapsOfOwners(List<Long> ids, EntityType type) {

		if (ids == null || ids.isEmpty()) {
			return new ArrayList<EntityMap>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);

		Root<EntityMap> root = query.from(EntityMap.class);
		query.where(cb.and(root.get("entityId").in(ids), cb.equal(root.get("type"), type),
				cb.equal(root.get("permission"), PermissionType.OWNER)));
		
		root.fetch("account", JoinType.LEFT);
		query.orderBy(cb.asc(root.get("entityId"))).distinct(true);
		
		TypedQuery<EntityMap> q = createQuery(query);
		List<EntityMap> results = q.getResultList();
		return results;
	}


	public List<EntityMap> getEntityForAccount(Account account) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);

		Root<EntityMap> root = query.from(EntityMap.class);
		query.where(cb.equal(root.get("account"), account));

		return createQuery(query.distinct(true)).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<EntityMap> createUpdateEntities(List<EntityMap> entityMapList) throws HibernateException {
		List<EntityMap> syncedEnities = new ArrayList<EntityMap>(entityMapList.size());
		List<Long> entityIds = new ArrayList<Long>(entityMapList.size());
		
		Session session = getSessionFactory().openSession();

		// Set the session to persist data to the database only when a transaction is committed.
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction transaction = session.beginTransaction();

		try {
			// Create or update the listed entity maps to the database.
			for (EntityMap em : entityMapList) {
				// Try to get the database version of the entity.
				EntityMap oldMap = null;

				if (em.getId() != null) {
					oldMap = (EntityMap) session.get(EntityMap.class, em.getId());
				}

				// Save or update the entity map.
				if (oldMap != null) {
					oldMap.setPermission(em.getPermission());
					session.update(oldMap);
					entityIds.add(oldMap.getId());
				} else {
					em.setId(null);
					Long newId = (Long) session.save(em);
					entityIds.add(newId);
				}
			}

			transaction.commit();

			// Get the entity maps that were just saved to the database.
			CriteriaBuilder cb = getCriteriaBuilder();
			CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);

			Root<EntityMap> root = query.from(EntityMap.class);
			root.fetch("account", JoinType.LEFT);
			query.where(root.get("id").in(entityIds)).distinct(true);

			for (EntityMap em : createQuery(query).getResultList()) {
				if (em.getAccount() != null) {
					for (PermissionGroupMember pgm : em.getAccount().getPermissionGroupMemberList()) {
						pgm.getPermissionGroup().toString();
					}
				}

				syncedEnities.add(em);
			}
		} finally {
			if (session.isDirty()) {
				transaction.rollback();
			}
			session.close();
		}

		return syncedEnities;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<EntityMap> removeEntityMaps(List<Long> ids) throws HibernateException {
		List<EntityMap> removedEntities = new ArrayList<EntityMap>(ids.size());
		
		Session session = getSessionFactory().openSession();
		
		// persist data to the database only when a transaction is committed.
		session.setHibernateFlushMode(FlushMode.COMMIT);
		Transaction transaction = session.beginTransaction();

		try {
			for (Long id : ids) {
				EntityMap toDel = session.find(EntityMap.class, id);

				// If an entity map entry is found delete it, and add it to the removed list.
				if (toDel != null) {
					session.delete(toDel);
					removedEntities.add(toDel);
				}
			}

			transaction.commit();
			
		} catch (Exception e) {
	        transaction.rollback();
	        e.printStackTrace();
	    } finally {
	    	session.close();
		}

		return removedEntities;
	}

	/**
	 * @inheritDoc
	 */
	public List<EntityMap> getByEntityIds(Account account, Set<PermissionGroup> permissionGroups, Set<Long> entityIds,
			EntityType entityType) {

		// just return an empty list if no entity IDs are specified
		if (entityIds.isEmpty()) {
			return new ArrayList<EntityMap>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);

		Root<EntityMap> root = query.from(EntityMap.class);
		Predicate predicate = cb.conjunction();

		// if any permission groups has been passed in, we need to get any of the entities are are associated with them
		if (!permissionGroups.isEmpty()) {
			predicate = cb.and(predicate, cb.or(root.get(EntityMap.PERMISSION_GROUP).in(permissionGroups),
					cb.equal(root.get(EntityMap.ACCOUNT), account)));
		} else {
			predicate = cb.and(predicate, cb.equal(root.get(EntityMap.ACCOUNT), account));
		}

		predicate = cb.and(predicate, cb.equal(root.get("type"), entityType));
		predicate = cb.and(predicate, root.get("entityId").in(entityIds));
		query.where(predicate).distinct(true);

		List<EntityMap> entityMaps = createQuery(query).getResultList();
		return entityMaps;
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterEntity(EntityType type, Long entityId) throws HibernateException {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);
		Root<EntityMap> root = query.from(EntityMap.class);
		query.where(cb.and(cb.equal(root.get("type"), type), cb.equal(root.get("entityId"), entityId)));
		
		Query<EntityMap> q = createQuery(query.distinct(true));
		
		for(EntityMap em:q.getResultList()) {
			this.remove(em.getId());
		}
	}

	public List<EntityMap> getByAccountAndType(Account account, EntityType type) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EntityMap> query = cb.createQuery(EntityMap.class);

		Root<EntityMap> root = query.from(EntityMap.class);
		query.where(cb.and(cb.equal(root.get(EntityMap.ACCOUNT), account), cb.equal(root.get("type"), type)));
		
		TypedQuery<EntityMap> q = createQuery(query.distinct(true));
		return q.getResultList();
	}
}
