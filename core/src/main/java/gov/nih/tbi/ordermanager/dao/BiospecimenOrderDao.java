package gov.nih.tbi.ordermanager.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.OrderStatus;

import java.util.Date;
import java.util.List;

public interface BiospecimenOrderDao extends GenericDao<BiospecimenOrder, Long> {

	public List<BiospecimenOrder> getByUser(User user);

	public List<BiospecimenOrder> getAllOrders();

	public BiospecimenOrder getOrderByIdForUser(Long orderId, User user);

	/**
	 * This method returns true if an orderTitle has already been created in the system, excluding the order with the
	 * given orderId (i.e. when editing an existing order).
	 * 
	 * @param orderTitle - orderTitle
	 * @param orderId - orderId, to exclude the order in case of editing existing order, null if not new.
	 * @return true if an orderTitle has already been created in the system.
	 */
	public boolean isOrderTitleUnique(String orderTitle, Long orderId);
	
	/*
	 * This is the beginning of a biospecimenOrder search. This is not a functionality we will need
	 * to full develop, therefore this will be a partial implementation of the search
	 */
	public List<BiospecimenOrder> search(Date submittedBeginDate, Date submittedEndDate, List<OrderStatus> status);

}
