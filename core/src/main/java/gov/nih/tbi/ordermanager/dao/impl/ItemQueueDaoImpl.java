package gov.nih.tbi.ordermanager.dao.impl;

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
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.ordermanager.dao.ItemQueueDao;
import gov.nih.tbi.ordermanager.model.ItemQueue;

@Transactional("metaTransactionManager")
@Repository
public class ItemQueueDaoImpl extends GenericDaoImpl<ItemQueue, Long> implements ItemQueueDao {

	@Autowired
	public ItemQueueDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(ItemQueue.class, sessionFactory);
	}

	public ItemQueue findByUser(User user) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ItemQueue> query = cb.createQuery(ItemQueue.class);
		Root<ItemQueue> root = query.from(ItemQueue.class);
		query.where(cb.equal(root.get("user"), user)).distinct(true);
		ItemQueue queue = getUniqueResult(query);
		return queue;
	}

}
