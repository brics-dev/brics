package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.ValueRangeDao;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

@Transactional("dictionaryTransactionManager")
@Repository
public class ValueRangeDaoImpl extends GenericDictDaoImpl<ValueRange, Long> implements ValueRangeDao {

	@Autowired
	public ValueRangeDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(ValueRange.class, sessionFactory);
	}

	@Override
	public ValueRange getByDeNameAndPv(DataElement de, String pvValue) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ValueRange> query = cb.createQuery(ValueRange.class);
		Root<ValueRange> root = query.from(ValueRange.class);

		query.where(cb.and(cb.equal(root.join("dataElement", JoinType.LEFT).get("id"), de.getId()),
				cb.like(cb.upper(root.get("valueRange")), pvValue)));

		return getUniqueResult(query);
	}

	@Override
	public List<ValueRange> getByDeName(DataElement de) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ValueRange> query = cb.createQuery(ValueRange.class);
		Root<ValueRange> root = query.from(ValueRange.class);

		query.where(cb.equal(root.join("dataElement", JoinType.LEFT).get("id"), de.getId()));
		return createQuery(query).getResultList();
	}
}
