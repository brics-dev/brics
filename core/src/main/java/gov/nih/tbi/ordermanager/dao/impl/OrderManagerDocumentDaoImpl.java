package gov.nih.tbi.ordermanager.dao.impl;

import java.util.List;

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
import gov.nih.tbi.ordermanager.dao.OrderManagerDocumentDao;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderManagerDocument;

@Transactional("metaTransactionManager")
@Repository
public class OrderManagerDocumentDaoImpl extends GenericDaoImpl<OrderManagerDocument, Long> implements OrderManagerDocumentDao {

	@Autowired
	public OrderManagerDocumentDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(OrderManagerDocument.class, sessionFactory);
	}

	@Override
	public List<OrderManagerDocument> getDocumentsForOrder(BiospecimenOrder order) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<OrderManagerDocument> query = cb.createQuery(OrderManagerDocument.class);
		Root<OrderManagerDocument> root = query.from(OrderManagerDocument.class);
		query.where(cb.equal(root.get("biospecimenOrder"), order)).distinct(true);

		List<OrderManagerDocument> result = createQuery(query).getResultList();
		return result;
	}
}
