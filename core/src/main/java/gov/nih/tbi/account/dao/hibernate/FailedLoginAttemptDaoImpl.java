
package gov.nih.tbi.account.dao.hibernate;

import java.util.Date;
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
import gov.nih.tbi.account.dao.FailedLoginAttemptDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.FailedLoginAttempt;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;

/**
 * Hibernate implementation of the FailedLoginAttempDao
 * 
 * @author Andrew Johnson
 */
@Transactional("metaTransactionManager")
@Repository("failedLoginAttemptDao")
public class FailedLoginAttemptDaoImpl extends GenericDaoImpl<FailedLoginAttempt, Long> implements FailedLoginAttemptDao {

	@Autowired
	public FailedLoginAttemptDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(FailedLoginAttempt.class, sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<FailedLoginAttempt> getAll(Account account, Date currentDate) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<FailedLoginAttempt> query = cb.createQuery(FailedLoginAttempt.class);

		Root<FailedLoginAttempt> root = query.from(persistentClass);
		query.distinct(true);
		query.where(cb.and(cb.equal(root.get("account"), account),
				cb.between(root.get("eventDate"), currentDate, new Date())));

		return createQuery(query).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAll(Account account) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<FailedLoginAttempt> query = cb.createQuery(FailedLoginAttempt.class);

		Root<FailedLoginAttempt> root = query.from(persistentClass);
		query.distinct(true);
		query.where(cb.equal(root.get("account"), account));

		List<FailedLoginAttempt> resultList = createQuery(query).getResultList();
		removeAll(resultList);
	}
}
