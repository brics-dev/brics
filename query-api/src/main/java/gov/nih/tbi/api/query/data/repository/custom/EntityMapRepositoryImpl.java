package gov.nih.tbi.api.query.data.repository.custom;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Repository;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.commons.model.EntityType;

@Repository
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EntityMapRepositoryImpl implements EntityMapRepoCustom {

	@PersistenceContext
    private EntityManager entityManager;
	
	
	public List<EntityMap> listUserAccess(Account account, Set<PermissionGroup> currentGroups, EntityType type,
			boolean isAdmin) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
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

		List<EntityMap> out = entityManager.createQuery(query).getResultList();
		return out;
	}
}
