
package gov.nih.tbi.dictionary.dao.hibernate;

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
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.ClassificationDao;
import gov.nih.tbi.dictionary.model.hibernate.Classification;

@Transactional("dictionaryTransactionManager")
@Repository
public class ClassificationDaoImpl extends GenericDictDaoImpl<Classification, Long> implements ClassificationDao {

	@Autowired
	public ClassificationDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(Classification.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<Classification> getAll() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Classification> query = cb.createQuery(Classification.class);
		Root<Classification> root = query.from(Classification.class);

		query.where(cb.equal(root.get("isActive"), Boolean.TRUE));
		query.orderBy(cb.asc(root.get("id")));

		TypedQuery<Classification> q = createQuery(query.distinct(true));
		return q.getResultList();
	}

	/**
	 * @inheritDoc
	 */
	public List<Classification> getUserList() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Classification> query = cb.createQuery(Classification.class);
		Root<Classification> root = query.from(Classification.class);

		query.where(cb.and(
				cb.equal(root.get("isActive"), Boolean.TRUE), cb.equal(root.get("canCreate"), Boolean.TRUE)));
		query.orderBy(cb.asc(root.get("id")));

		TypedQuery<Classification> q = createQuery(query.distinct(true));
		return q.getResultList();
	}
}
