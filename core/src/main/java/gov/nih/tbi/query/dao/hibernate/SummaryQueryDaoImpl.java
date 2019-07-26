package gov.nih.tbi.query.dao.hibernate;

import java.security.AccessControlException;
import java.util.List;

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
	public List<SummaryQuery> getAllStudySummaryQueriesShortnames() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SummaryQuery> query = cb.createQuery(SummaryQuery.class);
		Root<SummaryQuery> root = query.from(persistentClass);

		query.where(cb.equal(root.get("requiresStudy"), true)).distinct(true);
		List<SummaryQuery> result = createQuery(query).getResultList();
		return result;
	}

	@Override
	public List<SummaryQuery> getAllProgramSummaryQueriesShortnames() {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SummaryQuery> query = cb.createQuery(SummaryQuery.class);
		Root<SummaryQuery> root = query.from(persistentClass);

		query.where(cb.equal(root.get("requiresStudy"), false)).distinct(true);
		List<SummaryQuery> result = createQuery(query).getResultList();
		return result;
	}


	/**
	 * This is not intended to be implemented, but is required by the interface.
	 */
	@Override
	public SummaryQuery save(SummaryQuery object) {
		throw new AccessControlException("Summary Query DAO is read only");
	}

}
