
package gov.nih.tbi.commons.dao.hibernate;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.UserDao;
import gov.nih.tbi.commons.model.hibernate.User;

@Transactional("metaTransactionManager")
@Repository
public class UserDaoImpl extends GenericDaoImpl<User, Long> implements UserDao {

	@Autowired
	public UserDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(User.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public User getByEmail(String email) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<User> query = cb.createQuery(User.class);

		Root<User> root = query.from(persistentClass);
		query.where(cb.equal(cb.upper(root.get("email")), email.toUpperCase()));
		
		return getUniqueResult(query);
	}

	/**
	 * @inheritDoc
	 */
	public List<String> getAllEmails() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);

		Root<User> root = query.from(persistentClass);
		query.select(root.get(CoreConstants.EMAIL)).distinct(true);

		List<String> out = createQuery(query).getResultList();
		return out;
	}

	/**
	 * {@inheritDoc}
	 */
	public User getByName(String firstName, String lastName) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<User> query = cb.createQuery(User.class);

		Root<User> root = query.from(persistentClass);
		query.where(cb.and(
				cb.equal(cb.upper(root.get("firstName")), firstName.toUpperCase()),
				cb.equal(cb.upper(root.get("lastName")), lastName.toUpperCase())));

		return getUniqueResult(query);
	}
	
	@Override
	public List<User> getUserDetailsByIds(Set<Long> userIds) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<User> query = cb.createQuery(User.class);

		Root<User> root = query.from(persistentClass);
		query.where(root.get("id").in(userIds));
		
		return createQuery(query).getResultList();		
	}
}
