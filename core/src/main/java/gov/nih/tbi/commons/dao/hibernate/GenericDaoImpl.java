
package gov.nih.tbi.commons.dao.hibernate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.commons.dao.GenericDao;

/**
 * Hibernate implementation of the Generic Dao
 * 
 * @author Andrew Johnson
 * 
 */
@Transactional
public class GenericDaoImpl<T, PK extends Serializable> implements GenericDao<T, PK> {

	protected final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

	protected Class<T> persistentClass;
	private SessionFactory sessionFactory;

	public GenericDaoImpl(final Class<T> persistentClass, SessionFactory sessionFactory) {

		this.persistentClass = persistentClass;
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	public Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	public List<T> getAll() {

		CriteriaQuery<T> query = getCriteriaBuilder().createQuery(persistentClass);
		query.from(persistentClass);
		List<T> toReturn = createQuery(query).getResultList();
		return toReturn;
	}

	public T get(PK id) {

		T entity = getSession().find(persistentClass, id);

		if (entity == null) {
			log.warn("Uh oh, '" + this.persistentClass + "' object with id '" + id + "' not found...");
			throw new ObjectRetrievalFailureException(this.persistentClass, id);
		}

		return entity;
	}

	public boolean exists(PK id) {

		T entity = getSession().find(persistentClass, id);
		return entity != null;
	}

	public T save(T object) {

		T newObj = (T) getSession().merge(object);
		return newObj;
	}

	public void remove(PK id) {

		getSession().delete(this.get(id));
	}

	public void removeAll(List<T> removeList) {

		if (removeList != null && !removeList.isEmpty()) {
			CriteriaDelete<T> criteriaDelete = getCriteriaBuilder().createCriteriaDelete(persistentClass);

			Root<T> root = criteriaDelete.from(persistentClass);
			criteriaDelete.where(root.in(removeList));
			getSession().createQuery(criteriaDelete).executeUpdate();
		}
	}

	public CriteriaBuilder getCriteriaBuilder() {
		return getSession().getCriteriaBuilder();
	}

	public <X> Query<X> createQuery(CriteriaQuery<X> query) {
		return getSession().createQuery(query);
	}

	public <X> X getUniqueResult(CriteriaQuery<X> query) {

		try {
			return createQuery(query).getSingleResult();

		} catch (NoResultException ex) {
			return null;
		}
	}

	public void batchSave(Set<T> objects) {

		if (objects == null || objects.isEmpty()) {
			return;
		}

		Session session = getSession();

		for (T object : objects) {
			session.save(object);
		}
	}
}
