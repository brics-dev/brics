
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
import gov.nih.tbi.account.dao.StateDao;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.hibernate.State;

@Transactional("metaTransactionManager")
@Repository
public class StateDaoImpl extends GenericDaoImpl<State, Long> implements StateDao {

	@Autowired
	public StateDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(State.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<State> getAll() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<State> query = cb.createQuery(State.class);

		Root<State> root = query.from(persistentClass);
		query.orderBy(cb.asc(root.get("name")));

		TypedQuery<State> q = createQuery(query.distinct(true));
		return q.getResultList();
	}
}
