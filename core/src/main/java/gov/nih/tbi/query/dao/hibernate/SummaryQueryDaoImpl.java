package gov.nih.tbi.query.dao.hibernate;

import java.security.AccessControlException;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.query.dao.SummaryQueryDao;
import gov.nih.tbi.query.model.SummaryResult;
import gov.nih.tbi.query.model.hibernate.SummaryQuery;

/**
 * Interface for the SQL Database communication that contains the Summary Data Queries.
 * 
 * @author Bill Puschmann
 *
 */
@Transactional("metaTransactionManager")
@Repository
public class SummaryQueryDaoImpl extends GenericDaoImpl<SummaryQuery, Long> implements SummaryQueryDao {
	

	@Autowired
	public SummaryQueryDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {
		super(SummaryQuery.class, sessionFactory);
	}

	public SummaryQueryDaoImpl(Class<SummaryQuery> persistentClass, SessionFactory sessionFactory) {
		super(persistentClass, sessionFactory);
	}

	/**
	 * Retrieves the SummaryQuery object from the Database based on the shortname/key of the item.
	 */
	@Override
	public SummaryQuery getSummaryByName(String shortname) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SummaryQuery> query = cb.createQuery(SummaryQuery.class);
		Root<SummaryQuery> root = query.from(persistentClass);

		query.where(cb.equal(root.get("shortname"), shortname)).distinct(true);

		return getUniqueResult(query);
	}

	@Override
	public List<SummaryQuery> getAllStudySummaryQueriesShortnames(String instance) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SummaryQuery> query = cb.createQuery(SummaryQuery.class);
		Root<SummaryQuery> root = query.from(persistentClass);

		query.where(cb.equal(root.get("requiresStudy"), true),cb.equal(root.get("instance"), instance.toUpperCase())).distinct(true);
		List<SummaryQuery> result = createQuery(query).getResultList();
		return result;
	}

	@Override
	public List<SummaryQuery> getAllProgramSummaryQueriesShortnames(String instance) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SummaryQuery> query = cb.createQuery(SummaryQuery.class);
		Root<SummaryQuery> root = query.from(persistentClass);

		query.where(cb.equal(root.get("requiresStudy"), false),cb.equal(root.get("instance"), instance.toUpperCase())).distinct(true);
		List<SummaryQuery> result = createQuery(query).getResultList();
		return result;
	}
	
	/**
	 * Calls postgres with the given query and parses the results into a key/value map of variables and counts.
	 * 
	 * These queries should only be returning two variables: "value" and "count".  Anything else will be ignored.
	 */
	@Override
	public SummaryResult getWithQuery(String queryString) {
		
		SummaryResult summaryResult = new SummaryResult();
		Map<String, String> summaryResultsMap = summaryResult.getResults();
		// Use exists in the query to avoid looping through the entire dataset records of the study
		Query query = getSession().createNativeQuery(queryString);
		
		@SuppressWarnings("unchecked")
		List<Object[]> results = query.getResultList();
		
		for (Object[] obj : results) {
			String count = ((String) obj[0].toString());
			String category =  ((String) obj[1].toString());
			summaryResultsMap.put(category, count);
		}

		return summaryResult;
	}

	@Override
	public List<SummaryQuery> getStudySummaryQueriesShortnames(String instance) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SummaryQuery> query = cb.createQuery(SummaryQuery.class);
		Root<SummaryQuery> root = query.from(persistentClass);

		query.where(cb.equal(root.get("requiresStudy"), true),cb.equal(root.get("forSummaryData"), true),cb.equal(root.get("instance"), instance.toUpperCase())).distinct(true);
		List<SummaryQuery> result = createQuery(query).getResultList();
		return result;
	}

	@Override
	public List<SummaryQuery> getProgramSummaryQueriesShortnames(String instance) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SummaryQuery> query = cb.createQuery(SummaryQuery.class);
		Root<SummaryQuery> root = query.from(persistentClass);
		query.where(cb.equal(root.get("requiresStudy"), false),cb.equal(root.get("forSummaryData"), true),cb.equal(root.get("instance"), instance.toUpperCase())).distinct(true);
		List<SummaryQuery> result = createQuery(query).getResultList();
		return result;
	}
	
	
	public Integer getSubjectCountByStudy(String studyId){
		Integer subjectCount = 0;
		String hql = "select count( distinct subject_guid) " + 
				"from dataset_subject ds " + 
				"join dataset d " + 
				"on ds.dataset_id = d.id " + 
				"where d.dataset_status_id in (0,1) " + 
				"and d.study_id = ?";

		Query query = getSession().createNativeQuery(hql);
		query.setParameter(1, Integer.parseInt(studyId));
		
		List<Integer> result = query.getResultList();
		subjectCount = ((Number) result.get(0)).intValue();
		return subjectCount;
	}

	/**
	 * This is not intended to be implemented, but is required by the interface.
	 */
	@Override
	public SummaryQuery save(SummaryQuery object) {
		throw new AccessControlException("Summary Query DAO is read only");
	}

}
