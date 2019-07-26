
package gov.nih.tbi.account.dao.hibernate;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.PreviousPasswordDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.PreviousPassword;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;

/**
 * Hibernate implementation of the PreviousPasswordDao
 * 
 * @author Andrew Johnson
 */
@Transactional("metaTransactionManager")
@Repository
public class PreviousPasswordDaoImpl extends GenericDaoImpl<PreviousPassword, Long> implements PreviousPasswordDao {

	@Autowired
	public PreviousPasswordDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(PreviousPassword.class, sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PreviousPassword> getLast(Account account, int count) {

		CriteriaBuilder builder = getCriteriaBuilder();
		CriteriaQuery<PreviousPassword> query = builder.createQuery(PreviousPassword.class);

		Root<PreviousPassword> root = query.from(PreviousPassword.class);
		query.select(root).distinct(true);
		query.where(builder.equal(root.get("account"), account));
		query.orderBy(builder.asc(root.get("eventDate")));

		TypedQuery<PreviousPassword> q = createQuery(query).setMaxResults(count);
		return q.getResultList();
	}

}
