
package gov.nih.tbi.ordermanager.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.ordermanager.dao.BiospecimenItemDao;
import gov.nih.tbi.ordermanager.model.BiospecimenItem;

@Transactional("metaTransactionManager")
@Repository
public class BiospecimenItemDaoImpl extends GenericDaoImpl<BiospecimenItem, Long> implements BiospecimenItemDao {

	@Autowired
	public BiospecimenItemDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(BiospecimenItem.class, sessionFactory);
	}

	@Override
	public Map<Long, BiospecimenItem> getBiospecimenItems(Set<Long> biospecimenIds) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BiospecimenItem> query = cb.createQuery(BiospecimenItem.class);
		Root<BiospecimenItem> root = query.from(persistentClass);

		query.where(root.get("id").in(biospecimenIds)).distinct(true);
		List<BiospecimenItem> biospecimenItems = createQuery(query).getResultList();

		Map<Long, BiospecimenItem> biospecimenMap = new HashMap<Long, BiospecimenItem>();

		for (BiospecimenItem biospecimenItem : biospecimenItems) {
			biospecimenMap.put(biospecimenItem.getId(), biospecimenItem);
		}

		return biospecimenMap;
	}

}
