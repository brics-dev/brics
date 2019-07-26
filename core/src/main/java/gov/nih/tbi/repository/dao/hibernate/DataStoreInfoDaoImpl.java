
package gov.nih.tbi.repository.dao.hibernate;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.repository.dao.DataStoreInfoDao;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;

/**
 * Dao implementation to store general information about data stores.
 * 
 * @author dhollo
 * 
 */
@Transactional("metaTransactionManager")
@Repository
public class DataStoreInfoDaoImpl extends GenericDaoImpl<DataStoreInfo, Long> implements DataStoreInfoDao {

	@Autowired
	public DataStoreInfoDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(DataStoreInfo.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<DataStoreInfo> search(String key, Boolean tabular, Boolean federated, Boolean archived,
			PaginationData pageData) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DataStoreInfo> query = cb.createQuery(DataStoreInfo.class);

		Subquery<Long> subquery = query.subquery(Long.class);
		Root<DataStoreInfo> subRoot = subquery.from(persistentClass);
		Predicate subPredicate = cb.conjunction();

		// Add the search term
		if (key != null) {
			key = CoreConstants.WILDCARD + key + CoreConstants.WILDCARD;
		}

		// Apply the federated and archived filters if they are present
		if (federated != null) {
			subPredicate = cb.and(subPredicate, cb.equal(subRoot.get("federated"), federated));
		}
		if (archived != null) {
			subPredicate = cb.and(subPredicate, cb.equal(subRoot.get("archived"), archived));
		}
		if (tabular != null) {
			subPredicate = cb.and(subPredicate, cb.equal(subRoot.get("tabular"), tabular));
		}

		subquery.select(subRoot.get("id")).distinct(true);
		subquery.where(subPredicate);

		// The select criteria limits the results to a single page.
		// TODO: BasicDataStoreInfo or DataStoreInfo ???
		Root<DataStoreInfo> root = query.from(DataStoreInfo.class);
		query.where(root.get("id").in(subquery));

		// Pagination block
		if (pageData != null) {
			// get the count of distinct IDs only to compute the total results before pagination
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<DataStoreInfo> countRoot = countQuery.from(DataStoreInfo.class);
			countQuery.select(cb.countDistinct(countRoot.get("id")));
			countQuery.where(countRoot.get("id").in(subquery));

			long count = createQuery(countQuery).getSingleResult();
			pageData.setNumSearchResults((int) count);

			// Add Sorting
			if (pageData.getSort() != null && !pageData.getSort().equals("null")) {
				if (!pageData.getAscending()) {
					query.orderBy(cb.asc(root.get(pageData.getSort())));
				} else {
					query.orderBy(cb.desc(root.get(pageData.getSort())));
				}
			} else {
				query.orderBy(cb.asc(root.get("id")));
			}
		}

		TypedQuery<DataStoreInfo> q = createQuery(query.distinct(true));

		// Add Pagination
		if (pageData != null && pageData.getPage() != null) {
			q.setMaxResults(pageData.getPageSize()).setFirstResult(pageData.getPageSize() * (pageData.getPage() - 1));
		}

		List<DataStoreInfo> list = q.getResultList();
		return list;
	}

	/**
	 * @InheritDoc
	 */
	public List<DataStoreInfo> getAllSorted(String sortColumn, boolean sortAsc) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DataStoreInfo> query = cb.createQuery(DataStoreInfo.class);
		Root<DataStoreInfo> root = query.from(DataStoreInfo.class);

		if (!sortAsc) {
			query.orderBy(cb.desc(root.get(sortColumn)));
		} else {
			query.orderBy(cb.asc(root.get(sortColumn)));
		}

		return createQuery(query.distinct(true)).getResultList();
	}

	/*
	 * @InheritDoc
	 */
	@Override
	public DataStoreInfo getByDataStructureId(Long dataStructureId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DataStoreInfo> query = cb.createQuery(DataStoreInfo.class);
		Root<DataStoreInfo> root = query.from(DataStoreInfo.class);

		query.where(cb.equal(root.get("dataStructureId"), dataStructureId)).distinct(true);
		return getUniqueResult(query);
	}
}
