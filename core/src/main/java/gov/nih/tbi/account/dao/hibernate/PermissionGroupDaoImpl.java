package gov.nih.tbi.account.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.PermissionGroupDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.PermissionGroupStatus;

@Transactional("metaTransactionManager")
@Repository
public class PermissionGroupDaoImpl extends GenericDaoImpl<PermissionGroup, Long> implements PermissionGroupDao {
	@Autowired
	public PermissionGroupDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {
		super(PermissionGroup.class, sessionFactory);
	}

	@Override
	public PermissionGroup get(Long id) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<PermissionGroup> query = cb.createQuery(PermissionGroup.class);

		Root<PermissionGroup> root = query.from(PermissionGroup.class);
		query.where(cb.equal(root.get(CoreConstants.ID), id));

		PermissionGroup out = getUniqueResult(query.distinct(true));

		// Calling all of the toString() methods may look pretty strange, but it is needed for all of the lazy
		// fetching to work correctly.
		if (out != null) {
			out.getMemberSet().toString();

			for (PermissionGroupMember pgm : out.getMemberSet()) {
				pgm.getAccount().toString();
				pgm.getAccount().getAccountHistory().toString();
				pgm.getPermissionGroupStatus().toString();
			}
		}

		return out;
	}

	public List<PermissionGroup> getPublicGroups() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<PermissionGroup> query = cb.createQuery(PermissionGroup.class);

		Root<PermissionGroup> root = query.from(PermissionGroup.class);
		query.where(cb.equal(root.get(PermissionGroup.PUBLIC_STATUS), true)).distinct(true);

		List<PermissionGroup> groupList = createQuery(query).getResultList();

		return groupList;
	}

	public List<PermissionGroup> getPrivateGroups() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<PermissionGroup> query = cb.createQuery(PermissionGroup.class);

		Root<PermissionGroup> root = query.from(PermissionGroup.class);
		query.where(cb.equal(root.get(PermissionGroup.PUBLIC_STATUS), false)).distinct(true);
		
		List<PermissionGroup> groupList = createQuery(query).getResultList();
		return groupList;
	}
	
	public List<PermissionGroup> getGrantedGroups(Account account) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<PermissionGroup> query = cb.createQuery(PermissionGroup.class);

		Root<PermissionGroup> root = query.from(PermissionGroup.class);
		Join<PermissionGroup, PermissionGroupMember> memberJoin = root.join("memberSet");
		query.where(cb.and(cb.equal(memberJoin.get("account"), account),
				cb.equal(memberJoin.get("permissionGroupStatus"), PermissionGroupStatus.ACTIVE)));
		query.distinct(true);
		
		return createQuery(query).getResultList();
	}

	public PermissionGroup getByName(String groupName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<PermissionGroup> query = cb.createQuery(PermissionGroup.class);

		Root<PermissionGroup> root = query.from(PermissionGroup.class);
		query.select(root).distinct(true);
		query.where(cb.equal(root.get(PermissionGroup.GROUP_NAME), groupName));

		PermissionGroup pg = getUniqueResult(query);

		// ????????????????? TODO - ??????????????????
		// All of the toString() calls below are actually needed, since they allow the lazy fetched objects
		// to function correctly.
		if (pg != null) {
			if (pg.getMemberSet() != null) {
				pg.getMemberSet().toString();
			}

			if (pg.getEntityMapSet() != null) {
				pg.getEntityMapSet().toString();
			}

			for (EntityMap em : pg.getEntityMapSet()) {
				em.toString();
			}
		}

		return pg;
	}
}
