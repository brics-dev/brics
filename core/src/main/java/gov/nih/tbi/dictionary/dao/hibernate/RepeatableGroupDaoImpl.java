
package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import gov.nih.tbi.dictionary.dao.RepeatableGroupDao;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;

/**
 * Hibernate implementation of the Repeatable Group Dao.
 * 
 * @author mvalei
 */
@Transactional("dictionaryTransactionManager")
@Repository
public class RepeatableGroupDaoImpl extends GenericDictDaoImpl<RepeatableGroup, Long> implements RepeatableGroupDao {

	/**
	 * @inheritDoc
	 */
	@Autowired
	public RepeatableGroupDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(RepeatableGroup.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<Long> getIdsByNameAndDS(String repeatableGroupName, Long dataStructureId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<RepeatableGroup> root = query.from(RepeatableGroup.class);

		query.select(root.get("id")).distinct(true);
		query.where(cb.and(cb.equal(root.join("dataStructure", JoinType.LEFT).get("id"), dataStructureId),
				cb.equal(root.get("name"), repeatableGroupName)));

		return createQuery(query).getResultList();
	}

	/**
	 * @inheritDoc
	 */
	public Map<Long, RepeatableGroup> getByIds(Set<Long> ids) {

		Map<Long, RepeatableGroup> rgMap = new HashMap<Long, RepeatableGroup>();

		if (ids == null || ids.isEmpty()) {
			return rgMap;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<RepeatableGroup> query = cb.createQuery(RepeatableGroup.class);
		Root<RepeatableGroup> root = query.from(RepeatableGroup.class);

		query.where(root.get("id").in(ids));
		List<RepeatableGroup> rgs = createQuery(query).getResultList();

		for (RepeatableGroup rg : rgs) {
			rgMap.put(rg.getId(), rg);
		}

		return rgMap;
	}
}
