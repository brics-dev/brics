
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
import gov.nih.tbi.dictionary.dao.DomainDao;
import gov.nih.tbi.dictionary.model.hibernate.Domain;

@Transactional("dictionaryTransactionManager")
@Repository
public class DomainDaoImpl extends GenericDictDaoImpl<Domain, Long> implements DomainDao {

	@Autowired
	public DomainDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(Domain.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<Domain> getAll() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Domain> query = cb.createQuery(Domain.class);
		Root<Domain> root = query.from(Domain.class);

		query.where(cb.equal(root.get("isActive"), Boolean.TRUE));
		query.orderBy(cb.asc(root.get("name")));

		TypedQuery<Domain> q = createQuery(query.distinct(true));
		return q.getResultList();
	}

}
