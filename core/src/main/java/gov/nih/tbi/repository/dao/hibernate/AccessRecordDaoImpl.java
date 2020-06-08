package gov.nih.tbi.repository.dao.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.DataSource;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.repository.dao.AccessRecordDao;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.Study;

@Transactional("metaTransactionManager")
@Repository
public class AccessRecordDaoImpl extends GenericDaoImpl<AccessRecord, Long> implements
		AccessRecordDao {	
	static Logger logger = Logger.getLogger(AccessRecordDaoImpl.class);
	
	@Autowired
	public AccessRecordDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(AccessRecord.class, sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AccessRecord> getAll(Long studyId, Date startDate, Date endDate, PaginationData pageData) {
		
		
		String hql = "SELECT ac.id, ac.dataset, ac.account, ac.queueDate, ac.recordCount, ac.dataSource FROM AccessRecord ac "
				+ "LEFT JOIN ac.account a "
				+ "LEFT JOIN ac.dataset d "
				+ "LEFT JOIN d.study s "
				+ "WHERE (ac.account.hideAccessRecords is null OR ac.account.hideAccessRecords = false) "
				+ "AND ac.queueDate BETWEEN :startDate AND :endDate ";

		Query query = getSession().createQuery(hql);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		query.setFirstResult(pageData.getPageSize() * (pageData.getPage() - 1));
		query.setMaxResults(pageData.getPageSize());

		List<Object[]> list = query.getResultList();
		List<AccessRecord> arList = new ArrayList<AccessRecord>();

		for (int i = 0; i < list.size(); i++) {
			BasicDataset dataset = (BasicDataset) list.get(i)[1];
			Account account = (Account) list.get(i)[2];
			Date qd = (Date) list.get(i)[3];
			Long recordCount = (Long) list.get(i)[4];
			DataSource ds = (DataSource) list.get(i)[5];

			AccessRecord ar = new AccessRecord(dataset, account, qd, recordCount, ds);
			arList.add(ar);
		}
		
		return arList;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<AccessRecord> getByStudyIdList(List<Long> studyIdList, Date startDate, Date endDate,
			PaginationData pageData) {

		String hql = "SELECT ac.id, ac.dataset, ac.account, ac.queueDate, ac.recordCount, ac.dataSource FROM AccessRecord ac "
				+"LEFT JOIN ac.account a "
				+"LEFT JOIN ac.dataset d "
				+"LEFT JOIN d.study s "
				+"WHERE (ac.account.hideAccessRecords is null OR ac.account.hideAccessRecords = false) "
				+" AND (ac.queueDate BETWEEN :startDate AND :endDate) "
				+" AND (s.id IN (:studyIdList))";
		
		Query query = getSession().createQuery(hql);
		query.setParameter("startDate", startDate);
		query.setParameter("endDate", endDate);
		query.setParameter("studyIdList", studyIdList);
		query.setFirstResult(pageData.getPageSize() * (pageData.getPage() - 1));
		query.setMaxResults(pageData.getPageSize());
		
		List<Object[]> list = query.getResultList();
		List<AccessRecord> arList = new ArrayList<AccessRecord>();
		
		for(int i=0; i < list.size(); i++){
			BasicDataset dataset = (BasicDataset) list.get(i)[1];
			Account account = (Account)list.get(i)[2];
			Date qd= (Date) list.get(i)[3];
			Long recordCount = (Long) list.get(i)[4];
			DataSource ds = (DataSource) list.get(i)[5];

			AccessRecord ar = new AccessRecord(dataset, account, qd, recordCount,ds);
			arList.add(ar);		
		}
		return arList;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AccessRecord> searchAccessRecords(Long studyId, String dsId, Long recCount, String dsName,
			DatasetStatus status, String searchText, Long dataSource, String username, Date queueDate, Date oldestDate,
			PaginationData pageData) {
		
		// ids gives a list of access record IDs that match this study
		List<Long> ids = getIDsOfAccessRecord(studyId, pageData, searchText, oldestDate);
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<AccessRecord>();
		}
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<AccessRecord> query = cb.createQuery(AccessRecord.class);
		Root<AccessRecord> root = query.from(AccessRecord.class);
		
		// Subquery for search restrictions (rather then the pagination restrictions).
		Subquery<Long> subquery = query.subquery(Long.class);
		Root<AccessRecord> subRoot = subquery.from(AccessRecord.class);		
		
		
		List<Predicate> predicates = new ArrayList<Predicate>();
		predicates.add(subRoot.get("id").in(ids));

		if (!StringUtils.isEmpty(searchText)) {
			Predicate searchClause = cb.disjunction();
			searchClause =
					cb.or(searchClause, cb.equal(subRoot.get("dataSource"), DataSource.getByNamePartial(searchText)));
			
			if (recCount != null) {
				searchClause = cb.or(searchClause, cb.equal(subRoot.get("recordCount"), recCount));
			}
			
			if (status != null) {
				searchClause = cb.or(searchClause, cb.equal(subRoot.get("datasetStatus"), status));
			}

			searchClause = cb.or(searchClause, cb.like(cb.upper(subRoot.join("dataset", JoinType.LEFT).get("prefixedId")), "%" + dsId.toUpperCase() + "%"));
			searchClause = cb.or(searchClause, cb.like(cb.upper(subRoot.join("dataset", JoinType.LEFT).get("name")), "%" + dsName.toUpperCase() + "%"));
			searchClause = cb.or(searchClause, cb.like(cb.upper(subRoot.join("account", JoinType.LEFT).get("userName")), "%" + username.toUpperCase() + "%"));
			
			if (queueDate != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(queueDate);
				c.add(Calendar.DATE, 1);
				Date dt = c.getTime();
				searchClause = cb.or(searchClause, cb.between(subRoot.get("queueDate"), queueDate, dt));
			}
			
			predicates.add(searchClause);
		}

		if (oldestDate != null) {
			predicates.add(cb.greaterThanOrEqualTo(subRoot.get("queueDate"), oldestDate));
		}
		Predicate subPredicate = cb.and(predicates.toArray(new Predicate[predicates.size()]));

		// This projection alters the criteria to return a list of distinct ids instead of complete records
		subquery.select(subRoot.get("id")).distinct(true);
		subquery.where(subPredicate);
		
		Join<AccessRecord, BasicDataset> dJoin = root.join("dataset", JoinType.LEFT);
		Join<AccessRecord, Account> aJoin = root.join("account", JoinType.LEFT);		
		
		// The select criteria limits the results to a single page.
		query.where(root.get("id").in(subquery));
		
		// Pagination block
		if (pageData != null) {

			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<AccessRecord> countRoot = countQuery.from(AccessRecord.class);
			countQuery.select(cb.countDistinct(countRoot.get("id")));
			countQuery.where(countRoot.get("id").in(subquery));

			long count = getUniqueResult(countQuery);
			pageData.setNumFilteredResults((int) count);
			
			String sort = pageData.getSort();
			if (sort != null) {
				if (sort.equals("username")) {
					Expression<String> sortExp1 = aJoin.get("user").get("lastName");
					Expression<String> sortExp2 = aJoin.get("user").get("firstName");
					query.orderBy(pageData.getAscending() ? cb.asc(sortExp1) : cb.desc(sortExp1),
							pageData.getAscending() ? cb.asc(sortExp2) : cb.desc(sortExp2));
	
				} else {
					Expression<Object> orderExp = null;
					if (sort.equals("id") || sort.equals("name")) {
						orderExp = dJoin.get(sort);
					} else if (sort.equals("status")) {
						orderExp = dJoin.get("datasetStatus");
					} else if (sort.equals("source")) {
						orderExp = root.get("dataSource");
					} else if (sort.equals("date")) {
						orderExp = root.get("queueDate");
					} else {
						orderExp = root.get(sort);
					}
					query.orderBy(pageData.getAscending() ? cb.asc(orderExp) : cb.desc(orderExp));
				}
			} else {
				query.orderBy(cb.asc(root.get("queueDate")));
			}
			
		}
		
		TypedQuery<AccessRecord> q = createQuery(query);

		if (pageData != null && pageData.getPage() != null) {

			q.setMaxResults(pageData.getPageSize()).setFirstResult((pageData.getPageLength()) * (pageData.getPage() - 1));
		}
		List<AccessRecord> list = q.getResultList();
		
		return list;
	}
	
	public List<Long> getIDsOfAccessRecord(Long studyId, PaginationData pageData, String searchText, Date oldestDate) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<AccessRecord> root = query.from(AccessRecord.class);

		Join<AccessRecord, BasicDataset> dJoin = root.join("dataset", JoinType.LEFT);
		Join<AccessRecord, Account> aJoin = root.join("account", JoinType.LEFT);
		Join<BasicDataset, Study> sJoin = dJoin.join("study", JoinType.LEFT);

		query.select(root.get("id"));
		query.where(cb.and(cb.equal(sJoin.get("id"), studyId),
				cb.or(cb.isNull(aJoin.get("hideAccessRecords")), cb.equal(aJoin.get("hideAccessRecords"), false))));

		TypedQuery<Long> q = createQuery(query);
	    return q.getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int countAccessRecordsStudy(Long studyId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<AccessRecord> root = query.from(AccessRecord.class);

		Join<AccessRecord, Account> aJoin = root.join("account", JoinType.LEFT);
		Join<BasicDataset, Study> sJoin = root.join("dataset", JoinType.LEFT).join("study", JoinType.LEFT);

		query.where(cb.and(cb.equal(sJoin.get("id"), studyId),
				cb.or(cb.isNull(aJoin.get("hideAccessRecords")), cb.equal(aJoin.get("hideAccessRecords"), false))));

		query.select(cb.count(root)).distinct(true);
		
		Long returnValue = getUniqueResult(query);
		return returnValue.intValue();
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public int countAccessRecords(Long datasetId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);

		Root<AccessRecord> root = query.from(AccessRecord.class);
		query.select(cb.countDistinct(root.get("id")));
		query.where(cb.equal(root.join("dataset", JoinType.LEFT).get("id"), datasetId));
		
		int returnValue = getUniqueResult(query).intValue();
		return returnValue;
	}

	
	
	/**
	 * {@inheritDoc}
	 */
	public List<AccessRecord> getAccessRecordsByDataset(Long datasetId) {
		if(datasetId == null) {
			return new ArrayList<AccessRecord>();
		}
		Query query = getSessionFactory().getCurrentSession().createQuery("FROM AccessRecord ar WHERE ar.dataset.id in (?1)");
		query.setParameter(1, datasetId);
		List<AccessRecord> accessRecords = query.getResultList();
		return accessRecords;
	}

}
