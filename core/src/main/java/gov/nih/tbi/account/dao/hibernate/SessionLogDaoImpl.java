package gov.nih.tbi.account.dao.hibernate;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.QueryTimeoutException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.SessionLogDao;
import gov.nih.tbi.account.model.hibernate.SessionLog;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.util.PaginationData;

/**
 * DAO class for interaction with the session_log database table which links to the
 * SessionLog model class.
 * 
 * @author Joshua Park(jospark)
 *
 */
@Repository
public class SessionLogDaoImpl extends GenericDaoImpl<SessionLog, Long> implements SessionLogDao {

	private static Logger logger = Logger.getLogger(SessionLogDao.class);
	
	private static final String ATTRIBUTE_TIMEIN = "timeIn";
	private static final String ATTRIBUTE_TIMEOUT = "timeOut";
	private static final String ATTRIBUTE_SESSIONSTATUS = "sessionStatus";
	private static final String ATTRIBUTE_ID = "id";
	private static final String ATTRIBUTE_USERNAME = "username";
	
	private static final String FILTERSTATUS_ACTIVE = "Active";
	private static final String FILTERSTATUS_EXPIRED = "Expired";
	
	@Autowired
	public SessionLogDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(SessionLog.class, sessionFactory);
	}

	/**
	 * Search the table given the provided filter, search, sort, and pagination parameters.
	 * 
	 * @param pageData paginationData defining the sort and pagination information
	 * @param searchVal the search string entered by the user
	 * @param searchColumns available searchable columns
	 * @param filterStatus value of the status filter (null or empty string if none)
	 * @param filterStartDate value of the start date filter (null if none)
	 * @param filterEndDate  value of the end date filter (null if none)
	 */
	public List<SessionLog> search(PaginationData pageData, String searchVal, List<String> searchColumns, String filterStatus, Date filterStartDate, Date filterEndDate, boolean export) {
		
		/*
		 * Query with:
		 * 	- search included
		 * 	- status set to "active"
		 * 	- time in set
		 * 	- time out set
		 *  
		 * SELECT * FROM session_log
		 * WHERE
		 * 	(
		 * 		username LIKE "%searchVal%"
		 * 		OR
		 * 		...
		 * 	)
		 * 	AND
		 * 	time_out = null
		 *  AND
		 * 	time_in > filterStartDate
		 * 	AND
		 * 	(
		 * 		time_out < filterEndDate 
		 * 		OR 
		 * 		time_out = null
		 * 	)
		 * ORDER BY pageData.sort pageData.ascending
		 * LIMIT pageData.pageSize OFFSET pageData.pageSize * pageData.page - 1
		 * 
		 * 
		 * WHICH CAN BE SIMPLIFIED AS
		 * 
		 * GET COLUMNS WHERE
		 * - AND(
		 * 		AND(all filters),
		 * 		OR(search properties)
		 * 	 )
		 * So this function sets up a conjunction of those two and uses null predicates where there is no criteria
		 */
		
		pageData.setNumSearchResults(countUnfiltered());
		
		// build the basic query
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SessionLog> query = cb.createQuery(persistentClass);
		Root<SessionLog> root = query.from(persistentClass);
		
		Predicate filterPredicate = buildFilterPredicate(cb, root, filterStatus, filterStartDate, filterEndDate);
		Predicate searchPredicate = buildSearchPredicate(cb, root, searchColumns, searchVal);
		
		// get filtered count (without pagination)
		pageData.setNumFilteredResults((int) getCountFiltered(cb, filterStatus, filterStartDate, filterEndDate, searchColumns, searchVal));
		
		// finalize core of the query
		if (filterPredicate != null && searchPredicate != null) {
			query.where(cb.and(filterPredicate, searchPredicate));
		}
		else if (filterPredicate != null) {
			query.where(filterPredicate);
		}
		else if (searchPredicate != null) {
			query.where(searchPredicate);
		}
		
		// Add Sorting
		if (pageData.getSort() != null && !pageData.getSort().equals("null")) {
			// special case for sessionStatus.  If the value is sessionStatus, use time log out
			String sortCol = pageData.getSort();
			if (pageData.getSort().equals(ATTRIBUTE_SESSIONSTATUS)) {
				sortCol = ATTRIBUTE_TIMEOUT;
			}
			
			Path<Object> orderPath = root.get(sortCol);
			query.orderBy(pageData.getAscending() ? cb.asc(orderPath) : cb.desc(orderPath));
			
		} else {
			query.orderBy(cb.asc(root.get("timeIn")));
		}
		
		Query<SessionLog> q = createQuery(query);
		
		// add Pagination
		if (!export && pageData != null && pageData.getPage() != null) {
			q.setMaxResults(pageData.getPageSize())
			 .setFirstResult(pageData.getPageSize() * (pageData.getPage() - 1));
		}
		// else get all. That could be risky but it's a good test condition
		
		return q.getResultList();
	}
	
	/**
	 * Builds the filters predicate. This lays out three filters: start datetime, end datetime, and status
	 * status: All, only Expired, only Active
	 * start date: if present, no entries with session start date before the given
	 * end date: if present, no entries with session start after the given
	 * 
	 * @param cb CriteriaBuilder to help build the criteria
	 * @param root root element of the query
	 * @param filterStatus status filter value
	 * @param filterStartDate start date filter value
	 * @param filterEndDate end date filter value
	 * @return Predicate filter query if filters are available. Otherwise an empty Predicate (true = true)
	 */
	private Predicate buildFilterPredicate(CriteriaBuilder cb, Root<SessionLog> root, String filterStatus, Date filterStartDate, Date filterEndDate) {
		
		// initialize to the null predicate (true = true) and overwrite later if needed
		Predicate output = null;
		List<Predicate> filterPredicates = new ArrayList<Predicate>();
		
		if (filterStatus != null && !filterStatus.isEmpty()) {
			if (filterStatus.equals(FILTERSTATUS_ACTIVE)) {
				// active means there is no time out
				filterPredicates.add(cb.isNull(root.get(ATTRIBUTE_TIMEOUT)));
			}
			else if (filterStatus.equals(FILTERSTATUS_EXPIRED)) {
				filterPredicates.add(cb.isNotNull(root.get(ATTRIBUTE_TIMEOUT)));
			}
			// else, don't add because the other option is "all"
		}
		if (filterStartDate != null) {
			filterPredicates.add(cb.greaterThanOrEqualTo(root.get(ATTRIBUTE_TIMEIN), filterStartDate));
		}
		if (filterEndDate != null) {
			filterPredicates.add(cb.lessThanOrEqualTo(root.get(ATTRIBUTE_TIMEOUT), filterEndDate));
		}
		
		// combine filters
		if (filterPredicates.size() > 0) {
			output = cb.and(filterPredicates.toArray(new Predicate[filterPredicates.size()]));
		}

		return output;
	}
	
	/**
	 * Builds the search predicate. This sets up text searching across all searchable columns
	 * (defined in the input json)
	 * 
	 * @param cb CriteriaBuilder to help build the query
	 * @param root root element of the query
	 * @param searchColumns available search columns
	 * @param searchVal string value the user entered
	 * @return Predicate describing the search criteria or empty predicate (true = true) otherwise
	 */
	private Predicate buildSearchPredicate(CriteriaBuilder cb, Root<SessionLog> root, List<String> searchColumns, String searchVal) {
		
		// initialize to the null predicate (true = true) and overwrite later if needed
		Predicate output = null;
		if (searchVal != null && !searchVal.equals("") && !searchColumns.isEmpty()) {
			String searchKey = CoreConstants.WILDCARD + searchVal.toUpperCase() + CoreConstants.WILDCARD;
			
			List<Predicate> searchPredicates = new ArrayList<Predicate>();
			for (String searchColumn : searchColumns) {
				searchPredicates.add(cb.like(cb.upper(root.get(searchColumn)), searchKey));
			}
			if (searchPredicates.size() > 0) {
				output = cb.or(searchPredicates.toArray(new Predicate[searchPredicates.size()]));
			}
		}
		
		return output;
	}
	
	/**
	 * Uses the filter and search predicates from the main query to count the number
	 * of filtered results BEFORE pagination.
	 *  
	 * @param cb criteriaBuilder used to build the query
	 * @param filterPredicate filter predicate from the main query
	 * @param searchPredicate search predicate from the main query
	 * @return number of rows in the (un-paginated) main query
	 */
	private long getCountFiltered(CriteriaBuilder cb, String filterStatus, Date filterStartDate, Date filterEndDate, List<String> searchColumns, String searchVal) {
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<SessionLog> countRoot = countQuery.from(SessionLog.class);
		// I would have preferred to use the predicates from the main query but the "reference" (root) wasn't the same
		// I also tried bringing the root in to this function but jpa didn't like that either
		Predicate filterPredicate = buildFilterPredicate(cb, countRoot, filterStatus, filterStartDate, filterEndDate);
		Predicate searchPredicate = buildSearchPredicate(cb, countRoot, searchColumns, searchVal);
		if (filterPredicate != null && searchPredicate != null) {
			countQuery.where(cb.and(filterPredicate, searchPredicate));
		}
		else if (filterPredicate != null) {
			countQuery.where(filterPredicate);
		}
		else if (searchPredicate != null) {
			countQuery.where(searchPredicate);
		}
		countQuery.select(cb.countDistinct(countRoot.get(ATTRIBUTE_ID)));
		Query<Long> q = createQuery(countQuery);
		return q.getSingleResult();
	}
	
	/**
	 * Get the count of available records before any filters/searches/pagination is applied.
	 * This query can be very simple because there's no permissions or any pre-filter
	 * so we just count all rows.
	 * 
	 * @return integer count of records
	 */
	public int countUnfiltered() {
		String countSql = "select count(tgt) as count from session_log";
		Query<BigInteger> query = getSession().createNativeQuery(countSql);
		
		try {
			return query.getSingleResult().intValue();
		}
		catch(NoResultException e) {
			logger.warn("No results found when querying for number of records in session_log. This is unusual");
			return 0;
		}
		catch(QueryTimeoutException e) {
			logger.error("query to count unfiltered SessionLogs timed out", e);
			return 0;
		}
	}
	
	public List<SessionLog> getAllActiveSessions() {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SessionLog> query = cb.createQuery(persistentClass);
		Root<SessionLog> root = query.from(persistentClass);
		query.where(cb.isNull(root.get(ATTRIBUTE_TIMEOUT)));
		query.orderBy(cb.asc(root.get(ATTRIBUTE_TIMEIN)));
		Query<SessionLog> q = createQuery(query);
		return q.getResultList();
	}
	
	public List<SessionLog> getActiveSessions(String username) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SessionLog> query = cb.createQuery(persistentClass);
		Root<SessionLog> root = query.from(persistentClass);
		query.where(cb.and(
				cb.equal(root.get(ATTRIBUTE_USERNAME), username),
				cb.isNull(root.get(ATTRIBUTE_TIMEOUT))
		));
		query.orderBy(cb.asc(root.get(ATTRIBUTE_TIMEIN)));
		Query<SessionLog> q = createQuery(query);
		return q.getResultList();
	}
	
	
}
