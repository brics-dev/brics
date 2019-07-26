
package gov.nih.tbi.ordermanager.dao.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.ordermanager.dao.BiospecimenOrderDao;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.Comment;
import gov.nih.tbi.ordermanager.model.OrderManagerDocument;
import gov.nih.tbi.ordermanager.model.OrderStatus;

@Transactional("metaTransactionManager")
@Repository
public class BiospecimenOrderDaoImpl extends GenericDaoImpl<BiospecimenOrder, Long> implements BiospecimenOrderDao {

	@Autowired
	public BiospecimenOrderDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(BiospecimenOrder.class, sessionFactory);
	}

	public List<BiospecimenOrder> getByUser(User user) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BiospecimenOrder> query = cb.createQuery(BiospecimenOrder.class);
		Root<BiospecimenOrder> root = query.from(BiospecimenOrder.class);
		query.where(cb.equal(root.get("user"), user)).distinct(true);

		List<BiospecimenOrder> result = createQuery(query).getResultList();
		if (result != null) {
			if (result.isEmpty() == false) {
				this.processLazyLoadedAttributes(result);
			}
		}
		return result;
	}

	@Override
	public List<BiospecimenOrder> getAllOrders() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BiospecimenOrder> query = cb.createQuery(BiospecimenOrder.class);
		query.from(BiospecimenOrder.class);

		List<BiospecimenOrder> result = createQuery(query.distinct(true)).getResultList();
		if (result != null) {
			if (result.isEmpty() == false) {
				this.processLazyLoadedAttributes(result);
			}
		}
		return result;
	}

	@Override
	public BiospecimenOrder getOrderByIdForUser(Long orderId, User user) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BiospecimenOrder> query = cb.createQuery(BiospecimenOrder.class);
		Root<BiospecimenOrder> root = query.from(BiospecimenOrder.class);
		query.where(cb.and(cb.equal(root.get("user"), user), cb.equal(root.get("id"), orderId))).distinct(true);

		BiospecimenOrder order = getUniqueResult(query);
		if (order != null) {
			this.processLazyLoadedAttributes(order);
		}
		return order;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nih.tbi.ordermanager.dao.BiospecimenOrderDao#search(java.util.Date, java.util.Date, java.util.List)
	 * parameters should be added to this function as needed to expand search capabilities
	 */
	public List<BiospecimenOrder> search(Date submittedBeginDate, Date submittedEndDate, List<OrderStatus> status) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BiospecimenOrder> query = cb.createQuery(BiospecimenOrder.class);
		Root<BiospecimenOrder> root = query.from(BiospecimenOrder.class);
		Predicate predicate = cb.conjunction();

		if (submittedBeginDate != null && submittedEndDate != null) {
			predicate = cb.and(predicate,
					cb.greaterThanOrEqualTo(root.get(BiospecimenOrder.Date_Submitted), submittedBeginDate));
			predicate = cb.and(predicate,
					cb.lessThanOrEqualTo(root.get(BiospecimenOrder.Date_Submitted), submittedEndDate));
		}

		if (status != null && !status.isEmpty()) {
			predicate = cb.and(predicate, root.get(BiospecimenOrder.Order_Status).in(status));
		}

		query.where(predicate).distinct(true);
		List<BiospecimenOrder> orderList = createQuery(query).getResultList();
		return orderList;

	}

	private void processLazyLoadedAttributes(Collection<BiospecimenOrder> orders) {

		for (BiospecimenOrder order : orders) {
			this.processLazyLoadedAttributes(order);
		}
	}

	private void processLazyLoadedAttributes(BiospecimenOrder order) {

		for (Comment comment : order.getCommentList()) {
			comment.toString();
		}

		for (OrderManagerDocument doc : order.getDocumentList()) {
			doc.toString();
		}
	}

	@Override
	public boolean isOrderTitleUnique(String orderTitle, Long orderId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<BiospecimenOrder> root = query.from(BiospecimenOrder.class);
		Predicate predicate = cb.equal(root.get("orderTitle"), orderTitle);

		if (orderId != null) {
			predicate = cb.and(predicate, cb.notEqual(root.get("id"), orderId));
		}

		query.select(cb.countDistinct(root));
		query.where(predicate);

		long count = getUniqueResult(query);
		return count == 0;
	}
}
