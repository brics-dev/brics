
package gov.nih.tbi.query.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.query.dao.SavedQueryDao;
import gov.nih.tbi.query.model.hibernate.SavedQuery;


@Transactional("metaTransactionManager")
@Repository
public class SavedQueryDaoImpl extends GenericDaoImpl<SavedQuery, Long> implements SavedQueryDao {

	@Autowired
	public SavedQueryDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(SavedQuery.class, sessionFactory);
	}


	/**
	 * @inheritDoc
	 */
	public List<SavedQuery> getByIds(List<Long> ids) {

		if (ids == null || !ids.isEmpty()) {
			return new ArrayList<SavedQuery>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SavedQuery> query = cb.createQuery(SavedQuery.class);

		Root<SavedQuery> root = query.from(SavedQuery.class);
		query.where(root.get("id").in(ids)).distinct(true);

		return createQuery(query).getResultList();
	}


	public List<SavedQuery> getSavedQueryInfoByIds(Set<Long> ids) {

		if (ids == null || !ids.isEmpty()) {
			return new ArrayList<SavedQuery>();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createTupleQuery();

		Root<SavedQuery> root = query.from(SavedQuery.class);
		query.multiselect(root.get("id").alias("id"), root.get("name").alias("name"),
				root.get("description").alias("description"), root.get("lastUpdated").alias("lastUpdated"));
		query.where(root.get("id").in(ids)).distinct(true);

		List<Tuple> rows = createQuery(query).getResultList();
		List<SavedQuery> list = new ArrayList<SavedQuery>();

		for (Tuple row : rows) {
			SavedQuery sq = new SavedQuery();
			sq.setId((Long) row.get("id"));
			sq.setName((String) row.get("name"));
			sq.setDescription((String) row.get("description"));
			sq.setLastUpdated((Date) row.get("lastUpdated"));
			list.add(sq);
		}
		return list;
	}


	public List<SavedQuery> getAllSavedQueries() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createTupleQuery();

		Root<SavedQuery> root = query.from(SavedQuery.class);
		query.multiselect(root.get("id").alias("id"), root.get("name").alias("name"),
				root.get("description").alias("description"), root.get("lastUpdated").alias("lastUpdated"));

		List<Tuple> rows = createQuery(query).getResultList();
		List<SavedQuery> list = new ArrayList<SavedQuery>();

		for (Tuple row : rows) {
			SavedQuery sq = new SavedQuery();
			sq.setId((Long) row.get("id"));
			sq.setName((String) row.get("name"));
			sq.setDescription((String) row.get("description"));
			sq.setLastUpdated((Date) row.get("lastUpdated"));
			list.add(sq);
		}

		return list;
	}


	public boolean isSavedQueryNameUnique(String savedQueryName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);

		Root<SavedQuery> root = query.from(SavedQuery.class);
		query.where(cb.like(cb.upper(root.get("name")), savedQueryName.toUpperCase() + "%")).distinct(true);
		query.select(cb.countDistinct(root));
		long count = createQuery(query).getSingleResult();

		return count == 0;
	}

	public SavedQuery getByName(String savedQueryName) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SavedQuery> query = cb.createQuery(SavedQuery.class);

		Root<SavedQuery> root = query.from(SavedQuery.class);
		query.where(cb.like(cb.upper(root.get("name")), savedQueryName.toUpperCase())).distinct(true);

		return getUniqueResult(query);
	}

	public List<SavedQuery> searchSavedQuery(Set<Long> savedQueryIdsList, String savedQueryName,
			String savedQueryDescription, Date startDateRange, Date endDateRange, boolean includeCopies) {

		List<SavedQuery> savedQueryList = new ArrayList<SavedQuery>();

		if (savedQueryIdsList != null && savedQueryIdsList.isEmpty()) {
			// If non-admin User doesn't have permission to see any saved query, just return an empty list
			return savedQueryList;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createTupleQuery();

		Root<SavedQuery> root = query.from(SavedQuery.class);
		query.multiselect(root.get("id").alias("id"), root.get("name").alias("name"),
				root.get("description").alias("description"), root.get("lastUpdated").alias("lastUpdated"));
		Predicate predicate = cb.conjunction();

		// Non-admin user will have a not-null savedQueryIdsList
		if (savedQueryIdsList != null && !savedQueryIdsList.isEmpty()) {
			predicate = cb.and(predicate, root.get("id").in(savedQueryIdsList));
		}

		// set name: case insensitive and wild card matching
		if (savedQueryName != null) {
			savedQueryName = CoreConstants.WILDCARD + savedQueryName + CoreConstants.WILDCARD;
			predicate = cb.and(predicate, cb.like(cb.upper(root.get("name")), savedQueryName.toUpperCase()));
		}
		if (savedQueryDescription != null) {
			savedQueryDescription = CoreConstants.WILDCARD + savedQueryDescription + CoreConstants.WILDCARD;
			predicate =
					cb.and(predicate, cb.like(cb.upper(root.get("description")), savedQueryDescription.toUpperCase()));
		}
		if (startDateRange != null && endDateRange != null) {
			predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("lastUpdated"), startDateRange));
			predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("lastUpdated"), endDateRange));
		}
		if (includeCopies) {
			predicate = cb.and(predicate, cb.equal(root.get("copyFlag"), includeCopies));
		} else {
			predicate = cb.and(predicate, cb.equal(root.get("copyFlag"), false));
		}

		List<Tuple> rows = createQuery(query).getResultList();
		for (Tuple row : rows) {
			SavedQuery sq = new SavedQuery();
			sq.setId((Long) row.get("id"));
			sq.setName((String) row.get("name"));
			sq.setDescription((String) row.get("description"));
			sq.setLastUpdated((Date) row.get("lastUpdated"));
			savedQueryList.add(sq);
		}

		return savedQueryList;
	}

}
