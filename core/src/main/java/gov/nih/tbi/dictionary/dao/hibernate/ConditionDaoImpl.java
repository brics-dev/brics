
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

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.ConditionDao;
import gov.nih.tbi.dictionary.model.hibernate.Condition;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;

@Repository
public class ConditionDaoImpl extends GenericDictDaoImpl<Condition, Long> implements ConditionDao {

	@Autowired
	public ConditionDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(Condition.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<Condition> getByDataElement(DataElement dataElement) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Condition> query = cb.createQuery(Condition.class);

		Root<Condition> root = query.from(Condition.class);
		query.where(cb.equal(root.join("mapElement").get("dataElement"), dataElement));

		TypedQuery<Condition> q = createQuery(query.distinct(true));
		return q.getResultList();
	}
}
